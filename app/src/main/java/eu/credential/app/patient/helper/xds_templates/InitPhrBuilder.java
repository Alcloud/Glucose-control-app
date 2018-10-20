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

public class InitPhrBuilder extends AbstractBuilder {

    public String build(String patientId, String partUUID,
                        String documentClassCode, String documentClassCodeSystem,
                        String documentClassCodeDisplay, String documentFormatCodeSystem,
                        String documentFormatGoalDisplay, String folderEntryUuid
    ) {
        Map<String, Object> values = new HashMap<>();

        @SuppressLint("SimpleDateFormat")
        String newFolderCreationTime = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(Calendar.getInstance().getTime());

        values.put("PART_UUID", partUUID);

        values.put("MESSAGE_UUID", UUID.randomUUID());

        values.put("SUBMISSION_SET_ENTRY_UUID", "urn:uuid:" + UUID.randomUUID());
        values.put("SUBMISSION_SET_SUBMISSION_TIME", newFolderCreationTime);

        values.put("SUBMISSION_SET_CONTENT_TYPE_CODE_UUID", UUID.randomUUID());

        values.put("SUBMISSION_SET_CLASSIFICATION_UUID", UUID.randomUUID());

        values.put("SUBMISSION_SET_SOURCE_ID_UUID", UUID.randomUUID());

        values.put("SUBMISSION_SET_UNIQUE_ID_UUID", UUID.randomUUID());
        values.put("SUBMISSION_SET_UNIQUE_ID_VALUE", UUID.randomUUID());

        values.put("SUBMISSION_SET_PATIENT_ID_UUID", UUID.randomUUID());
        values.put("PATIENT_ID", patientId); // z.B. 1^^^&1.3.6.1.4.1.21367.2005.3.7&ISO

        values.put("LAST_UPDATE_TIME", newFolderCreationTime);

        values.put("NEW_FOLDER_CLASS_CODE_UUID", UUID.randomUUID());
        values.put("NEW_FOLDER_CLASS_CODE_NODE_REPRESENTATION", documentClassCode);  // z.B. DUR
        values.put("NEW_FOLDER_CLASS_CODE_SYSTEM", documentClassCodeSystem); // z.B. "1.3.6.1.4.1.19376.3.276.1.5.8"
        values.put("NEW_FOLDER_CLASS_CODE_LOCALIZED_STRING", documentClassCodeDisplay); // z.B. Durchführungsprotokoll

        values.put("NEW_FOLDER_CONFIDENTIALITY_CODE_UUID", UUID.randomUUID());

        values.put("NEW_FOLDER_FORMAT_CODE_SYSTEM", documentFormatCodeSystem); // z.B. "1.2.276.0.76.3.1.158.9999.1"
        values.put("NEW_FOLDER_FORMAT_GOAL_LOCALIZED_STRING", documentFormatGoalDisplay); // z.B. Palliativtherapie

        values.put("NEW_FOLDER_UNIQUE_ID_UUID", UUID.randomUUID());
        values.put("NEW_FOLDER_UNIQUE_ID_VALUE", "1.2.4." + System.currentTimeMillis());

        values.put("NEW_FOLDER_PATIENT_ID_UUID", UUID.randomUUID());

        values.put("ASSOCIATION_SUBMISSION_SET_ASSOCIATION_UUID", UUID.randomUUID());

        values.put("FOLDER_ENTRY_UUID", "urn:uuid:" + folderEntryUuid);

        values.put("EMPTY_LINE", "\n");


        return build(values, "initPhr.mustache");
    }
}
