package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.AclMessage.Performative;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.test.AgentAssert;
import net.codjo.agent.test.DummyAgent;
import net.codjo.agent.test.SemaphoreByToken;
import net.codjo.agent.test.Story;
import net.codjo.test.common.LogString;
import java.util.List;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.agent.protocol.ContractNetInitiator}.
 */
public class ContractNetInitiatorTest extends TestCase {
    private Story story = new Story();
    private LogString log = new LogString();
    private ContractNetInitiator initiator;


    public void test_nominal() throws Exception {
        story.record().startTester("responder-a")
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.CFP))
              .assertReceivedMessage(MessageTemplate.matchProtocol(ContractNetProtocol.ID))
              .replyWith(AclMessage.Performative.PROPOSE, "proposition-from-a")
              .then()
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.ACCEPT_PROPOSAL))
              .replyWith(AclMessage.Performative.INFORM, "done");

        ContractInitiatorHandlerMock handler = new ContractInitiatorHandlerMock(log);
        handler.mockAcceptProposalInHandleAllResponses();
        story.record().startAgent("initiator", createInitiator(handler, createCFPMessage("responder-a")));

        story.execute();

        handler.waitForHandleAllResultNotifications();
        log.assertContent("handlePropose(proposition-from-a, Acceptances(0))"
                          + ", handleAllResponses(unmodifiableList[proposition-from-a], Acceptances(0))"
                          + ", handleInform(done)"
                          + ", handleAllResultNotifications(unmodifiableList[done])");
        assertInitatorHasBeenDone();
    }


    public void test_initiatorRejectProposal() throws Exception {
        story.record().startTester("responder-a")
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.CFP))
              .replyWith(AclMessage.Performative.PROPOSE, "proposition-from-a")
              .then()
              .receiveMessage()
              .assertReceivedMessage(
                    MessageTemplate.matchPerformative(AclMessage.Performative.REJECT_PROPOSAL));

        ContractInitiatorHandlerMock handler = new ContractInitiatorHandlerMock(log);
        handler.mockRefuseProposalInHandlePropose();
        story.record().startAgent("initiator", createInitiator(handler, createCFPMessage("responder-a")));

        story.execute();

        log.assertContent("handlePropose(proposition-from-a, Acceptances(0))"
                          + ", handleAllResponses(unmodifiableList[proposition-from-a], Acceptances(1))");
        assertInitatorHasBeenDone();
    }


    public void test_responderRefuseCfp() throws Exception {

        story.record().startTester("responder-a")
              .receiveMessage(MessageTemplate.matchPerformative(AclMessage.Performative.CFP))
              .replyWith(AclMessage.Performative.REFUSE, "refuse-from-a");

        ContractInitiatorHandlerMock handler = new ContractInitiatorHandlerMock(log);
        story.record().startAgent("initiator", createInitiator(handler, createCFPMessage("responder-a")));

        story.execute();

        handler.waitForHandleAllResponses();

        log.assertContent("handleRefuse(refuse-from-a)"
                          + ", handleAllResponses(unmodifiableList[refuse-from-a], Acceptances(0))");

        assertInitatorHasBeenDone();
    }


    public void test_conversationId() throws Exception {
        AclMessage message = new AclMessage(AclMessage.Performative.CFP);
        message.setConversationId("my-conv-id");

        new ContractNetInitiator(new DummyAgent(),
                                 new ContractInitiatorHandlerMock(log),
                                 message);
        assertEquals("my-conv-id", message.getConversationId());
    }


    public void test_setConversationIdIfUndefined() throws Exception {
        AclMessage message = new AclMessage(AclMessage.Performative.CFP);

        new ContractNetInitiator(new DummyAgent(),
                                 new ContractInitiatorHandlerMock(log),
                                 message);
        assertNotNull(message.getConversationId());
    }


    public void test_neverFailsWrapping_unsupported() throws Exception {
        createInitiator(new ContractInitiatorHandlerMock(log), new AclMessage(Performative.CFP));

        try {
            new DummyAgent().addBehaviour(Behaviour.thatNeverFails(initiator));
            fail();
        }
        catch (UnsupportedOperationException ex) {
            ;
        }
    }


    @Override
    protected void setUp() throws Exception {
        story.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
    }


    private void assertInitatorHasBeenDone() {
        story.getAgentContainerFixture().assertUntilOk(AgentAssert.behaviourDone(initiator));
    }


    private DummyAgent createInitiator(ContractInitiatorHandlerMock handler, AclMessage cfpMessage) {
        DummyAgent initiatorAgent = new DummyAgent();
        initiator = new ContractNetInitiator(initiatorAgent, handler, cfpMessage);
        initiatorAgent.addBehaviour(initiator);
        return initiatorAgent;
    }


    private AclMessage createCFPMessage(String destAgentName) {
        AclMessage cfp = new AclMessage(AclMessage.Performative.CFP);
        cfp.addReceiver(new Aid(destAgentName));
        cfp.setProtocol(ContractNetProtocol.ID);
        return cfp;
    }


    private static class ContractInitiatorHandlerMock
          extends InitiatorHandlerMock
          implements ContractNetInitiator.Handler {
        private static final SemaphoreByToken.Token HANDLE_ALL_RESULT_NOTIFICATIONS =
              new SemaphoreByToken.Token("HANDLE_ALL_RESULT_NOTIFICATIONS");
        private static final SemaphoreByToken.Token HANDLE_ALL_RESPONSES =
              new SemaphoreByToken.Token("handleAllResponses");
        private boolean acceptProposalInHandleAllResponses = false;
        private boolean refuseProposalInHandlePropose = false;


        ContractInitiatorHandlerMock(LogString log) {
            super(log);
        }


        public void handlePropose(AclMessage propose,
                                  ContractNetInitiator.Acceptances acceptances) {
            log.call("handlePropose", propose.getContent(), "Acceptances(" + acceptances.size() + ")");
            if (refuseProposalInHandlePropose) {
                acceptances.rejectProposal(propose);
            }
        }


        public void handleAllResponses(List<AclMessage> responses,
                                       ContractNetInitiator.Acceptances acceptances) {
            log.call("handleAllResponses", toString(responses), "Acceptances(" + acceptances.size() + ")");
            if (acceptProposalInHandleAllResponses) {
                for (AclMessage response : responses) {
                    acceptances.acceptProposal(response);
                }
            }
            semaphore.release(HANDLE_ALL_RESPONSES);
        }


        @Override
        public void handleRefuse(AclMessage refuse) {
            log.call("handleRefuse", refuse.getContent());
        }


        @Override
        public void handleInform(AclMessage inform) {
            log.call("handleInform", inform.getContent());
        }


        public void handleAllResultNotifications(List<AclMessage> resultNotifications) {
            log.call("handleAllResultNotifications", toString(resultNotifications));
            semaphore.release(HANDLE_ALL_RESULT_NOTIFICATIONS);
        }


        public void waitForHandleAllResultNotifications() {
            semaphore.waitFor(HANDLE_ALL_RESULT_NOTIFICATIONS);
        }


        public void waitForHandleAllResponses() {
            semaphore.waitFor(HANDLE_ALL_RESPONSES);
        }


        private StringBuilder toString(List<AclMessage> responses) {
            StringBuilder buffer = new StringBuilder();
            for (AclMessage message : responses) {
                if (buffer.length() != 0) {
                    buffer.append(", ");
                }
                buffer.append(message.getContent());
            }

            String type = determineListType(responses);

            return buffer.insert(0, type + "[").append("]");
        }


        private String determineListType(List<AclMessage> responses) {
            try {
                AclMessage message = new AclMessage(AclMessage.Performative.AGREE);
                responses.add(message);
                responses.remove(message);
            }
            catch (UnsupportedOperationException ex) {
                return "unmodifiableList";
            }
            return "";
        }


        public void mockAcceptProposalInHandleAllResponses() {
            acceptProposalInHandleAllResponses = true;
        }


        public void mockRefuseProposalInHandlePropose() {
            refuseProposalInHandlePropose = true;
        }
    }
}
