package eu.credential.app.patient.orchestration.http;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import at.tugraz.iaik.pre.afgh.key.PREAFGHPrivateKey;
import at.tugraz.iaik.pre.afgh.key.PREAFGHPublicKey;
import eu.credential.dms.common.utils.Crypto;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.xds_templates.CreateDocumentBuilder;
import eu.credential.app.patient.helper.xds_templates.DeprecateDocumentBuilder;
import eu.credential.app.patient.helper.xds_templates.InitPhrBuilder;
import eu.credential.app.patient.helper.xds_templates.RegistryStoredQueryBuilder;
import eu.credential.app.patient.helper.xds_templates.RetrieveDocumentBuilder;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.SetURLConnection;

import eu.credential.phrcrypto.PHRCryptoUtils;
import eu.credential.phrcrypto.PHRKeyUtils;

/**
 * Created by Aleksei Piatkin on 19.01.17.
 * <p>
 * This class helps to save and get data from PHR account.
 * // dataIdDMS4 - Glucose document id
 * // dataIdDMS5 - Weight document id
 */
public class PHRdata extends AsyncTask<Object, Object, JSONArray> {

    private static final String TAG = PHRdata.class.getSimpleName();

    private String accountId;
    private String operationId;
    private String documentType;
    private JSONArray content;
    private String documentFormatCode = "";
    private String phrDocumentPartId = "";
    private KeyPair keyPair;
    private boolean flag = true;

    private String ssoToken = SavePreferences.getDefaultsString("ssoToken", PatientApp.getContext());
    private String phrToken = SavePreferences.getDefaultsString("phrToken", PatientApp.getContext());

    public PHRdata(String accountId, String operationId) {
        super();
        this.accountId = accountId;
        this.operationId = operationId;
    }

    public PHRdata(String documentType, String operationId, JSONArray content) {
        super();
        this.accountId = SavePreferences.getDefaultsString("accountId", PatientApp.getContext());
        this.documentType = documentType;
        this.operationId = operationId;
        this.content = content;
    }

    @Override
    protected JSONArray doInBackground(Object... params) {
        JSONArray parameter = new JSONArray();
        JSONArray error = new JSONArray();

        phrDocumentPartId = UUID.randomUUID().toString();

        new SetURLConnection.SetURLConnectionBuilder()
                .protocol("phr2.protocol")
                .host("phr2.host")
                .port("phr2.port")
                .path("phr2.path")
                .phrDocumentPartId(phrDocumentPartId)
                .ssoToken(ssoToken)
                .phrToken(phrToken).build();
        Log.i("Request_PHR", "ssoToken: " + ssoToken);

        // Set type of document
        if ((operationId != null && operationId.equals("glucoseId")) ||
                (documentType != null && documentType.equals("glucoseId"))) {
            documentFormatCode = "urn:fhg:fokus:fc:bz:2018";
        } else if ((operationId != null && operationId.equals("weightId")) ||
                (documentType != null && documentType.equals("weightId"))) {
            documentFormatCode = "urn:fhg:fokus:fc:ge:2018";
        } else if ((operationId != null && operationId.equals("hba1c")) ||
                (documentType != null && documentType.equals("hba1c"))) {
            documentFormatCode = "urn:fhg:fokus:fc:hba1c:2018";
        }

        try {
            error.put(new JSONObject("{\"error\": \"true\"}"));
            if (operationId != null && operationId.equals("initPhr")) {
                initPhr();
            }

            if (operationId != null && operationId.equals("createDocuments")) {
                createDocumentRequest();
            }

            if (operationId != null && (operationId.equals("retrieveDocuments") || operationId.equals("newMeasurement"))) {
                parameter = retrieveDocument();
            }

        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        if (!flag) {
            return error;
        } else return parameter;
    }

    private void initPhr() throws IOException, ParserConfigurationException {
        String requestMessage;
        String folderId = UUID.randomUUID().toString();
        SavePreferences.setDefaultsString("registryObject", folderId, PatientApp.getContext());

        InitPhrBuilder ipb = new InitPhrBuilder();

        requestMessage = ipb.build(accountId, phrDocumentPartId,
                "LAB", "1.3.6.1.4.1.19376.3.276.1.5.8",
                "Laborergebnisse",
                "1.2.276.0.76.3.1.158.9999.1",
                "Palliativtherapie", folderId
        );

        long startTime = System.nanoTime();

        HttpURLConnection httpURLConnection = SetURLConnection.setConnection("POST",
                "initPhr", requestMessage);

        long endTime = System.nanoTime();
        Log.i(TAG, "Registration|Create PHR folder|" + SetURLConnection.setURL()
                + "/initPhr|-|" + startTime / 1000000 + "|" +
                (endTime - startTime) / 1000000 + "|" + httpURLConnection.getResponseCode());

        if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Builder to parse xml data
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            // Generate phr keys
            keyPair = PHRKeyUtils.createPHRKey();
            String publicPHRKey = PHRKeyUtils.getPublicKeyAsBase64(keyPair.getPublic());
            String privatePHRKey = PHRKeyUtils.getPrivateKeyAsBase64(keyPair.getPrivate());

            // save phr keys to file
            PHRKeyUtils.saveKeyPair(keyPair, PatientApp.getContext().getFilesDir() + "");

            try {
                Document doc = builder.parse(new java.io.ByteArrayInputStream(xmlToString(httpURLConnection).getBytes()));
                XPath xpath = getXPath();

                phrToken = xpath.evaluate("//soap:Envelope/soap:Header/" +
                        "Patient/Token/text()", doc);

                SavePreferences.setDefaultsString("phrToken", phrToken, PatientApp.getContext());
                Log.i("Request_PHR", "phrToken: " + phrToken);

            } catch (SAXException | IOException | XPathExpressionException e) {
                flag = false;
                e.printStackTrace();
            }
            String[] fileContent = {"folderId: " + folderId, "phrToken: " + phrToken, "publicPHRKey: " +
                    publicPHRKey, "privatePHRKey: " + privatePHRKey, "accountId: " + accountId};
            KeyPair kpOwner = createKeyPairFromB64(SavePreferences.getDefaultsString("publicKey", PatientApp.getContext()),
                    SavePreferences.getDefaultsString("privateKey", PatientApp.getContext()));

            String schemeName = Crypto.Scheme.PRE.AFGH.name;
            String[] fileContentEnc = encryptFileContent(kpOwner, fileContent, schemeName);

            //save PHR ids and keys to DMS
            new UpdateParticipantData.UpdateParticipantBuilder()
                    .fileContent(fileContentEnc)
                    .dataId(SavePreferences.getDefaultsString("dataIdDMS2", PatientApp.getContext()))
                    .operationId("phrKey").build().execute();
        } else {
            flag = false;
            Log.d("myLogs", "no connection");
        }
    }

