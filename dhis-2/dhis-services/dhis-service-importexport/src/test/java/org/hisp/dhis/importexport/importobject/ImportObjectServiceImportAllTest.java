package org.hisp.dhis.importexport.importobject;

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

import org.amplecode.quick.BatchHandler;
import org.amplecode.quick.BatchHandlerFactory;
import org.hisp.dhis.DhisTest;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategory;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOption;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.importexport.GroupMemberAssociation;
import org.hisp.dhis.importexport.GroupMemberType;
import org.hisp.dhis.importexport.ImportDataValue;
import org.hisp.dhis.importexport.ImportObjectService;
import org.hisp.dhis.importexport.ImportObjectStatus;
import org.hisp.dhis.jdbc.batchhandler.ImportDataValueBatchHandler;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
@SuppressWarnings("unused")
public class ImportObjectServiceImportAllTest
    extends DhisTest
{
    private ImportObjectService importObjectService;

    private BatchHandlerFactory batchHandlerFactory;

    // -------------------------------------------------------------------------
    // CategoryOption
    // -------------------------------------------------------------------------

    private DataElementCategoryOption categoryOptionA;
    private DataElementCategoryOption categoryOptionB;
    private DataElementCategoryOption categoryOptionC;
    private DataElementCategoryOption categoryOptionD;

    private DataElementCategoryOption categoryOptionADuplicate;
    private DataElementCategoryOption categoryOptionBDuplicate;
    private DataElementCategoryOption categoryOptionCDuplicate;
    private DataElementCategoryOption categoryOptionDDuplicate;

    // -------------------------------------------------------------------------
    // Category
    // -------------------------------------------------------------------------

    private DataElementCategory categoryA;
    private DataElementCategory categoryB;

    private DataElementCategory categoryADuplicate;
    private DataElementCategory categoryBDuplicate;

    // -------------------------------------------------------------------------
    // CategoryCombo
    // -------------------------------------------------------------------------

    private DataElementCategoryCombo categoryComboA;
    private DataElementCategoryCombo categoryComboB;

    private DataElementCategoryCombo categoryComboADuplicate;
    private DataElementCategoryCombo categoryComboBDuplicate;

    // -------------------------------------------------------------------------
    // CategoryOptionCombo
    // -------------------------------------------------------------------------

    private DataElementCategoryOptionCombo categoryOptionComboA;
    private DataElementCategoryOptionCombo categoryOptionComboB;
    private DataElementCategoryOptionCombo categoryOptionComboC;
    private DataElementCategoryOptionCombo categoryOptionComboD;

    private DataElementCategoryOptionCombo categoryOptionComboADuplicate;
    private DataElementCategoryOptionCombo categoryOptionComboBDuplicate;
    private DataElementCategoryOptionCombo categoryOptionComboCDuplicate;
    private DataElementCategoryOptionCombo categoryOptionComboDDuplicate;

    // -------------------------------------------------------------------------
    // CategoryCategoryOptionAssociation
    // -------------------------------------------------------------------------

    private GroupMemberAssociation categoryCategoryOptionAssociationA;
    private GroupMemberAssociation categoryCategoryOptionAssociationB;
    private GroupMemberAssociation categoryCategoryOptionAssociationC;
    private GroupMemberAssociation categoryCategoryOptionAssociationD;

    // -------------------------------------------------------------------------
    // CategoryComboCategoryAssociation
    // -------------------------------------------------------------------------

    private GroupMemberAssociation categoryComboCategoryAssociationA;
    private GroupMemberAssociation categoryComboCategoryAssociationB;
    private GroupMemberAssociation categoryComboCategoryAssociationC;

    // -------------------------------------------------------------------------
    // DataElement
    // -------------------------------------------------------------------------

    private DataElement dataElementA;
    private DataElement dataElementB;
    private DataElement dataElementC;
    private DataElement dataElementD;

    private DataElement dataElementAModified;
    private DataElement dataElementBModified;
    private DataElement dataElementCModified;
    private DataElement dataElementDModified;

    // -------------------------------------------------------------------------
    // DataElementGroup
    // -------------------------------------------------------------------------

    private DataElementGroup dataElementGroupA;
    private DataElementGroup dataElementGroupB;
    private DataElementGroup dataElementGroupC;

    private DataElementGroup dataElementGroupADuplicate;
    private DataElementGroup dataElementGroupBDuplicate;
    private DataElementGroup dataElementGroupCDuplicate;

    private GroupMemberAssociation dataElementGroupAssociationA;
    private GroupMemberAssociation dataElementGroupAssociationB;
    private GroupMemberAssociation dataElementGroupAssociationC;
    private GroupMemberAssociation dataElementGroupAssociationD;
    private GroupMemberAssociation dataElementGroupAssociationE;
    private GroupMemberAssociation dataElementGroupAssociationF;
    private GroupMemberAssociation dataElementGroupAssociationG;
    private GroupMemberAssociation dataElementGroupAssociationH;
    private GroupMemberAssociation dataElementGroupAssociationI;
    private GroupMemberAssociation dataElementGroupAssociationJ;
    private GroupMemberAssociation dataElementGroupAssociationK;
    private GroupMemberAssociation dataElementGroupAssociationL;

    // -------------------------------------------------------------------------
    // Period
    // -------------------------------------------------------------------------

    private PeriodType periodTypeA;

    private Period periodA;

    private Period periodADuplicate;

    // -------------------------------------------------------------------------
    // OrganisationUnit
    // -------------------------------------------------------------------------

    private OrganisationUnit organisationUnitA;
    private OrganisationUnit organisationUnitB;
    private OrganisationUnit organisationUnitC;

    private OrganisationUnit organisationUnitAModified;
    private OrganisationUnit organisationUnitBModified;
    private OrganisationUnit organisationUnitCModified;

    // -------------------------------------------------------------------------
    // RelationshipAssociation
    // -------------------------------------------------------------------------

    private GroupMemberAssociation relationshipAssociationA;
    private GroupMemberAssociation relationshipAssociationB;

    // -------------------------------------------------------------------------
    // DataValue
    // -------------------------------------------------------------------------

    private DataValue dataValueA;
    private DataValue dataValueB;
    private DataValue dataValueC;
    private DataValue dataValueD;
    private DataValue dataValueE;
    private DataValue dataValueF;
    private DataValue dataValueG;
    private DataValue dataValueH;
    private DataValue dataValueI;

    private DataValue dataValueADuplicate;
    private DataValue dataValueBDuplicate;
    private DataValue dataValueCDuplicate;
    private DataValue dataValueDDuplicate;
    private DataValue dataValueEDuplicate;
    private DataValue dataValueFDuplicate;
    private DataValue dataValueGDuplicate;
    private DataValue dataValueHDuplicate;
    private DataValue dataValueIDuplicate;

    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
    {
        // ---------------------------------------------------------------------
        // Services
        // ---------------------------------------------------------------------

        importObjectService = (ImportObjectService) getBean( ImportObjectService.ID );

        batchHandlerFactory = (BatchHandlerFactory) getBean( "batchHandlerFactory" );

        dataElementService = (DataElementService) getBean( DataElementService.ID );

        categoryService = (DataElementCategoryService) getBean( DataElementCategoryService.ID );

        periodService = (PeriodService) getBean( PeriodService.ID );

        organisationUnitService = (OrganisationUnitService) getBean( OrganisationUnitService.ID );

        dataValueService = (DataValueService) getBean( DataValueService.ID );

        // ---------------------------------------------------------------------
        // CategoryOption
        // ---------------------------------------------------------------------

        categoryOptionA = new DataElementCategoryOption( "CategoryOptionA" );
        categoryOptionB = new DataElementCategoryOption( "CategoryOptionB" );
        categoryOptionC = new DataElementCategoryOption( "CategoryOptionC" );
        categoryOptionD = new DataElementCategoryOption( "CategoryOptionD" );

        categoryService.addDataElementCategoryOption( categoryOptionA );
        categoryService.addDataElementCategoryOption( categoryOptionB );
        categoryService.addDataElementCategoryOption( categoryOptionC );
        categoryService.addDataElementCategoryOption( categoryOptionD );

        categoryOptionADuplicate = new DataElementCategoryOption( "CategoryOptionA" );
        categoryOptionBDuplicate = new DataElementCategoryOption( "CategoryOptionB" );
        categoryOptionCDuplicate = new DataElementCategoryOption( "CategoryOptionC" );
        categoryOptionDDuplicate = new DataElementCategoryOption( "CategoryOptionD" );

        categoryOptionADuplicate.setId( 'A' );
        categoryOptionBDuplicate.setId( 'B' );
        categoryOptionCDuplicate.setId( 'C' );
        categoryOptionDDuplicate.setId( 'D' );

        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryOptionADuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryOptionBDuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryOptionCDuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryOptionDDuplicate, null );

        // ---------------------------------------------------------------------
        // Category
        // ---------------------------------------------------------------------

        categoryA = new DataElementCategory( "CategoryA" );
        categoryB = new DataElementCategory( "CategoryB" );

        categoryA.getCategoryOptions().add( categoryOptionA );
        categoryA.getCategoryOptions().add( categoryOptionB );
        categoryB.getCategoryOptions().add( categoryOptionC );
        categoryB.getCategoryOptions().add( categoryOptionD );

        categoryService.addDataElementCategory( categoryA );
        categoryService.addDataElementCategory( categoryB );

        categoryADuplicate = new DataElementCategory( "CategoryA" );
        categoryBDuplicate = new DataElementCategory( "CategoryB" );

        categoryADuplicate.setId( 'A' );
        categoryBDuplicate.setId( 'B' );

        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryADuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryBDuplicate, null );

        // ---------------------------------------------------------------------
        // Category - CategoryOption Association
        // ---------------------------------------------------------------------

        categoryCategoryOptionAssociationA = new GroupMemberAssociation( 'A', 'A', 1 );
        categoryCategoryOptionAssociationB = new GroupMemberAssociation( 'A', 'B', 2 );
        categoryCategoryOptionAssociationC = new GroupMemberAssociation( 'B', 'C', 1 );
        categoryCategoryOptionAssociationD = new GroupMemberAssociation( 'B', 'D', 2 );

        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.CATEGORY_CATEGORYOPTION, categoryCategoryOptionAssociationA );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.CATEGORY_CATEGORYOPTION, categoryCategoryOptionAssociationB );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.CATEGORY_CATEGORYOPTION, categoryCategoryOptionAssociationC );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.CATEGORY_CATEGORYOPTION, categoryCategoryOptionAssociationD );

        // ---------------------------------------------------------------------
        // CategoryCombo
        // ---------------------------------------------------------------------

        categoryComboA = new DataElementCategoryCombo( "CategoryComboA" );
        categoryComboB = new DataElementCategoryCombo( "CategoryComboB" );

        categoryComboA.getCategories().add( categoryA );
        categoryComboA.getCategories().add( categoryB );
        categoryComboB.getCategories().add( categoryA );

        categoryService.addDataElementCategoryCombo( categoryComboA );
        categoryService.addDataElementCategoryCombo( categoryComboB );

        categoryComboADuplicate = new DataElementCategoryCombo( "CategoryComboA" );
        categoryComboBDuplicate = new DataElementCategoryCombo( "CategoryComboB" );

        categoryComboADuplicate.setId( 'A' );
        categoryComboBDuplicate.setId( 'B' );

        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryComboADuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryComboBDuplicate, null );

        // ---------------------------------------------------------------------
        // CategoryCombo - Category Association
        // ---------------------------------------------------------------------

        categoryComboCategoryAssociationA = new GroupMemberAssociation( 'A', 'A', 1 );
        categoryComboCategoryAssociationB = new GroupMemberAssociation( 'A', 'B', 2 );
        categoryComboCategoryAssociationC = new GroupMemberAssociation( 'B', 'A', 1 );

        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.CATEGORYCOMBO_CATEGORY, categoryComboCategoryAssociationA );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.CATEGORYCOMBO_CATEGORY, categoryComboCategoryAssociationB );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.CATEGORYCOMBO_CATEGORY, categoryComboCategoryAssociationC );

        // ---------------------------------------------------------------------
        // CategoryOptionCombo
        // ---------------------------------------------------------------------

        categoryOptionComboA = new DataElementCategoryOptionCombo();
        categoryOptionComboB = new DataElementCategoryOptionCombo();
        categoryOptionComboC = new DataElementCategoryOptionCombo();
        categoryOptionComboD = new DataElementCategoryOptionCombo();

        categoryOptionComboA.setCategoryCombo( categoryComboA );
        categoryOptionComboB.setCategoryCombo( categoryComboA );
        categoryOptionComboC.setCategoryCombo( categoryComboB );
        categoryOptionComboD.setCategoryCombo( categoryComboB );

        categoryOptionComboA.getCategoryOptions().add( categoryOptionA );
        categoryOptionComboA.getCategoryOptions().add( categoryOptionC );
        categoryOptionComboB.getCategoryOptions().add( categoryOptionB );
        categoryOptionComboB.getCategoryOptions().add( categoryOptionD );
        categoryOptionComboC.getCategoryOptions().add( categoryOptionA );
        categoryOptionComboD.getCategoryOptions().add( categoryOptionB );

        categoryService.addDataElementCategoryOptionCombo( categoryOptionComboA );
        categoryService.addDataElementCategoryOptionCombo( categoryOptionComboB );
        categoryService.addDataElementCategoryOptionCombo( categoryOptionComboC );
        categoryService.addDataElementCategoryOptionCombo( categoryOptionComboD );

        categoryOptionComboADuplicate = new DataElementCategoryOptionCombo();
        categoryOptionComboBDuplicate = new DataElementCategoryOptionCombo();
        categoryOptionComboCDuplicate = new DataElementCategoryOptionCombo();
        categoryOptionComboDDuplicate = new DataElementCategoryOptionCombo();

        categoryOptionComboADuplicate.setId( 'A' );
        categoryOptionComboBDuplicate.setId( 'B' );
        categoryOptionComboCDuplicate.setId( 'C' );
        categoryOptionComboDDuplicate.setId( 'D' );

        categoryOptionComboADuplicate.setCategoryCombo( categoryComboADuplicate );
        categoryOptionComboBDuplicate.setCategoryCombo( categoryComboADuplicate );
        categoryOptionComboCDuplicate.setCategoryCombo( categoryComboBDuplicate );
        categoryOptionComboDDuplicate.setCategoryCombo( categoryComboBDuplicate );

        categoryOptionComboADuplicate.getCategoryOptions().add( categoryOptionADuplicate );
        categoryOptionComboADuplicate.getCategoryOptions().add( categoryOptionCDuplicate );
        categoryOptionComboBDuplicate.getCategoryOptions().add( categoryOptionBDuplicate );
        categoryOptionComboBDuplicate.getCategoryOptions().add( categoryOptionDDuplicate );
        categoryOptionComboCDuplicate.getCategoryOptions().add( categoryOptionADuplicate );
        categoryOptionComboDDuplicate.getCategoryOptions().add( categoryOptionBDuplicate );

        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryOptionComboADuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryOptionComboBDuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryOptionComboCDuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, categoryOptionComboDDuplicate, null );

        // ---------------------------------------------------------------------
        // DataElement
        // ---------------------------------------------------------------------

        dataElementA = createDataElement( 'A', categoryComboA );
        dataElementB = createDataElement( 'B', categoryComboA );
        dataElementC = createDataElement( 'C', categoryComboA );
        dataElementD = createDataElement( 'D', categoryComboA );

        dataElementAModified = createDataElement( 'A', categoryComboADuplicate );
        dataElementBModified = createDataElement( 'B', categoryComboADuplicate );
        dataElementCModified = createDataElement( 'C', categoryComboADuplicate );
        dataElementDModified = createDataElement( 'D', categoryComboADuplicate );

        dataElementAModified.setId( 'A' );
        dataElementBModified.setId( 'B' );
        dataElementCModified.setId( 'C' );
        dataElementDModified.setId( 'D' );

        dataElementAModified.setShortName( "ShortNameModifiedA" );
        dataElementBModified.setShortName( "ShortNameModifiedB" );
        dataElementCModified.setShortName( "ShortNameModifiedC" );
        dataElementDModified.setShortName( "ShortNameModifiedD" );

        // ---------------------------------------------------------------------
        // DataElementGroup
        // ---------------------------------------------------------------------

        dataElementGroupA = createDataElementGroup( 'A' );
        dataElementGroupB = createDataElementGroup( 'B' );
        dataElementGroupC = createDataElementGroup( 'C' );

        dataElementGroupADuplicate = createDataElementGroup( 'A' );
        dataElementGroupBDuplicate = createDataElementGroup( 'B' );
        dataElementGroupCDuplicate = createDataElementGroup( 'C' );

        dataElementGroupADuplicate.setId( 'A' );
        dataElementGroupBDuplicate.setId( 'B' );
        dataElementGroupCDuplicate.setId( 'C' );

        dataElementGroupAssociationA = new GroupMemberAssociation( 'A', 'A' );
        dataElementGroupAssociationB = new GroupMemberAssociation( 'A', 'B' );
        dataElementGroupAssociationC = new GroupMemberAssociation( 'A', 'C' );
        dataElementGroupAssociationD = new GroupMemberAssociation( 'A', 'D' );
        dataElementGroupAssociationE = new GroupMemberAssociation( 'B', 'A' );
        dataElementGroupAssociationF = new GroupMemberAssociation( 'B', 'B' );
        dataElementGroupAssociationG = new GroupMemberAssociation( 'B', 'C' );
        dataElementGroupAssociationH = new GroupMemberAssociation( 'B', 'D' );
        dataElementGroupAssociationI = new GroupMemberAssociation( 'C', 'A' );
        dataElementGroupAssociationJ = new GroupMemberAssociation( 'C', 'B' );
        dataElementGroupAssociationK = new GroupMemberAssociation( 'C', 'C' );
        dataElementGroupAssociationL = new GroupMemberAssociation( 'C', 'D' );

        // ---------------------------------------------------------------------
        // Period
        // ---------------------------------------------------------------------

        periodTypeA = periodService.getPeriodTypeByName( MonthlyPeriodType.NAME );

        periodA = createPeriod( periodTypeA, getDate( 1, 0, 2000 ), getDate( 31, 0, 2000 ) );

        periodADuplicate = createPeriod( periodTypeA, getDate( 1, 0, 2000 ), getDate( 31, 0, 2000 ) );

        periodADuplicate.setId( 'A' );

        // ---------------------------------------------------------------------
        // OrganisationUnit
        // ---------------------------------------------------------------------

        organisationUnitA = createOrganisationUnit( 'A' );
        organisationUnitB = createOrganisationUnit( 'B' );
        organisationUnitC = createOrganisationUnit( 'C' );

        organisationUnitAModified = createOrganisationUnit( 'A' );
        organisationUnitBModified = createOrganisationUnit( 'B' );
        organisationUnitCModified = createOrganisationUnit( 'C' );

        organisationUnitAModified.setId( 'A' );
        organisationUnitBModified.setId( 'B' );
        organisationUnitCModified.setId( 'C' );

        organisationUnitAModified.setShortName( "ShortNameModifiedA" );
        organisationUnitBModified.setShortName( "ShortNameModifiedB" );
        organisationUnitCModified.setShortName( "ShortNameModifiedC" );

        // ---------------------------------------------------------------------
        // RelationshipAssociation
        // ---------------------------------------------------------------------

        relationshipAssociationA = new GroupMemberAssociation( 'A', 'B' );
        relationshipAssociationB = new GroupMemberAssociation( 'B', 'C' );

        // ---------------------------------------------------------------------
        // DataValue
        // ---------------------------------------------------------------------

        dataValueA = createDataValue( dataElementA, periodA, organisationUnitA, "10", categoryOptionComboA );
        dataValueB = createDataValue( dataElementA, periodA, organisationUnitB, "10", categoryOptionComboA );
        dataValueC = createDataValue( dataElementA, periodA, organisationUnitC, "10", categoryOptionComboA );
        dataValueD = createDataValue( dataElementB, periodA, organisationUnitA, "10", categoryOptionComboA );
        dataValueE = createDataValue( dataElementB, periodA, organisationUnitB, "10", categoryOptionComboA );
        dataValueF = createDataValue( dataElementB, periodA, organisationUnitC, "10", categoryOptionComboA );
        dataValueG = createDataValue( dataElementC, periodA, organisationUnitA, "10", categoryOptionComboA );
        dataValueH = createDataValue( dataElementC, periodA, organisationUnitB, "10", categoryOptionComboA );
        dataValueI = createDataValue( dataElementC, periodA, organisationUnitC, "10", categoryOptionComboA );

        dataValueADuplicate = createDataValue( dataElementAModified, periodADuplicate, organisationUnitAModified, "10", categoryOptionComboADuplicate );
        dataValueBDuplicate = createDataValue( dataElementAModified, periodADuplicate, organisationUnitBModified, "10", categoryOptionComboADuplicate );
        dataValueCDuplicate = createDataValue( dataElementAModified, periodADuplicate, organisationUnitCModified, "10", categoryOptionComboADuplicate );
        dataValueDDuplicate = createDataValue( dataElementBModified, periodADuplicate, organisationUnitAModified, "10", categoryOptionComboADuplicate );
        dataValueEDuplicate = createDataValue( dataElementBModified, periodADuplicate, organisationUnitBModified, "10", categoryOptionComboADuplicate );
        dataValueFDuplicate = createDataValue( dataElementBModified, periodADuplicate, organisationUnitCModified, "10", categoryOptionComboADuplicate );
        dataValueGDuplicate = createDataValue( dataElementCModified, periodADuplicate, organisationUnitAModified, "10", categoryOptionComboADuplicate );
        dataValueHDuplicate = createDataValue( dataElementCModified, periodADuplicate, organisationUnitBModified, "10", categoryOptionComboADuplicate );
        dataValueIDuplicate = createDataValue( dataElementCModified, periodADuplicate, organisationUnitCModified, "10", categoryOptionComboADuplicate );
    }

    @Override
    public boolean emptyDatabaseAfterTest()
    {
        return true;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Ignore
    @Test
    public void testImportAllWithNewOnly()
    {
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementAModified, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementBModified, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementCModified, null );

        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementGroupADuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementGroupBDuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementGroupCDuplicate, null );

        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationA );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationB );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationC );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationE );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationF );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationG );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationI );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationJ );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationK );

        dbmsManager.clearSession();

        importObjectService.importAll();

        Collection<DataElement> dataElements = dataElementService.getAllDataElements();

        assertEquals( dataElements.size(), 3 );

        assertTrue( dataElements.contains( dataElementA ) );
        assertTrue( dataElements.contains( dataElementB ) );
        assertTrue( dataElements.contains( dataElementC ) );

        Collection<DataElementGroup> dataElementGroups = dataElementService.getAllDataElementGroups();

        assertEquals( dataElementGroups.size(), 3 );

        assertTrue( dataElementGroups.contains( dataElementGroupA ) );
        assertTrue( dataElementGroups.contains( dataElementGroupB ) );
        assertTrue( dataElementGroups.contains( dataElementGroupC ) );

        for ( DataElementGroup dataElementGroup : dataElementGroups )
        {
            assertEquals( dataElementGroup.getMembers().size(), 3 );

            assertTrue( dataElementGroup.getMembers().containsAll( dataElements ) );
        }

        assertEquals( importObjectService.getImportObjects( DataElement.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( DataElementGroup.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( GroupMemberAssociation.class ).size(), 0 );
    }

    @Ignore
    @Test
    public void testImportAllWithUpdatesOnly()
    {
        dataElementService.addDataElement( dataElementA );
        dataElementService.addDataElement( dataElementB );
        dataElementService.addDataElement( dataElementC );

        dataElementGroupA.getMembers().add( dataElementA );
        dataElementGroupB.getMembers().add( dataElementA );
        dataElementGroupC.getMembers().add( dataElementA );

        dataElementService.addDataElementGroup( dataElementGroupA );
        dataElementService.addDataElementGroup( dataElementGroupB );
        dataElementService.addDataElementGroup( dataElementGroupC );

        importObjectService.addImportObject( ImportObjectStatus.UPDATE, dataElementAModified, dataElementA );
        importObjectService.addImportObject( ImportObjectStatus.UPDATE, dataElementBModified, dataElementB );
        importObjectService.addImportObject( ImportObjectStatus.UPDATE, dataElementCModified, dataElementC );

        importObjectService.addImportObject( ImportObjectStatus.MATCH, GroupMemberType.NONE, dataElementGroupADuplicate );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, GroupMemberType.NONE, dataElementGroupBDuplicate );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, GroupMemberType.NONE, dataElementGroupCDuplicate );

        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationB );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationC );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationF );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationG );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationJ );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationK );

        dbmsManager.clearSession();

        importObjectService.importAll();

        Collection<DataElement> dataElements = dataElementService.getAllDataElements();

        assertEquals( dataElements.size(), 3 );

        assertTrue( dataElements.contains( dataElementAModified ) );
        assertTrue( dataElements.contains( dataElementBModified ) );
        assertTrue( dataElements.contains( dataElementCModified ) );

        Collection<DataElementGroup> dataElementGroups = dataElementService.getAllDataElementGroups();

        assertEquals( dataElementGroups.size(), 3 );

        assertTrue( dataElementGroups.contains( dataElementGroupA ) );
        assertTrue( dataElementGroups.contains( dataElementGroupB ) );
        assertTrue( dataElementGroups.contains( dataElementGroupC ) );

        for ( DataElementGroup dataElementGroup : dataElementGroups )
        {
            assertEquals( dataElementGroup.getMembers().size(), 3 );

            assertTrue( dataElementGroup.getMembers().containsAll( dataElements ) );
        }

        assertEquals( importObjectService.getImportObjects( DataElement.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( DataElementGroup.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( GroupMemberAssociation.class ).size(), 0 );
    }

    @Ignore
    @Test
    public void testImportAllWithMatchOnly()
    {
        dataElementService.addDataElement( dataElementA );
        dataElementService.addDataElement( dataElementB );
        dataElementService.addDataElement( dataElementC );

        dataElementGroupA.getMembers().add( dataElementA );
        dataElementGroupB.getMembers().add( dataElementA );
        dataElementGroupC.getMembers().add( dataElementA );

        dataElementService.addDataElementGroup( dataElementGroupA );
        dataElementService.addDataElementGroup( dataElementGroupB );
        dataElementService.addDataElementGroup( dataElementGroupC );

        importObjectService.addImportObject( ImportObjectStatus.MATCH, dataElementAModified, null );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, dataElementBModified, null );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, dataElementCModified, null );

        importObjectService.addImportObject( ImportObjectStatus.MATCH, GroupMemberType.NONE, dataElementGroupADuplicate );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, GroupMemberType.NONE, dataElementGroupBDuplicate );
        importObjectService.addImportObject( ImportObjectStatus.MATCH, GroupMemberType.NONE, dataElementGroupCDuplicate );

        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationB );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationC );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationF );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationG );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationJ );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationK );

        dbmsManager.clearSession();

        importObjectService.importAll();

        Collection<DataElement> dataElements = dataElementService.getAllDataElements();

        assertEquals( dataElements.size(), 3 );

        assertTrue( dataElements.contains( dataElementA ) );
        assertTrue( dataElements.contains( dataElementB ) );
        assertTrue( dataElements.contains( dataElementC ) );

        Collection<DataElementGroup> dataElementGroups = dataElementService.getAllDataElementGroups();

        assertEquals( dataElementGroups.size(), 3 );

        assertTrue( dataElementGroups.contains( dataElementGroupA ) );
        assertTrue( dataElementGroups.contains( dataElementGroupB ) );
        assertTrue( dataElementGroups.contains( dataElementGroupC ) );

        for ( DataElementGroup dataElementGroup : dataElementGroups )
        {
            assertEquals( dataElementGroup.getMembers().size(), 3 );

            assertTrue( dataElementGroup.getMembers().containsAll( dataElements ) );
        }

        assertEquals( importObjectService.getImportObjects( DataElement.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( DataElementGroup.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( GroupMemberAssociation.class ).size(), 0 );
    }

    @Ignore
    @Test
    public void testMatchAndImportAll()
    {
        int existingObjectIdA = dataElementService.addDataElement( dataElementA );

        dataElementGroupA.getMembers().add( dataElementA );

        int existingObjectIdB = dataElementService.addDataElementGroup( dataElementGroupA );

        int importObjectIdA = importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementBModified, null );
        int importObjectIdB = importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementCModified, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementDModified, null );

        int importObjectIdC = importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementGroupBDuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementGroupCDuplicate, null );

        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationF );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationG );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationH );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationJ );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationK );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationL );

        dbmsManager.clearSession();

        importObjectService.matchObject( importObjectIdA, existingObjectIdA );
        importObjectService.matchObject( importObjectIdB, existingObjectIdA );
        importObjectService.matchObject( importObjectIdC, existingObjectIdB );

        importObjectService.importAll();

        Collection<DataElement> dataElements = dataElementService.getAllDataElements();

        assertEquals( dataElements.size(), 2 );

        assertTrue( dataElements.contains( dataElementA ) );
        assertTrue( dataElements.contains( dataElementDModified ) );

        Collection<DataElementGroup> dataElementGroups = dataElementService.getAllDataElementGroups();

        assertEquals( dataElementGroups.size(), 2 );

        assertTrue( dataElementGroups.contains( dataElementGroupA ) );
        assertTrue( dataElementGroups.contains( dataElementGroupCDuplicate ) );

        for ( DataElementGroup dataElementGroup : dataElementGroups )
        {
            assertEquals( dataElementGroup.getMembers().size(), 2 );

            assertTrue( dataElementGroup.getMembers().containsAll( dataElements ) );
        }

        assertEquals( importObjectService.getImportObjects( DataElement.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( DataElementGroup.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( GroupMemberAssociation.class ).size(), 0 );
    }

    @Ignore
    @Test
    public void testDeleteAndImportAll()
    {
        dataElementService.addDataElement( dataElementA );

        dataElementGroupA.getMembers().add( dataElementA );

        dataElementService.addDataElementGroup( dataElementGroupA );

        int importObjectIdA = importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementBModified, null );
        int importObjectIdB = importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementCModified, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementDModified, null );

        int importObjectIdC = importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementGroupBDuplicate, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementGroupCDuplicate, null );

        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationF );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationG );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationH );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationJ );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationK );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.DATAELEMENTGROUP, dataElementGroupAssociationL );

        dbmsManager.clearSession();

        importObjectService.cascadeDeleteImportObject( importObjectIdA );
        importObjectService.cascadeDeleteImportObject( importObjectIdB );
        importObjectService.cascadeDeleteImportObject( importObjectIdC );

        importObjectService.importAll();

        Collection<DataElement> dataElements = dataElementService.getAllDataElements();

        assertEquals( dataElements.size(), 2 );

        assertTrue( dataElements.contains( dataElementA ) );
        assertTrue( dataElements.contains( dataElementDModified ) );

        Collection<DataElementGroup> dataElementGroups = dataElementService.getAllDataElementGroups();

        assertEquals( dataElementGroups.size(), 2 );

        assertTrue( dataElementGroups.contains( dataElementGroupA ) );
        assertTrue( dataElementGroups.contains( dataElementGroupCDuplicate ) );

        for ( DataElementGroup dataElementGroup : dataElementGroups )
        {
            assertEquals( dataElementGroup.getMembers().size(), 1 );
        }

        assertEquals( importObjectService.getImportObjects( DataElement.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( DataElementGroup.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( GroupMemberAssociation.class ).size(), 0 );
    }

    @Ignore
    @Test
    public void testImportOrganisationUnitRelationships()
    {
        importObjectService.addImportObject( ImportObjectStatus.NEW, organisationUnitAModified, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, organisationUnitBModified, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, organisationUnitCModified, null );

        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.ORGANISATIONUNITRELATIONSHIP, relationshipAssociationA );
        importObjectService.addImportObject( ImportObjectStatus.NEW, GroupMemberType.ORGANISATIONUNITRELATIONSHIP, relationshipAssociationB );

        dbmsManager.clearSession();

        importObjectService.importAll();

        organisationUnitAModified = organisationUnitService.getOrganisationUnitByName( organisationUnitAModified.getName() ).get( 0 );
        organisationUnitBModified = organisationUnitService.getOrganisationUnitByName( organisationUnitBModified.getName() ).get( 0 );
        organisationUnitCModified = organisationUnitService.getOrganisationUnitByName( organisationUnitCModified.getName() ).get( 0 );

        assertNotNull( organisationUnitAModified );
        assertNotNull( organisationUnitBModified );
        assertNotNull( organisationUnitCModified );

        assertNull( organisationUnitAModified.getParent() );
        assertNotNull( organisationUnitBModified.getParent() );
        assertNotNull( organisationUnitCModified.getParent() );

        assertTrue( organisationUnitBModified.getParent().equals( organisationUnitAModified ) );
        assertTrue( organisationUnitCModified.getParent().equals( organisationUnitBModified ) );

        assertEquals( importObjectService.getImportObjects( OrganisationUnit.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( GroupMemberAssociation.class ).size(), 0 );
    }

    @Ignore
    @Test
    public void testImportDataValueImportAll()
    {
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementAModified, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementBModified, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, dataElementCModified, null );

        importObjectService.addImportObject( ImportObjectStatus.NEW, periodADuplicate, null );

        importObjectService.addImportObject( ImportObjectStatus.NEW, organisationUnitAModified, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, organisationUnitBModified, null );
        importObjectService.addImportObject( ImportObjectStatus.NEW, organisationUnitCModified, null );

        BatchHandler<ImportDataValue> batchHandler = batchHandlerFactory.createBatchHandler( ImportDataValueBatchHandler.class );

        batchHandler.init();

        batchHandler.addObject( new ImportDataValue( dataValueADuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueBDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueCDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueDDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueEDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueFDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueGDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueHDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueIDuplicate, ImportObjectStatus.NEW ) );

        batchHandler.flush();

        dbmsManager.clearSession();

        importObjectService.importAll();

        assertEquals( dataElementService.getAllDataElements().size(), 3 );

        assertEquals( periodService.getPeriodsByPeriodType( periodTypeA ).size(), 1 );

        assertEquals( organisationUnitService.getAllOrganisationUnits().size(), 3 );

        assertEquals( dataValueService.getAllDataValues().size(), 9 );

        assertEquals( importObjectService.getImportObjects( DataElement.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( Period.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( OrganisationUnit.class ).size(), 0 );
    }

    @Ignore
    @Test
    public void testImportDataValueImportAllWithUpdates()
    {
        dataElementService.addDataElement( dataElementA );
        dataElementService.addDataElement( dataElementB );
        dataElementService.addDataElement( dataElementC );

        organisationUnitService.addOrganisationUnit( organisationUnitA );
        organisationUnitService.addOrganisationUnit( organisationUnitB );
        organisationUnitService.addOrganisationUnit( organisationUnitC );

        dataValueService.addDataValue( dataValueA );
        dataValueService.addDataValue( dataValueE );
        dataValueService.addDataValue( dataValueI );

        importObjectService.addImportObject( ImportObjectStatus.UPDATE, dataElementAModified, dataElementA );
        importObjectService.addImportObject( ImportObjectStatus.UPDATE, dataElementBModified, dataElementB );
        importObjectService.addImportObject( ImportObjectStatus.UPDATE, dataElementCModified, dataElementC );

        importObjectService.addImportObject( ImportObjectStatus.MATCH, periodADuplicate, null );

        importObjectService.addImportObject( ImportObjectStatus.UPDATE, organisationUnitAModified, organisationUnitA );
        importObjectService.addImportObject( ImportObjectStatus.UPDATE, organisationUnitBModified, organisationUnitB );
        importObjectService.addImportObject( ImportObjectStatus.UPDATE, organisationUnitCModified, organisationUnitC );

        BatchHandler<ImportDataValue> batchHandler = batchHandlerFactory.createBatchHandler( ImportDataValueBatchHandler.class );

        batchHandler.init();

        batchHandler.addObject( new ImportDataValue( dataValueADuplicate, ImportObjectStatus.UPDATE ) );
        batchHandler.addObject( new ImportDataValue( dataValueBDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueCDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueDDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueEDuplicate, ImportObjectStatus.UPDATE ) );
        batchHandler.addObject( new ImportDataValue( dataValueFDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueGDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueHDuplicate, ImportObjectStatus.NEW ) );
        batchHandler.addObject( new ImportDataValue( dataValueIDuplicate, ImportObjectStatus.UPDATE ) );

        batchHandler.flush();

        dbmsManager.clearSession();

        importObjectService.importAll();

        assertEquals( dataElementService.getAllDataElements().size(), 3 );

        assertEquals( periodService.getPeriodsByPeriodType( periodTypeA ).size(), 1 );

        assertEquals( organisationUnitService.getAllOrganisationUnits().size(), 3 );

        assertEquals( dataValueService.getAllDataValues().size(), 9 );

        assertEquals( importObjectService.getImportObjects( DataElement.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( Period.class ).size(), 0 );
        assertEquals( importObjectService.getImportObjects( OrganisationUnit.class ).size(), 0 );
    }
}
