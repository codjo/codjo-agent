/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Agent Dummy capturant le premier message qui lui est envoyé.
 *
 * @see AgentTest#test_send() Pour un cas d'utilisation
 */
class JadeResponderAgentMock extends jade.core.Agent {
    private final Object synchronizer = new Object();
    private jade.lang.acl.ACLMessage receivedMessage = null;


    @Override
    public void setup() {
        addBehaviour(new ReceiveAndStoreMessageBehavior());
    }


    public jade.lang.acl.ACLMessage waitReceivedMessage(int timeout)
          throws InterruptedException {
        synchronized (synchronizer) {
            synchronizer.wait(timeout);
        }
        return receivedMessage;
    }


    private class ReceiveAndStoreMessageBehavior extends jade.core.behaviours.Behaviour {
        @Override
        public void action() {
            jade.lang.acl.ACLMessage aclMessage = myAgent.receive();
            if (aclMessage == null) {
                block();
                return;
            }

            synchronized (synchronizer) {
                receivedMessage = aclMessage;
                synchronizer.notifyAll();
            }
            myAgent.doDelete();
        }


        @Override
        public boolean done() {
            return receivedMessage != null;
        }
    }
}
