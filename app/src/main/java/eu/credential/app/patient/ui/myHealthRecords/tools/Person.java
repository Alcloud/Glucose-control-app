package eu.credential.app.patient.ui.myHealthRecords.tools;

public class Person {
    private String name;
    private int i;

    public Person (String name, int i){
        this.name = name;
        this.i = i;
    }
    private final String[] event = new String[]{"has added data.", "wants to add data.", "no event."};

    public String getName() {
        return name;
    }

    public String getEvent() {
        if (i>=0 && i<2){
            return event[i];
        }
        else {
            return event[2];
        }
    }
}