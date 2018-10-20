package eu.credential.app.patient.helper;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.orchestration.services.RefreshSchedule;

public class LogoutService {

    private static RefreshSchedule refreshSchedule = new RefreshSchedule();
    /**
     * To clear user preferences after logout
     */
    public static void clearAllPreferences() {
        // Reset all user preferences after logout
        SavePreferences.setDefaultsBoolean("login", false, PatientApp.getContext());
        SavePreferences.setDefaultsString("ssoToken", "", PatientApp.getContext());
        // ids for DMS document
        SavePreferences.setDefaultsString("dataIdDMS1", "", PatientApp.getContext());
        SavePreferences.setDefaultsString("dataIdDMS2", "", PatientApp.getContext());
        SavePreferences.setDefaultsString("dataIdDMS3", "", PatientApp.getContext());
        SavePreferences.setDefaultsString("parentId", "", PatientApp.getContext());
        // user private data
        SavePreferences.setDefaultsString("userName", "", PatientApp.getContext());
        SavePreferences.setDefaultsString("userSurname", "", PatientApp.getContext());
        SavePreferences.setDefaultsString("accountId", "", PatientApp.getContext());
        // event subscribing data
        SavePreferences.setDefaultsBoolean("checkNewData", false, PatientApp.getContext());
        SavePreferences.setDefaultsString("prefNewData", "", PatientApp.getContext());
        SavePreferences.setDefaultsBoolean("checkDocumentAccess", false, PatientApp.getContext());
        SavePreferences.setDefaultsString("prefDocumentAccess", "", PatientApp.getContext());
        // Policy data
        SavePreferences.setDefaultsBoolean("readdocument", false, PatientApp.getContext());
        SavePreferences.setDefaultsBoolean("editdocument", false, PatientApp.getContext());
        // source ids for PHR document
        SavePreferences.setDefaultsString("registryObject", "", PatientApp.getContext());
        SavePreferences.setDefaultsString("phrToken", "", PatientApp.getContext());
        // stop ssoToken refreshing
        refreshSchedule.stopRepeatingTask();
    }
}