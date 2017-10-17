package eu.credential.app.patient.ui.my_doctors.doctors_address_book;

public class Doctor {
    private String name;
    private String surname;
    private String role;
    private String mainRole;
    private String city;
    private String id;

    public Doctor(String name, String surname, String role, String city) {
        this.name = name;
        this.surname = surname;
        this.role = role;
        this.city = city;
    }

    public Doctor() {

    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getMainRole() {
        return mainRole;
    }

    public void setMainRole(String mainRole) {
        this.mainRole = mainRole;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
