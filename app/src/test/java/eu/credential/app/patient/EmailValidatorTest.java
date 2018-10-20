package eu.credential.app.patient;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import eu.credential.app.patient.ui.registration.RegistrationFirstStepActivity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EmailValidatorTest {
    private RegistrationFirstStepActivity registrationFirstStepActivity = new RegistrationFirstStepActivity();
    private Class c = registrationFirstStepActivity.getClass();
    @Test
    public void correctEmailSimpleReturnsTrue() {
        try {
            Method method = c.getDeclaredMethod("isEmailValid", String.class);
            if (method != null) {
                method.setAccessible(true);
                assertThat(method.invoke(registrationFirstStepActivity, "name@email.com"), is(true));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
