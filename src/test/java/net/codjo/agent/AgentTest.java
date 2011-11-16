/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.DummyAgent;
import net.codjo.test.common.LogString;
import java.util.Timer;
import java.util.TimerTask;
import junit.framework.TestCase;
/**
 * Classe de test de {@link Agent}.
 */
public class AgentTest extends TestCase {
    private AgentContainerFixture containerFixture = new AgentContainerFixture();


    public void test_addBehaviour() throws Exception {
        Agent agent = new DummyAgent();

        BehaviourMock myBehaviourMock = new BehaviourMock();
        agent.addBehaviour(myBehaviourMock);
        assertSame(agent, myBehaviourMock.getAgent());

        containerFixture.startNewAgent("Pat", agent);

        myBehaviourMock.log.assertContent("action(), done()");
    }


    public void test_removeBehaviour() throws Exception {
        BehaviourMock myBehaviourMock = new BehaviourMock();

        Agent agent = new Agent(myBehaviourMock);
        agent.removeBehaviour(myBehaviourMock);

        containerFixture.startNewAgent("Pat", agent);

        myBehaviourMock.log.assertContent("");

        agent.removeBehaviour(myBehaviourMock);
        assertNotNull(myBehaviourMock.getAgent());
    }


    public void test_killAgent() throws Exception {
        final LogString log = new LogString();
        Agent kamikazeAgent =
              new DummyAgent() {
                  @Override
                  protected void setup() {
                      log.call("setup");
                  }


                  @Override
                  protected void tearDown() {
                      log.call("tearDown");
                  }
              };
        containerFixture.startNewAgent("kamikaze", kamikazeAgent);
        kamikazeAgent.addBehaviour(new StartAndDieBehaviour());

        containerFixture.waitForAgentDeath("kamikaze");
        log.assertContent("setup(), tearDown()");
    }


    public void test_getO2AObject() throws Exception {
        Agent anAgent = new DummyAgent();
        anAgent.setEnabledO2ACommunication(true, 10);
        anAgent.putO2AObject("O2AObject", false);

        assertEquals("O2AObject", anAgent.getO2AObject());
    }


    public void test_getO2AObject_communicationDisabled()
          throws InterruptedException {
        AgentMock anAgent = new AgentMock();

        anAgent.putO2AObject("O2AObject", false);
        assertNull(anAgent.getO2AObject());

        anAgent.setEnabledO2ACommunication(false, 0);
        anAgent.putO2AObject("O2AObject", true);
        assertNull(anAgent.getO2AObject());
    }


    public void test_receive() throws Exception {
        // Ajout Responder couche agf-agent
        Agent responder = new InheritedAgent();
        containerFixture.startNewAgent("responder", responder);

        // Ajout Initiator Jade
        containerFixture.sendRequestMessageTo("myContent", "responder");

        // Attente du message envoyé
        long start = System.currentTimeMillis();
        AclMessage message;
        do {
            message = responder.receive();
        }
        while (message == null && (System.currentTimeMillis() - start) < 1000);

        // Assert
        assertNotNull(message);
        assertEquals(containerFixture.toString(), message.getSender().getLocalName());
        assertEquals("myContent", message.getContentObject().toString());
    }


    public void test_receive_match() throws Exception {
        // Ajout Responder couche agf-agent
        Agent responder = new InheritedAgent();
        containerFixture.startNewAgent("responder", responder);

        // Ajout Initiator Jade
        AclMessage messageToBeSent = buildACLMessage("responder", "myContent");
        messageToBeSent.setConversationId("convId");
        containerFixture.sendMessage(messageToBeSent);

        // Attente du message envoyé
        MessageTemplate template = MessageTemplate.matchConversationId("convId");
        AclMessage message = containerFixture.receiveMessage(responder, template);

        // Assert
        assertNotNull(message);
        assertEquals(containerFixture.toString(), message.getSender().getLocalName());
        assertEquals("myContent", message.getContentObject().toString());
    }


