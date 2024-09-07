package SplunkExt;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Subject {
    // these observers get notified when data changes 
    private Set<Observer> observers;
    // for storing data
    private Set<ConcurrentHashMap<String, Object>> hashSet;

    /*
     * constructor 
     */
    public Subject() {
        observers = ConcurrentHashMap.newKeySet(); //new HashSet<>();
        hashSet = ConcurrentHashMap.newKeySet(); //new HashSet<>();
    }

    /*
     * add an observer
     * @observer a new observer 
     */
    public void addObserver(Observer observer) {
        if (observer != null && !observers.contains(observer))
            observers.add(observer);
    }

    /*
     * remove an observer
     * @observer observer to be removed 
     */
    public void removeObserver(Observer observer) {
        if (observer != null && observers.contains(observer))
            observers.remove(observer);
    } 
    
    /*
     * clear the hashset and populate it with new data
     * @newData the new data to replace the old data
     */
    public void updateSet(Set<ConcurrentHashMap<String, Object>> newData) {
        if (newData.hashCode() != hashSet.hashCode()) {
            hashSet.clear();
            // hashSet.addAll(newData);
            hashSet = newData;
            notifyObservers();
        } else {
            System.out.println("repetetive data");
        }
    }

    /*
     * notifys observers
     */
    public void notifyObservers() {
        for (Observer obs : observers)
            obs.onUpdate();
    }

    /*
     * a getter method
     */
    public Set<ConcurrentHashMap<String, Object>> getData() {
        return hashSet;
    }

}
