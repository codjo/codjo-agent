package net.codjo.agent.test;
import java.io.IOException;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.ServiceMock;
import net.codjo.test.common.LogString;

import static net.codjo.agent.test.AgentStep.logInfo;

public class StoryTest extends TestCase {
    private LogString log = new LogString();
    private Story story = new Story(new AgentContainerFixture());
    private static final int LITTLE_TIMEOUT = 200;


    public void test_installService() throws Exception {
        story.installService(ServiceMock.class);

        assertServiceIsActive(ServiceMock.class);
    }


    public void test_installService_using() throws Exception {
        LogString local = new LogString();

        story.installService(ServiceMockUsingLogString.class)
              .using(local);

        assertServiceIsActive(ServiceMockUsingLogString.class);

        local.assertContent("ServiceMockUsingLogString.<init>(LogString)");
    }


    public void test_installService_usingTwice() throws Exception {
        Story.ServiceConfiguration conf = story.installService(ServiceMockUsingLogString.class)
              .using(1);
        assertSame(conf, conf.using(2.5));
    }


    public void test_storyRecordStartContainer() throws Exception {
        assertFalse(story.getAgentContainerFixture().isContainerStarted());
        story.record();
        assertTrue(story.getAgentContainerFixture().isContainerStarted());
    }


    public void test_startAgent() throws Exception {
        TesterAgent first = new TesterAgent();
        first.record().receiveMessage().log(log, "received OK");

        TesterAgent second = new TesterAgent();
        second.record().sendMessage(AclMessage.Performative.INFORM, new Aid("first"), "message content");

        story.record().startAgent("first", first);
        story.record().startAgent("second", second);
        story.execute();

        log.assertContent("received OK");
    }


    public void test_startAgent_failure() throws Exception {
        story.record().startTester("sameName");
        story.record().startTester("sameName");

        story.setTimeout(LITTLE_TIMEOUT);
        assertStoryExecuteFailure("agent 'unit-test-director' : Impossible de démarrer l'agent 'sameName'");
    }


    public void test_testerAgent_assertFailure() throws Exception {
        story.setTimeout(100);
        story.record().startTester("first")
              .receiveMessage()
              .assertReceivedMessage(MessageTemplate.matchPerformative(AclMessage.Performative.AGREE));

        story.record().startTester("second")
              .sendMessage(AclMessage.Performative.INFORM, new Aid("first"), "message content");

        try {
            story.execute();
            unitTestShouldFail();
        }
        catch (AssertionFailedError ex) {
            TesterAgentTest.assertStartWith(
                  "agent 'first' : Received message does not match the template: '( Perfomative: AGREE )'",
                  ex.getMessage());
        }
    }


    public void test_addAction() throws Exception {
        story.record().addAction(new AgentContainerFixture.Runnable() {
            public void run() {
                fail("failed action");
            }
        });

        story.getAgentContainerFixture().setMaxTryBeforeFailure(2);

        assertStoryExecuteFailure("agent 'unit-test-director' : failed action");
    }


    public void test_addAction_exception() throws Exception {
        story.record().addAction(new AgentContainerFixture.Runnable() {
            public void run() throws Exception {
                throw new IOException("oupss");
            }
        });

        assertStoryExecuteFailure("agent 'unit-test-director' : Echec de l'action (oupss)");
    }


    public void test_receiveMessageTimeout() throws Exception {
        story.record().startTester("fede").receiveMessage();

        try {
            story.setTimeout(100);
            story.execute();
            unitTestShouldFail();
        }
        catch (AssertionFailedError ex) {
            assertEquals("steps restant pour 'fede': [ReceiveMessageStep[Match ALL Template]]",
                         ex.getMessage());
        }
    }


    public void test_addAssert() throws Exception {
        // TODO Move Assertion interface + Runnable (see AgentContainerFixture)
        story.record().addAssert(new AgentAssert.Assertion() {
            private boolean firstCall = true;


            public void check() throws AssertionFailedError {
                if (firstCall) {
                    log.info("first call (failure)");
                    firstCall = false;
                    fail();
                }
                else {
                    log.info("second call (ok)");
                }
            }
        });

        story.execute();

        log.assertContent("first call (failure), second call (ok)");
    }


