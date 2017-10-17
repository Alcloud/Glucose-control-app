package eu.credential.app.patient.ui.my_doctors.doctors_address_book;

/*
 * Created by Aleksei Piatkin on 19.07.17.
 */

public class Doctor {
    private String name;
    private String surname;
    private String [] role;
    private String mainRole;
    private String city;
    private String id;

    public Doctor() {

    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    String getMainRole() {
        return mainRole;
    }

    void setMainRole(String mainRole) {
        this.mainRole = mainRole;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String [] getRole() {
        return role;
    }

    public void setRole(String [] role) {
        this.role = role;
    }

    String getCity() {
        return city;
    }

    void setCity(String city) {
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
