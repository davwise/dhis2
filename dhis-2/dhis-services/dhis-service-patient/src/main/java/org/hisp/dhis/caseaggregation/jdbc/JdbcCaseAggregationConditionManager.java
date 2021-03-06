/*
 * Copyright (c) 2004-2012, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.caseaggregation.jdbc;

import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.OBJECT_ORGUNIT_COMPLETE_PROGRAM_STAGE;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.OBJECT_PATIENT;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.OBJECT_PATIENT_ATTRIBUTE;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.OBJECT_PATIENT_PROGRAM_STAGE_PROPERTY;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.OBJECT_PATIENT_PROPERTY;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.OBJECT_PROGRAM;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.OBJECT_PROGRAM_PROPERTY;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.OBJECT_PROGRAM_STAGE;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.OBJECT_PROGRAM_STAGE_DATAELEMENT;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.OBJECT_PROGRAM_STAGE_PROPERTY;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.SEPARATOR_ID;
import static org.hisp.dhis.caseaggregation.CaseAggregationCondition.SEPARATOR_OBJECT;
import static org.hisp.dhis.patient.scheduling.CaseAggregateConditionSchedulingManager.TASK_AGGREGATE_QUERY_BUILDER_LAST_12_MONTH;
import static org.hisp.dhis.patient.scheduling.CaseAggregateConditionSchedulingManager.TASK_AGGREGATE_QUERY_BUILDER_LAST_3_MONTH;
import static org.hisp.dhis.patient.scheduling.CaseAggregateConditionSchedulingManager.TASK_AGGREGATE_QUERY_BUILDER_LAST_6_MONTH;
import static org.hisp.dhis.patient.scheduling.CaseAggregateConditionSchedulingManager.TASK_AGGREGATE_QUERY_BUILDER_LAST_MONTH;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hisp.dhis.caseaggregation.CaseAggregateSchedule;
import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionManager;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.GridHeader;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.period.CalendarPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.system.grid.ListGrid;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.system.util.TextUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.annotation.Async;

/**
 * @author Chau Thu Tran
 */
