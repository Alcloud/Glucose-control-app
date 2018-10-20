package eu.credential.app.patient.helper;

public interface AsyncTaskCompleteListener<T> {
    public void onTaskComplete(T result);
}
