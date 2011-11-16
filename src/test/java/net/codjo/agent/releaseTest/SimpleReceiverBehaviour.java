/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.releaseTest;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Behaviour;
import net.codjo.agent.test.Semaphore;
/**
 */
class SimpleReceiverBehaviour extends Behaviour {
    private AclMessage receivedMessage;
    private Semaphore semaphore;


    SimpleReceiverBehaviour(Semaphore semaphore) {
        this.semaphore = semaphore;
    }


    SimpleReceiverBehaviour() {
    }


    public AclMessage getACLMessage() {
        return receivedMessage;
    }


    @Override
    protected void action() {
        AclMessage message = getAgent().receive();
        if (message == null) {
            block();
            return;
        }
        receivedMessage = message;
        if (semaphore != null) {
            semaphore.release();
        }
    }


    @Override
    public boolean done() {
        return receivedMessage != null;
    }
}
