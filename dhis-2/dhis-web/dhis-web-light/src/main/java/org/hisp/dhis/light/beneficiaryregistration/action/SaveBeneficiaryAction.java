/*
 * Copyright (c) 2004-2011, University of Oslo
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

package org.hisp.dhis.light.beneficiaryregistration.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.light.utils.FormUtils;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.patient.Patient;
import org.hisp.dhis.patient.PatientAttribute;
import org.hisp.dhis.patient.PatientAttributeOption;
import org.hisp.dhis.patient.PatientAttributeOptionService;
import org.hisp.dhis.patient.PatientAttributeService;
import org.hisp.dhis.patient.PatientIdentifier;
import org.hisp.dhis.patient.PatientIdentifierType;
import org.hisp.dhis.patient.PatientIdentifierTypeService;
import org.hisp.dhis.patient.PatientService;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.util.ContextUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

public class SaveBeneficiaryAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PatientService patientService;

    public PatientService getPatientService()
    {
        return patientService;
    }

    public void setPatientService( PatientService patientService )
    {
        this.patientService = patientService;
    }

    private OrganisationUnitService organisationUnitService;

    public OrganisationUnitService getOrganisationUnitService()
    {
        return organisationUnitService;
    }

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private PatientIdentifierTypeService patientIdentifierTypeService;

    public PatientIdentifierTypeService getPatientIdentifierTypeService()
    {
        return patientIdentifierTypeService;
    }

    public void setPatientIdentifierTypeService( PatientIdentifierTypeService patientIdentifierTypeService )
    {
        this.patientIdentifierTypeService = patientIdentifierTypeService;
    }

    private PatientAttributeService patientAttributeService;

    public PatientAttributeService getPatientAttributeService()
    {
        return patientAttributeService;
    }

    public void setPatientAttributeService( PatientAttributeService patientAttributeService )
    {
        this.patientAttributeService = patientAttributeService;
    }

    private PatientAttributeOptionService patientAttributeOptionService;

    public PatientAttributeOptionService getPatientAttributeOptionService()
    {
        return patientAttributeOptionService;
    }

    public void setPatientAttributeOptionService( PatientAttributeOptionService patientAttributeOptionService )
    {
        this.patientAttributeOptionService = patientAttributeOptionService;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private Integer orgUnitId;

    public Integer getOrgUnitId()
    {
        return orgUnitId;
    }

    public void setOrgUnitId( Integer orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    private String patientFullName;

    public String getPatientFullName()
    {
        return patientFullName;
    }

    public void setPatientFullName( String patientFullName )
    {
        this.patientFullName = patientFullName;
    }

    private String gender;

    public String getGender()
    {
        return gender;
    }

    public void setGender( String gender )
    {
        this.gender = gender;
    }

    private String dateOfBirth;

    public String getDateOfBirth()
    {
        return dateOfBirth;
    }

    public void setDateOfBirth( String dateOfBirth )
    {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean validated;

    public boolean isValidated()
    {
        return validated;
    }

    public void setValidated( boolean validated )
    {
        this.validated = validated;
    }

    public Map<String, String> validationMap = new HashMap<String, String>();

    public Map<String, String> getValidationMap()
    {
        return validationMap;
    }

    public void setValidationMap( Map<String, String> validationMap )
    {
        this.validationMap = validationMap;
    }

    public Map<String, String> previousValues = new HashMap<String, String>();

    public Map<String, String> getPreviousValues()
    {
        return previousValues;
    }

    public void setPreviousValues( Map<String, String> previousValues )
    {
        this.previousValues = previousValues;
    }

    private String dobType;

    public String getDobType()
    {
        return dobType;
    }

    public void setDobType( String dobType )
    {
        this.dobType = dobType;
    }

    private Integer patientId;

    public Integer getPatientId()
    {
        return patientId;
    }

    public void setPatientId( Integer patientId )
    {
        this.patientId = patientId;
    }

    private Collection<PatientIdentifierType> patientIdentifierTypes;

    public Collection<PatientIdentifierType> getPatientIdentifierTypes()
    {
        return patientIdentifierTypes;
    }

    public void setPatientIdentifierTypes( Collection<PatientIdentifierType> patientIdentifierTypes )
    {
        this.patientIdentifierTypes = patientIdentifierTypes;
    }

    private Collection<PatientAttribute> patientAttributes;

    public Collection<PatientAttribute> getPatientAttributes()
    {
        return patientAttributes;
    }

    public void setPatientAttributes( Collection<PatientAttribute> patientAttributes )
    {
        this.patientAttributes = patientAttributes;
    }

    @Override
    public String execute()
        throws Exception
    {
        Patient patient = new Patient();
        Set<PatientIdentifier> patientIdentifierSet = new HashSet<PatientIdentifier>();
        Set<PatientAttribute> patientAttributeSet = new HashSet<PatientAttribute>();
        List<PatientAttributeValue> patientAttributeValues = new ArrayList<PatientAttributeValue>();

        patientIdentifierTypes = patientIdentifierTypeService.getAllPatientIdentifierTypes();
        patientAttributes = patientAttributeService.getAllPatientAttributes();
        patient.setOrganisationUnit( organisationUnitService.getOrganisationUnit( orgUnitId ) );

        if ( this.patientFullName.trim().length() < 2 )
        {
            validationMap.put( "fullName", "is_invalid_name" );
        }
        else
        {
            patientFullName = patientFullName.trim();

            int startIndex = patientFullName.indexOf( ' ' );
            int endIndex = patientFullName.lastIndexOf( ' ' );

            String firstName = patientFullName.toString();
            String middleName = "";
            String lastName = "";

            if ( patientFullName.indexOf( ' ' ) != -1 )
            {
                firstName = patientFullName.substring( 0, startIndex );
                if ( startIndex == endIndex )
                {
                    middleName = "";
                    lastName = patientFullName.substring( startIndex + 1, patientFullName.length() );
                }
                else
                {
                    middleName = patientFullName.substring( startIndex + 1, endIndex );
                    lastName = patientFullName.substring( endIndex + 1, patientFullName.length() );
                }
            }

            patient.setFirstName( firstName );
            patient.setMiddleName( middleName );
            patient.setLastName( lastName );
        }

        patient.setGender( gender );
        patient.setRegistrationDate( new Date() );
        patient.setDobType( dobType.charAt( 0 ) );

        if ( dobType.equals( "A" ) )
        {
            try
            {
                patient.setBirthDateFromAge( Integer.parseInt( dateOfBirth ), Patient.AGE_TYPE_YEAR );
            }
            catch ( NumberFormatException nfe )
            {
                validationMap.put( "dob", "is_invalid_number" );
            }
        }
        else
        {
            try
            {
                DateTimeFormatter sdf = ISODateTimeFormat.yearMonthDay();
                DateTime date = sdf.parseDateTime( dateOfBirth );
                patient.setBirthDate( date.toDate() );
            }
            catch ( Exception e )
            {
                validationMap.put( "dob", "is_invalid_date" );
            }
        }

        HttpServletRequest request = (HttpServletRequest) ActionContext.getContext().get(
            ServletActionContext.HTTP_REQUEST );
        Map<String, String> parameterMap = ContextUtils.getParameterMap( request );
         
        for ( PatientIdentifierType patientIdentifierType : patientIdentifierTypeService.getAllPatientIdentifierTypes() )
        {
            if ( patientIdentifierType.getProgram() == null )
            {
                String key = "IDT" + patientIdentifierType.getId();
                String value = parameterMap.get( key );
                if ( value != null )
                {
                    if ( patientIdentifierType.isMandatory() && value.trim().equals( "" ) )
                    {
                        this.validationMap.put( key, "is_empty" );
                    }
                    else if ( patientIdentifierType.getType().equals( "number" ) && !FormUtils.isNumber( value ) )
                    {
                        this.validationMap.put( key, "is_invalid_number" );
                    }
                    else
                    {
                        PatientIdentifier patientIdentifier = new PatientIdentifier();
                        patientIdentifier.setIdentifierType( patientIdentifierType );
                        patientIdentifier.setPatient( patient );
                        patientIdentifierSet.add( patientIdentifier );
                        patientIdentifier.setIdentifier( value.trim() );
                    }
                    this.previousValues.put( key, value );
                }
            }
        }

        for ( PatientAttribute patientAttribute : patientAttributeService.getAllPatientAttributes() )
        {
            patientAttributeSet.add( patientAttribute );
            if ( patientAttribute.getProgram() == null )
            {
                String key = "AT" + patientAttribute.getId();
                String value = parameterMap.get( key );
                if ( value != null )
                {
                    if ( patientAttribute.isMandatory() && value.trim().equals( "" ) )
                    {
                        this.validationMap.put( key, "is_empty" );
                    }
                    else if ( patientAttribute.getValueType().equals( PatientAttribute.TYPE_INT )
                        && !FormUtils.isInteger( value ) )
                    {
                        this.validationMap.put( key, "is_invalid_number" );
                    }
                    else if ( patientAttribute.getValueType().equals( PatientAttribute.TYPE_DATE )
                        && !FormUtils.isDate( value ) )
                    {
                        this.validationMap.put( key, "is_invalid_date" );
                    }
                    else
                    {
                        PatientAttributeValue patientAttributeValue = new PatientAttributeValue();
                        if ( PatientAttribute.TYPE_COMBO.equalsIgnoreCase( patientAttribute.getValueType() ) )
                        {
                            PatientAttributeOption option = patientAttributeOptionService.get( NumberUtils.toInt(
                                value, 0 ) );
                            if ( option != null )
                            {
                                patientAttributeValue.setPatientAttributeOption( option );
                            }
                        }
                        patientAttributeValue.setPatient( patient );
                        patientAttributeValue.setPatientAttribute( patientAttribute );
                        patientAttributeValue.setValue( value.trim() );
                        patientAttributeValues.add( patientAttributeValue );
                    }
                    this.previousValues.put( key, value );
                }
            }
        }

        if ( this.validationMap.size() > 0 )
        {
            this.validated = false;
            this.previousValues.put( "fullName", this.patientFullName );
            this.previousValues.put( "gender", this.gender );
            this.previousValues.put( "dob", this.dateOfBirth );
            this.previousValues.put( "dobType", this.dobType );
            return ERROR;
        }
        patient.setIdentifiers( patientIdentifierSet );
        patient.setAttributes( patientAttributeSet );
        patientId = patientService.createPatient( patient, null, null, patientAttributeValues );
        validated = true;

        return SUCCESS;
    }
}