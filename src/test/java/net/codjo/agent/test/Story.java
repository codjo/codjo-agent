package net.codjo.agent.test;
import net.codjo.agent.Agent;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.AgentController;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.JadeWrapper;
import net.codjo.agent.Service;
import net.codjo.agent.imtp.NoConnectionIMTPManager;
import net.codjo.agent.test.AgentContainerFixture.Runnable;
import net.codjo.test.common.fixture.Fixture;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
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
        getErrorManager().setSemaphore(storyEndSemaphore);

        setErrorManager();
        listenEndOfStory();

        agentContainerFixture.startNewAgent("unit-test-director", masterAgent);

        waitThatAllTesterAgentHasFinished();

        getErrorManager().assertNoError();
        assertAllStoriesFinished();
    }


    public void setAssertTimeout(long assertTimeout) {
        agentContainerFixture.setAssertTimeout(assertTimeout);
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
            Assert.fail("steps restant pour '" + testerAgent.getAID().getLocalName() + "': "
                        + list.toString());
        }
    }


    private StoryErrorManager getErrorManager() {
        return masterAgent.getErrorManager();
    }


    private void waitThatAllTesterAgentHasFinished() {
        storyEndSemaphore.acquire(testerAgents.size() + 1);
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


        public void startAgent(final String nickName, final Agent agent) {
            if (agent instanceof TesterAgent) {
                story.testerAgents.add((TesterAgent)agent);
                ((TesterAgent)agent).addStoryListener(new TesterAgentListenerLog(nickName));
            }
            story.masterAgent.record().addStep(story.createStartAgentStep(nickName, agent));
        }


        public TesterAgentRecorder startTester(String nickName) {
            TesterAgent testerAgent = new TesterAgent();
            startAgent(nickName, testerAgent);
            return testerAgent.record();
        }


        public void addAction(final AgentContainerFixture.Runnable runnable) {
            story.masterAgent.record().addStep(new RunnableStep(runnable));
        }


        public void addAssert(final AgentAssert.Assertion assertion) {
            story.masterAgent.record().addStep(new AssertStep(story, assertion));
        }


        public void assertNumberOfAgentWithService(final int expectedCount, final String serviceType) {
            story.masterAgent.record().addStep(new OneShotStep() {
                public void run(Agent agent) throws AssertionFailedError {
                    story.agentContainerFixture.assertNumberOfAgentWithService(expectedCount, serviceType);
                }
            });
        }


        public void assertAgentWithService(final String[] expectedLocalNames, final String serviceType) {
            story.masterAgent.record().addStep(new OneShotStep() {
                public void run(Agent agent) throws AssertionFailedError {
                    story.agentContainerFixture.assertAgentWithService(expectedLocalNames, serviceType);
                }
            });
        }


        public void assertNotContainsAgent(final String agentNickName) {
            story.masterAgent.record().addStep(new OneShotStep() {
                public void run(Agent agent) throws AssertionFailedError {
                    story.agentContainerFixture.assertNotContainsAgent(agentNickName);
                }
            });
        }


        public void assertContainsAgent(final String agentNickName) {
            story.masterAgent.record().addStep(new OneShotStep() {
                public void run(Agent agent) throws AssertionFailedError {
                    story.agentContainerFixture.assertContainsAgent(agentNickName);
                }
            });
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
    }
}
