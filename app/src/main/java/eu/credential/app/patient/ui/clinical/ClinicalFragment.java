package eu.credential.app.patient.ui.clinical;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.credential_v020.R;

/**
 * Created by Administrator on 08.03.2017.
 */

public class ClinicalFragment extends ListFragment {

    // Dummy data
    final String[] documents = new String[]{"item1", "item2", "biological..","item1", "item2", "biological..","item1", "item2", "biological..","item1", "item2", "biological.."};
    final String[] documentDate = new String[]{"12.06.17", "22.01.17", "09.12.16","12.06.17", "22.01.17", "09.12.16","12.06.17", "22.01.17", "09.12.16","12.06.17", "22.01.17", "09.12.16"};

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getResources().getString(R.string.error_list));
        MyListAdapter myListAdapter = new MyListAdapter(getActivity(),
                R.layout.clinical_list_documents, documents, documentDate);
        setListAdapter(myListAdapter);
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent documentIntent = new Intent(getActivity(), DocumentClinicalActivity.class);
        startActivity(documentIntent);
    }

    public class MyListAdapter extends ArrayAdapter<String> {

        private Context myContext;

        public MyListAdapter(Context context, int textViewResourceId,
                             String[] objects, String[] objects2) {
            super(context, textViewResourceId, objects);
            myContext = context;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // return super.getView(position, convertView, parent);

            LayoutInflater inflater = (LayoutInflater) myContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.clinical_list_documents, parent,
                    false);
            TextView documentNameTextView = (TextView) row.findViewById(R.id.textViewName);
            TextView documentdatumTextView = (TextView) row.findViewById(R.id.textViewSubName);
            documentNameTextView.setText(documents[position]);
            documentdatumTextView.setText(documentDate[position]);
            ImageView iconImageView = (ImageView) row.findViewById(R.id.imageViewIcon);
            ImageView nextImageView = (ImageView) row.findViewById(R.id.imageViewNext);

            iconImageView.setImageResource(R.drawable.document);
            nextImageView.setImageResource(R.drawable.arrow);

            return row;
        }
    }
}
