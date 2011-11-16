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
import net.codjo.agent.MessageTemplate;
/**
 * Agent 'Participant' du protocole 'fipa-request' / 'fipa-query'.
 *
 * @see net.codjo.agent.protocol.RequestProtocol
 */
public class RequestParticipant extends Behaviour {
    private RequestParticipantHandler participantHandler;


    public RequestParticipant(Agent participantAgent, RequestParticipantHandler handler,
                              MessageTemplate template) {
        this.participantHandler = handler;

        jade.proto.AchieveREResponder responder =
              new ParticipantBehaviorAdapter(participantAgent, template);

        JadeWrapper.wrapp(this, responder);
    }


    @Override
    protected final void action() {
    }


    @Override
    public final boolean done() {
        return false;
    }


    private class ParticipantBehaviorAdapter extends jade.proto.AchieveREResponder implements Unbreakable {
        ParticipantBehaviorAdapter(Agent agent, MessageTemplate template) {
            super(JadeWrapper.unwrapp(agent), JadeWrapper.unwrapp(template));
        }


        @Override
        protected jade.lang.acl.ACLMessage handleRequest(jade.lang.acl.ACLMessage aclMessage)
              throws NotUnderstoodException, RefuseException {
            return JadeWrapper.unwrapp(participantHandler.handleRequest(JadeWrapper.wrapp(aclMessage)));
        }


        @Override
        protected jade.lang.acl.ACLMessage prepareResultNotification(
              jade.lang.acl.ACLMessage request,
              jade.lang.acl.ACLMessage response) throws FailureException {
            return JadeWrapper.unwrapp(participantHandler.executeRequest(
                  JadeWrapper.wrapp(request),
                  JadeWrapper.wrapp(response)));
        }


        public jade.core.behaviours.Behaviour makeMeUnbreakable(UncaughtErrorHandler handler) {
            participantHandler = new FailSafeWrapper(participantHandler, handler);
            return this;
        }
    }
    private class FailSafeWrapper implements RequestParticipantHandler {
        private RequestParticipantHandler handler;
        private UncaughtErrorHandler uncaughtErrorHandler;


        FailSafeWrapper(RequestParticipantHandler handler, UncaughtErrorHandler uncaughtErrorHandler) {
            this.handler = handler;
            this.uncaughtErrorHandler = uncaughtErrorHandler;
        }


        public AclMessage executeRequest(AclMessage request, AclMessage agreement) throws FailureException {
            try {
                return handler.executeRequest(request, agreement);
            }
            catch (Throwable e) {
                uncaughtErrorHandler.handle(e, RequestParticipant.this);
                throw new FailureException(internalMessageError(e));
            }
        }


        public AclMessage handleRequest(AclMessage request) throws RefuseException, NotUnderstoodException {
            try {
                return handler.handleRequest(request);
            }
            catch (Throwable e) {
                uncaughtErrorHandler.handle(e, RequestParticipant.this);
                throw new NotUnderstoodException(internalMessageError(e));
            }
        }
    }
}
