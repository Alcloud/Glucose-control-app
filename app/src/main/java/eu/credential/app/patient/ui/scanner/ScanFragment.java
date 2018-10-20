package eu.credential.app.patient.ui.scanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import eu.credential.app.patient.integration.scanner.IntentIntegrator;
import eu.credential.app.patient.integration.scanner.IntentResult;
import eu.credential.app.patient.orchestration.http.PHRdata;

/**
 * Created by Aleksei Piatkin on 24.04.17.
 * <p>
 * A scanner screen.
 */
public class ScanFragment extends Fragment {
    private TextView formatTxt, contentTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scan, container, false);

        ImageButton scanBtn = v.findViewById(R.id.scan_button);
        ImageButton scanBtn1 = v.findViewById(R.id.scan_button1);
        formatTxt = v.findViewById(R.id.scan_format);
        contentTxt = v.findViewById(R.id.scan_content);

        scanBtn.setOnClickListener(v1 -> {
            /*if (v1.getId() == R.id.scan_button) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
                scanIntegrator.initiateScan();
            }*/
            String glucoseValue = String.valueOf(ThreadLocalRandom.current().nextInt(97, 119));
            saveMeasurementToPHR(setGlucoseInPHRDocument("11", glucoseValue, "gk",
                    "1", "1", "1", "111",
                    String.valueOf(Calendar.getInstance().getTime())), "glucoseId");
            Toast.makeText(getContext(), "Last glucose value: " + glucoseValue, Toast.LENGTH_SHORT).show();
        });
        scanBtn1.setOnClickListener(v11 -> {
            /*if (v1.getId() == R.id.scan_button) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
                scanIntegrator.initiateScan();
            }*/
            String weightValue = String.valueOf(ThreadLocalRandom.current().nextInt(67, 80));
            saveMeasurementToPHR(setWeightInPHRDocument(weightValue, "1", "1",
                    "1", "1", "1",
                    String.valueOf(Calendar.getInstance().getTime())), "weightId");
            Toast.makeText(getContext(), "Last weight value: " + weightValue + " kg", Toast.LENGTH_SHORT).show();
        });
        return v;
    }

    @SuppressLint("SetTextI18n")
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);
        } else {
            Toast.makeText(getActivity().getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method add the new measurement to array in PHR document.
     * When no document exist, firstly create a new one.
     *
     * @param measurementJSONObject
     */
    private void saveMeasurementToPHR(String measurementJSONObject, String type) {
        JSONObject jsonObject;
        try {

            // check if the PHR document exist and retrieve the document with glucose or weight data
            JSONArray documentContentArray = new PHRdata(type, "newMeasurement",
                    null).execute().get();

            if (documentContentArray != null && !documentContentArray.toString().equals("[]")) {
                // add new value to document content
                jsonObject = new JSONObject(measurementJSONObject);
                documentContentArray.put(jsonObject);

                // create a new PHR document
                new PHRdata(type, "createDocuments", documentContentArray).execute();

                // first create a document in PHR
            } else {
                documentContentArray = new JSONArray();
                jsonObject = new JSONObject(measurementJSONObject);
                documentContentArray.put(jsonObject);
                new PHRdata(type, "createDocuments", documentContentArray).execute();
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method create a new JSON element with weight measurement values.
     */
    private String setWeightInPHRDocument(String weight, String height, String bmi, String weightUnit,
                                          String heightUnit, String deviceTime, String receiveTime) {
        return "{\n" +
                "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
                "  \"title\": \"Weight Measurement\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"weight\": {\n" +
                "      \"description\": \"measured mass in weight units\",\n" +
                "      \"type\": \"number\",\n" +
                "      \"value\": \"" + weight + "\",\n" +
                "      \"minimum\": 0\n" +
                "    },\n" +
                "    \"weightUnit\": {\n" +
                "      \"description\": \"mass unit\",\n" +
                "      \"value\": \"" + weightUnit + "\",\n" +
                "      \"enum\": [\n" +
                "        \"kg\",\n" +
                "        \"lb\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"height\": {\n" +
                "      \"description\": \"measured size in height units\",\n" +
                "      \"type\": \"number\",\n" +
                "      \"value\": \"" + height + "\",\n" +
                "      \"minimum\": 0\n" +
                "    },\n" +
                "    \"heightUnit\": {\n" +
                "      \"description\": \"size unit\",\n" +
                "      \"value\": \"" + heightUnit + "\",\n" +
                "      \"enum\": [\n" +
                "        \"m\",\n" +
                "        \"in\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"bmi\": {\n" +
                "      \"description\": \"calculated body mass index\",\n" +
                "      \"type\": \"number\",\n" +
                "      \"value\": \"" + bmi + "\",\n" +
                "      \"minimum\": 0\n" +
                "    },\n" +
                "    \"deviceTime\": {\n" +
                "      \"description\": \"ISO time string given by measurement device\",\n" +
                "      \"value\": \"" + deviceTime + "\",\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    \"receiveTime\": {\n" +
                "      \"description\": \"ISO time string, stating when the app received measurement from device\",\n" +
                "      \"value\": \"" + receiveTime + "\",\n" +
                "      \"type\": \"string\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
    }

    /**
     * This method create a new JSON element with glucose measurement values.
     */
    private String setGlucoseInPHRDocument(String sequenceNumber, String concentration, String unit,
                                           String fluidType, String sampleLocation, String sensorStatus,
                                           String deviceTime, String receiveTime) {
        return "{\n" +
                "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
                "  \"title\": \"Glucose Measurement\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"sequenceNumber\": {\n" +
                "      \"description\": \"internal measurement id given by the device\",\n" +
                "      \"value\": \"" + sequenceNumber + "\",\n" +
                "      \"type\": \"integer\",\n" +
                "      \"minimum\": 0\n" +
                "    },\n" +
                "    \"deviceTime\": {\n" +
                "      \"description\": \"ISO time string given by measurement device\",\n" +
                "      \"value\": \"" + deviceTime + "\",\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    \"receiveTime\": {\n" +
                "      \"description\": \"ISO time string, stating when the app received measurement from device\",\n" +
                "      \"value\": \"" + receiveTime + "\",\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    \"concentration\": {\n" +
                "      \"description\": \"how much glucose the device has measured in the given unit\",\n" +
                "      \"value\": \"" + concentration + "\",\n" +
                "      \"type\": \"number\",\n" +
                "      \"minimum\": 0\n" +
                "    },\n" +
                "    \"unit\": {\n" +
                "      \"description\": \"physical unit describing the glucose concentration\",\n" +
                "      \"value\": \"" + unit + "\",\n" +
                "      \"enum\": [\n" +
                "        \"kg/L\",\n" +
                "        \"mol/L\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"fluidType\": {\n" +
                "      \"description\": \"fluid type delivered to device\",\n" +
                "      \"value\": \"" + fluidType + "\",\n" +
                "      \"enum\": [\n" +
                "        \"Capillary Whole blood\",\n" +
                "        \"Capillary Plasma\",\n" +
                "        \"Venous Whole blood\",\n" +
                "        \"Venous Plasma\",\n" +
                "        \"Arterial Whole blood\",\n" +
                "        \"Arterial Plasma\",\n" +
                "        \"Undetermined Whole blood\",\n" +
                "        \"Undetermined Plasma\",\n" +
                "        \"Interstitial Fluid (ISF)\",\n" +
                "        \"Control Solution\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"sampleLocation\": {\n" +
                "      \"description\": \"body location the fluid was taken from\",\n" +
                "      \"value\": \"" + sampleLocation + "\",\n" +
                "      \"enum\": [\n" +
                "        \"Finger\",\n" +
                "        \"Alternate Site Test (AST)\",\n" +
                "        \"Earlobe\",\n" +
                "        \"Control solution\",\n" +
                "        \"Sample Location value not available\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"sensorStatus\": {\n" +
                "      \"description\": \"technical annunciations by the measurement device\",\n" +
                "      \"value\": \"" + sensorStatus + "\",\n" +
                "      \"type\": \"array\",\n" +
                "      \"uniqueItems\": true,\n" +
                "      \"minItems\": 0,\n" +
                "      \"items\": {\n" +
                "        \"enum\": [\n" +
                "          \"Device battery low at time of measurement\",\n" +
                "          \"Sensor malfunction or faulting at time of measurement\",\n" +
                "          \"Sample size for blood or control solution insufficient at time of measurement\",\n" +
                "          \"Strip insertion error\",\n" +
                "          \"Strip type incorrect for device\",\n" +
                "          \"Sensor result higher than the device can process\",\n" +
                "          \"Sensor result lower than the device can process\",\n" +
                "          \"Sensor temperature too high for valid test/result at time of measurement\",\n" +
                "          \"Sensor temperature too low for valid test/result at time of measurement\",\n" +
                "          \"Sensor read interrupted because strip was pulled too soon at time of measurement\",\n" +
                "          \"General device fault has occurred in the sensor\",\n" +
                "          \"Time fault has occurred in the sensor and time may be inaccurate\"\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
    }
}
