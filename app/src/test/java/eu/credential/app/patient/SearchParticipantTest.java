package eu.credential.app.patient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import eu.credential.app.patient.ui.searchParticipant.SearchParticipant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchParticipantTest {

    private SearchParticipant searchParticipant = new SearchParticipant();
    private Class c = searchParticipant.getClass();

    /**
     * Looking for specific doctor and using for that a filter.
     * Filter params: participantPLZ and participantName
     */
    @Test
    public void doctorShouldBeFoundAfterFiltering() {
        try {
            JSONObject doctor1 = new JSONObject();
            JSONObject doctor2 = new JSONObject();
            doctor1.put("uid", "UUID:6eae013f-6d8b-409d-bef8-f38f2cba1b2b");
            doctor1.put("name", "Zart");
            doctor1.put("postalCode", "12345");
            doctor2.put("uid", "UUID:00001111");
            doctor2.put("name", "Erna");
            doctor2.put("postalCode", "12459");
            doctor2.put("hpdProviderMailingAddress", "email");
            doctor2.put("hcRegisteredName", "name");
            doctor2.put("postalAddress", "address");
            doctor2.put("hcSpecialization", "family doctor");
            JSONArray testArray = new JSONArray();
            testArray.put(doctor1);
            testArray.put(doctor2);

            Method setDoctorListMethod = c.getDeclaredMethod("setDoctorList", Integer.TYPE);
            Method searchFilterMethod = c.getDeclaredMethod("searchFilter", Integer.TYPE);
            Field participantPLZ = c.getDeclaredField("participantPLZ");
            Field participantName = c.getDeclaredField("participantName");
            Field doctorArray = c.getDeclaredField("doctorArray");

            participantPLZ.setAccessible(true);
            participantName.setAccessible(true);
            doctorArray.setAccessible(true);
            participantPLZ.set(searchParticipant, "12459");
            participantName.set(searchParticipant, "Erna");
            doctorArray.set(searchParticipant, testArray);

            if (setDoctorListMethod != null && searchFilterMethod != null) {
                setDoctorListMethod.setAccessible(true);
                searchFilterMethod.setAccessible(true);
                searchFilterMethod.invoke(searchParticipant, 1);
                setDoctorListMethod.invoke(searchParticipant, 1);
                assertThat(SearchParticipant.listDoctor.get(0).getId(), is("00001111"));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | JSONException | NoSuchFieldException e) {
            e.printStackTrace();
        }

    }
}
