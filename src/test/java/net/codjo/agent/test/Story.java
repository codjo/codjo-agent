package net.codjo.agent.test;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import net.codjo.agent.Agent;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.AgentController;
import net.codjo.agent.Aid;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.JadeWrapper;
import net.codjo.agent.Service;
import net.codjo.agent.imtp.NoConnectionIMTPManager;
import net.codjo.agent.test.AgentContainerFixture.Runnable;
import net.codjo.test.common.LogString;
import net.codjo.test.common.fixture.Fixture;
import net.codjo.util.time.Chronometer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

import static java.util.Arrays.asList;
/**
 * Description d'un scenario de test unitaire. <p>Exemple d'utilisation :</p>
 * <pre>
 * story.record().startTester("first")
 *          .receiveMessage()
 *          .assertReceivedMessage(MessageTemplate.matchPerformative(AclMessage.AGREE));
 *
 * story.record().startAgent("second", myAgentThatSendMessage);
 *
 * story.execute();
 * </pre>
 */
public class Story implements Fixture {
    public static enum ConnectionType {
        NO_CONNECTION,
        DEFAULT_CONNECTION;
    }
    private StoryRecorder recorder = new StoryRecorder(this);
    private final AgentContainerFixture agentContainerFixture;
    private TesterAgent masterAgent = new TesterAgent();
    private Semaphore storyEndSemaphore = new Semaphore();
    private List<TesterAgent> testerAgents = new ArrayList<TesterAgent>();
    private ContainerConfiguration configuration;
    private LogString logString;


    public Story() {
        this(new AgentContainerFixture());
    }


    public Story(ConnectionType connectionType) {
        this(new AgentContainerFixture(), connectionType);
    }


    public Story(AgentContainerFixture fixture) {
        this(fixture, ConnectionType.NO_CONNECTION);
    }


    public Story(AgentContainerFixture fixture, ConnectionType connectionType) {
        this(fixture, connectionType, null);
    }


    protected Story(AgentContainerFixture fixture, ConnectionType connectionType, LogString logString) {
        this.logString = (logString == null) ? new LogString() : logString;
        this.agentContainerFixture = fixture;
        setTimeout(Semaphore.DEFAULT_TIMEOUT * 2);

        configuration = containerConfigurationFor(connectionType);
    }


    static ContainerConfiguration containerConfigurationFor(ConnectionType connectionType) {
        ContainerConfiguration configuration = new ContainerConfiguration();
        configuration.setLocalPort(AgentContainer.CONTAINER_PORT);

        if (connectionType == ConnectionType.NO_CONNECTION) {
            configuration.setParameter("mtps", null);
            configuration.setParameter("imtp", NoConnectionIMTPManager.class.getName());
        }
        return configuration;
    }


    public ServiceConfiguration installService(Class<? extends Service> aClass) {
        getConfiguration().addService(aClass.getName());
        return new ServiceConfiguration();
    }


    public class ServiceConfiguration {

        public ServiceConfiguration using(Object object) {
            MutablePicoContainer pico = JadeWrapper.getPicoContainer(configuration);
            if (pico == null) {
                pico = new DefaultPicoContainer();
                JadeWrapper.setPicoContainer(configuration, pico);
            }
            pico.registerComponentInstance(object);
            return this;
        }
    }


    public ContainerConfiguration getConfiguration() {
        return configuration;
    }


    public AgentContainerFixture getAgentContainerFixture() {
        return agentContainerFixture;
    }


    public StoryRecorder record() {
        if (!agentContainerFixture.isContainerStarted()) {
            agentContainerFixture.startContainer(configuration);
        }
        return recorder;
    }


    public void execute() throws ContainerFailureException {
        Logger log = Logger.getLogger(getClass());
        log.info("Executing Story with timeout=" + storyEndSemaphore.getTimeout() + " ms, assertTimeout="
                 + agentContainerFixture.getAssertTimeout() + " ms, maxTryBeforeFailure="
                 + agentContainerFixture.getMaxTryBeforeFailure());
        Chronometer chronometer = new Chronometer();
        chronometer.start();
        try {
            getErrorManager().setSemaphore(storyEndSemaphore);

            setErrorManager();
            listenEndOfStory();

            agentContainerFixture.startNewAgent("unit-test-director", masterAgent);

            int nbUnexecutedSteps = waitThatAllTesterAgentHasFinished();
            if (nbUnexecutedSteps > 0) {
                log.error(
                      "\n!!!\n!!! A Story timeout happened => " + nbUnexecutedSteps + " steps were not executed\n!!!");
            }

            getErrorManager().assertNoError();
            assertAllStoriesFinished();
        }
        finally {
            chronometer.stop();
            log.info("The story was executed in " + chronometer.getDelay() + " ms");
        }
    }


