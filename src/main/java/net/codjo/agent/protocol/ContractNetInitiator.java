package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
import net.codjo.agent.JadeWrapper;
import net.codjo.agent.util.IdUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
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
 * @see ContractNetInitiator
 */
public class ContractNetInitiator extends Behaviour {
    private final Handler initiatorHandler;
    private final ContractNetInitiatorAdapter adaptee;


    public ContractNetInitiator(Agent initiatorAgent, Handler handler, AclMessage callForProposal) {
        if (callForProposal.getConversationId() == null) {
            callForProposal.setConversationId(String.format("cfp-%s", IdUtil.createUniqueId(initiatorAgent)));
        }
        initiatorHandler = handler;
        adaptee = new ContractNetInitiatorAdapter(initiatorAgent, callForProposal);
        JadeWrapper.wrapp(this, adaptee);
    }


    @Override
    protected final void action() {
    }


    @Override
    public boolean done() {
        return adaptee.done();
    }


    public static interface Handler {

        void handlePropose(AclMessage propose, Acceptances acceptances);


        void handleRefuse(AclMessage aclMessage);


        void handleAllResponses(List<AclMessage> responses, Acceptances acceptances);


        void handleFailure(AclMessage aclMessage);


        void handleInform(AclMessage aclMessage);


        void handleAllResultNotifications(List<AclMessage> resultNotifications);

// ------- From Std
//        public void handleAgree(AclMessage agree);


        public void handleOutOfSequence(AclMessage outOfSequenceMessage);


        public void handleNotUnderstood(AclMessage notUnderstoodMessage);
    }
    public abstract static class AbstractHandler implements Handler {
        public void handlePropose(AclMessage propose, Acceptances acceptances) {
        }


        public void handleRefuse(AclMessage aclMessage) {
        }


        public void handleAllResponses(List<AclMessage> responses, Acceptances acceptances) {
        }


        public void handleFailure(AclMessage aclMessage) {
        }


        public void handleInform(AclMessage aclMessage) {
        }


        public void handleAllResultNotifications(List<AclMessage> resultNotifications) {
        }


        public void handleOutOfSequence(AclMessage outOfSequenceMessage) {
        }


        public void handleNotUnderstood(AclMessage notUnderstoodMessage) {
        }
    }
    public interface Acceptances {
        public AclMessage acceptProposal(AclMessage propose);


        public AclMessage rejectProposal(AclMessage propose);


        int size();
    }
    private class ContractNetInitiatorAdapter extends jade.proto.ContractNetInitiator {
        ContractNetInitiatorAdapter(Agent agent, AclMessage message) {
            super(JadeWrapper.unwrapp(agent), JadeWrapper.unwrapp(message));
        }


        @Override
        protected void handleNotUnderstood(jade.lang.acl.ACLMessage notUnderstood) {
            initiatorHandler.handleNotUnderstood(JadeWrapper.wrapp(notUnderstood));
        }


        @Override
        protected void handleOutOfSequence(jade.lang.acl.ACLMessage outOfSequenceMessage) {
            initiatorHandler.handleOutOfSequence(JadeWrapper.wrapp(outOfSequenceMessage));
        }


        /**
         * @noinspection CollectionDeclaredAsConcreteClass,UseOfObsoleteCollectionType
         */
        @Override
        protected void handlePropose(jade.lang.acl.ACLMessage propose, Vector acceptances) {
            initiatorHandler.handlePropose(JadeWrapper.wrapp(propose),
                                           new AcceptancesImpl(acceptances));
        }


        @Override
        protected void handleRefuse(jade.lang.acl.ACLMessage refuse) {
            initiatorHandler.handleRefuse(JadeWrapper.wrapp(refuse));
        }


        @Override
        protected void handleFailure(jade.lang.acl.ACLMessage failure) {
            initiatorHandler.handleFailure(JadeWrapper.wrapp(failure));
        }


        @Override
        protected void handleInform(jade.lang.acl.ACLMessage inform) {
            initiatorHandler.handleInform(JadeWrapper.wrapp(inform));
        }


        /**
         * @noinspection CollectionDeclaredAsConcreteClass,UseOfObsoleteCollectionType
         */
        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            initiatorHandler.handleAllResponses(Collections.unmodifiableList(toAgfMessages(responses)),
                                                new AcceptancesImpl(acceptances));
        }


        /**
         * @noinspection CollectionDeclaredAsConcreteClass,UseOfObsoleteCollectionType
         */
        @Override
        protected void handleAllResultNotifications(Vector resultNotifications) {
            initiatorHandler.handleAllResultNotifications(
                  Collections.unmodifiableList(toAgfMessages(resultNotifications)));
        }


        private List<AclMessage> toAgfMessages(List responses) {
            List<AclMessage> agfResponses = new ArrayList<AclMessage>(responses.size());
            for (Object response : responses) {
                agfResponses.add(JadeWrapper.wrapp((jade.lang.acl.ACLMessage)response));
            }
            return agfResponses;
        }
    }
    private static class AcceptancesImpl implements Acceptances {
        private final List<jade.lang.acl.ACLMessage> jadeAcceptances;


        AcceptancesImpl(List jadeAcceptances) {
            //noinspection unchecked
            this.jadeAcceptances = (List<jade.lang.acl.ACLMessage>)jadeAcceptances;
        }


        public AclMessage acceptProposal(AclMessage propose) {
            AclMessage reply = propose.createReply(AclMessage.Performative.ACCEPT_PROPOSAL);
            jadeAcceptances.add(JadeWrapper.unwrapp(reply));
            return reply;
        }


        public AclMessage rejectProposal(AclMessage propose) {
            AclMessage reply = propose.createReply(AclMessage.Performative.REJECT_PROPOSAL);
            jadeAcceptances.add(JadeWrapper.unwrapp(reply));
            return reply;
        }


        public int size() {
            return jadeAcceptances.size();
        }
    }
}
