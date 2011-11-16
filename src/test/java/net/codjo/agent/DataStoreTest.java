package net.codjo.agent;
import junit.framework.TestCase;
/**
 *
 */
public class DataStoreTest extends TestCase {
    private DataStore dataStore = new DataStore();


    public void test_getJadeDataStore() throws Exception {
        jade.core.behaviours.DataStore jade = dataStore.getJadeDataStore();
        assertNotNull(jade);
        assertSame(jade, dataStore.getJadeDataStore());
    }


    public void test_put() throws Exception {
        String value = "value";
        dataStore.put("key", value);
        assertSame(value, dataStore.get("key"));
    }


    public void test_remove() throws Exception {
        dataStore.put("key", "value");
        Object removedObject = dataStore.remove("key");
        assertEquals("value", removedObject);
        assertNull(dataStore.get("key"));
    }


    public void test_clear() throws Exception {
        dataStore.put("key", "value");
        dataStore.clear();
        assertNull(dataStore.get("key"));
    }


    public void test_constructor() {
        jade.core.behaviours.DataStore jade = new jade.core.behaviours.DataStore();
        dataStore = new DataStore(jade);
        assertSame(jade, dataStore.getJadeDataStore());
    }


    public void test_wrapp() {
        jade.core.behaviours.DataStore jade = new jade.core.behaviours.DataStore();
        assertSame(jade, dataStore.wrapp(jade).getJadeDataStore());
    }
}
