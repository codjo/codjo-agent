package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
import net.codjo.agent.JadeWrapper;
import net.codjo.agent.MessageTemplate;
import org.apache.log4j.Logger;
/**
 * Agent 'Initiateur' du protocole 'fipa-contract-net'.
 *
 * <p> <b> NB</b> : Le slot reply-by permet de positionner un timeout sur l'attente de reception d'une offre.
 * <pre>
 *   // We want to receive a reply in 10 secs
 *   msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
 * </pre>
 * </p>
 *
 * @see net.codjo.agent.protocol.ContractNetParticipant
 */
public class ContractNetParticipant extends Behaviour {
    private ContractNetParticipant.Handler handler;
    private final ContractNetParticipant.ParticipantBehaviorAdapter adaptee;


    public ContractNetParticipant(Agent participantAgent,
                                  ContractNetParticipant.Handler handler) {
        this(participantAgent, handler, createMessageTemplate());
    }


    public ContractNetParticipant(Agent participantAgent,
                                  ContractNetParticipant.Handler handler,
                                  MessageTemplate template) {
        this.handler = handler;
        adaptee = new ContractNetParticipant.ParticipantBehaviorAdapter(participantAgent, template);
        JadeWrapper.wrapp(this, adaptee);
    }


    @Override
    protected final void action() {
    }


    @Override
    public boolean done() {
        return adaptee.done();
    }


    public static MessageTemplate createMessageTemplate() {
        return MessageTemplate.and(MessageTemplate.matchProtocol(ContractNetProtocol.ID),
                                   MessageTemplate.matchPerformative(AclMessage.Performative.CFP));
    }


    public static interface Handler {
        AclMessage prepareResponse(AclMessage cfp) throws NotUnderstoodException, RefuseException;


        void handleRejectProposal(AclMessage cfp, AclMessage propose, AclMessage rejectProposal);


        AclMessage prepareResultNotification(AclMessage cfp, AclMessage propose, AclMessage accept)
              throws FailureException;


        void handleOutOfSequence(AclMessage cfp, AclMessage propose, AclMessage outOfSequenceMsg);
    }

    private class ParticipantBehaviorAdapter extends jade.proto.ContractNetResponder implements Unbreakable {
        ParticipantBehaviorAdapter(Agent agent, MessageTemplate template) {
            super(JadeWrapper.unwrapp(agent), JadeWrapper.unwrapp(template));
        }


        @Override
        protected jade.lang.acl.ACLMessage handleCfp(jade.lang.acl.ACLMessage request)
              throws jade.domain.FIPAAgentManagement.NotUnderstoodException,
                     jade.domain.FIPAAgentManagement.RefuseException {
            if (request.getConversationId() == null) {
                Logger.getLogger(ContractNetParticipant.class)
                      .warn("No conversation Id set : " + request.toString());
            }
            return JadeWrapper.unwrapp(handler.prepareResponse(JadeWrapper.wrapp(request)));
        }


        @Override
        protected void handleRejectProposal(jade.lang.acl.ACLMessage cfp,
                                            jade.lang.acl.ACLMessage propose,
                                            jade.lang.acl.ACLMessage rejectProposal) {
            handler.handleRejectProposal(JadeWrapper.wrapp(cfp),
                                         JadeWrapper.wrapp(propose),
                                         JadeWrapper.wrapp(rejectProposal));
        }


        @Override
        protected jade.lang.acl.ACLMessage handleAcceptProposal(jade.lang.acl.ACLMessage cfp,
                                                                jade.lang.acl.ACLMessage propose,
                                                                jade.lang.acl.ACLMessage accept)
              throws jade.domain.FIPAAgentManagement.FailureException {
            return JadeWrapper.unwrapp(handler.prepareResultNotification(JadeWrapper.wrapp(cfp),
                                                                         JadeWrapper.wrapp(propose),
                                                                         JadeWrapper.wrapp(accept)));
        }


        @Override
        protected void handleOutOfSequence(jade.lang.acl.ACLMessage cfp,
                                           jade.lang.acl.ACLMessage propose,
                                           jade.lang.acl.ACLMessage outOfSequenceMsg) {
            handler.handleOutOfSequence(JadeWrapper.wrapp(cfp),
                                        JadeWrapper.wrapp(propose),
                                        JadeWrapper.wrapp(outOfSequenceMsg));
        }


        public jade.core.behaviours.Behaviour makeMeUnbreakable(UncaughtErrorHandler uncaughtErrorHandler) {
            handler = new FailSafeWrapper(handler, uncaughtErrorHandler);
            return this;
        }
    }
    private class FailSafeWrapper implements Handler {
        private Handler handler;
        private UncaughtErrorHandler uncaughtErrorHandler;


        FailSafeWrapper(Handler handler, UncaughtErrorHandler uncaughtErrorHandler) {
            this.handler = handler;
            this.uncaughtErrorHandler = uncaughtErrorHandler;
        }


        public AclMessage prepareResponse(AclMessage cfp) throws NotUnderstoodException, RefuseException {
            try {
                return handler.prepareResponse(cfp);
            }
            catch (NotUnderstoodException e) {
                throw e;
            }
            catch (RefuseException e) {
                throw e;
            }
            catch (Throwable error) {
                uncaughtErrorHandler.handle(error, ContractNetParticipant.this);
                throw new RefuseException(internalMessageError(error));
            }
        }


        public void handleRejectProposal(AclMessage cfp, AclMessage propose, AclMessage rejectProposal) {
            try {
                handler.handleRejectProposal(cfp, propose, rejectProposal);
            }
            catch (Throwable error) {
                uncaughtErrorHandler.handle(error, ContractNetParticipant.this);
            }
        }


        public AclMessage prepareResultNotification(AclMessage cfp, AclMessage propose, AclMessage accept)
              throws FailureException {
            try {
                return handler.prepareResultNotification(cfp, propose, accept);
            }
            catch (FailureException e) {
                throw e;
            }
            catch (Throwable error) {
                uncaughtErrorHandler.handle(error, ContractNetParticipant.this);
                throw new FailureException(internalMessageError(error));
            }
        }


        public void handleOutOfSequence(AclMessage cfp, AclMessage propose, AclMessage outOfSequenceMsg) {
            try {
                handler.handleOutOfSequence(cfp, propose, outOfSequenceMsg);
            }
            catch (Exception error) {
                uncaughtErrorHandler.handle(error, ContractNetParticipant.this);
            }
        }
    }
}
