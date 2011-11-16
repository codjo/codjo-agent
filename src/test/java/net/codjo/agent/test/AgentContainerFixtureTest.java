package net.codjo.agent.test;
import net.codjo.test.common.LogString;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 *
 */
public class AgentContainerFixtureTest extends TestCase {
    private LogString log = new LogString();
    AgentContainerFixture fixture = new AgentContainerFixture();


    public void test_assertUntilOk() throws Exception {
        fixture.assertUntilOk(new AgentAssert.Assertion() {
            int index = 0;


            public void check() throws Throwable {
                index++;
                log.call("check", index);
                if (index < 2) {
                    throw new NullPointerException();
                }
            }
        });
        log.assertContent("check(1), check(2)");
    }


    public void test_assertUntilOk_failure() throws Exception {
        try {
            fixture.assertUntilOk(new AgentAssert.Assertion() {
                int index = 0;


                public void check() throws Throwable {
                    index++;
                    log.call("check", index);
                    throw new NullPointerException("erreur");
                }
            });
            throw new Error("Doit echouer");
        }
        catch (AssertionFailedError ex) {
            assertEquals("erreur", ex.getLocalizedMessage());
            assertTrue(ex.getCause() instanceof NullPointerException);
        }
        log.assertContent("check(1), check(2), check(3), check(4)");
    }


    @Override
    protected void setUp() throws Exception {
        fixture.setAssertTimeout(1);
        fixture.setMaxTryBeforeFailure(4);
    }
}
