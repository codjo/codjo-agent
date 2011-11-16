/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.AclMessage.Performative;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.MessageTemplate;
import static net.codjo.agent.MessageTemplate.matchAll;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.DummyAgent;
import static net.codjo.agent.test.MessageBuilder.message;
import net.codjo.agent.test.Semaphore;
import net.codjo.agent.test.Story.ConnectionType;
import net.codjo.agent.test.TesterAgent;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
/**
 * Classe de test de {@link RequestParticipant}.
 */
public class RequestParticipantTest extends TestCase {
    private LogString log;
    private ParticipantHandlerMock handlerMock;
    private AgentContainerFixture fixture = new AgentContainerFixture();
    private TesterAgent initiator = new TesterAgent();
    private Agent participant = new DummyAgent();


    @Override
    protected void setUp() throws Exception {
        log = new LogString();
        handlerMock =
              new ParticipantHandlerMock(new LogString("participantHandler", log));
        fixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }


    public void test_receive_request() throws Exception {
        initializeProtocol();

        handlerMock.waitForMessage();

        log.assertContent("participantHandler.handleRequest(aclMessage(REQUEST))");
    }


    public void test_send_refuse() throws Exception {
        handlerMock.mockRegisterError(new RefuseException("refuse-content"));

        initializeProtocol();

        assertReceivedMessage(AclMessage.Performative.REFUSE, "(refuse-content)");
    }


    public void test_send_notUnderstood() throws Exception {
        handlerMock.mockRegisterError(new NotUnderstoodException("notUnderstood-content"));

        initializeProtocol();

        assertReceivedMessage(AclMessage.Performative.NOT_UNDERSTOOD, "(notUnderstood-content)");
    }


    public void test_send_agree() throws Exception {
        handlerMock.mockRegisterAgree("agree-content");

        initializeProtocol();

        assertReceivedMessage(AclMessage.Performative.AGREE, "agree-content");
    }


    public void test_send_result() throws Exception {
        handlerMock.mockHandleExecuteResult("c'est fait !");

        initializeProtocol();

        assertReceivedMessage(AclMessage.Performative.INFORM, "c'est fait !");
    }


    public void test_send_resultFailure() throws Exception {
        handlerMock.mockHandleExecuteFailure(new FailureException("c'est foutu !"));

        initializeProtocol();
        assertReceivedMessage(AclMessage.Performative.FAILURE, "(c'est foutu !)");
    }


    public void test_neverFailsWrapping_nominal() throws Exception {
        fixture.startContainer(ConnectionType.NO_CONNECTION);

        fixture.startNewAgent("initiator", initiator);

        participant.addBehaviour(Behaviour.thatNeverFails(
              new RequestParticipant(this.participant, handlerMock, matchAll())));

        fixture.startNewAgent("participant", this.participant);

        initiator.send(message(Performative.REQUEST).to("participant").get());

        handlerMock.waitForMessage();

        log.assertContent("participantHandler.handleRequest(aclMessage(REQUEST))");
    }


    private void initializeProtocol() throws ContainerFailureException {
        fixture.startContainer(ConnectionType.NO_CONNECTION);
        fixture.startNewAgent("initiator", initiator);

        MessageTemplate template =
              MessageTemplate.and(MessageTemplate.matchProtocol(RequestProtocol.REQUEST),
                                  MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST));

        participant.addBehaviour(new RequestParticipant(this.participant, handlerMock,
                                                        template));
        fixture.startNewAgent("participant", this.participant);

        AclMessage message = new AclMessage(AclMessage.Performative.REQUEST);
        message.setContentObject("request-content");
        message.setProtocol(RequestProtocol.REQUEST);
        message.addReceiver(new Aid("participant"));
        message.setConversationId("initiator_" + System.currentTimeMillis());

        initiator.send(message);
    }


    private void assertReceivedMessage(AclMessage.Performative performative, String content) {
        fixture.assertStringMessage(fixture.receiveMessage(initiator),
                                    RequestProtocol.REQUEST, performative, content);
    }


    private static class ParticipantHandlerMock implements RequestParticipantHandler {
        private final LogString log;
        private Semaphore semaphore = new Semaphore();
        private RefuseException refuse;
        private NotUnderstoodException notUnderstood;
        private String agree;
        private AclMessage requestMessage;
        private FailureException failure;
        private String result;


        ParticipantHandlerMock(LogString log) {
            this.log = log;
        }


        public AclMessage handleRequest(AclMessage request)
              throws RefuseException, NotUnderstoodException {
            requestMessage = request;
            log.call("handleRequest",
                     "aclMessage("
                     + AclMessage.performativeToString(request.getPerformative()) + ")");

            AclMessage responseMessage = null;

            if (refuse != null) {
                throw refuse;
            }
            else if (notUnderstood != null) {
                throw notUnderstood;
            }
            else if (agree != null) {
                responseMessage = requestMessage.createReply(AclMessage.Performative.AGREE);
                responseMessage.setContent(agree);
            }
            semaphore.release();
            return responseMessage;
        }


        public AclMessage executeRequest(AclMessage request, AclMessage agreement)
              throws FailureException {
            if (failure != null) {
                throw failure;
            }
            semaphore.release();
            AclMessage reply = request.createReply(AclMessage.Performative.INFORM);
            reply.setContent(result);
            return reply;
        }


        public void waitForMessage() {
            semaphore.acquire();
        }


        public void mockRegisterError(RefuseException refuseException) {
            this.refuse = refuseException;
        }


        public void mockRegisterError(NotUnderstoodException notUnderstoodException) {
            this.notUnderstood = notUnderstoodException;
        }


        public void mockRegisterAgree(String agreeContent) {
            this.agree = agreeContent;
        }


        public AclMessage getRequestMessage() {
            return requestMessage;
        }


        public void mockHandleExecuteFailure(FailureException failureException) {
            this.failure = failureException;
        }


        public void mockHandleExecuteResult(String executeResult) {
            this.result = executeResult;
        }
    }
}
