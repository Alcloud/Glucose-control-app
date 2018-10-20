package eu.credential.app.patient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.*;

import eu.credential.app.patient.ui.searchParticipant.DoctorDetailsActivity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DoctorInAddressBookExistTest {

    private DoctorDetailsActivity doctorDetailsActivity = new DoctorDetailsActivity();
    private Class c = doctorDetailsActivity.getClass();

    @Test
    public void doctorExistInAddressBook() {
        try {
            JSONObject doctor1 = new JSONObject();
            JSONObject doctor2 = new JSONObject();
            doctor1.put("id", "6eae013f-6d8b-409d-bef8-f38f2cba1b2b");
            doctor1.put("name", "Zart");
            doctor2.put("id", "6eae013f-6d8b-409d-bef8-f38hfhfba1b2b");
            doctor2.put("name", "Erna");
            JSONArray doctorArray = new JSONArray();
            doctorArray.put(doctor1);
            doctorArray.put(doctor2);

            Method method = c.getDeclaredMethod("doctorInAddressBookExist", JSONArray.class, String.class);
            if (method != null) {
                method.setAccessible(true);
                assertThat(method.invoke(doctorDetailsActivity, doctorArray, "6eae013f-6d8b-409d-bef8-f38f2cba1b2b"), is(1));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | JSONException e) {
            e.printStackTrace();
        }
    }
}