    public void test_blockingReceive() throws Exception {
        Agent responder = new InheritedAgent();
        containerFixture.startNewAgent("responder", responder);

        final long startTime = System.currentTimeMillis();

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    containerFixture.sendRequestMessageTo("myContent", "responder");
                }
                catch (ContainerFailureException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 500);

        //noinspection deprecation
        AclMessage message = responder.blockingReceive();
        final long endTime = System.currentTimeMillis();

        assertNotNull(message);
        assertTrue(endTime - startTime >= 500);
        assertEquals(containerFixture.toString(), message.getSender().getLocalName());
        assertEquals("myContent", message.getContentObject().toString());
    }


    public void test_blockingReceive_match() throws Exception {
        Agent responder = new InheritedAgent();
        containerFixture.startNewAgent("responder", responder);

        final long startTime = System.currentTimeMillis();

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    AclMessage messageToBeSent = buildACLMessage("responder", "myContent");
                    messageToBeSent.setConversationId("convId");
                    containerFixture.sendMessage(messageToBeSent);
                }
                catch (ContainerFailureException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 500);

        MessageTemplate template = MessageTemplate.matchConversationId("convId");
        //noinspection deprecation
        AclMessage message = responder.blockingReceive(template);
        final long endTime = System.currentTimeMillis();

        assertNotNull(message);
        assertTrue(endTime - startTime >= 500);
        assertEquals(containerFixture.toString(), message.getSender().getLocalName());
        assertEquals("myContent", message.getContentObject().toString());
    }


    public void test_blockingReceive_timeout() throws Exception {
        Agent responder = new InheritedAgent();
        containerFixture.startNewAgent("responder", responder);

        final long startTime = System.currentTimeMillis();

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    containerFixture.sendRequestMessageTo("myContent", "responder");
                }
                catch (ContainerFailureException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 500);

        //noinspection deprecation
        AclMessage message = responder.blockingReceive(1000);
        final long endTime = System.currentTimeMillis();

        assertNotNull(message);
        assertTrue(endTime - startTime >= 500);
        assertTrue(endTime - startTime <= 1000);
        assertEquals(containerFixture.toString(), message.getSender().getLocalName());
        assertEquals("myContent", message.getContentObject().toString());
    }


    public void test_blockingReceive_timeout_notBlocking() throws Exception {
        Agent responder = new InheritedAgent();
        containerFixture.startNewAgent("responder", responder);

        //noinspection deprecation
        AclMessage message = responder.blockingReceive(500);

        assertNull(message);
    }


    public void test_blockingReceive_matchAndTimeout() throws Exception {
        Agent responder = new InheritedAgent();
        containerFixture.startNewAgent("responder", responder);

        final long startTime = System.currentTimeMillis();

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    AclMessage messageToBeSent = buildACLMessage("responder", "myContent");
                    messageToBeSent.setConversationId("convId");
                    containerFixture.sendMessage(messageToBeSent);
                }
                catch (ContainerFailureException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 500);

        MessageTemplate template = MessageTemplate.matchConversationId("convId");
        //noinspection deprecation
        AclMessage message = responder.blockingReceive(template, 1000);
        final long endTime = System.currentTimeMillis();

        assertNotNull(message);
        assertTrue(endTime - startTime >= 500);
        assertTrue(endTime - startTime <= 1000);
        assertEquals(containerFixture.toString(), message.getSender().getLocalName());
        assertEquals("myContent", message.getContentObject().toString());
    }


    public void test_send() throws Exception {
        // Ajout Responder
        JadeResponderAgentMock responderAgent = new JadeResponderAgentMock();
        containerFixture.getContainer().getJadeContainer()
              .acceptNewAgent("responder", responderAgent).start();

        // Ajout Initiator
        BehaviourMock myBehaviourMock =
              new BehaviourMock() {
                  @Override
                  protected void action() {
                      getAgent().send(buildACLMessage("responder", "myContent"));
                  }
              };
        containerFixture.getContainer()
              .acceptNewAgent("initiator", new InheritedAgent(myBehaviourMock))
              .start();

        // Attente du message envoyé
        jade.lang.acl.ACLMessage message = responderAgent.waitReceivedMessage(2000);

        // Assert
        assertNotNull(message);
        assertEquals("initiator", message.getSender().getLocalName());
        assertEquals("myContent", message.getContentObject().toString());
    }


    public void test_getContainer() throws Exception {
        AgentContainer initialContainer = containerFixture.getContainer();

        Agent agent = new DummyAgent();
        try {
            assertNull(agent.getAgentContainer());
            fail("NullPointerException attempted.");
        }
        catch (NullPointerException e) {
            ;
        }

        initialContainer.acceptNewAgent("Chon", agent).start();
        AgentContainer myAgentContainer = agent.getAgentContainer();
        assertNotNull(myAgentContainer);
        assertNotSame(initialContainer, myAgentContainer);
        assertTrue(myAgentContainer.same(initialContainer.getJadeContainer()));
    }


    public void test_getServiceHelper() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.setLocalPort(AgentContainer.CONTAINER_PORT);
        containerConfiguration.addService(AGFServiceWithHelper.class.getName());
        containerFixture.startContainer(containerConfiguration);

        Agent agent = new DummyAgent();
        containerFixture.startNewAgent("pat", agent);

        ServiceHelper helper = agent.getHelper(AGFServiceWithHelper.NAME);

        assertNotNull(helper);
        assertSame(agent, ((AGFServiceHelper)helper).initedAgent);
    }


    public void test_getAgentStateName() throws Exception {
        DummyAgent myAgent = new DummyAgent();
        assertEquals("Initiated", myAgent.getAgentStateName());

        containerFixture.startNewAgent("pat", myAgent);
        myAgent.getSemaphore().waitFor(DummyAgent.SETUP);
        assertEqualsOneOf(new String[]{"Active", "Idle"}, myAgent.getAgentStateName());

        myAgent.die();
        containerFixture.waitForAgentDeath("pat");
        assertEquals("Deleted", myAgent.getAgentStateName());
    }


    public void test_getAid_noAid() throws Exception {
        assertNull(new Agent().getAID());
    }


    public void test_getAMS() throws Exception {
        Agent agent = new DummyAgent();

        containerFixture.startNewAgent("Pat", agent);

        assertEquals(agent.getJadeAgent().getAMS(), agent.getAMS().getJadeAID());
    }


    public void test_getAMS_noAid() throws Exception {
        assertNull(new Agent().getAMS());
    }


    @Override
    protected void tearDown() throws Exception {
        containerFixture.doTearDown();
    }


    @Override
    protected void setUp() throws Exception {
        containerFixture.doSetUp();
    }


    private AclMessage buildACLMessage(String receiverName, String content) {
        AclMessage myRequest = new AclMessage(AclMessage.Performative.REQUEST);
        myRequest.addReceiver(new Aid(receiverName));
        myRequest.setContentObject(content);
        return myRequest;
    }


    private void assertEqualsOneOf(String[] expecteds, String actual) {
        for (String expected : expecteds) {
            if (expected.equals(actual)) {
                return;
            }
        }
        fail("actual : " + actual);
    }


    class InheritedAgent extends Agent {
        LogString log = new LogString();


        InheritedAgent() {
        }


        InheritedAgent(Behaviour behaviour) {
            addBehaviour(behaviour);
        }


        @Override
        protected void setup() {
            log.call("setup");
        }


        @Override
        protected void tearDown() {
            log.call("tearDown");
        }
    }

    public static class AGFServiceWithHelper implements Service {
        private static final String NAME = "AGFServiceWithHelper";


        public String getName() {
            return NAME;
        }


        public void boot(ContainerConfiguration containerConfiguration) throws ServiceException {
        }


        public ServiceHelper getServiceHelper(Agent agent) {
            return new AGFServiceHelper();
        }
    }

    static class AGFServiceHelper implements ServiceHelper {
        Agent initedAgent;


        public void init(Agent agent) {
            initedAgent = agent;
        }
    }

    private static class StartAndDieBehaviour extends BehaviourMock {
        @Override
        protected void action() {
            getAgent().die();
        }
    }
}
