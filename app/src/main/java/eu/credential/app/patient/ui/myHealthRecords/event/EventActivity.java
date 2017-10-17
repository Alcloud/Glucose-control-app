package eu.credential.app.patient.ui.myHealthRecords.event;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import eu.credential.app.patient.ui.myHealthRecords.tools.Person;
import eu.credential.app.patient.ui.myHealthRecords.tools.MyAdapter;
import java.util.ArrayList;

public class EventActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Person person;

        ArrayList<Person> persons = new ArrayList<>();

        person = new Person("Alex Smith", 0);
        persons.add(person);

        person = new Person("Ralph Beamer",1);
        persons.add(person);

        person = new Person("Don John",3);
        persons.add(person);

        setListAdapter(new MyAdapter(this, persons));
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent documentIntent = new Intent(this, EventDescriptionActivity.class);
        startActivity(documentIntent);
    }
}
