/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
import net.codjo.agent.JadeWrapper;
/**
 * Agent 'Initiateur' du protocole 'fipa-request' / 'fipa-query'.
 *
 * @see net.codjo.agent.protocol.RequestProtocol
 */
public class RequestInitiator extends Behaviour {
    private InitiatorHandler initiatorHandler;
    private InitiatorBehaviorAdapter behaviorAdapter;


    public RequestInitiator(Agent initiatorAgent, InitiatorHandler initiatorHandler,
                            AclMessage requestMessage) {
        this.initiatorHandler = initiatorHandler;
        behaviorAdapter = new InitiatorBehaviorAdapter(initiatorAgent, requestMessage);
        JadeWrapper.wrapp(this, behaviorAdapter);
    }


    @Override
    protected final void action() {
    }


    @Override
    public final boolean done() {
        return behaviorAdapter.done();
    }


    private class InitiatorBehaviorAdapter extends jade.proto.AchieveREInitiator implements Unbreakable {
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


        public jade.core.behaviours.Behaviour makeMeUnbreakable(UncaughtErrorHandler handler) {
            initiatorHandler = new FailSafeWrapper(initiatorHandler, handler);
            return this;
        }
    }
    private class FailSafeWrapper implements InitiatorHandler {
        private InitiatorHandler handler;
        private UncaughtErrorHandler uncaughtErrorHandler;


        private FailSafeWrapper(InitiatorHandler handler, UncaughtErrorHandler uncaughtErrorHandler) {
            this.handler = handler;
            this.uncaughtErrorHandler = uncaughtErrorHandler;
        }


        public void handleAgree(AclMessage agree) {
            try {
                handler.handleAgree(agree);
            }
            catch (Throwable e) {
                uncaughtErrorHandler.handle(e, RequestInitiator.this);
            }
        }


        public void handleRefuse(AclMessage refuse) {
            try {
                handler.handleRefuse(refuse);
            }
            catch (Throwable e) {
                uncaughtErrorHandler.handle(e, RequestInitiator.this);
            }
        }


        public void handleInform(AclMessage inform) {
            try {
                handler.handleInform(inform);
            }
            catch (Throwable e) {
                uncaughtErrorHandler.handle(e, RequestInitiator.this);
            }
        }


        public void handleFailure(AclMessage failure) {
            try {
                handler.handleFailure(failure);
            }
            catch (Throwable e) {
                uncaughtErrorHandler.handle(e, RequestInitiator.this);
            }
        }


        public void handleOutOfSequence(AclMessage outOfSequenceMessage) {
            try {
                handler.handleOutOfSequence(outOfSequenceMessage);
            }
            catch (Throwable e) {
                uncaughtErrorHandler.handle(e, RequestInitiator.this);
            }
        }


        public void handleNotUnderstood(AclMessage notUnderstoodMessage) {
            try {
                handler.handleNotUnderstood(notUnderstoodMessage);
            }
            catch (Throwable e) {
                uncaughtErrorHandler.handle(e, RequestInitiator.this);
            }
        }
    }
}
