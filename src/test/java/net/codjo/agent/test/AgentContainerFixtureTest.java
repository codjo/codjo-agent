package net.codjo.agent.test;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import net.codjo.test.common.LogString;
/**
 *
 */
public class AgentContainerFixtureTest extends TestCase {
    private static final int MAX_TRY_BEFORE_FAILURE = 4;
    private static final int ASSERT_TIMEOUT = 1;
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
        final ListAppender logs = ListAppender.createAndAddToRootLogger();

        try {
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
            assertTrue("nbTries logged", logs.matchesOneLine(
                  "ERROR: \n!!!\n!!! AgentAssert failed after " + MAX_TRY_BEFORE_FAILURE + " tries, with a timeout of "
                  + ASSERT_TIMEOUT + " ms per try\n!!!"));
        }
        catch (AssertionFailedError afe) {
            logs.printTo(System.out);
            throw afe;
        }
        finally {
            logs.removeFromRootLogger();
        }
    }


    @Override
    protected void setUp() throws Exception {
        fixture.setAssertTimeout(ASSERT_TIMEOUT);
        fixture.setMaxTryBeforeFailure(MAX_TRY_BEFORE_FAILURE);
    }
}
