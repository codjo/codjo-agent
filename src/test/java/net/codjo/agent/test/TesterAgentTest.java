/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.DFService;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.UserId;
import net.codjo.test.common.LogString;
import junit.framework.Assert;
import junit.framework.TestCase;
/**
 * Classe de test de {@link TesterAgent}.
 */
public class TesterAgentTest extends TestCase {
    private static final String INTERLOCUTOR_ID = "interlocutor";
    private static final String TESTER_ID = "tester";
    private AgentContainerFixture fixture = new AgentContainerFixture();
    private LogString log = new LogString();
    private TesterAgent agent;
    private DummyAgent interlocutor;
    private UserId userId = UserId.createId("me", "secret");

/*
    public void test_receivedMessageHistory() throws Exception {
        agent.record()
              .receiveMessage(MessageTemplate.matchSender(new Aid(INTERLOCUTOR_ID)))
              .addSubStep(new SubStep() {
                  public void run(Agent agent, AclMessage message) throws Exception {
                      agent.getReceivedMessage(
                  }
              });

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.sendMessage(interlocutor,
                            createCustomMessage(AclMessage.Performative.REQUEST, TESTER_ID, null,
                                                "ma question"));

        AclMessage received = fixture.receiveMessage(interlocutor);
        assertEquals("ma reponse", received.getContent());
    }
*/


    public void test_reuseStoryPart() throws Exception {
        TesterAgentRecorder recorder = agent.record();

        recorder.play(new StoryPart() {
            public void record(TesterAgentRecorder recorder) {
                log.call("record", recorder);
            }
        });

        log.assertContent("record(" + recorder + ")");
    }


    public void test_registerToDf() throws Exception {
        agent.record().registerToDF("imports");

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.assertNumberOfAgentWithService(1, "imports");
    }


    public void test_registerToDf_fullDescription() throws Exception {
        agent.record().registerToDF(new DFService.AgentDescription(
              new DFService.ServiceDescription("imports", "red-application")));

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.assertNumberOfAgentWithService(1, "imports");
    }


    public void test_sendMessage() throws Exception {
        agent.record().sendMessage(createCustomMessage(AclMessage.Performative.INFORM,
                                                       INTERLOCUTOR_ID, "my-protocol", "1"));

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.assertReceivedMessage(interlocutor, "my-protocol", AclMessage.Performative.INFORM, "1");
    }


    public void test_sendMessageWithProtocol() throws Exception {
        agent.record().sendMessage(AclMessage.Performative.INFORM,
                                   "my-protocol",
                                   new Aid(INTERLOCUTOR_ID),
                                   "1");

        fixture.startNewAgent(TESTER_ID, agent);

        AclMessage message = fixture.receiveMessage(interlocutor);
        assertEquals("my-protocol", message.getProtocol());
        assertEquals(AclMessage.Performative.INFORM, message.getPerformative());
        assertEquals("1", message.getContent());
    }


    public void test_sendMessageTwice() throws Exception {
        agent.record().sendMessage(createCustomMessage(AclMessage.Performative.INFORM,
                                                       INTERLOCUTOR_ID, "my-protocol", "1"));
        agent.record().sendMessage(AclMessage.Performative.INFORM, new Aid(INTERLOCUTOR_ID), "2");

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.assertReceivedMessage(interlocutor, "my-protocol", AclMessage.Performative.INFORM, "1");
        fixture.assertReceivedMessage(interlocutor, null, AclMessage.Performative.INFORM, "2");
    }


    public void test_sendMessageAndDie() throws Exception {
        agent.record().sendMessage(AclMessage.Performative.INFORM, new Aid(INTERLOCUTOR_ID), "1").die();

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.assertReceivedMessage(interlocutor, null, AclMessage.Performative.INFORM, "1");
        fixture.assertNotContainsAgent(TESTER_ID);

        assertSame(agent.record(),
                   agent.record().sendMessage(new AclMessage(AclMessage.Performative.CFP)).then());
    }


