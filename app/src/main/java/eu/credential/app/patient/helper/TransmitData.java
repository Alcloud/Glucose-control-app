package eu.credential.app.patient.helper;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
/**
 * Created by Aleksei Piatkin on 24.10.17.
 * <p>
 * This class makes it possible to exchange data between fragments.
 */
public class TransmitData {

    private String id;
    private String previousId;
    private String name;
    private String surname;
    private String role;
    private String previousname;
    private String previoussurname;
    private Fragment fragment;
    private DialogFragment dialogFragment;
    private FragmentActivity fragmentActivity;
    private int activity;

    private TransmitData(final TransmitDataBuilder transmitDataBuilder) {
        this.id = transmitDataBuilder.getId();
        this.previousId = transmitDataBuilder.getPreviousId();
        this.name = transmitDataBuilder.getName();
        this.surname = transmitDataBuilder.getSurname();
        this.role = transmitDataBuilder.getRole();
        this.previousname = transmitDataBuilder.getPreviousname();
        this.previoussurname = transmitDataBuilder.getPrevioussurname();
        this.activity = transmitDataBuilder.getActivity();
        this.fragment = transmitDataBuilder.getFragment();
        this.dialogFragment = transmitDataBuilder.getDialogFragment();
        this.fragmentActivity = transmitDataBuilder.getFragmentActivity();
    }

    private Bundle transmitDataToFragment() {
        //Send data to another fragment
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("previousid", previousId);
        bundle.putString("role", role);
        bundle.putString("name", name);
        bundle.putString("surname", surname);
        bundle.putString("previousname", previousname);
        bundle.putString("previoussurname", previoussurname);
        bundle.putInt("activity", activity);
        return bundle;
    }

    public void changeFragmentToDialog() {
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        dialogFragment.setArguments(transmitDataToFragment());
        dialogFragment.show(transaction, "dialog");
    }

    public void changeFragment(@IdRes int containerViewId) {
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(containerViewId, fragment);
        fragment.setArguments(transmitDataToFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public static class TransmitDataBuilder {
        private String id;
        private String previousId;
        private String name;
        private String surname;
        private String role;
        private String previousname;
        private String previoussurname;
        private Fragment fragment;
        private DialogFragment dialogFragment;
        private FragmentActivity fragmentActivity;
        private int activity;

        public TransmitDataBuilder id (final String id){
            this.id = id;
            return this;
        }
        public TransmitDataBuilder previousId (final String previousId){
            this.previousId = previousId;
            return this;
        }
        public TransmitDataBuilder name (final String name){
            this.name = name;
            return this;
        }
        public TransmitDataBuilder surname (final String surname){
            this.surname = surname;
            return this;
        }
        public TransmitDataBuilder role (final String role){
            this.role = role;
            return this;
        }
        public TransmitDataBuilder previousname (final String previousname){
            this.previousname = previousname;
            return this;
        }
        public TransmitDataBuilder previoussurname (final String previoussurname){
            this.previoussurname = previoussurname;
            return this;
        }
        public TransmitDataBuilder activity (final int activity){
            this.activity = activity;
            return this;
        }
        public TransmitDataBuilder fragment (final Fragment fragment){
            this.fragment = fragment;
            return this;
        }
        public TransmitDataBuilder dialogFragment (final DialogFragment dialogFragment){
            this.dialogFragment = dialogFragment;
            return this;
        }
        public TransmitDataBuilder fragmentActivity (final FragmentActivity fragmentActivity){
            this.fragmentActivity = fragmentActivity;
            return this;
        }
        public String getId() {
            return id;
        }

        public String getPreviousId() {
            return previousId;
        }

        public String getName() {
            return name;
        }

        public String getSurname() {
            return surname;
        }

        public String getRole() {
            return role;
        }

        String getPreviousname() {
            return previousname;
        }

        String getPrevioussurname() {
            return previoussurname;
        }

        public int getActivity() {
            return activity;
        }

        public Fragment getFragment() {
            return fragment;
        }

        DialogFragment getDialogFragment() {
            return dialogFragment;
        }

        FragmentActivity getFragmentActivity() {
            return fragmentActivity;
        }

        public TransmitData build(){
            return new TransmitData(this);
        }
    }
}
