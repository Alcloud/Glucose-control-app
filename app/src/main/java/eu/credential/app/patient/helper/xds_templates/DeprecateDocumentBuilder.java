package eu.credential.app.patient.helper.xds_templates;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeprecateDocumentBuilder extends AbstractBuilder {
	
	public String build(String patientId, String oldDocumentEntryUuid) {

		Map<String, Object> values = new HashMap<>();

		@SuppressLint("SimpleDateFormat")
		String newDocumentCreationTime = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(Calendar.getInstance().getTime());

		values.put("MESSAGE_UUID", "urn:uuid:" + UUID.randomUUID());
		values.put("SUBMISSION_SET_ENTRY_UUID", "urn:uuid:" + UUID.randomUUID());
		values.put("SUBMISSION_SET_SUBMISSION_TIME", newDocumentCreationTime);
		values.put("SUBMISSION_SET_CONTENT_TYPE_CODE_UUID", "urn:uuid:" + UUID.randomUUID());
		values.put("SUBMISSION_SET_CLASSIFICATION_UUID", "urn:uuid:" + UUID.randomUUID());
		values.put("SUBMISSION_SET_SOURCE_ID_UUID", "urn:uuid:" + UUID.randomUUID());
		values.put("SUBMISSION_SET_UNIQUE_ID_UUID", "urn:uuid:" + UUID.randomUUID());
		values.put("SUBMISSION_SET_UNIQUE_ID_VALUE", "urn:uuid:" + UUID.randomUUID());
		values.put("SUBMISSION_SET_PATIENT_ID_UUID", "urn:uuid:" + UUID.randomUUID());
		values.put("PATIENT_ID", patientId); // z.B. 1^^^&1.3.6.1.4.1.21367.2005.3.7&ISO
		values.put("ASSOCIATION_SUBMISSION_SET_OLD_DOCUMENT_UUID", "urn:uuid:" + UUID.randomUUID());
		values.put("OLD_DOCUMENT_ENTRY_UUID", oldDocumentEntryUuid); // document to be deprecated z.B. urn:uuid:c79753a8-405d-42e3-a4f5-8b09b94ea47f
		values.put("EMPTY_LINE", "\n");
		
		return build(values, "deprecateDocuments.mustache");
	}
	
}
