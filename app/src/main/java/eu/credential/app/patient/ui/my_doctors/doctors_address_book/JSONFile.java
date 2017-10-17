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

/**
 * Created by Aleksei Piatkin on 21.07.17.
 * <p>
 * This class saves and reads JSON files to store some user data local.
 */

public class JSONFile {
    private static final String TAG = "File1";
    static JSONParser parser = new JSONParser();

    @SuppressWarnings("unchecked")
    // only for performance, should be deleted
    public static void saveJSONAccess(Context context, String name) throws IOException {

        JSONObject obj = new JSONObject();
        obj.put("Date", name);

        try (FileWriter file = new FileWriter(context.getFilesDir() + "/" + 2 + ".json")) {
            file.write(obj.toJSONString());
            Log.d(TAG, "Object: " + obj);
        }
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

    // load preference Id
    public static String readJSONpreference(Context context, String name) throws IOException {

        String preference = "";
        File folder = new File(context.getFilesDir().getPath());
        File[] listOfFiles = folder.listFiles();
        File file;
        if (listOfFiles != null) {
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