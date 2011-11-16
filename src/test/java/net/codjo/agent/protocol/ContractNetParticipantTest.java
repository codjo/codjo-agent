package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import static net.codjo.agent.Behaviour.thatNeverFails;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.test.AgentAssert;
import net.codjo.agent.test.DummyAgent;
import net.codjo.agent.test.Story;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
/**
 * Classe de test de {@link ContractNetParticipant}.
 */
public class ContractNetParticipantTest extends TestCase {
    private LogString log = new LogString();
    private Story story = new Story();
    private ContractNetParticipant participantBehaviour;


    public void test_nominal() throws Exception {
        nominalTestImpl(FailSafe.NO);
    }


    private void nominalTestImpl(FailSafe failSafe) throws ContainerFailureException {
        HandlerMock handler = new HandlerMock();
        handler.mockResponsesContent("j'ai une super promo");
        handler.mockResultContent("la promo se trouve a Auchan");

        story.record().startAgent("participant", createParticipant(handler, failSafe));

        story.record().startTester("initiator")
              .sendMessage(createCfpMessage(new Aid("participant"), "proposal"))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.PROPOSE))
              .replyWith(AclMessage.Performative.ACCEPT_PROPOSAL, "ok j'accepte")
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.INFORM))
              .assertReceivedMessage(MessageTemplate.matchContent("la promo se trouve a Auchan"));

        story.execute();

        log.assertContent("prepareResponse(cfp[proposal]), "
                          + "prepareResultNotification(cfp[proposal], propose[j'ai une super promo], accept[ok j'accepte])");
        assertFalse(participantBehaviour.done());
    }


    public void test_refuseProposal() throws Exception {

        HandlerMock handler = new HandlerMock();
        handler.mockRefuseProposal("pas de proposition");

        story.record().startAgent("participant", createParticipant(handler));

        story.record().startTester("initiator")
              .sendMessage(createCfpMessage(new Aid("participant"), "proposal"))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REFUSE));

        story.execute();
        assertFalse(participantBehaviour.done());
    }


    public void test_rejectProposal() throws Exception {

        HandlerMock handler = new HandlerMock();
        handler.mockResponsesContent("j'ai une super promo");

        story.record().startAgent("participant", createParticipant(handler));

        story.record().startTester("initiator")
              .sendMessage(createCfpMessage(new Aid("participant"), "proposal"))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.PROPOSE))
              .replyWith(AclMessage.Performative.REJECT_PROPOSAL, "aucun interet");

        story.record().addAssert(AgentAssert.log(log,
                                                 "prepareResponse(cfp[proposal]), "
                                                 + "handleRejectProposal(cfp[proposal], "
                                                 + "propose[j'ai une super promo], "
                                                 + "rejectProposal[aucun interet])"));

        story.execute();

        assertFalse(participantBehaviour.done());
    }


    public void test_neverFaisWrapping_nominal() throws Exception {
        nominalTestImpl(FailSafe.YES);
    }


    public void test_neverFailsWrapping() throws Exception {
        HandlerMock handler = new HandlerMock() {
            @Override
            public AclMessage prepareResponse(AclMessage cfp) throws NotUnderstoodException, RefuseException {
                throw new NullPointerException();
            }
        };

        story.record().startAgent("participant", createParticipant(handler, FailSafe.YES));

        story.record().startTester("initiator")
              .sendMessage(createCfpMessage(new Aid("participant"), "proposal"))
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REFUSE));

        story.execute();
    }


    public void test_defaultTemplateMessage() throws Exception {
        MessageTemplate template = ContractNetParticipant.createMessageTemplate();

        AclMessage message = new AclMessage(AclMessage.Performative.INFORM);

        assertFalse(template.match(message));

        message.setPerformative(AclMessage.Performative.CFP);
        assertFalse(template.match(message));

        message.setProtocol(ContractNetProtocol.ID);
        assertTrue(template.match(message));
    }


    @Override
    protected void setUp() throws Exception {
        story.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
    }


    private AclMessage createCfpMessage(Aid receiver, String content) {
        AclMessage cfp = new AclMessage(AclMessage.Performative.CFP);
        cfp.setProtocol(ContractNetProtocol.ID);
        cfp.addReceiver(receiver);
        cfp.setContent(content);
        cfp.setConversationId("test-" + System.currentTimeMillis());
        return cfp;
    }


    private Agent createParticipant(ContractNetParticipant.Handler handler) {
        return createParticipant(handler, FailSafe.NO);
    }


    private Agent createParticipant(ContractNetParticipant.Handler handler, FailSafe type) {
        Agent agent = new DummyAgent();
        participantBehaviour = new ContractNetParticipant(agent, handler);
        if (type == FailSafe.YES) {
            agent.addBehaviour(thatNeverFails(participantBehaviour));
        }
        else {
            agent.addBehaviour(participantBehaviour);
        }
        return agent;
    }


    enum FailSafe {
        YES,
        NO
    }

    private class HandlerMock implements ContractNetParticipant.Handler {
        private String responseContent;
        private String resultContent;
        private String refuseProposal;


        public AclMessage prepareResponse(AclMessage cfp) throws NotUnderstoodException, RefuseException {
            log.call("prepareResponse", "cfp[" + cfp.getContent() + "]");

            if (refuseProposal != null) {
                throw new RefuseException(refuseProposal);
            }

            AclMessage propose = cfp.createReply(AclMessage.Performative.PROPOSE);
            propose.setContent(responseContent);
            return propose;
        }


        public void handleRejectProposal(AclMessage cfp, AclMessage propose, AclMessage rejectProposal) {
            log.call("handleRejectProposal",
                     "cfp[" + cfp.getContent() + "]",
                     "propose[" + propose.getContent() + "]",
                     "rejectProposal[" + rejectProposal.getContent() + "]");
        }


        public AclMessage prepareResultNotification(AclMessage cfp, AclMessage propose, AclMessage accept)
              throws FailureException {
            log.call("prepareResultNotification",
                     "cfp[" + cfp.getContent() + "]",
                     "propose[" + propose.getContent() + "]",
                     "accept[" + accept.getContent() + "]");
            AclMessage result = accept.createReply(AclMessage.Performative.INFORM);
            result.setContent(resultContent);
            return result;
        }


        public void handleOutOfSequence(AclMessage cfp, AclMessage propose, AclMessage outOfSequenceMsg) {
            log.call("handleOutOfSequence",
                     "cfp[" + cfp.getContent() + "]",
                     "propose[" + propose.getContent() + "]",
                     "outOfSequenceMsg[" + outOfSequenceMsg.getContent() + "]");
        }


        public void mockResponsesContent(String content) {
            this.responseContent = content;
        }


        public void mockResultContent(String content) {
            this.resultContent = content;
        }


        public void mockRefuseProposal(String message) {
            this.refuseProposal = message;
        }
    }
}