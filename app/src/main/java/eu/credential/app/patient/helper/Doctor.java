package eu.credential.app.patient.helper;

/*
 * Created by Aleksei Piatkin on 19.07.17.
 */

public class Doctor {
    private String name;
    private String surname;
    private String [] role;
    private String mainRole;
    private String city;
    private String postCode;
    private String id;

    public Doctor() {

    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
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

    public String [] getRole() {
        return role;
    }

    public void setRole(String [] role) {
        this.role = role;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
