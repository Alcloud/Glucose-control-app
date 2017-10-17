package eu.credential.app.patient.ui.myHealthRecords.protocol;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ProtocolActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String[] date = new String[] {"12.02.2017","31.01.2017"};
        final String[] eventName = new String[] {"Sam Smith had add a new data","New data from..."};

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, date) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(date[position]);
                text2.setText(eventName[position]);
                return view;
            }
        };
        setListAdapter(adapter);
    }
}