public class JdbcCaseAggregationConditionManager
    implements CaseAggregationConditionManager
{
    private final String IS_NULL = "is null";

    private final String PROPERTY_AGE = "age";

    private final String IN_CONDITION_GET_ALL = "*";

    private final String IN_CONDITION_START_SIGN = "@";

    private final String IN_CONDITION_END_SIGN = "#";

    private final String IN_CONDITION_COUNT_X_TIMES = "COUNT";

    public static final String STORED_BY_DHIS_SYSTEM = "DHIS-System";

    // -------------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------------

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------------------------
    // Implementation Methods
    // -------------------------------------------------------------------------

    @Override
    public List<Integer> executeSQL( String sql )
    {
        try
        {
            List<Integer> patientIds = jdbcTemplate.query( sql, new RowMapper<Integer>()
            {
                public Integer mapRow( ResultSet rs, int rowNum )
                    throws SQLException
                {
                    return rs.getInt( 1 );
                }
            } );

            return patientIds;
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            return null;
        }
    }

    @Async
    public Future<?> aggregate( ConcurrentLinkedQueue<CaseAggregateSchedule> caseAggregateSchedule, String taskStrategy )
    {
        taskLoop: while ( true )
        {
            CaseAggregateSchedule dataSet = caseAggregateSchedule.poll();

            if ( dataSet == null )
            {
                break taskLoop;
            }

            Collection<Period> periods = getPeriods( dataSet.getPeriodTypeName(), taskStrategy );

            runAggregate( null, dataSet, periods );
        }

        return null;
    }

    public Grid getAggregateValue( CaseAggregationCondition caseAggregationCondition, Collection<Integer> orgunitIds,
        Period period, I18nFormat format, I18n i18n )
    {
        Collection<Integer> _orgunitIds = getServiceOrgunit( DateUtils.getMediumDateString( period.getStartDate() ),
            DateUtils.getMediumDateString( period.getEndDate() ) );
        orgunitIds.retainAll( _orgunitIds );

        if ( orgunitIds.size() > 0 )
        {
            Grid grid = new ListGrid();
            grid.setTitle( caseAggregationCondition.getDisplayName() );
            grid.setSubtitle( format.formatPeriod( period ) );

            grid.addHeader( new GridHeader( i18n.getString( "dataelementid" ), true, true ) );
            grid.addHeader( new GridHeader( i18n.getString( "categoryoptioncomboid" ), true, true ) );
            grid.addHeader( new GridHeader( i18n.getString( "periodid" ), true, true ) );
            grid.addHeader( new GridHeader( i18n.getString( "organisationunitid" ), true, true ) );
            grid.addHeader( new GridHeader( i18n.getString( "comment" ), true, true ) );
            grid.addHeader( new GridHeader( i18n.getString( "dataelementname" ), false, true ) );
            grid.addHeader( new GridHeader( i18n.getString( "categoryoptioncomboname" ), false, true ) );
            grid.addHeader( new GridHeader( i18n.getString( "organisationunitname" ), false, true ) );
            grid.addHeader( new GridHeader( i18n.getString( "value" ), false, true ) );

            Integer deSumId = (caseAggregationCondition.getDeSum() == null) ? null : caseAggregationCondition
                .getDeSum().getId();
            String sql = parseExpressionToSql( false, caseAggregationCondition.getAggregationExpression(),
                caseAggregationCondition.getOperator(), caseAggregationCondition.getAggregationDataElement().getId(),
                caseAggregationCondition.getAggregationDataElement().getDisplayName(), caseAggregationCondition
                    .getOptionCombo().getId(), caseAggregationCondition.getOptionCombo().getDisplayName(), deSumId,
                orgunitIds, period );

            SqlRowSet rs = jdbcTemplate.queryForRowSet( sql );
            grid.addRows( rs );

            return grid;
        }

        return null;
    }

    /**
     * Insert data elements into database directly
     * 
     */
    public void insertAggregateValue( CaseAggregationCondition caseAggregationCondition,
        Collection<Integer> orgunitIds, Period period )
    {
        Integer deSumId = (caseAggregationCondition.getDeSum() == null) ? null : caseAggregationCondition.getDeSum()
            .getId();

        insertAggregateValue( caseAggregationCondition.getAggregationExpression(),
            caseAggregationCondition.getOperator(), caseAggregationCondition.getAggregationDataElement().getId(),
            caseAggregationCondition.getOptionCombo().getId(), deSumId, orgunitIds, period );
    }

    private void insertAggregateValue( String expression, String operator, Integer dataElementId,
        Integer optionComboId, Integer deSumId, Collection<Integer> orgunitIds, Period period )
    {
        // Delete all data value from this period which created from DHIS-system
        // after to run Aggregate Query Builder

        String periodtypeSql = "select periodtypeid from periodtype where name='" + period.getPeriodType().getName()
            + "'";
        int periodTypeId = jdbcTemplate.queryForObject( periodtypeSql, Integer.class );

        String periodSql = "select periodid from period where periodtypeid=" + periodTypeId + " and startdate='"
            + DateUtils.getMediumDateString( period.getStartDate() ) + "' and enddate='"
            + DateUtils.getMediumDateString( period.getEndDate() ) + "'";

        SqlRowSet rs = jdbcTemplate.queryForRowSet( periodSql );

        int periodid = 0;
        if ( rs.next() )
        {
            periodid = rs.getInt( "periodid" );
        }

        if ( periodid == 0 )
        {
            String insertSql = "insert into period (periodtypeid,startdate,enddate) " + " VALUES " + "("
                + period.getPeriodType().getId() + ",'" + DateUtils.getMediumDateString( period.getStartDate() )
                + "','" + DateUtils.getMediumDateString( period.getEndDate() ) + "' )";
            jdbcTemplate.execute( insertSql );

            period.setId( jdbcTemplate.queryForObject( insertSql, Integer.class ) );
        }
        else
        {
            period.setId( periodid );

            String deleteDataValueSql = "delete from datavalue where dataelementid=" + dataElementId
                + " and categoryoptioncomboid=" + optionComboId + " and sourceid in ("
                + TextUtils.getCommaDelimitedString( orgunitIds ) + ") and periodid=" + periodid + "";

            jdbcTemplate.execute( deleteDataValueSql );
        }

        // insert data elements into database directly

        String sql = parseExpressionToSql( true, expression, operator, dataElementId, "dataelementname", optionComboId,
            "optionComboname", deSumId, orgunitIds, period );

        jdbcTemplate.execute( sql );
    }

    public String parseExpressionToSql( boolean isInsert, String caseExpression, String operator,
        Integer aggregateDeId, String aggregateDeName, Integer optionComboId, String optionComboName, Integer deSumId,
        Collection<Integer> orgunitIds, Period period )
    {
        String sql = "SELECT '" + aggregateDeId + "' as dataelementid, '" + optionComboId
            + "' as categoryoptioncomboid, ou.organisationunitid as sourceid, '" + period.getId() + "' as periodid,'"
            + CaseAggregationCondition.AUTO_STORED_BY + "' as comment, ";

        if ( isInsert )
        {
            sql = "INSERT INTO datavalue (dataelementid, categoryoptioncomboid, sourceid, periodid, comment, value)"
                + sql;
        }
        else
        {
            sql += "'" + period.getIsoDate() + "' as periodIsoDate,'" + aggregateDeName + "' as dataelementname, '"
                + optionComboName + "' as categoryoptioncomboname, " + "ou.name as organisationunitname, ";
        }

        if ( operator.equals( CaseAggregationCondition.AGGRERATION_COUNT )
            || operator.equals( CaseAggregationCondition.AGGRERATION_SUM ) )
        {
            if ( hasOrgunitProgramStageCompleted( caseExpression ) )
            {
                sql += createSQL( caseExpression, operator, orgunitIds,
                    DateUtils.getMediumDateString( period.getStartDate() ),
                    DateUtils.getMediumDateString( period.getEndDate() ) );
            }
            else
            {
                if ( operator.equals( CaseAggregationCondition.AGGRERATION_COUNT ) )
                {
                    sql += operator + " (distinct(pi.patientid) ) as value ";
                }
                else
                {
                    sql += operator + " (distinct(psi.programinstanceid ) ) as value ";
                }

                sql += "FROM programstageinstance as psi ";
                boolean hasPatients = hasPatientCriteria( caseExpression );
                boolean hasProgramInstances = hasProgramInstanceCriteria( caseExpression );

                if ( hasPatients )
                {
                    sql += "INNER JOIN patient p on p.patientid=pi.patientid  ";
                    sql += "INNER JOIN programinstance as pi ON pi.programinstanceid = psi.programinstanceid ";
                }
                if ( (hasProgramInstances && !hasPatients)
                    || operator.equals( CaseAggregationCondition.AGGRERATION_COUNT ) )
                {
                    sql += "INNER JOIN programinstance as pi ON pi.programinstanceid = psi.programinstanceid ";
                }
                sql += "INNER JOIN organisationunit ou on ou.organisationunitid=psi.organisationunitid WHERE "
                    + createSQL( caseExpression, operator, orgunitIds,
                        DateUtils.getMediumDateString( period.getStartDate() ),
                        DateUtils.getMediumDateString( period.getEndDate() ) );

                sql += "GROUP BY ou.organisationunitid, ou.name";
            }
        }
        else
        {
            sql += " " + operator + "( cast( pdv.value as DOUBLE PRECISION ) ) ";
            sql += "FROM patientdatavalue pdv ";
            sql += "    INNER JOIN programstageinstance psi  ";
            sql += "            ON psi.programstageinstanceid = pdv.programstageinstanceid ";
            sql += "    INNER JOIN organisationunit ou ";
            sql += "            ON ou.organisationunitid=psi.organisationunitid ";
            sql += "WHERE executiondate >='" + DateUtils.getMediumDateString( period.getStartDate() ) + "'  ";
            sql += "    AND executiondate <='" + DateUtils.getMediumDateString( period.getEndDate() )
                + "' AND pdv.dataelementid=" + deSumId;

            if ( caseExpression != null && !caseExpression.isEmpty() )
            {
                sql += " AND "
                    + createSQL( caseExpression, operator, orgunitIds,
                        DateUtils.getMediumDateString( period.getStartDate() ),
                        DateUtils.getMediumDateString( period.getEndDate() ) );
            }

            sql += "GROUP BY ou.organisationunitid, ou.name";

        }

        sql = sql.replaceAll( "COMBINE", "" );
        System.out.println( "\n\n ==== \n " + sql );
        return sql;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    /**
     * Aggregate data values for the dataset by periods with a organisation unit
     * list
     * 
     */
    private void runAggregate( Collection<Integer> orgunitIds, CaseAggregateSchedule dataSet, Collection<Period> periods )
    {
        String sql = "select caseaggregationconditionid, aggregationdataelementid, optioncomboid, "
            + " cagg.aggregationexpression as caseexpression, cagg.operator as caseoperator, cagg.desum as desumid "
            + "     from caseaggregationcondition cagg inner join datasetmembers dm "
            + "             on cagg.aggregationdataelementid=dm.dataelementid inner join dataset ds "
            + "             on ds.datasetid = dm.datasetid inner join periodtype pt "
            + "             on pt.periodtypeid=ds.periodtypeid inner join dataelement de "
            + "             on de.dataelementid=dm.dataelementid where ds.datasetid = " + dataSet.getDataSetId();

        SqlRowSet rs = jdbcTemplate.queryForRowSet( sql );

        while ( rs.next() )
        {
            for ( Period period : periods )
            {
                // -------------------------------------------------------------
                // Get formula, agg-dataelement and option-combo
                // -------------------------------------------------------------

                int dataelementId = rs.getInt( "aggregationdataelementid" );
                int optionComboId = rs.getInt( "optioncomboid" );
                String caseExpression = rs.getString( "caseexpression" );
                String caseOperator = rs.getString( "caseoperator" );
                int deSumId = rs.getInt( "desumid" );

                Collection<Integer> _orgunitIds = getServiceOrgunit(
                    DateUtils.getMediumDateString( period.getStartDate() ),
                    DateUtils.getMediumDateString( period.getEndDate() ) );

                if ( orgunitIds == null )
                {
                    orgunitIds = new HashSet<Integer>();
                    orgunitIds.addAll( _orgunitIds );
                }
                else
                {
                    orgunitIds.retainAll( _orgunitIds );
                }

                // ---------------------------------------------------------------------
                // Aggregation
                // ---------------------------------------------------------------------

                if ( _orgunitIds.size() > 0 )
                {
                    insertAggregateValue( caseExpression, caseOperator, dataelementId, optionComboId, deSumId,
                        orgunitIds, period );
                }
            }

        }
    }

    /**
     * Generate period list based on period Type and taskStrategy option
     * 
     * @param periodTypeName The name of period type
     * @param taskStrategy Specify how to get period list based on period type
     *        of each dataset. There are four options, include last month, last
     *        3 month, last 6 month and last 12 month
     * 
     */
    private Collection<Period> getPeriods( String periodTypeName, String taskStrategy )
    {
        Calendar calStartDate = Calendar.getInstance();

        if ( TASK_AGGREGATE_QUERY_BUILDER_LAST_MONTH.equals( taskStrategy ) )
        {
            calStartDate.add( Calendar.MONTH, -1 );
        }
        else if ( TASK_AGGREGATE_QUERY_BUILDER_LAST_3_MONTH.equals( taskStrategy ) )
        {
            calStartDate.add( Calendar.MONTH, -3 );
        }
        else if ( TASK_AGGREGATE_QUERY_BUILDER_LAST_6_MONTH.equals( taskStrategy ) )
        {
            calStartDate.add( Calendar.MONTH, -6 );
        }
        else if ( TASK_AGGREGATE_QUERY_BUILDER_LAST_12_MONTH.equals( taskStrategy ) )
        {
            calStartDate.add( Calendar.MONTH, -12 );
        }

        Date startDate = calStartDate.getTime();

        Calendar calEndDate = Calendar.getInstance();

        Date endDate = calEndDate.getTime();

        CalendarPeriodType periodType = (CalendarPeriodType) PeriodType.getPeriodTypeByName( periodTypeName );
        String sql = "select periodtypeid from periodtype where name='" + periodTypeName + "'";
        int periodTypeId = jdbcTemplate.queryForObject( sql, Integer.class );

        Collection<Period> periods = periodType.generatePeriods( startDate, endDate );

        for ( Period period : periods )
        {
            String start = DateUtils.getMediumDateString( period.getStartDate() );
            String end = DateUtils.getMediumDateString( period.getEndDate() );

            sql = "select periodid from period where periodtypeid=" + periodTypeId + " and startdate='" + start
                + "' and enddate='" + end + "'";
            int periodid = 0;
            SqlRowSet rs = jdbcTemplate.queryForRowSet( sql );
            if ( rs.next() )
            {
                periodid = rs.getInt( "periodid" );
            }

            if ( periodid == 0 )
            {
                String insertSql = "insert into period (periodtypeid,startdate,enddate) " + " VALUES " + "("
                    + periodTypeId + ",'" + start + "','" + end + "' )";
                jdbcTemplate.execute( insertSql );

                period.setId( jdbcTemplate.queryForObject( sql, Integer.class ) );
            }
            else
            {
                period.setId( periodid );
            }
        }

        return periods;
    }

    /**
     * Return standard SQL from query builder formula
     * 
     * @param caseExpression The query builder expression
     * @param operator There are six operators, includes Number of persons,
     *        Number of visits, Sum, Average, Minimum and Maximum of data
     *        element values.
     * @param deType Aggregate Data element type
     * @param orgunitIds The ids of organisation units where to aggregate data
     *        value
     * @param startDate Start date
     * @param endDate End date
     */
    private String createSQL( String caseExpression, String operator, Collection<Integer> orgunitIds, String startDate,
        String endDate )
    {
        boolean orgunitCompletedProgramStage = false;

        StringBuffer sqlResult = new StringBuffer();

        String sqlOrgunitCompleted = "";

        String[] expression = caseExpression.split( "(AND|OR)" );
        caseExpression = caseExpression.replaceAll( "AND", " ) AND " );
        caseExpression = caseExpression.replaceAll( "OR", " ) OR " );

        // ---------------------------------------------------------------------
        // parse expressions
        // ---------------------------------------------------------------------

        Pattern patternCondition = Pattern.compile( CaseAggregationCondition.regExp );

        Matcher matcherCondition = patternCondition.matcher( caseExpression );

        String condition = "";

        int index = 0;
        while ( matcherCondition.find() )
        {
            String match = matcherCondition.group();

            match = match.replaceAll( "[\\[\\]]", "" );

            String[] info = match.split( SEPARATOR_OBJECT );
            if ( info[0].equalsIgnoreCase( OBJECT_PATIENT ) )
            {
                condition = getConditionForPatient( orgunitIds, operator, startDate, endDate );
            }
            else if ( info[0].equalsIgnoreCase( OBJECT_PATIENT_PROPERTY ) )
            {
                String propertyName = info[1];
                condition = getConditionForPatientProperty( propertyName, operator, startDate, endDate );
            }
            else if ( info[0].equalsIgnoreCase( OBJECT_PATIENT_ATTRIBUTE ) )
            {
                int attributeId = Integer.parseInt( info[1] );
                condition = getConditionForPatientAttribute( attributeId, orgunitIds );
                condition += ")";
            }
            else if ( info[0].equalsIgnoreCase( OBJECT_PROGRAM_STAGE_DATAELEMENT ) )
            {
                String[] ids = info[1].split( SEPARATOR_ID );

                int programId = Integer.parseInt( ids[0] );
                String programStageId = ids[1];
                int dataElementId = Integer.parseInt( ids[2] );

                String compareValue = expression[index].replace( "[" + match + "]", "" ).trim();

                boolean isExist = compareValue.equals( IS_NULL ) ? false : true;
                condition = getConditionForDataElement( isExist, programId, programStageId, dataElementId, orgunitIds,
                    startDate, endDate );
            }

            else if ( info[0].equalsIgnoreCase( OBJECT_PROGRAM_PROPERTY ) )
            {
                condition = getConditionForProgramProperty( operator, startDate, endDate, info[1] );
            }
            else if ( info[0].equalsIgnoreCase( OBJECT_PROGRAM ) )
            {
                String[] ids = info[1].split( SEPARATOR_ID );
                condition = getConditionForProgram( ids[0], operator, orgunitIds, startDate, endDate );
                if ( ids.length > 1 )
                {
                    condition += ids[1];
                }
            }
            else if ( info[0].equalsIgnoreCase( OBJECT_PROGRAM_STAGE ) )
            {
                String[] ids = info[1].split( SEPARATOR_ID );
                if ( ids.length == 2 && ids[1].equals( IN_CONDITION_COUNT_X_TIMES ) )
                {
                    condition = getConditionForCountProgramStage( ids[0], operator, orgunitIds, startDate, endDate );
                }
                else
                {
                    condition = getConditionForProgramStage( ids[0], operator, orgunitIds, startDate, endDate );
                }
            }
            else if ( info[0].equalsIgnoreCase( OBJECT_PROGRAM_STAGE_PROPERTY ) )
            {
                condition = getConditionForProgramStageProperty( info[1], operator, orgunitIds, startDate, endDate );
            }
            else if ( info[0].equalsIgnoreCase( OBJECT_PATIENT_PROGRAM_STAGE_PROPERTY ) )
            {
                condition = getConditionForPatientProgramStageProperty( info[1], operator, startDate, endDate );
            }
            else if ( info[0].equalsIgnoreCase( OBJECT_ORGUNIT_COMPLETE_PROGRAM_STAGE ) )
            {
                sqlOrgunitCompleted += getConditionForOrgunitProgramStageCompleted( info[1], operator, orgunitIds,
                    startDate, endDate, orgunitCompletedProgramStage );
                orgunitCompletedProgramStage = true;
            }

            matcherCondition.appendReplacement( sqlResult, condition );

            index++;
        }

        matcherCondition.appendTail( sqlResult );

        if ( !sqlOrgunitCompleted.isEmpty() )
        {
            sqlOrgunitCompleted = sqlOrgunitCompleted.substring( 0, sqlOrgunitCompleted.length() - 2 );
        }

        sqlResult.append( sqlOrgunitCompleted );

        String sql = sqlResult.toString();

        sql = sql.replaceAll( IN_CONDITION_START_SIGN, "(" );
        sql = sql.replaceAll( IN_CONDITION_END_SIGN, ")" );
        sql = sql.replaceAll( IS_NULL, " " );

        return sql + " ) ";
    }

    /**
     * Return standard SQL of the expression to compare data value as null
     * 
     */
    private String getConditionForDataElement( boolean isExist, int programId, String programStageId,
        int dataElementId, Collection<Integer> orgunitIds, String startDate, String endDate )
    {
        String keyExist = (isExist == true) ? "EXISTS" : "NOT EXISTS";

        String sql = " " + keyExist + " ( SELECT * "
            + "FROM patientdatavalue _pdv inner join programstageinstance _psi "
            + "ON _pdv.programstageinstanceid=_psi.programstageinstanceid JOIN programinstance _pi "
            + "ON _pi.programinstanceid=_psi.programinstanceid "
            + "WHERE psi.programstageinstanceid=_pdv.programstageinstanceid AND _pdv.dataelementid=" + dataElementId
            + "  AND _psi.organisationunitid in (" + TextUtils.getCommaDelimitedString( orgunitIds ) + ")  "
            + "AND _pi.programid = " + programId + " AND psi.executionDate>='" + startDate
            + "' AND psi.executionDate <= '" + endDate + "' ";

        if ( !programStageId.equals( IN_CONDITION_GET_ALL ) )
        {
            sql += " AND _psi.programstageid = " + programStageId;
        }

        if ( isExist )
        {
            sql += " AND _pdv.value ";
        }

        return sql;
    }

    /**
     * Return standard SQL of a dynamic patient-attribute expression. E.g [CA:1]
     * 
     */
    private String getConditionForPatientAttribute( int attributeId, Collection<Integer> orgunitIds )
    {
        String sql = " EXISTS ( SELECT * " + "FROM patientattributevalue _pav "
            + "WHERE _pav.patientid = pi.patientid " + "and _pav.patientattributeid=" + attributeId
            + "  AND p.organisationunitid in (" + TextUtils.getCommaDelimitedString( orgunitIds ) + ") AND _pav.value ";

        return sql;
    }

    /**
     * Return standard SQL of the expression which is used for calculating total
     * of person registration
     * 
     */
    private String getConditionForPatient( Collection<Integer> orgunitIds, String operator, String startDate,
        String endDate )
    {
        String sql = " EXISTS ( SELECT * " + "FROM patient _p " + "WHERE _p.patientid = pi.patientid "
            + "AND _p.registrationdate>='" + startDate + "' AND _p.registrationdate<='" + endDate + "' "
            + "AND p.organisationunitid in (" + TextUtils.getCommaDelimitedString( orgunitIds ) + ") ";

        return sql;
    }

    /**
     * Return standard SQL of the patient-fixed-attribute expression. E.g
     * [CP:gender]
     * 
     */
    private String getConditionForPatientProperty( String propertyName, String operator, String startDate,
        String endDate )
    {
        String sql = " EXISTS (SELECT * FROM patient _p WHERE _p.patientid = pi.patientid AND ";

        if ( propertyName.equals( PROPERTY_AGE ) )
        {
            sql += "DATE(registrationdate) - DATE(birthdate) ";
        }
        else
        {
            sql += propertyName + " ";
        }

        return sql;
    }

    /**
     * Return standard SQL of the program-property expression. E.g
     * [PC:executionDate]
     * 
     */
    private String getConditionForPatientProgramStageProperty( String propertyName, String operator, String startDate,
        String endDate )
    {
        String sql = " EXISTS ( SELECT * from programstageinstance _psi "
            + "WHERE _psi.programstageinstanceid=psi.programstageinstanceid AND _psi.executionDate>='" + startDate
            + "' and _psi.executionDate<='" + endDate + "' and " + propertyName;

        return sql;
    }

    /**
     * Return standard SQL of the program expression. E.g
     * [PP:DATE@enrollmentdate#-DATE@dateofincident#] for geting the number of
     * days between date of enrollment and date of incident.
     * 
     */
    private String getConditionForProgramProperty( String operator, String startDate, String endDate, String property )
    {
        String sql = " EXISTS ( SELECT * FROM programinstance as _pi WHERE psi.programinstanceid=_pi.programsinstanceid AND "
            + "_pi.enrollmentdate>='"
            + startDate
            + "' "
            + "AND _pi.enrollmentdate<='"
            + endDate
            + "'  AND "
            + property
            + " ";

        return sql;
    }

    /**
     * Return standard SQL to retrieve the number of persons enrolled into the
     * program. E.g [PG:1]
     * 
     */
    private String getConditionForProgram( String programId, String operator, Collection<Integer> orgunitIds,
        String startDate, String endDate )
    {
        String sql = " EXISTS ( SELECT * FROM programinstance as _pi "
            + "inner join programstageinstance _psi on _pi.programinstanceid=_psi.programinstanceid "
            + "WHERE psi.programstageinstanceid=_psi.programstageinstanceid AND _pi.programid=" + programId + " "
            + " AND _psi.organisationunitid in (" + TextUtils.getCommaDelimitedString( orgunitIds )
            + ") AND _pi.enrollmentdate >= '" + startDate + "' AND _pi.enrollmentdate <= '" + endDate + "' ";

        return sql;
    }

    /**
     * Return standard SQL to retrieve the number of visits a program-stage. E.g
     * [PS:1]
     * 
     */
    private String getConditionForProgramStage( String programStageId, String operator, Collection<Integer> orgunitIds,
        String startDate, String endDate )
    {
        String sql = " EXISTS ( SELECT * FROM programinstance as _pi INNER JOIN programstageinstance _psi "
            + "ON _pi.programinstanceid = _psi.programinstanceid WHERE _psi.programstageinstanceid=psi.programstageinstanceid "
            + "AND _psi.programstageid=" + programStageId + " AND _psi.executiondate >= '" + startDate
            + "' AND _psi.executiondate <= '" + endDate + "' AND _psi.organisationunitid in ("
            + TextUtils.getCommaDelimitedString( orgunitIds ) + ")  ";

        return sql;
    }

    /**
     * Return standard SQL to retrieve the x-time of a person visited one
     * program-stage. E.g a mother came to a hospital 3th time for third
     * trimester.
     * 
     */
    private String getConditionForCountProgramStage( String programStageId, String operator,
        Collection<Integer> orgunitIds, String startDate, String endDate )
    {
        String sql = " EXISTS ( SELECT * FROM programstageinstance as _psi "
            + "WHERE psi.programstageinstanceid=_psi.programstageinstanceid AND _psi.organisationunitid in ("
            + TextUtils.getCommaDelimitedString( orgunitIds ) + ") and _psi.programstageid = " + programStageId + " "
            + "AND _psi.executionDate >= '" + startDate + "' AND _psi.executionDate <= '" + endDate + "' "
            + "GROUP BY _psi.programinstanceid,_psi.programstageinstanceid "
            + "HAVING count(_psi.programstageinstanceid) ";

        return sql;

    }

    /**
     * Return standard SQL to retrieve the number of days between report-date
     * and due-date. E.g [PSP:DATE@executionDate#-DATE@dueDate#]
     * 
     */
    private String getConditionForProgramStageProperty( String property, String operator,
        Collection<Integer> orgunitIds, String startDate, String endDate )
    {
        String sql = " EXISTS ( SELECT * FROM programstageinstance _psi "
            + "WHERE psi.programstageinstanceid=_psi.programstageinstanceid AND _psi.executiondate >= '" + startDate
            + "' AND _psi.executiondate <= '" + endDate + "' AND _psi.organisationunitid in ("
            + TextUtils.getCommaDelimitedString( orgunitIds ) + ") AND " + property + " ";

        return sql;
    }

    /**
     * Return standard SQL to retrieve the number of children orgunits has all
     * program-stage-instance completed and due-date. E.g [PSIC:1]
     * 
     * @flag True if there are many stages in the expression
     * 
     */
    private String getConditionForOrgunitProgramStageCompleted( String programStageId, String operator,
        Collection<Integer> orgunitIds, String startDate, String endDate, boolean flag )
    {
        String sql = "";
        if ( !flag )
        {
            sql = " '1' FROM organisationunit ou WHERE ou.organisationunitid in ("
                + TextUtils.getCommaDelimitedString( orgunitIds ) + ")  ";
        }

        sql += " AND EXISTS ( SELECT programstageinstanceid FROM programstageinstance _psi "
            + " WHERE _psi.organisationunitid=ou.organisationunitid AND _psi.programstageid = " + programStageId
            + " AND _psi.completed=true AND _psi.executiondate >= '" + startDate + "' AND _psi.executiondate <= '"
            + endDate + "' ) ";

        return sql;
    }

    /**
     * Return the Ids of organisation units which patients registered or events
     * happened.
     * 
     */
    private Collection<Integer> getServiceOrgunit( String startDate, String endDate )
    {
        String sql = "(select organisationunitid from programstageinstance where executiondate>= '" + startDate
            + "' and executiondate<='" + endDate + "')";
        sql += " UNION ";
        sql += "( select distinct organisationunitid from patient where registrationdate>='" + startDate
            + "' and registrationdate<='" + endDate + "')";

        Collection<Integer> orgunitIds = new HashSet<Integer>();
        orgunitIds = jdbcTemplate.query( sql, new RowMapper<Integer>()
        {
            public Integer mapRow( ResultSet rs, int rowNum )
                throws SQLException
            {
                return rs.getInt( 1 );
            }
        } );

        return orgunitIds;
    }

    public boolean hasOrgunitProgramStageCompleted( String expresstion )
    {
        Pattern pattern = Pattern.compile( CaseAggregationCondition.regExp );
        Matcher matcher = pattern.matcher( expresstion );
        while ( matcher.find() )
        {
            String match = matcher.group();

            match = match.replaceAll( "[\\[\\]]", "" );

            String[] info = match.split( SEPARATOR_OBJECT );

            if ( info[0].equalsIgnoreCase( CaseAggregationCondition.OBJECT_ORGUNIT_COMPLETE_PROGRAM_STAGE ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasPatientCriteria( String expresstion )
    {
        Pattern pattern = Pattern.compile( CaseAggregationCondition.regExp );
        Matcher matcher = pattern.matcher( expresstion );
        while ( matcher.find() )
        {
            String match = matcher.group();

            match = match.replaceAll( "[\\[\\]]", "" );

            String[] info = match.split( SEPARATOR_OBJECT );

            if ( info[0].equalsIgnoreCase( CaseAggregationCondition.OBJECT_PATIENT )
                || info[0].equalsIgnoreCase( CaseAggregationCondition.OBJECT_PATIENT_PROPERTY ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasProgramInstanceCriteria( String expresstion )
    {
        Pattern pattern = Pattern.compile( CaseAggregationCondition.regExp );
        Matcher matcher = pattern.matcher( expresstion );
        while ( matcher.find() )
        {
            String match = matcher.group();

            match = match.replaceAll( "[\\[\\]]", "" );

            String[] info = match.split( SEPARATOR_OBJECT );

            if ( info[0].equalsIgnoreCase( CaseAggregationCondition.OBJECT_PROGRAM_PROPERTY )
                || info[0].equalsIgnoreCase( CaseAggregationCondition.OBJECT_PROGRAM )
                || info[0].equalsIgnoreCase( CaseAggregationCondition.OBJECT_PROGRAM_STAGE ) )
            {
                return true;
            }
        }

        return false;
    }

}
