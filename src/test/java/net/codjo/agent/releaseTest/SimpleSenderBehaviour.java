/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.releaseTest;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Behaviour;
/**
 * {@link Behaviour} simple ne faisant que lancer un message.
 */
class SimpleSenderBehaviour extends Behaviour {
    private AclMessage messageToBeSend;


    SimpleSenderBehaviour(AclMessage messageToBeSend) {
        this.messageToBeSend = messageToBeSend;
    }


    @Override
    protected void action() {
        getAgent().send(messageToBeSend);
    }


    @Override
    public boolean done() {
        return true;
    }
}
