package eu.credential.app.patient.orchestration.services;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import eu.credential.app.patient.helper.LogoutService;
import eu.credential.app.patient.orchestration.http.SsoTokenRefresh;
import eu.credential.app.patient.ui.LoginActivity;

public class RefreshSchedule {

    private final static int INTERVAL = 1000 * 60 * 5; //5 minutes
    private static Handler handler = new Handler();
    private static Activity activity;

    public RefreshSchedule(Activity activity){
        super();
        RefreshSchedule.activity = activity;
    }
    public RefreshSchedule(){
        super();
    }
    private static CountDownTimer timer = new CountDownTimer(10 * 60 * 1000, 1000) {
        public void onTick(long millisUntilFinished) {
            Log.v("Main_Timer", "Service Started");
        }

        public void onFinish() {
            Log.v("Main_Timer", "Call Logout by Service");
            Toast.makeText(
                    activity, "Your session is over. Please, log in again.",
                    Toast.LENGTH_LONG).show();
            LogoutService.clearAllPreferences();
            // back to login screen
            Intent i = new Intent(activity, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(i);
        }
    };

    private static Runnable handlerTask = new Runnable() {
        @Override
        public void run() {
            SsoTokenRefresh ssoTokenRefresh = new SsoTokenRefresh();
            ssoTokenRefresh.execute();
            handler.postDelayed(handlerTask, INTERVAL);
        }
    };

    /**
     * Timer. Auto log out, when timer is finished.
     */
    public CountDownTimer timer() {
        return timer;
    }

    public void startRepeatingTask() {
        handlerTask.run();
    }

    public void stopRepeatingTask() {
        handler.removeCallbacks(handlerTask);
    }
}