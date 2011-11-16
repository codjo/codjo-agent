package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.AclMessage.Performative;
import static net.codjo.agent.AclMessage.Performative.REQUEST;
import net.codjo.agent.Agent;
import static net.codjo.agent.Behaviour.thatNeverFails;
import net.codjo.agent.ContainerFailureException;
import static net.codjo.agent.MessageTemplate.and;
import static net.codjo.agent.MessageTemplate.matchContent;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import static net.codjo.agent.MessageTemplate.matchProtocol;
import net.codjo.agent.test.AgentAssert;
import net.codjo.agent.test.AgentAssert.Assertion;
import net.codjo.agent.test.DummyAgent;
import static net.codjo.agent.test.MessageBuilder.message;
import net.codjo.agent.test.Story;
import net.codjo.test.common.LogString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RequestInitiatorTest {
    private LogString log;
    private Story story = new Story();
    private RequestInitiator initiator;
    private InitiatorHandlerMock initiatorHandlerMock;
    private Agent initiatorAgent;


    @Before
    public void setUp() throws Exception {
        log = new LogString();
        initiatorHandlerMock = new InitiatorHandlerMock(new LogString("initiator", log));
        initiatorAgent = new DummyAgent();
        story.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        story.doTearDown();
    }


    @Test
    public void test_send_request() throws Exception {
        story.record().startTester("responder")
              .receiveMessage()
              .assertReceivedMessage(
                    and(and(matchProtocol(RequestProtocol.REQUEST),
                            matchPerformative(REQUEST)),
                        matchContent("content")));

        initiator = new RequestInitiator(initiatorAgent,
                                         initiatorHandlerMock,
                                         message(REQUEST)
                                               .to("responder")
                                               .usingProtocol(RequestProtocol.REQUEST)
                                               .withContent("content")
                                               .get());
        story.record().startAgent("initiator", new DummyAgent(initiator));
        story.execute();
    }


    @Test
    public void test_receive_agree() throws Exception {
        participantStoryBoard(Performative.AGREE,
                              "agree-content",
                              "initiator.handleAgree(agree-content)");
        story.record().addAssert(AgentAssert.behaviourNotDone(initiator));
        story.execute();
    }


    @Test
    public void test_receive_refuse() throws Exception {
        participantStoryBoard(Performative.REFUSE,
                              "refuse-content",
                              "initiator.handleRefuse(refuse-content)");
        story.record().addAssert(AgentAssert.behaviourDone(initiator));
        story.execute();
    }


    @Test
    public void test_receive_inform() throws Exception {
        participantStoryBoard(Performative.INFORM,
                              "inform-content",
                              "initiator.handleInform(inform-content)");
        story.record().addAssert(AgentAssert.behaviourDone(initiator));
        story.execute();
    }


    @Test
    public void test_receive_failure() throws Exception {
        participantStoryBoard(Performative.FAILURE,
                              "handleFailure-content",
                              "initiator.handleFailure(handleFailure-content)");
        story.record().addAssert(AgentAssert.behaviourDone(initiator));
        story.execute();
    }


    @Test
    public void test_receive_notUnderstood() throws Exception {
        participantStoryBoard(Performative.NOT_UNDERSTOOD,
                              "handleOutOfSequence-content",
                              "initiator.handleNotUnderstood(handleOutOfSequence-content)");
        story.record().addAssert(AgentAssert.behaviourDone(initiator));
        story.execute();
    }


    @Test
    public void test_receive_outOfSequence() throws Exception {
        participantStoryBoard(REQUEST,
                              "handleOutOfSequence-content",
                              "initiator.handleOutOfSequence(handleOutOfSequence-content)");
        story.record().addAssert(AgentAssert.behaviourNotDone(initiator));
        story.execute();
    }


    @Test
    public void test_neverFailsWrapping_nominal() throws Exception {
        story.record().startTester("responder")
              .receiveMessage();

        initiator = new RequestInitiator(initiatorAgent,
                                         initiatorHandlerMock,
                                         message(REQUEST)
                                               .to("responder")
                                               .usingProtocol(RequestProtocol.REQUEST)
                                               .get());
        initiatorAgent.addBehaviour(thatNeverFails(initiator));

        story.record().startAgent("initiator", new DummyAgent(initiator));
        story.execute();
    }


    private AclMessage createMessage() {
        return message(REQUEST)
              .to("responder")
              .withContent("content")
              .get();
    }


    private void participantStoryBoard(Performative replyPerformative,
                                       String replyContent,
                                       final String expectedLog) throws ContainerFailureException {
        story.record().startTester("responder")
              .receiveMessage()
              .assertReceivedMessage(matchPerformative(REQUEST))
              .replyWithContent(replyPerformative, replyContent);

        initiator = new RequestInitiator(initiatorAgent, initiatorHandlerMock, createMessage());
        initiatorAgent.addBehaviour(initiator);

        story.record().startAgent("initiator", initiatorAgent);

        story.record().addAssert(new Assertion() {
            public void check() throws Throwable {
                log.assertContent(expectedLog);
            }
        });
    }
}
