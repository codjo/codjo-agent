package net.codjo.agent.test;
import net.codjo.agent.test.AgentAssert.Assertion;
import net.codjo.test.common.LogString;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class AgentAssertTest {

    @Test
    public void test_logAndClear() throws Exception {
        LogString logString = new LogString();
        logString.call("start");

        Assertion assertion = AgentAssert.logAndClear(logString, "start(), stop()");

        try {
            assertion.check();
            fail();
        }
        catch (Throwable throwable) {
            ;
        }

        logString.assertContent("start()");

        logString.call("stop");

        try {
            assertion.check();
        }
        catch (Throwable throwable) {
            fail();
        }

        assertTrue(logString.getContent().length() == 0);
    }
}
