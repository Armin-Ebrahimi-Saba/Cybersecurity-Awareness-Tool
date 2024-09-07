package SplunkExt;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Observer in the Observer-Subject pattern 
 */
public class Observer {
    private boolean isNotified;
    private Subject subject;

    /*
     * constructor
     */
    public Observer(Subject subject) {
        isNotified = false;
        this.subject = subject;
        subject.addObserver(this);
    }

    /*
     * set notified flags
     */
    public void onUpdate() {
        System.out.println("onUpdate() called");
        isNotified = true;
    }

    /*
     * returns the last obtained data
     */
    public Set<ConcurrentHashMap<String, Object>> getUpdate() {
        System.out.println("getUpdate() called");
        if (isNotified) {
            System.out.println("getUpdate() branch accepted");
            isNotified = false;
            return subject.getData();
        }
        return null;
    }
}

