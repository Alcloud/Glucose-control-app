package eu.credential.app.patient;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import eu.credential.app.patient.orchestration.collection.CollectorService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class setJSONStringTest {

    private CollectorService collectorService = new CollectorService();
    private Class c = collectorService.getClass();

    @Test
    public void shouldSetACorrectURLPath() {

        try {
            Method method = c.getDeclaredMethod("setWeightInPHRDocument", String.class,
                    String.class, String.class, String.class, String.class, String.class, String.class);
            if (method != null) {
                method.setAccessible(true);
                assertThat(method.invoke(collectorService, "75", "180", "20", "kg", "cm",
                        "253453664", "4564365456"), is("{\n  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n  \"title\": \"Weight Measurement\",\n  \"type\": \"object\",\n  \"properties\": {\n    \"weight\": {\n      \"description\": \"measured mass in weight units\",\n      \"type\": \"number\",\n      \"value\": \"75\",\n      \"minimum\": 0\n    },\n    \"weightUnit\": {\n      \"description\": \"mass unit\",\n      \"value\": \"kg\",\n      \"enum\": [\n        \"kg\",\n        \"lb\"\n      ]\n    },\n    \"height\": {\n      \"description\": \"measured size in height units\",\n      \"type\": \"number\",\n      \"value\": \"180\",\n      \"minimum\": 0\n    },\n    \"heightUnit\": {\n      \"description\": \"size unit\",\n      \"value\": \"cm\",\n      \"enum\": [\n        \"m\",\n        \"in\"\n      ]\n    },\n    \"bmi\": {\n      \"description\": \"calculated body mass index\",\n      \"type\": \"number\",\n      \"value\": \"20\",\n      \"minimum\": 0\n    },\n    \"deviceTime\": {\n      \"description\": \"ISO time string given by measurement device\",\n      \"value\": \"253453664\",\n      \"type\": \"string\"\n    },\n    \"receiveTime\": {\n      \"description\": \"ISO time string, stating when the app received measurement from device\",\n      \"value\": \"4564365456\",\n      \"type\": \"string\"\n    }\n  }\n}\n"));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
