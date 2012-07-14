/*
 * Copyright (c) 2004-2009, University of Oslo
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

package org.hisp.dhis.patient;

import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.patientattributevalue.PatientAttributeValue;
import org.hisp.dhis.patientattributevalue.PatientAttributeValueService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.user.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chau Thu Tran
 * 
 * @version DefaultPatientRegistrationFormService.java 9:44:29 AM Jan 31, 2013 $
 */
@Transactional
public class DefaultPatientRegistrationFormService
    implements PatientRegistrationFormService
{
    private static final String TAG_OPEN = "<";

    private static final String TAG_CLOSE = "/>";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PatientRegistrationFormStore formStore;

    public void setFormStore( PatientRegistrationFormStore formStore )
    {
        this.formStore = formStore;
    }

    private PatientIdentifierTypeService identifierTypeService;

    public void setIdentifierTypeService( PatientIdentifierTypeService identifierTypeService )
    {
        this.identifierTypeService = identifierTypeService;
    }

    private PatientIdentifierService identifierService;

    public void setIdentifierService( PatientIdentifierService identifierService )
    {
        this.identifierService = identifierService;
    }

    private PatientAttributeService attributeService;

    public void setAttributeService( PatientAttributeService attributeService )
    {
        this.attributeService = attributeService;
    }

    private PatientAttributeValueService attributeValueService;

    public void setAttributeValueService( PatientAttributeValueService attributeValueService )
    {
        this.attributeValueService = attributeValueService;
    }

    // -------------------------------------------------------------------------
    // PatientRegistrationForm implementation
    // -------------------------------------------------------------------------

    @Override
    public int savePatientRegistrationForm( PatientRegistrationForm registrationForm )
    {
        return formStore.save( registrationForm );
    }

    @Override
    public void deletePatientRegistrationForm( PatientRegistrationForm registrationForm )
    {
        formStore.delete( registrationForm );
    }

    @Override
    public void updatePatientRegistrationForm( PatientRegistrationForm registrationForm )
    {
        formStore.update( registrationForm );
    }

    @Override
    public PatientRegistrationForm getPatientRegistrationForm( int id )
    {
        return formStore.get( id );
    }

    @Override
    public Collection<PatientRegistrationForm> getAllPatientRegistrationForms()
    {
        return formStore.getAll();
    }

    @Override
    public PatientRegistrationForm getPatientRegistrationForm( Program program )
    {
        return formStore.get( program );
    }

    @Override
    public PatientRegistrationForm getCommonPatientRegistrationForm()
    {
        return formStore.getCommonForm();
    }

    @Override
    public String prepareDataEntryFormForAdd( String htmlCode, Collection<User> healthWorkers, Patient patient,
        ProgramInstance programInstance, I18n i18n, I18nFormat format )
    {
        int index = 1;

        StringBuffer sb = new StringBuffer();

        Matcher inputMatcher = INPUT_PATTERN.matcher( htmlCode );

        while ( inputMatcher.find() )
        {
            // -----------------------------------------------------------------
            // Get HTML input field code
            // -----------------------------------------------------------------

            String inputHtml = inputMatcher.group();
            Matcher fixedAttrMatcher = FIXED_ATTRIBUTE_PATTERN.matcher( inputHtml );
            Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher( inputHtml );
            Matcher dynamicAttrMatcher = DYNAMIC_ATTRIBUTE_PATTERN.matcher( inputHtml );
            Matcher programMatcher = PROGRAM_PATTERN.matcher( inputHtml );
            index++;

            if ( fixedAttrMatcher.find() && fixedAttrMatcher.groupCount() > 0 )
            {
                String fixedAttr = fixedAttrMatcher.group( 1 );

                // Get value
                String value = "";
                if ( patient != null )
                {
                    Object object = getValueFromPatient( StringUtils.capitalize( fixedAttr ), patient );
                    if ( object != null )
                    {
                        if ( object instanceof Date )
                        {
                            value = format.formatDate( (Date) object );
                        }
                        else if ( object instanceof User )
                        {
                            value = ((User) object).getId() + "";
                        }
                        else
                        {
                            value = object.toString();
                        }
                    }
                }

                inputHtml = getFixedAttributeField( inputHtml, fixedAttr, value.toString(), healthWorkers, i18n, index );
            }
            else if ( identifierMatcher.find() && identifierMatcher.groupCount() > 0 )
            {
                Integer id = Integer.parseInt( identifierMatcher.group( 1 ) );
                PatientIdentifierType identifierType = identifierTypeService.getPatientIdentifierType( id );

                // Get value
                String value = "";
                if ( patient != null )
                {
                    PatientIdentifier patientIdentifier = identifierService.getPatientIdentifier( identifierType,
                        patient );
                    if ( patientIdentifier != null )
                    {
                        value = patientIdentifier.getIdentifier();
                    }
                }

                inputHtml = "<input id=\"iden" + id + "\" name=\"iden" + id + "\" tabindex=\"" + index + "\" value=\""
                    + value + "\" ";

                inputHtml += "class=\"{validate:{required:" + identifierType.isMandatory() + ",";
                if ( identifierType.getNoChars() != null )
                {
                    inputHtml += "maxlength:" + identifierType.getNoChars() + ",";
                }

                if ( PatientIdentifierType.VALUE_TYPE_NUMBER.equals( identifierType.getType() ) )
                {
                    inputHtml += "number:true";
                }
                else if ( PatientIdentifierType.VALUE_TYPE_LETTER.equals( identifierType.getType() ) )
                {
                    inputHtml += "lettersonly:true";
                }
                inputHtml += "}}\" " + TAG_CLOSE;
            }
            else if ( dynamicAttrMatcher.find() && dynamicAttrMatcher.groupCount() > 0 )
            {
                Integer id = Integer.parseInt( dynamicAttrMatcher.group( 1 ) );
                PatientAttribute attribute = attributeService.getPatientAttribute( id );

                // Get value
                String value = "";
                if ( patient != null )
                {
                    PatientAttributeValue attributeValue = attributeValueService.getPatientAttributeValue( patient,
                        attribute );
                    if ( attributeValue != null )
                    {
                        value = attributeValue.getValue();
                    }
                }

                inputHtml = getAttributeField( inputHtml, attribute, value, i18n, index );
            }
            else if ( programMatcher.find() && programMatcher.groupCount() > 0 )
            {
                String property = programMatcher.group( 1 );

                // Get value
                String value = "";
                if ( programInstance != null )
                {
                    value = format.formatDate( ((Date) getValueFromProgram( StringUtils.capitalize( property ),
                        programInstance )) );
                }

                inputHtml = "<input id=\"" + property + "\" name=\"" + property + "\" tabindex=\"" + index
                    + "\" value=\"" + value + "\" " + TAG_CLOSE;
                inputHtml += "<script>datePicker(\"" + property + "\", true);</script>";
            }

            inputMatcher.appendReplacement( sb, inputHtml );
        }

        inputMatcher.appendTail( sb );

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private String getAttributeField( String inputHtml, PatientAttribute attribute, String value, I18n i18n, int index )
    {
        inputHtml = TAG_OPEN + "input id=\"attr" + attribute.getId() + "\" name=\"attr" + attribute.getId()
            + "\" tabindex=\"" + index + "\" ";

        inputHtml += "\" class=\"{validate:{required:" + attribute.isMandatory();
        if ( PatientAttribute.TYPE_INT.equals( attribute.getValueType() ) )
        {
            inputHtml += ",number:true";
        }
        inputHtml += "}}\" ";

        if ( attribute.getValueType().equals( PatientAttribute.TYPE_BOOL ) )
        {
            inputHtml = inputHtml.replaceFirst( "input", "select" ) + ">";

            if ( value.equals( "" ) )
            {
                inputHtml += "<option value=\"\" selected>" + i18n.getString( "no_value" ) + "</option>";
                inputHtml += "<option value=\"true\">" + i18n.getString( "yes" ) + "</option>";
                inputHtml += "<option value=\"false\">" + i18n.getString( "no" ) + "</option>";
            }
            else if ( value.equals( "true" ) )
            {
                inputHtml += "<option value=\"\">" + i18n.getString( "no_value" ) + "</option>";
                inputHtml += "<option value=\"true\" selected >" + i18n.getString( "yes" ) + "</option>";
                inputHtml += "<option value=\"false\">" + i18n.getString( "no" ) + "</option>";
            }
            else if ( value.equals( "false" ) )
            {
                inputHtml += "<option value=\"\">" + i18n.getString( "no_value" ) + "</option>";
                inputHtml += "<option value=\"true\">" + i18n.getString( "yes" ) + "</option>";
                inputHtml += "<option value=\"false\" selected >" + i18n.getString( "no" ) + "</option>";
            }

            inputHtml += "</select>";
        }
        else if ( attribute.getValueType().equals( PatientAttribute.TYPE_COMBO ) )
        {
            inputHtml = inputHtml.replaceFirst( "input", "select" ) + ">";

            for ( PatientAttributeOption option : attribute.getAttributeOptions() )
            {
                inputHtml += "<option value=\"" + option.getName() + "\" ";
                if ( option.getName().equals( value ) )
                {
                    inputHtml += " selected ";
                }
                inputHtml += ">" + option.getName() + "</option>";
            }
            inputHtml += "</select>";
        }
        else if ( attribute.getValueType().equals( PatientAttribute.TYPE_DATE ) )
        {
            String jQueryCalendar = "<script>datePicker(\"attr" + attribute.getId() + "\", false);</script>";
            inputHtml += " value=\"" + value + "\"" + TAG_CLOSE;
            inputHtml += jQueryCalendar;
        }
        else
        {
            inputHtml += TAG_CLOSE;
        }
        return inputHtml;
    }

    private String getFixedAttributeField( String inputHtml, String fixedAttr, String value,
        Collection<User> healthWorkers, I18n i18n, int index )
    {
        inputHtml = TAG_OPEN + "input id=\"" + fixedAttr + "\" name=\"" + fixedAttr + "\" tabindex=\"" + index
            + "\" value=\"" + value + "\" ";

        // Fullname fields
        if ( fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_FULLNAME ) )
        {
            inputHtml += " class=\"{validate:{required:true, rangelength:[3,50]}}\" " + TAG_CLOSE;
        }

        // Phone number fields
        else if ( fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_PHONE_NUMBER ) )
        {
            inputHtml += " class=\"{validate:{phone:true}}\" " + TAG_CLOSE;
        }

        // Gender selector
        if ( fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_GENDER ) )
        {
            inputHtml = inputHtml.replaceFirst( "input", "select" ) + ">";

            if ( value.equals( "" ) || value.equals( Patient.FEMALE ) )
            {
                inputHtml += "<option value=\"M\" >" + i18n.getString( "male" ) + "</option>";
                inputHtml += "<option value=\"F\" selected >" + i18n.getString( "female" ) + "</option>";
                inputHtml += "<option value=\"T\">" + i18n.getString( "transgender" ) + "</option>";
            }
            else if ( value.equals( Patient.MALE ) )
            {
                inputHtml += "<option value=\"M\" selected >" + i18n.getString( "male" ) + "</option>";
                inputHtml += "<option value=\"F\">" + i18n.getString( "female" ) + "</option>";
                inputHtml += "<option value=\"T\">" + i18n.getString( "transgender" ) + "</option>";
            }
            else if ( value.equals( Patient.TRANSGENDER ) )
            {
                inputHtml += "<option value=\"M\">" + i18n.getString( "male" ) + "</option>";
                inputHtml += "<option value=\"F\">" + i18n.getString( "female" ) + "</option>";
                inputHtml += "<option value=\"T\" selected >" + i18n.getString( "transgender" ) + "</option>";
            }
            inputHtml += "</select>";
        }

        // Date field
        else if ( fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_BIRTHDATE )
            || fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_DEATH_DATE )
            || fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_REGISTRATION_DATE ) )
        {
            inputHtml += TAG_CLOSE;
            if ( fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_BIRTHDATE )
                || fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_REGISTRATION_DATE ) )
            {
                inputHtml += "<script>datePicker(\"" + fixedAttr + "\", true);</script>";
            }
            else
            {
                inputHtml += "<script>datePicker(\"" + fixedAttr + "\", false);</script>";
            }
        }

        // DobType field
        else if ( fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_DOB_TYPE ) )
        {
            inputHtml = inputHtml.replaceFirst( "input", "select" ) + ">";

            if ( value.equals( "" ) || value.equals( Patient.DOB_TYPE_VERIFIED + "" ) )
            {
                inputHtml += "<option value=\"V\" selected >" + i18n.getString( "verified" ) + "</option>";
                inputHtml += "<option value=\"D\">" + i18n.getString( "declared" ) + "</option>";
                inputHtml += "<option value=\"A\">" + i18n.getString( "approximated" ) + "</option>";
            }
            else if ( value.equals( Patient.DOB_TYPE_DECLARED + "" ) )
            {
                inputHtml += "<option value=\"V\">" + i18n.getString( "verified" ) + "</option>";
                inputHtml += "<option value=\"D\" selected >" + i18n.getString( "declared" ) + "</option>";
                inputHtml += "<option value=\"A\">" + i18n.getString( "approximated" ) + "</option>";
            }
            else if ( value.equals( Patient.DOB_TYPE_APPROXIATED + "" ) )
            {
                inputHtml += "<option value=\"V\">" + i18n.getString( "verified" ) + "</option>";
                inputHtml += "<option value=\"D\">" + i18n.getString( "declared" ) + "</option>";
                inputHtml += "<option value=\"A\" selected >" + i18n.getString( "approximated" ) + "</option>";
            }

            inputHtml += "</select>";
        }

        // Health-worker field
        else if ( fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_HEALTH_WORKER ) )
        {
            inputHtml = inputHtml.replaceFirst( "input", "select" ) + ">";
            inputHtml += "<option value=\"\" selected >" + i18n.getString( "please_select" ) + "</option>";

            for ( User healthWorker : healthWorkers )
            {
                inputHtml += "<option value=\"" + healthWorker.getId() + "\" ";
                if ( value.equals( healthWorker.getId() + "" ) )
                {
                    inputHtml += " selected ";
                }
                inputHtml += ">" + healthWorker.getName() + "</option>";
            }
            inputHtml += "</select>";
        }

        // IsDead field
        else if ( fixedAttr.equals( PatientRegistrationForm.FIXED_ATTRIBUTE_IS_DEAD ) )
        {
            inputHtml += " type='checkbox' ";

            if ( value.equals( "true" ) )
            {
                inputHtml += " checked ";
            }

            inputHtml += TAG_CLOSE;
        }

        return inputHtml;
    }

    private Object getValueFromPatient( String property, Patient patient )
    {
        try
        {
            return Patient.class.getMethod( "get" + property ).invoke( patient );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        return null;
    }

    private Object getValueFromProgram( String property, ProgramInstance programInstance )
    {
        try
        {
            return ProgramInstance.class.getMethod( "get" + property ).invoke( programInstance );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        return null;
    }

}