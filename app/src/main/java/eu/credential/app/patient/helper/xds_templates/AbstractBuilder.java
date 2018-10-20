package eu.credential.app.patient.helper.xds_templates;

import android.content.Context;
import android.content.res.AssetManager;

import com.samskivert.mustache.Mustache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import eu.credential.app.patient.PatientApp;

public abstract class AbstractBuilder {

    private Context context = PatientApp.getContext();

    protected String build(Map<String, Object> values, String templateName) {

        Writer writer = new StringWriter();

        Mustache.compiler().compile(getTemplate(templateName)).execute(values, writer);

        String toReturn = writer.toString();

        // remove comments
        toReturn = toReturn.replaceAll("<!--.*?-->", "");

        return toReturn;
    }

    private String getTemplate(String templateName){
        StringBuilder buf = new StringBuilder();
        AssetManager am = context.getAssets();
        try {
            InputStream inputStream = am.open(templateName);

            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }
}
