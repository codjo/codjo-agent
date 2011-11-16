/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.releaseTest;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.AgentController;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.test.Semaphore;
import net.codjo.agent.test.SemaphoreByToken;
import junit.framework.TestCase;
/**
 * Classe principal une serie de test release sur la couche agent.
 */
public class MainTest extends TestCase {
    public static final SemaphoreByToken.Token SETUP = new SemaphoreByToken.Token("setup");
    public static final SemaphoreByToken.Token ACTION = new SemaphoreByToken.Token("action");
    private AgentContainer container = AgentContainer.createMainContainer();


    public void test_communicate() throws Exception {
        InitiatorAgent initiatorAgent = new InitiatorAgent();
        ResponderAgent resopnderAgent = new ResponderAgent();

        container.acceptNewAgent(ResponderAgent.ID, resopnderAgent).start();
        container.acceptNewAgent(InitiatorAgent.ID, initiatorAgent).start();

        String result = initiatorAgent.waitForReply();
        assertEquals("InitiatorAgent.send(), Responder.reply()", result);
    }


    public void test_o2a() throws Exception {
        SemaphoreByToken semaphore = new SemaphoreByToken();
        O2Agent anAgent = new MainTest.O2Agent(semaphore);

        AgentController controller = container.acceptNewAgent("unused", anAgent);

        controller.start();
        semaphore.waitFor(SETUP);
        controller.putO2AObject("value");
        semaphore.waitFor(ACTION);

        assertEquals("value", anAgent.get02AValue());
    }


    @Override
    protected void setUp() throws Exception {
        container.start();
    }


    @Override
    protected void tearDown() throws Exception {
        container.stop();
    }


    private class InitiatorAgent extends Agent {
        public static final String ID = "initiator";
        private final Semaphore semaphore = new Semaphore();
        private final SimpleReceiverBehaviour receiverBehavior =
              new SimpleReceiverBehaviour(semaphore);


        @Override
        protected void setup() {
            AclMessage aclMessage = new AclMessage(AclMessage.Performative.REQUEST);
            aclMessage.addReceiver(new Aid(ResponderAgent.ID));
            aclMessage.setContentObject("InitiatorAgent.send()");
            addBehaviour(new SimpleSenderBehaviour(aclMessage));

            addBehaviour(receiverBehavior);
        }


        @Override
        protected void tearDown() {
        }


        public String waitForReply() throws InterruptedException {
            semaphore.acquire();
            return receiverBehavior.getACLMessage().getContentObject().toString();
        }
    }

    private class ResponderAgent extends Agent {
        public static final String ID = "responder";


        @Override
        protected void setup() {
            addBehaviour(new AutomaticReplyBehaviour());
        }


        @Override
        protected void tearDown() {
        }
    }

    private static class AutomaticReplyBehaviour extends SimpleReceiverBehaviour {
        @Override
        protected void action() {
            super.action();
            if (done()) {
                AclMessage reply = getACLMessage().createReply();
                reply.setContentObject(getACLMessage().getContentObject().toString()
                                       + ", Responder.reply()");
                getAgent().send(reply);
            }
        }
    }

    private static class O2Agent extends Agent {
        private SemaphoreByToken semaphore;
        private Object a02AValue;


        O2Agent(SemaphoreByToken semaphore) {
            this.semaphore = semaphore;
        }


        @Override
        protected void setup() {
            setEnabledO2ACommunication(true, 100);
            addBehaviour(new O2AgentBehaviour(this));
            semaphore.release(SETUP);
        }


        @Override
        protected void tearDown() {
        }


        public Object get02AValue() {
            return a02AValue;
        }
    }

    public static class O2AgentBehaviour extends Behaviour {
        private O2Agent o2Agent;


        public O2AgentBehaviour(O2Agent o2Agent) {
            this.o2Agent = o2Agent;
        }


        @Override
        public void action() {
            Object value = getAgent().getO2AObject();
            if (value == null) {
                block();
                return;
            }
            o2Agent.a02AValue = value;
            o2Agent.semaphore.release(ACTION);
        }


        @Override
        public boolean done() {
            return o2Agent.a02AValue != null;
        }
    }
}