    public void setAssertTimeout(long assertTimeout) {
        agentContainerFixture.setAssertTimeout(assertTimeout);
    }


    public void setMaxTryBeforeFailure(int maxTryBeforeFailure) {
        agentContainerFixture.setMaxTryBeforeFailure(maxTryBeforeFailure);
    }


    private void setErrorManager() {
        for (TesterAgent testerAgent : testerAgents) {
            testerAgent.setErrorManager(getErrorManager());
        }
    }


    private void assertAllStoriesFinished() {
        for (TesterAgent testerAgent : testerAgents) {
            assertStoryFinished(testerAgent);
        }
        assertStoryFinished(masterAgent);
    }


    private void assertStoryFinished(TesterAgent testerAgent) {
        if (!testerAgent.isStoryFinished()) {
            List<Step> list = testerAgent.record().getSteps();
            Assert.fail("steps restant pour '" + testerAgent.getAID().getLocalName() + "':\n\t- "
                        + StringUtils.join(list, ",\n\t- ") + '\n');
        }
    }


    private StoryErrorManager getErrorManager() {
        return masterAgent.getErrorManager();
    }


    /**
     * @return The number of remaining steps.
     */
    private int waitThatAllTesterAgentHasFinished() {
        int tokenCount = testerAgents.size() + 1;
        int acquired = storyEndSemaphore.acquire(tokenCount);
        return tokenCount - acquired;
    }


    private void listenEndOfStory() {
        TesterAgentListenerMock listenEndOfStory = new TesterAgentListenerMock() {
            @Override
            public void storyFinished() {
                storyEndSemaphore.release();
            }
        };
        masterAgent.addStoryListener(listenEndOfStory);

        for (TesterAgent testerAgent : testerAgents) {
            testerAgent.addStoryListener(listenEndOfStory);
        }
    }


    public void doSetUp() throws Exception {
        agentContainerFixture.doSetUp();
    }


    public void doTearDown() throws Exception {
        agentContainerFixture.doTearDown();
    }


    public void setTimeout(long timeout) {
        storyEndSemaphore.setTimeout(timeout);
    }


    public AgentController getAgent(String agentNickName) throws ContainerFailureException {
        return getContainer().getAgent(agentNickName);
    }


    public AgentContainer getContainer() {
        return agentContainerFixture.getContainer();
    }


    public static class StoryRecorder {
        private final Story story;


        protected StoryRecorder(Story story) {
            this.story = story;
        }


        public void startAgent(Aid localAid, final Agent agent) {
            startAgent(localAid.getLocalName(), agent);
        }


        public void startAgent(final String nickName, final Agent agent) {
            if (agent instanceof TesterAgent) {
                story.testerAgents.add((TesterAgent)agent);
                ((TesterAgent)agent).addStoryListener(new TesterAgentListenerLog(nickName));
            }
            story.masterAgent.record().addStep(story.createStartAgentStep(nickName, agent));
        }


        public TesterAgentRecorder startTester(Aid localAid) {
            return startTester(localAid.getLocalName());
        }


        public TesterAgentRecorder startTester(String nickName) {
            TesterAgent testerAgent = new TesterAgent();
            startAgent(nickName, testerAgent);
            return testerAgent.record();
        }


        public void addAction(final AgentContainerFixture.Runnable runnable) {
            story.masterAgent.record().addStep(new RunnableStep(runnable));
        }


        public void acquire(Semaphore semaphore) {
            story.masterAgent.record().addStep(AgentStep.acquire(semaphore));
        }


        public void release(Semaphore semaphore) {
            story.masterAgent.record().addStep(AgentStep.release(semaphore));
        }


        public void logInfo(final LogString log, final String message) {
            story.masterAgent.record().addStep(AgentStep.logInfo(log, message));
        }


        public void addAssert(final AgentAssert.Assertion assertion) {
            story.masterAgent.record().addStep(new AssertStep(story, assertion));
        }