    private JSONArray retrieveDocument() throws IOException, ParserConfigurationException, JSONException {
        String requestMessage;
        JSONArray parameter = new JSONArray();
        String documentUniqueId = "";
        String oldDocumentEntryUuid = "";

        long startTime = System.nanoTime();

        RegistryStoredQueryBuilder rsqb = new RegistryStoredQueryBuilder();
        requestMessage = rsqb.build(accountId, documentFormatCode);

        HttpURLConnection httpURLConnection3 = SetURLConnection.setConnection("POST",
                "registryStoredQuery", requestMessage);

        if (httpURLConnection3.getResponseCode() == HttpURLConnection.HTTP_OK) {

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            try {
                Document doc = builder.parse(new java.io.ByteArrayInputStream(xmlToString(httpURLConnection3).getBytes()));
                XPath xpath = getXPath();

                documentUniqueId = xpath.evaluate("//soap:Envelope/soap:Body/" +
                        "query:AdhocQueryResponse/rim:RegistryObjectList/rim:ExtrinsicObject/" +
                        "rim:ExternalIdentifier[@identificationScheme=" +
                        "'urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab']/@value", doc);

                oldDocumentEntryUuid = xpath.evaluate("//soap:Envelope/soap:Body/" +
                        "query:AdhocQueryResponse/rim:RegistryObjectList/rim:ExtrinsicObject/" +
                        "rim:ExternalIdentifier[@identificationScheme=" +
                        "'urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab']/@registryObject", doc);
                Log.i("Request_PHR", "documentUniqueId: " + documentUniqueId);
                Log.i("Request_PHR", "registryObject: " + oldDocumentEntryUuid);

            } catch (SAXException | IOException | XPathExpressionException e) {
                flag = false;
                e.printStackTrace();
            }
            RetrieveDocumentBuilder rdb = new RetrieveDocumentBuilder();

            if (!documentUniqueId.equals("")) {
                requestMessage = rdb.build(phrDocumentPartId, "1.2.3.4.56789", documentUniqueId);

                HttpURLConnection httpURLConnection2 = SetURLConnection.setConnection("POST",
                        "retrieveDocuments", requestMessage);

                long endTime = System.nanoTime();
                Log.i(TAG, "Measure|Retrieve PHR document|" + SetURLConnection.setURL()
                        + "/retrieveDocuments|-|" + startTime / 1000000 + "|" +
                        (endTime - startTime) / 1000000 + "|" + httpURLConnection2.getResponseCode());
                if (httpURLConnection2.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // get and save the document content
                    byte[] data = Base64.decode(xmlDocumentContentToString(httpURLConnection2), Base64.DEFAULT);
                    parameter = new JSONArray(getDecryptedContent(data));
                } else {
                    parameter = new JSONArray();
                }
            }
            // deprecate document
            if (operationId.equals("newMeasurement") && !oldDocumentEntryUuid.equals("")) {
                DeprecateDocumentBuilder ddb = new DeprecateDocumentBuilder();
                requestMessage = ddb.build(accountId, oldDocumentEntryUuid);

                HttpURLConnection httpURLConnection = SetURLConnection
                        .setConnection("POST", "deprecateDocuments", requestMessage);
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i("Request_PHR", "Document was successful deprecated");
                    Log.d("Request_PHR", "retrieved document: " + parameter.toString());
                } else {
                    flag = false;
                }
            }
        }
        return parameter;
    }

    private void createDocumentRequest() throws IOException {
        String registryObject = SavePreferences.getDefaultsString("registryObject", PatientApp.getContext());

        CreateDocumentBuilder cdb = new CreateDocumentBuilder();

        String requestMessage = cdb.build(accountId, phrDocumentPartId,
                "LAB", "1.3.6.1.4.1.19376.3.276.1.5.8",
                "Laborergebnisse", "BEFU",
                "1.3.6.1.4.1.19376.3.276.1.5.9",
                "Ergebnisse Diagnostik", documentFormatCode,
                "1.2.276.0.76.3.1.158.9999.1", documentType,
                "PRA", "1.3.6.1.4.1.19376.3.276.1.5.2",
                "Arztpraxis", "LABO",
                "1.3.6.1.4.1.19376.3.276.1.5.4",
                "Laboratoriumsmedizin", getEncryptedContent()
        );

        long startTime = System.nanoTime();

        HttpURLConnection httpURLConnection1 = SetURLConnection.setConnection("POST",
                "createDocuments", requestMessage);

        long endTime = System.nanoTime();
        Log.i(TAG, "Measure|Create PHR document|" + SetURLConnection.setURL()
                + "/createDocuments|-|" + startTime / 1000000 + "|" +
                (endTime - startTime) / 1000000 + "|" + httpURLConnection1.getResponseCode());

        Log.i("Request_PHR", "message: " + httpURLConnection1.getResponseCode() +
                " " + registryObject);

        Log.i("Request_PHR", "CreateDocResponse: " + SetURLConnection.jsonToString(httpURLConnection1));
    }

    // Convert XML object to String format
    private String xmlToString(HttpURLConnection httpURLConnection) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String inline;

        while ((inline = inputReader.readLine()) != null) {
            if (inline.trim().startsWith("<")) {
                sb.append(inline);
            }
        }
        return sb.toString();
    }

    private String xmlDocumentContentToString(HttpURLConnection httpURLConnection) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String inline;
        while ((inline = inputReader.readLine()) != null) {
            if (!inline.trim().startsWith("<") && !inline.trim().startsWith("Content") && !inline.trim().startsWith("--")) {
                sb.append(inline);
            }
        }
        return sb.toString();
    }

    private XPath getXPath() {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "soap":
                        return "http://www.w3.org/2003/05/soap-envelope";
                    case "addr":
                        return "http://www.w3.org/2005/08/addressing";
                    case "query":
                        return "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0";
                    case "rim":
                        return "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";
                }
                return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
        });
        return xpath;
    }

    private String getEncryptedContent() {
        String contentBase64 = "";
        try {
            keyPair = PHRKeyUtils.loadKeyPair(PatientApp.getContext().getFilesDir().getPath());
            contentBase64 = PHRCryptoUtils.encrypt(Base64.encodeToString(content.toString().getBytes(),
                    Base64.DEFAULT), keyPair.getPublic());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return contentBase64;
    }

    private String getDecryptedContent(byte[] data) {
        String contentBase64 = "";
        try {
            keyPair = PHRKeyUtils.loadKeyPair(PatientApp.getContext().getFilesDir().getPath());
            contentBase64 = new String(Base64.decode(PHRCryptoUtils.decrypt(new String(data, StandardCharsets.UTF_8),
                    keyPair.getPrivate()), Base64.DEFAULT), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return contentBase64;
    }

    private static String[] encryptFileContent(final KeyPair kp, final String[] fileContent, final String schemeName) {
        ArrayList<String> fileContentEnc = new ArrayList<>();

        for (String content : fileContent) {
            byte[] contentEnc = Crypto.encrypt(kp, content, schemeName);
            if (contentEnc == null) {
                return null;
            }

            String contentEncB64 = Base64.encodeToString(contentEnc, Base64.NO_WRAP);

            fileContentEnc.add(contentEncB64);
        }

        String[] fileContentEncArr = new String[fileContentEnc.size()];
        fileContentEncArr = fileContentEnc.toArray(fileContentEncArr);

        return fileContentEncArr;
    }
    private static KeyPair createKeyPairFromB64(String pubKeyStr, String privKeyStr) {
        PublicKey pubKey;
        PrivateKey privKey;
        KeyPair kp = null;
        try {
            privKey = new PREAFGHPrivateKey(Base64.decode(privKeyStr.getBytes(), Base64.DEFAULT));
            pubKey = new PREAFGHPublicKey(Base64.decode(pubKeyStr.getBytes(), Base64.DEFAULT));
            kp = new KeyPair(pubKey, privKey);
        } catch (InvalidKeyException e) {
            System.out.println("Exception during conversion to PublicKey and PrivateKey from String!");
            e.printStackTrace();
        }
        return kp;
    }
}