    public void test_sendMessageLogAndDie() throws Exception {
        agent.addStoryListener(new TesterAgentListenerMock(log));
        agent.record().sendMessage(AclMessage.Performative.INFORM, new Aid(INTERLOCUTOR_ID), "1")
              .log(log, "my log")
              .die();

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.assertReceivedMessage(interlocutor, null, AclMessage.Performative.INFORM, "1");
        fixture.assertNotContainsAgent(TESTER_ID);
        log.assertContent("storyStarted(), "
                          + "stepStarted(SendMessageStep), "
                          + "my log, "
                          + "stepFinished(SendMessageStep), "
                          + "storyFinished()");
    }


    public void test_receiveMessageReplyUsingSerializedContent() throws Exception {
        agent.record()
              .receiveMessage(MessageTemplate.matchSender(new Aid(INTERLOCUTOR_ID)))
              .assertReceivedMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST))
              .assertReceivedMessageUserId(userId)
              .replyWithContent(AclMessage.Performative.INFORM, "ma reponse");

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.sendMessage(interlocutor,
                            createCustomMessage(AclMessage.Performative.REQUEST, TESTER_ID, null,
                                                "ma question"));

        fixture.assertReceivedMessage(interlocutor, null, AclMessage.Performative.INFORM, "ma reponse");

        assertFalse(agent.getErrorManager().hasError());
    }


    public void test_receiveMessageReply() throws Exception {
        agent.record()
              .receiveMessage(MessageTemplate.matchSender(new Aid(INTERLOCUTOR_ID)))
              .replyWith(AclMessage.Performative.INFORM, "ma reponse");

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.sendMessage(interlocutor,
                            createCustomMessage(AclMessage.Performative.REQUEST, TESTER_ID, null,
                                                "ma question"));

        AclMessage received = fixture.receiveMessage(interlocutor);
        assertEquals("ma reponse", received.getContent());
    }


    public void test_receiveMessageAssertFailure()
          throws Exception {
        agent.record()
              .receiveMessage(MessageTemplate.matchAll())
              .assertReceivedMessage(MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST))
              .log(log, "je ne dois pas être exécutée!!");

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.sendMessage(interlocutor,
                            createCustomMessage(AclMessage.Performative.QUERY, TESTER_ID, null, "hello"));

        fixture.assertUntilOk(new AgentAssert.Assertion() {
            public void check() {
                Assert.assertTrue(agent.getErrorManager().hasError());
            }
        });
        log.assertContent("");
        assertStartWith("agent 'tester' : "
                        + "Received message does not match the template: '( Perfomative: REQUEST )'",
                        agent.getErrorManager().getFirstErrorDescription());
    }


    public void test_addCustomSubStep() throws Exception {
        agent.record().sendMessage(AclMessage.Performative.INFORM, new Aid(INTERLOCUTOR_ID), "send")
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage message) {
                      log.info(message.getContentObject().toString());
                  }
              });

        fixture.startNewAgent(TESTER_ID, agent);

        fixture.assertBehaviourDone(agent.record().getBehaviour());
        log.assertContent("send");
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
        agent = new TesterAgent();
        interlocutor = new DummyAgent();
        fixture.startNewAgent(INTERLOCUTOR_ID, interlocutor);
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }


    private AclMessage createCustomMessage(AclMessage.Performative performative,
                                           String receiver,
                                           String protocol,
                                           String content) {
        AclMessage messageToBeSent = new AclMessage(performative);
        messageToBeSent.addReceiver(new Aid(receiver));
        messageToBeSent.setProtocol(protocol);
        messageToBeSent.setContentObject(content);
        messageToBeSent.encodeUserId(userId);
        return messageToBeSent;
    }


    public static void assertStartWith(String expected, String actual) {
        if (!actual.startsWith(expected)) {
            fail("assertStartWith expected:<" + expected + "> but was:<" + actual + ">");
        }
    }
}
