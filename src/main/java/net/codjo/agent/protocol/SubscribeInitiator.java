/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.JadeWrapper;
/**
 * Agent 'Initiateur' du protocole 'fipa-subscribe'.
 *
 * @see net.codjo.agent.protocol.SubscribeProtocol
 */
public class SubscribeInitiator extends Behaviour {
    private final InitiatorHandler initiatorHandler;
    private InitiatorBehaviorAdapter initiatorBehaviorAdapter;


    public SubscribeInitiator(Agent initiatorAgent, InitiatorHandler initiatorHandler,
                              AclMessage subscribe) {
        this.initiatorHandler = initiatorHandler;
        initiatorBehaviorAdapter =
              new InitiatorBehaviorAdapter(initiatorAgent, subscribe);
        JadeWrapper.wrapp(this, initiatorBehaviorAdapter);
    }


    @Override
    protected final void action() {
    }


    @Override
    public final boolean done() {
        return initiatorBehaviorAdapter.done();
    }


    public void cancel(Aid aid, boolean ignoreResponse) {
        initiatorBehaviorAdapter.cancel(JadeWrapper.unwrapp(aid), ignoreResponse);
    }


    private class InitiatorBehaviorAdapter extends jade.proto.SubscriptionInitiator {
        InitiatorBehaviorAdapter(Agent agent, AclMessage message) {
            super(JadeWrapper.unwrapp(agent), JadeWrapper.unwrapp(message));
        }


        @Override
        protected void handleAgree(jade.lang.acl.ACLMessage agree) {
            initiatorHandler.handleAgree(JadeWrapper.wrapp(agree));
        }


        @Override
        protected void handleInform(jade.lang.acl.ACLMessage inform) {
            initiatorHandler.handleInform(JadeWrapper.wrapp(inform));
        }


        @Override
        protected void handleFailure(jade.lang.acl.ACLMessage failure) {
            initiatorHandler.handleFailure(JadeWrapper.wrapp(failure));
        }


        @Override
        protected void handleRefuse(jade.lang.acl.ACLMessage refuse) {
            initiatorHandler.handleRefuse(JadeWrapper.wrapp(refuse));
        }


        @Override
        protected void handleOutOfSequence(jade.lang.acl.ACLMessage outOfSequence) {
            initiatorHandler.handleOutOfSequence(JadeWrapper.wrapp(outOfSequence));
        }


        @Override
        protected void handleNotUnderstood(jade.lang.acl.ACLMessage message) {
            initiatorHandler.handleNotUnderstood(JadeWrapper.wrapp(message));
        }
    }
}
