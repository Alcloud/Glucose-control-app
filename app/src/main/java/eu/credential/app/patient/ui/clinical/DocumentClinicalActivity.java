package eu.credential.app.patient.ui.clinical;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;

import com.example.administrator.credential_v020.R;

/**
 * Created by Aleksei Piatkin on 05.04.17.
 * <p>
 * A clinical document screen that can shows documents in different formats.
 */
public class DocumentClinicalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_clinical);
        Toolbar toolbar = findViewById(R.id.toolbar_document);
        WebView webView = findViewById(R.id.image_document);

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setPadding(0, 0, 0, 0);

        //TODO: Set a document name dynamicly (from list):
        toolbar.setTitle(R.string.document_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    public void onClickShare(View view) {
    }

    public void onClickDelete(View view) {
    }
}
