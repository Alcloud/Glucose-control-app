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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RetrieveDocumentBuilder extends AbstractBuilder {

    public String build(String partUUID, String repositoryUniqueId, String DocumentUniqueId) {
        Map<String, Object> values = new HashMap<>();

        values.put("PART_UUID", partUUID);
        values.put("MESSAGE_UUID", UUID.randomUUID());
        values.put("REPOSITORY_UNIQUE_ID", repositoryUniqueId); //1.2.3.4.56789
        values.put("DOCUMENT_UNIQUE_ID", DocumentUniqueId);
        values.put("EMPTY_LINE", "\n");

        return build(values, "retrieveDocument.mustache");
    }
}