        public void assertNumberOfAgentWithService(final int expectedCount, final String serviceType) {
            String description = "assertNumberOfAgentWithService(" + expectedCount + "," + serviceType + ")";
            story.masterAgent.record().addStep(new OneShotStep(description) {
                public void run(Agent agent) throws AssertionFailedError {
                    story.agentContainerFixture.assertNumberOfAgentWithService(expectedCount, serviceType);
                }
            });
        }


        public void assertAgentWithService(final String[] expectedLocalNames, final String serviceType) {
            String description = "assertAgentWithService(" + asList(expectedLocalNames) + "," + serviceType + ")";
            story.masterAgent.record().addStep(new OneShotStep(description) {
                public void run(Agent agent) throws AssertionFailedError {
                    story.agentContainerFixture.assertAgentWithService(expectedLocalNames, serviceType);
                }
            });
        }


        public void assertNotContainsAgent(final String agentNickName) {
            story.masterAgent.record().addStep(new OneShotStep("assertNotContainsAgent(" + agentNickName + ")") {
                public void run(Agent agent) throws AssertionFailedError {
                    story.agentContainerFixture.assertNotContainsAgent(agentNickName);
                }
            });
        }


        public void assertContainsAgent(final String agentNickName) {
            story.masterAgent.record().addStep(new OneShotStep("assertContainsAgent(" + agentNickName + ")") {
                public void run(Agent agent) throws AssertionFailedError {
                    story.agentContainerFixture.assertContainsAgent(agentNickName);
                }
            });
        }


        public void assertLog(final LogString log, final String expectedLogs) {
            addAssert(AgentAssert.log(log, expectedLogs));
        }


        public void assertLogContains(final LogString log, final String... expectedParts) {
            addAssert(AgentAssert.logContains(log, expectedParts));
        }


        public void killAgent(final String nickName) {
            assertContainsAgent(nickName);
            addAction(new Runnable() {
                public void run() throws Exception {
                    story.agentContainerFixture.killAgent(nickName);
                }
            });
        }


        public <T extends SystemMock> T mock(T systemMock) {
            systemMock.record(story);
            return systemMock;
        }
    }


    private Step createStartAgentStep(String nickName, Agent agent) {
        return new StartAgentStep(nickName, agent);
    }


    private class StartAgentStep implements Step {
        private final String nickName;
        private final Agent agent;


        StartAgentStep(String nickName, Agent agent) {
            this.nickName = nickName;
            this.agent = agent;
        }


        public void run(Agent me) throws AssertionFailedError {
            try {
                agentContainerFixture.startNewAgent(nickName, agent);
            }
            catch (ContainerFailureException e) {
                throw new IllegalArgumentException("Impossible de démarrer l'agent '"
                                                   + nickName + "'");
            }
        }


        public boolean done() {
            return true;
        }
    }

    private static class TesterAgentListenerLog implements TesterAgentListener {
        private String agentNickName;


        TesterAgentListenerLog(String agentNickName) {
            this.agentNickName = agentNickName;
        }


        public void storyStarted() {
            info("entre en scène");
        }


        public void stepStarted(Step step) {
            info("commence la step " + step);
        }


        public void stepFinished(Step step) {
//            info("termine la step");
        }


        public void storyFinished() {
            info("quitte la scène");
        }


        private void info(String message) {
            //noinspection UseOfSystemOutOrSystemErr
            System.out.println(agentNickName + " : " + message);
        }
    }
    private static class RunnableStep extends OneShotStep {
        private final AgentContainerFixture.Runnable runnable;


        RunnableStep(AgentContainerFixture.Runnable runnable) {
            this.runnable = runnable;
        }


        public void run(Agent agent) throws AssertionFailedError {
            try {
                runnable.run();
            }
            catch (Exception cause) {
                AssertionFailedError failure =
                      new AssertionFailedError("Echec de l'action (" + cause.getMessage() + ')');
                failure.initCause(cause);
                throw failure;
            }
        }


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("RunnableStep[");
            sb.append(runnable);
            sb.append(']');
            return sb.toString();
        }
    }
    private static class AssertStep extends OneShotStep {
        private final AgentAssert.Assertion assertion;
        private final Story story;


        AssertStep(Story story, AgentAssert.Assertion assertion) {
            this.assertion = assertion;
            this.story = story;
        }


        public void run(Agent agent) throws AssertionFailedError {
            story.agentContainerFixture.assertUntilOk(assertion);
        }


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("AssertStep[");
            sb.append(assertion);
            sb.append(']');
            return sb.toString();
        }
    }
}
