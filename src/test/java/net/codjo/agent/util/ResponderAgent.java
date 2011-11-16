/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.util;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.behaviour.CyclicBehaviour;
import net.codjo.agent.test.Semaphore;
/**
 * Agent utilisé dans les tests qui repond dès qu'il recoit un message.
 *
 * @see net.codjo.agent.test.TesterAgent
 */
public class ResponderAgent extends Agent {
    private AclMessage replyTemplate;
    private AclMessage lastReceivedMessage;
    private Semaphore semaphore;


    public ResponderAgent(Semaphore semaphore) {
        this.semaphore = semaphore;
    }


    public ResponderAgent() {
        this(new Semaphore());
    }


    @Override
    protected void setup() {
        addBehaviour(new ParticipantMock());
    }


    public void setReplyTemplate(AclMessage template) {
        replyTemplate = template;
    }


    @Override
    protected void tearDown() {
    }


    public AclMessage getLastReceivedMessage() {
        return lastReceivedMessage;
    }


    private class ParticipantMock extends CyclicBehaviour {
        @Override
        protected void action() {
            AclMessage receive = getAgent().receive();
            if (receive == null) {
                block();
                return;
            }
            lastReceivedMessage = receive;
            semaphore.release();

            if (replyTemplate != null) {
                AclMessage reply = receive.createReply(replyTemplate.getPerformative());
                reply.setContentObject(replyTemplate.getContentObject());
                getAgent().send(reply);
            }
        }
    }
}
