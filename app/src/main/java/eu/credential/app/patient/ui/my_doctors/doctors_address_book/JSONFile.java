package eu.credential.app.patient.ui.my_doctors.doctors_address_book;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class JSONFile {
    private static final String TAG = "File1";
    static JSONParser parser = new JSONParser();

    @SuppressWarnings("unchecked")
    public static void saveJSONDoctor(Context context, String name, String role, String city, String id) throws IOException {

        JSONObject obj = new JSONObject();
        obj.put("Name", name);
        obj.put("Role", role);
        obj.put("City", city);
        obj.put("Id", id);
        obj.put("MainRole", "doctor");

        try (FileWriter file = new FileWriter(context.getFilesDir() + "/" + 1 + name + ".json")) {
            file.write(obj.toJSONString());
            Log.d(TAG, "Object: " + obj);
        }
    }

    // only for performance, should be deleted
    public static void saveJSONAccess(Context context, String name) throws IOException {

        JSONObject obj = new JSONObject();
        obj.put("Date", name);

        try (FileWriter file = new FileWriter(context.getFilesDir() + "/" + 2 + ".json")) {
            file.write(obj.toJSONString());
            Log.d(TAG, "Object: " + obj);
        }
    }

    public static ArrayList<ArrayList<String>> readJSONDoctor(Context context) throws IOException {
        String doctorName;
        String doctorRole;
        String doctorCity;
        String doctorId;
        String doctorMainRole;

        ArrayList<ArrayList<String>> doctor1 = new ArrayList<>();

        File folder = new File(context.getFilesDir().getPath());
        File[] listOfFiles = folder.listFiles();
        File file;

        for (int i = 0; i < listOfFiles.length; i++) {
            file = listOfFiles[i];
            ArrayList<String> doctor = new ArrayList<>();
            if (file.isFile() && file.getName().startsWith("1")) {

                try {
                    Object obj = parser.parse(new FileReader(file));
                    JSONObject jsonObject = (JSONObject) obj;

                    doctorName = (String) jsonObject.get("Name");
                    doctorRole = (String) jsonObject.get("Role");
                    doctorCity = (String) jsonObject.get("City");
                    doctorId = (String) jsonObject.get("Id");
                    doctorMainRole = (String) jsonObject.get("MainRole");

                    doctor.add(doctorName);
                    doctor.add(doctorRole);
                    doctor.add(doctorCity);
                    doctor.add(doctorId);
                    doctor.add(doctorMainRole);

                    doctor1.add(doctor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(TAG, "Object: " + doctor1.toString());
        return doctor1;
    }

    // only for performance, should be deleted
    public static ArrayList<String> readJSONAccess(Context context) throws IOException {

        ArrayList<String> access = new ArrayList<>();
        File folder = new File(context.getFilesDir().getPath());
        File[] listOfFiles = folder.listFiles();
        File file;
        for (int i = 0; i < listOfFiles.length; i++) {
            file = listOfFiles[i];
            if (file.isFile() && file.getName().startsWith("2")) {
                try {
                    Object obj = parser.parse(new FileReader(file));
                    JSONObject jsonObject = (JSONObject) obj;

                    String a = (String) jsonObject.get("Date");

                    access.add(a);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(TAG, "Access: " + access.toString());
        return access;
    }

    // save preference Id
    public static void saveJSONpreference(Context context, String id, String name) throws IOException {

        JSONObject obj = new JSONObject();
        obj.put(name, id);

        try (FileWriter file = new FileWriter(context.getFilesDir().getPath() + "/" + name + ".json")) {
            file.write(obj.toJSONString());
            Log.d(TAG, "Preference ID saved: " + obj);
        }
    }

    // load preference
    public static String readJSONpreference(Context context, String name) throws IOException {

        String preference = "";
        File folder = new File(context.getFilesDir().getPath());
        File[] listOfFiles = folder.listFiles();
        File file;
        if(listOfFiles!=null){
            for (int i = 0; i < listOfFiles.length; i++) {
                file = listOfFiles[i];
                if (file.isFile() && file.getName().startsWith(name)) {
                    try {
                        Object obj = parser.parse(new FileReader(file));
                        JSONObject jsonObject = (JSONObject) obj;

                        preference = (String) jsonObject.get(name);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Log.d(TAG, "preference ID loaded: " + name + " " + preference);
        return preference;
    }
}