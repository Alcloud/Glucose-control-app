package eu.credential.app.patient.ui.myHealthRecords.protocol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.credential_v020.R;

import java.util.ArrayList;

import eu.credential.app.patient.helper.Protocol;
import eu.credential.app.patient.ui.myHealthRecords.event.EventDescriptionActivity;

public class ProtocolList extends ListFragment {

    private ArrayList<Protocol> listProtocolAdapter = new ArrayList<>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getResources().getString(R.string.error_list));
        MyProtocolAdapter myListAdapter = new MyProtocolAdapter(getActivity(),
                R.layout.fragment_protocol_list, ProtocolListMain.listProtocol);
        setListAdapter(myListAdapter);
        myListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
//        Intent documentIntent = new Intent(getActivity(), EventDescriptionActivity.class);
//        startActivity(documentIntent);
    }

    private class MyProtocolAdapter extends ArrayAdapter<Protocol> {
        private Context myContext;

        private TextView protocolId;
        private TextView protocolType;
        private TextView protocolRequestingUser;
        private TextView protocolTime;
        private ImageView protocolIndicator;

        MyProtocolAdapter(Context context, int textViewResourceId, ArrayList<Protocol> listProtocol) {
            super(context, textViewResourceId, listProtocol);
            myContext = context;
            listProtocolAdapter = listProtocol;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) myContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = null;
            if (inflater != null) {
                v = inflater.inflate(R.layout.fragment_protocol_list, parent, false);
            }
            // Get text
            if (v != null) {
                protocolId = v.findViewById(R.id.textViewProtocolId);
                protocolType = v.findViewById(R.id.textViewProtocolType);
                protocolRequestingUser = v.findViewById(R.id.textViewProtocolUser);
                protocolTime = v.findViewById(R.id.textViewProtocolTime);
                protocolIndicator = v.findViewById(R.id.protocol_indicator);
            }

            if (listProtocolAdapter.get(position).getEventId() != null) {
                protocolId.setText(listProtocolAdapter.get(position).getEventId());
            }
            if (listProtocolAdapter.get(position).getEventType() != null) {
                protocolType.setText(listProtocolAdapter.get(position).getEventType());
            }
            if (listProtocolAdapter.get(position).getRequestingUser() != null) {
                protocolRequestingUser.setText(listProtocolAdapter.get(position).getRequestingUser());
            }
            if (listProtocolAdapter.get(position).getEventCreationTime() != null) {
                protocolTime.setText(listProtocolAdapter.get(position).getEventCreationTime());
            }
            if ((listProtocolAdapter.get(position).getEventIndicator() != null
                    && listProtocolAdapter.get(position).getEventIndicator().equals("SUCCESS") &&
                    listProtocolAdapter.get(position).getResponseCode() == null) ||
                    (listProtocolAdapter.get(position).getResponseCode() != null
                    && listProtocolAdapter.get(position).getResponseCode().equals("200") &&
                    listProtocolAdapter.get(position).getEventIndicator() == null)) {
                protocolIndicator.setBackgroundResource(R.mipmap.check_mark_green);
            } else if ((listProtocolAdapter.get(position).getEventIndicator() != null
                    && !listProtocolAdapter.get(position).getEventIndicator().equals("SUCCESS") &&
                    listProtocolAdapter.get(position).getResponseCode() == null) ||
                    (listProtocolAdapter.get(position).getResponseCode() != null
                            && !listProtocolAdapter.get(position).getResponseCode().equals("200") &&
                            listProtocolAdapter.get(position).getEventIndicator() == null)) {
                protocolIndicator.setBackgroundResource(R.mipmap.warning_red);
            }

            return v;
        }
    }
}
