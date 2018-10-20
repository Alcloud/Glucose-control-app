package eu.credential.app.patient.ui.myHealthRecords.event;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.credential_v020.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import eu.credential.app.patient.helper.Notification;

public class EventList extends ListFragment {

    private ArrayList<Notification> eventListAdapter = new ArrayList<>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getResources().getString(R.string.error_list));
        MyEventAdapter myListAdapter = new MyEventAdapter(getActivity(),
                R.layout.fragment_event_list, EventListMain.eventList);
        setListAdapter(myListAdapter);
        myListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
//        Intent documentIntent = new Intent(getActivity(), EventDescriptionActivity.class);
//        startActivity(documentIntent);
    }

    private class MyEventAdapter extends ArrayAdapter<Notification> {
        private Context myContext;

        private TextView eventText;
        private TextView eventTime;

        MyEventAdapter(Context context, int textViewResourceId, ArrayList<Notification> eventList) {
            super(context, textViewResourceId, eventList);
            myContext = context;
            eventListAdapter = eventList;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) myContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = null;
            if (inflater != null) {
                v = inflater.inflate(R.layout.fragment_event_list, parent, false);
            }
            // Get text
            if (v != null) {
                eventText = v.findViewById(R.id.textViewText);
                eventTime = v.findViewById(R.id.textViewEventTime);
            }
            // check and inform if there is no connection
            if (eventListAdapter.get(0).getText().equals("error")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("Warning!");
                builder.setMessage(getString(R.string.server_not_respond));
                builder.setPositiveButton(getString(R.string.ok),
                        (arg0, arg1) -> {
                        });
                builder.show();
            } else {
                if (eventListAdapter.get(position).getDate() != null) {
                    eventTime.setText(timeFormat(eventListAdapter.get(position).getDate()));
                }
                if (eventListAdapter.get(position).getText() != null) {
                    eventText.setText(eventListAdapter.get(position).getText());
                }
            }
            return v;
        }
    }

    private String timeFormat(String time) {
        LocalDateTime datetime;
        String result = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            datetime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            result = datetime.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"));
        }
        return time;
    }
}

