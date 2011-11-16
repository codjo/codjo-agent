package net.codjo.agent.test;
import net.codjo.test.common.LogString;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 *
 */
public class StoryErrorManagerTest extends TestCase {
    private LogString log = new LogString();
    private StoryErrorManager manager = new StoryErrorManager();


    public void test_hasError() throws Exception {
        assertFalse(manager.hasError());

        manager.addError("agentName", new Throwable());

        assertTrue(manager.hasError());
    }


    public void test_assertNoError_oneError() throws Exception {
        manager.assertNoError();

        Throwable error = new Throwable("assertion failed");
        manager.addError("agentName", error);

        try {
            manager.assertNoError();
            unitTestFail();
        }
        catch (AssertionFailedError ex) {
            assertEquals("agent 'agentName' : assertion failed", ex.getMessage());
            assertSame(error, ex.getCause());
        }
    }


    public void test_getFirstErrorDescription() throws Exception {
        manager.assertNoError();
        assertNull(manager.getFirstErrorDescription());

        manager.addError("agentName", new NullPointerException());
        assertEquals("agent 'agentName' : java.lang.NullPointerException",
                     manager.getFirstErrorDescription());
    }


    public void test_assertNoError() throws Exception {
        manager.assertNoError();

        Throwable firstError = new Throwable("error 1");
        manager.addError("agent-a", firstError);
        manager.addError("agent-b", new Throwable("error 2"));

        try {
            manager.assertNoError();
            unitTestFail();
        }
        catch (AssertionFailedError ex) {
            assertEquals("agent 'agent-a' : error 1\nagent 'agent-b' : error 2",
                         ex.getMessage());
            assertSame(firstError, ex.getCause());
        }
    }


    public void test_addError_releaseSemaphore() throws Exception {
        manager.setSemaphore(new Semaphore() {
            @Override
            public void releaseAll() {
                log.call("releaseAll");
            }
        });

        manager.addError("agent-name", new Throwable());

        log.assertContent("releaseAll()");
    }


    private void unitTestFail() {
        throw new Error("Should Fail");
    }
}
