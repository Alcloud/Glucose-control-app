/************************************************************************************************************************
 * (C) Copyright 2016 Fraunhofer FOKUS (https://www.fokus.fraunhofer.de/)
 * Contact: ehealth@fokus.fraunhofer.de
 *
 * efa-client is licensed under a Creative Commons Attribution-NonCommercial 4.0 International Public License.
 * You should have received a copy of the license along with this work or you may obtain a copy at
 *
 * http://creativecommons.org/licenses/by-nc/4.0/
 *
 ************************************************************************************************************************/

package eu.credential.app.patient.helper.xds_templates;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;

public class CreateDocumentBuilder extends AbstractBuilder {

    public String build(String patientId, String partUUID, String documentClassCode,
                        String documentClassCodeSystem, String documentClassCodeDisplay,
                        String documentTypeCode, String documentTypeCodeSystem,
                        String documentTypeCodeDisplay, String documentFormatCode,
                        String documentFormatCodeSystem, String documentFormatCodeDisplay,
                        String hcfTypeCode, String hcfTypeCodeSystem, String hcfTypeCodeDisplay,
                        String practiceSettingCode, String practiceSettingCodeSystem,
                        String practiceSettingCodeDisplay, String documentContent) {

        Map<String, Object> values = new HashMap<>();

        @SuppressLint("SimpleDateFormat")
        String newDocumentCreationTime = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(Calendar.getInstance().getTime());

        values.put("PART_UUID", partUUID);

        values.put("MESSAGE_UUID", UUID.randomUUID());

        values.put("SUBMISSION_SET_ENTRY_UUID", "urn:uuid:" + UUID.randomUUID());
        values.put("SUBMISSION_SET_SUBMISSION_TIME", newDocumentCreationTime);

        values.put("SUBMISSION_SET_CONTENT_TYPE_CODE_UUID", UUID.randomUUID());

        values.put("SUBMISSION_SET_CLASSIFICATION_UUID", UUID.randomUUID());

        values.put("SUBMISSION_SET_SOURCE_ID_UUID", UUID.randomUUID());

        values.put("SUBMISSION_SET_UNIQUE_ID_UUID", UUID.randomUUID());
        values.put("SUBMISSION_SET_UNIQUE_ID_VALUE", UUID.randomUUID());

        values.put("SUBMISSION_SET_PATIENT_ID_UUID", UUID.randomUUID());
        values.put("PATIENT_ID", patientId); // z.B. 1^^^&1.3.6.1.4.1.21367.2005.3.7&ISO


        values.put("NEW_DOCUMENT_ENTRY_UUID", UUID.randomUUID());
        values.put("NEW_DOCUMENT_CREATION_TIME", newDocumentCreationTime);

        values.put("NEW_DOCUMENT_CLASS_CODE_UUID", UUID.randomUUID());
        values.put("NEW_DOCUMENT_CLASS_CODE_NODE_REPRESENTATION", documentClassCode);  // z.B. DUR
        values.put("NEW_DOCUMENT_CLASS_CODE_SYSTEM", documentClassCodeSystem); // z.B. "1.3.6.1.4.1.19376.3.276.1.5.8"
        values.put("NEW_DOCUMENT_CLASS_CODE_LOCALIZED_STRING", documentClassCodeDisplay); // z.B. Durchführungsprotokoll

        values.put("NEW_DOCUMENT_CONFIDENTIALITY_CODE_UUID", UUID.randomUUID());

        values.put("NEW_DOCUMENT_FORMAT_CODE_UUID", UUID.randomUUID());
        values.put("NEW_DOCUMENT_FORMAT_CODE_NODE_REPRESENTATION", documentFormatCode); // z.B. urn:fhg:fokus:fc:bz:2018
        values.put("NEW_DOCUMENT_FORMAT_CODE_SYSTEM", documentFormatCodeSystem); // z.B. "1.2.276.0.76.3.1.158.9999.1"
        values.put("NEW_DOCUMENT_FORMAT_CODE_LOCALIZED_STRING", documentFormatCodeDisplay); // z.B. Blutzuckermesswerte

        values.put("NEW_DOCUMENT_HCF_TYPE_CODE_UUID", UUID.randomUUID());
        values.put("NEW_DOCUMENT_HCF_TYPE_CODE_NODE_REPRESENTATION", hcfTypeCode); // z.B. PAT
        values.put("NEW_DOCUMENT_HCF_TYPE_CODE_SYSTEM", hcfTypeCodeSystem); // z.B. 1.3.6.1.4.1.19376.3.276.1.5.3
        values.put("NEW_DOCUMENT_HCF_TYPE_CODE_LOCALIZED_STRING", hcfTypeCodeDisplay); //z.B. Patient außerhalb der Betreuung

        values.put("NEW_DOCUMENT_PRACTICE_SETTING_CODE_UUID", UUID.randomUUID());
        values.put("NEW_DOCUMENT_PRACTICE_SETTING_CODE_NODE_REPRESENTATION", practiceSettingCode); // z.B. PAT
        values.put("NEW_DOCUMENT_PRACTICE_SETTING_CODE_SYSTEM", practiceSettingCodeSystem); // z.B. 1.3.6.1.4.1.19376.3.276.1.5.5
        values.put("NEW_DOCUMENT_PRACTICE_SETTING_CODE_LOCALIZED_STRING", practiceSettingCodeDisplay); //z.B. Patient außerhalb der Betreuung

        values.put("NEW_DOCUMENT_TYPE_CODE_UUID", UUID.randomUUID());
        values.put("NEW_DOCUMENT_TYPE_CODE_NODE_REPRESENTATION", documentTypeCode); // z.B. FUNK
        values.put("NEW_DOCUMENT_TYPE_CODE_SYSTEM", documentTypeCodeSystem); // z.B. 1.3.6.1.4.1.19376.3.276.1.5.9
        values.put("NEW_DOCUMENT_TYPE_CODE_LOCALIZED_STRING", documentTypeCodeDisplay); // z.B. Ergebnisse Funktionsdiagnostik

        values.put("NEW_DOCUMENT_UNIQUE_ID_UUID", UUID.randomUUID());
        values.put("NEW_DOCUMENT_UNIQUE_ID_VALUE", UUID.randomUUID());

        values.put("NEW_DOCUMENT_PATIENT_ID_UUID", UUID.randomUUID());

        values.put("ASSOCIATION_SUBMISSION_SET_NEW_DOCUMENT_UUID", UUID.randomUUID());

        values.put("ASSOCIATION_EXISTING_FOLDER_NEW_DOCUMENT_UUID", UUID.randomUUID());

        values.put("EXISTING_FOLDER_ENTRY_UUID", "urn:uuid:" + SavePreferences.getDefaultsString("registryObject", PatientApp.getContext()));

        values.put("ASSOCIATION_SUBMISSION_SET_ASSOCIATION_UUID", UUID.randomUUID());

        values.put("DOCUMENT_CONTENT", documentContent);

        values.put("EMPTY_LINE", "\n");


        return build(values, "createDocuments.mustache");
    }

}
