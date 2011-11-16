package net.codjo.agent;
/**
 *
 */
public class DataStore {
    private jade.core.behaviours.DataStore jadeDataStore = new jade.core.behaviours.DataStore();


    public DataStore() {
    }


    DataStore(jade.core.behaviours.DataStore jadeDataStore) {
        this.jadeDataStore = jadeDataStore;
    }


    public void put(Object key, Object value) {
        jadeDataStore.put(key, value);
    }


    public Object get(Object key) {
        return jadeDataStore.get(key);
    }


    public Object remove(Object key) {
        return jadeDataStore.remove(key);
    }


    public void clear() {
        jadeDataStore.clear();
    }


    jade.core.behaviours.DataStore getJadeDataStore() {
        return jadeDataStore;
    }


    DataStore wrapp(jade.core.behaviours.DataStore jade) {
        jadeDataStore = jade;
        return this;
    }
}
