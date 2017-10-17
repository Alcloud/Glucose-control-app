package eu.credential.app.patient.ui.myHealthRecords.event;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.administrator.credential_v020.R;

public class EventDescriptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_description);
        //Toolbar settings
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_event_description);
        toolbar.setTitle(R.string.diary_toolbar_event);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
