package eu.credential.app.patient.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.VideoView;

import com.example.administrator.credential_v020.R;

import eu.credential.app.patient.PatientApp;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        try {
            VideoView videoHolder = findViewById(R.id.splash_video);

            Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.credential);
            videoHolder.setVideoURI(video);

            videoHolder.setOnCompletionListener(mp -> jump());
            videoHolder.start();
        } catch (Exception ex) {
            jump();
        }
    }

    private void jump() {
        if (isFinishing())
            return;
        startActivity(new Intent(this, PatientApp.class));
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        jump();
        return true;
    }
}
