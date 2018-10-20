package eu.credential.app.patient.helper;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import eu.credential.accessmanagement.common.ClientProperties;
import eu.credential.accessmanagement.common.Utils;
import eu.credential.app.patient.PatientApp;

public class SetURLConnection {
    private static String protocol;
    private static String host;
    private static String port = null;
    private static String path;
    private static String phrDocumentPartId;
    private static String ssoToken;
    private static String phrToken;

    private SetURLConnection(final SetURLConnectionBuilder setURLConnectionBuilder) {
        protocol = setURLConnectionBuilder.getProtocol();
        host = setURLConnectionBuilder.getHost();
        port = setURLConnectionBuilder.getPort();
        path = setURLConnectionBuilder.getPath();
        phrDocumentPartId = setURLConnectionBuilder.getPhrDocumentPartId();
        ssoToken = setURLConnectionBuilder.getSsoToken();
        phrToken = setURLConnectionBuilder.getPhrToken();
    }

    public static String setURL() {
        ClientProperties clientProperties = null;
        String protocolString;
        String hostString;
        String portString = "";
        String pathString;
        try {
            clientProperties = Utils.getPropertiesFromContext(PatientApp.getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        protocolString = clientProperties != null ? clientProperties.getProperty(protocol) : null;
        hostString = clientProperties != null ? clientProperties.getProperty(host) : null;
        if (port != null) {
            portString = clientProperties != null ? clientProperties.getProperty(port) : null;
        }
        pathString = clientProperties != null ? clientProperties.getProperty(path) : null;
        if (port == null) {
            Log.i("Request_PHR", "path: " + protocolString + "://" + hostString + "/" + pathString);
            return protocolString + "://" + hostString + "/" + pathString;
        } else {
            Log.i("Request_PHR", "path: " + protocolString + "://" + hostString + ":" + portString + "/" + pathString);
            return protocolString + "://" + hostString + ":" + portString + "/" + pathString;
        }

    }

    /**
     * Set URL properties
     */
    public static HttpURLConnection setConnection(String method, String path, String requestMessage)
            throws IOException {
        URL url = new URL(setURL() + "/" + path);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(!method.equals("GET"));
        httpURLConnection.setRequestMethod(method);

        if (path.equals("retrieveDocuments") || path.equals("initPhr") || path.equals("createDocuments")) {
            switch (path) {
                case "createDocuments":
                    httpURLConnection.setRequestProperty("Content-Type", "multipart/related; type=\"application/xop+xml\"; start=\"<rootpart@test.org>\"; start-info=\"application/soap+xml\"; action=\"urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b\"; boundary=\"----=_Part_" + phrDocumentPartId + "\"");
                    if (ssoToken != null && !ssoToken.equals("") && phrToken != null && !phrToken.equals("")) {
                        httpURLConnection.setRequestProperty("Authorization", "Bearer " + ssoToken);
                        httpURLConnection.setRequestProperty("X-PHRToken", phrToken);
                    }
                    break;
                case "initPhr":
                    httpURLConnection.setRequestProperty("Content-Type", "multipart/related; type=\"application/xop+xml\"; start=\"<rootpart@test.org>\"; start-info=\"application/soap+xml\"; action=\"urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b\"; boundary=\"----=_Part_" + phrDocumentPartId + "\"");
                    if (ssoToken != null && !ssoToken.equals("")) {
                        httpURLConnection.setRequestProperty("Authorization", "Bearer " + ssoToken);
                    }
                    break;
                default:
                    httpURLConnection.setRequestProperty("Content-Type", "multipart/related; type=\"application/xop+xml\"; start=\"<rootpart@soapui.org>\"; start-info=\"application/soap+xml\"; action=\"urn:ihe:iti:2007:RetrieveDocumentSet\"; boundary=\"----=_Part_" + phrDocumentPartId + "\"");
                    if (ssoToken != null && !ssoToken.equals("") && phrToken != null && !phrToken.equals("")) {
                        httpURLConnection.setRequestProperty("Authorization", "Bearer " + ssoToken);
                        httpURLConnection.setRequestProperty("X-PHRToken", phrToken);
                    }
                    break;
            }
        } else if (path.equals("registryStoredQuery") || path.equals("deprecateDocuments")) {
            httpURLConnection.setRequestProperty("Content-Type", "application/soap+xml");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + ssoToken);
            httpURLConnection.setRequestProperty("X-PHRToken", phrToken);
        } else if (path.equals("addPreferences") || path.equals("deletePreferences") ||
                path.equals("getPreferences") || path.equals("resetPreferences") ||
                path.equals("getNotification") || path.equals("getNotificationList") ||
                path.startsWith("event?userid=" + SavePreferences.getDefaultsString("accountId", PatientApp.getContext())) || path.equals("regperm")) {
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + ssoToken);
        } else {
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
        }
        httpURLConnection.connect();

        if (!method.equals("GET")) {
            BufferedOutputStream bos = new BufferedOutputStream(httpURLConnection.getOutputStream());
            if (requestMessage != null) {
                bos.write(requestMessage.getBytes());
                bos.flush();
            }
            bos.close();
        }
        return httpURLConnection;
    }

    /**
     * Convert JSON object to String format
     */
    public static String jsonToString(HttpURLConnection httpURLConnection) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder sb = new StringBuilder(1024);
        String tmp;
        while ((tmp = reader.readLine()) != null)
            sb.append(tmp).append("\n");
        reader.close();
        return sb.toString();
    }

    public static class SetURLConnectionBuilder {
        private String protocol;
        private String host;
        private String port;
        private String path;
        private String phrDocumentPartId;
        private String ssoToken;
        private String phrToken;

        public SetURLConnectionBuilder protocol(final String protocol) {
            this.protocol = protocol;
            return this;
        }

        public SetURLConnectionBuilder host(final String host) {
            this.host = host;
            return this;
        }

        public SetURLConnectionBuilder port(final String port) {
            this.port = port;
            return this;
        }

        public SetURLConnectionBuilder phrToken(final String phrToken) {
            this.phrToken = phrToken;
            return this;
        }

        public SetURLConnectionBuilder phrDocumentPartId(final String phrDocumentPartId) {
            this.phrDocumentPartId = phrDocumentPartId;
            return this;
        }

        public SetURLConnectionBuilder ssoToken(final String ssoToken) {
            this.ssoToken = ssoToken;
            return this;
        }

        public SetURLConnectionBuilder path(final String path) {
            this.path = path;
            return this;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getHost() {
            return host;
        }

        public String getPort() {
            return port;
        }

        public String getPath() {
            return path;
        }

        public String getPhrDocumentPartId() {
            return phrDocumentPartId;
        }

        public String getSsoToken() {
            return ssoToken;
        }

        public String getPhrToken() {
            return phrToken;
        }

        public SetURLConnection build() {
            return new SetURLConnection(this);
        }
    }
}
