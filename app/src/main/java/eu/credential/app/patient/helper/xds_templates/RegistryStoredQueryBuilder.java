package eu.credential.app.patient.helper.xds_templates;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegistryStoredQueryBuilder extends AbstractBuilder {
	
	public String build(String patientId, String documentFormatCode) {

		Map<String, Object> values = new HashMap<>();
		
		values.put("MESSAGE_UUID", UUID.randomUUID());
		values.put("PATIENT_ID", patientId); // z.B. 1^^^&1.3.6.1.4.1.21367.2005.3.7&ISO
		
		values.put("DOCUMENT_FORMAT_CODE_NODE_REPRESENTATION", documentFormatCode); 
//		values.put("DOCUMENT_FORMAT_CODE_NODE_REPRESENTATION", "urn:fhg:fokus:fc:bz:2018"); // Blutzucker
//		values.put("DOCUMENT_FORMAT_CODE_NODE_REPRESENTATION", "urn:fhg:fokus:fc:ge:2018"); // Gewicht
//		values.put("DOCUMENT_FORMAT_CODE_NODE_REPRESENTATION", "urn:fhg:fokus:fc:hba1c:2018"); // HBA1c-Wert
		values.put("DOCUMENT_FORMAT_CODE_SYSTEM", "1.2.276.0.76.3.1.158.9999.1");
		
		return build(values, "registryStoredQuery.mustache");
	}
	
}