    public void test_assertContainsAgent() throws Exception {
        story.record().startTester("someone");

        story.record().assertContainsAgent("someone");

        story.execute();
    }


    public void test_assertContainsAgent_failure() throws Exception {
        story.record().assertContainsAgent("no-one");

        story.getAgentContainerFixture().setMaxTryBeforeFailure(2);

        assertStoryExecuteFailure("agent 'unit-test-director' : Agent 'no-one' existe");
    }


    public void test_assertNumberOfAgentWithService() throws Exception {
        story.record().startTester("agent-with-notificationCapabilities")
              .registerToDF("notification");

        story.record().assertNumberOfAgentWithService(1, "notification");

        story.record().assertNumberOfAgentWithService(1, "unknownServiceType");

        story.getAgentContainerFixture().setMaxTryBeforeFailure(2);

        assertStoryExecuteFailure("agent 'unit-test-director' : "
                                  + "Nombre d'agent avec service 'unknownServiceType' expected:<1> but was:<0>");
    }


    public void test_getAgent() throws Exception {
        story.record().startTester("columbo");

        story.record().addAction(new AgentContainerFixture.Runnable() {
            public void run() throws Exception {
                assertNotNull(story.getAgent("columbo"));
            }
        });

        story.execute();
    }


    public void test_semaphore_agentLevel() throws Exception {
        Semaphore semaphore = new Semaphore();

        story.record().startTester("second")
              .acquire(semaphore)
              .then()
              .perform(logInfo(log, "second"));

        story.record().startTester("first")
              .perform(logInfo(log, "first"))
              .then()
              .release(semaphore);

        story.record().assertLog(log, "first, second");

        story.execute();
    }


    public void test_semaphore_acquire() throws Exception {
        Semaphore semaphore = new Semaphore();

        story.record().startTester("second")
              .acquire(semaphore)
              .then()
              .perform(logInfo(log, "second"));

        story.record()
              .logInfo(log, "first");

        story.record()
              .release(semaphore);

        story.record()
              .assertLog(log, "first, second");

        story.execute();
    }


    public void test_semaphore_release() throws Exception {
        Semaphore semaphore = new Semaphore();

        story.record().startTester("first")
              .perform(logInfo(log, "first"))
              .then()
              .release(semaphore);

        story.record()
              .acquire(semaphore);

        story.record()
              .logInfo(log, "second");

        story.record()
              .assertLog(log, "first, second");

        story.execute();
    }


    public void test_setAssertTimeout() throws Exception {
        assertEquals(20, story.getAgentContainerFixture().getAssertTimeout());
        story.setAssertTimeout(1000);
        assertEquals(1000, story.getAgentContainerFixture().getAssertTimeout());
    }


    public void test_mock() throws Exception {
        story.record().mock(new MySystemMock())
              .startAgent("bobo");

        story.record().assertContainsAgent("bobo");

        story.execute();
    }


    private static class MySystemMock extends SystemMock {
        private Story story;


        @Override
        protected void record(Story myStory) {
            this.story = myStory;
        }


        public void startAgent(String nickName) {
            story.record().startTester(nickName);
        }
    }


    @Override
    protected void setUp() throws Exception {
        story.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
    }


    private void assertStoryExecuteFailure(String expectedError) throws ContainerFailureException {
        try {
            story.execute();
            unitTestShouldFail();
        }
        catch (AssertionFailedError ex) {
            assertEquals(expectedError, ex.getMessage());
        }
    }


    private void assertServiceIsActive(final Class aClass) throws ContainerFailureException {
        story.record().startTester("tester")
              .addStep(new OneShotStep() {
                  public void run(Agent agent) throws Exception {
                      agent.getHelper(aClass.getSimpleName());
                      log.info("service actif");
                  }
              });
        story.record().assertLog(log, "service actif");
        story.execute();
    }


    private void unitTestShouldFail() {
        throw new Error("Should Fail");
    }


    public static class ServiceMockUsingLogString extends ServiceMock {

        public ServiceMockUsingLogString(LogString log) {
            log.info("ServiceMockUsingLogString.<init>(LogString)");
        }
    }
}
