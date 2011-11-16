/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.AclMessage.Performative;
import static net.codjo.agent.AclMessage.Performative.AGREE;
import static net.codjo.agent.AclMessage.Performative.CANCEL;
import static net.codjo.agent.AclMessage.Performative.FAILURE;
import static net.codjo.agent.AclMessage.Performative.NOT_UNDERSTOOD;
import static net.codjo.agent.AclMessage.Performative.REFUSE;
import static net.codjo.agent.AclMessage.Performative.SUBSCRIBE;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
import net.codjo.agent.MessageTemplate;
import static net.codjo.agent.MessageTemplate.and;
import static net.codjo.agent.MessageTemplate.matchContent;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import static net.codjo.agent.MessageTemplate.matchProtocol;
import static net.codjo.agent.test.AgentAssert.log;
import net.codjo.agent.test.DummyAgent;
import net.codjo.agent.test.MessageBuilder;
import static net.codjo.agent.test.MessageBuilder.message;
import net.codjo.agent.test.OneShotStep;
import net.codjo.agent.test.Story;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
/**
 * Classe de test de {@link SubscribeParticipant}.
 */
public class SubscribeParticipantTest extends TestCase {
    private LogString log;
    private ParticipantHandlerMock participantHandlerMock;
    private Story story = new Story();
    private Agent participantAgent;


    @Override
    protected void setUp() throws Exception {
        log = new LogString();
        participantHandlerMock = new ParticipantHandlerMock(new LogString("participantHandler", log));
        participantAgent = new DummyAgent();
        participantAgent.addBehaviour(createBehaviour());
        story.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
    }


    public void test_receive_subscribe() throws Exception {
        story.record().startAgent("participant", participantAgent);

        story.record().startTester("initiator")
              .send(subscribeMessage("subscribe-content"));

        story.record()
              .addAssert(log(log, "participantHandler.handleSubscribe(souscription[subscribe-content])"));

        story.execute();
    }


    public void test_send_refuse() throws Exception {
        participantHandlerMock.mockRegisterError(new RefuseException("refuse-content"));

        story.record().startAgent("participant", participantAgent);

        story.record().startTester("initiator")
              .send(subscribeMessage("subscribe-content"))
              .then()
              .receiveMessage()
              .assertReceivedMessage(subscribeWithPerformative(REFUSE))
              .assertReceivedMessage(matchContent("(refuse-content)"))
              ;

        story.execute();
    }


    public void test_send_notUnderstood() throws Exception {
        participantHandlerMock.mockRegisterError(new NotUnderstoodException("notUnderstood-content"));

        story.record().startAgent("participant", participantAgent);

        story.record().startTester("initiator")
              .send(subscribeMessage("subscribe-content"))
              .then()
              .receiveMessage()
              .assertReceivedMessage(subscribeWithPerformative(NOT_UNDERSTOOD))
              .assertReceivedMessage(matchContent("(notUnderstood-content)"))
              ;

        story.execute();
    }


    public void test_send_agree() throws Exception {
        participantHandlerMock.mockRegisterAgree("agree-content");

        story.record().startAgent("participant", participantAgent);

        story.record().startTester("initiator")
              .send(subscribeMessage("subscribe-content"))
              .then()
              .receiveMessage()
              .assertReceivedMessage(subscribeWithPerformative(AGREE))
              .assertReceivedMessage(matchContent("agree-content"))
              ;

        story.execute();
    }


    public void test_receive_cancel() throws Exception {
        participantHandlerMock.mockRegisterAgree("agree-content");

        story.record().startAgent("participant", participantAgent);

        story.record().startTester("initiator")
              .send(subscribeMessage("subscribe-content"))
              .then()
              .receiveMessage(subscribeWithPerformative(AGREE))
              .then()
              .send(cancelMessage())
              ;

        story.record()
              .addAssert(log(log, "participantHandler.handleSubscribe(souscription[subscribe-content])"
                                  + ", participantHandler.handleCancel(souscription[subscribe-content])"));

        story.execute();

        assertSame(participantHandlerMock.getRegisterSubscription(),
                   participantHandlerMock.getDeregisterSubscription());
    }


    public void test_receive_cancel_onClosedByHandSubscription() throws Exception {
        participantHandlerMock.mockRegisterAgree("agree-content");

        story.record().startAgent("participant", participantAgent);

        story.record().startTester("initiator")
              .send(subscribeMessage("subscribe-content"))
              .then()
              .receiveMessage(subscribeWithPerformative(AGREE))
              .then()
              .perform(new OneShotStep() {
                  public void run(Agent agent) throws Exception {
                      log.clear();
                      participantHandlerMock.getRegisterSubscription().close();
                  }
              })
              .then()
              .send(cancelMessage())
              .then()
              .send(subscribeMessage("subscribe-content-2"))
              ;

        story.record()
              .addAssert(log(log, "participantHandler.handleSubscribe(souscription[subscribe-content-2])"));

        story.execute();
    }


    public void test_receive_cancel_send_failure() throws Exception {
        participantHandlerMock.mockRegisterAgree("agree-content");
        participantHandlerMock.mockDeregisterError(new FailureException("failure-content"));

        story.record().startAgent("participant", participantAgent);

        story.record().startTester("initiator")
              .send(subscribeMessage("subscribe-content"))
              .then()
              .receiveMessage(subscribeWithPerformative(AGREE))
              .then()
              .send(cancelMessage())
              .then()
              .receiveMessage()
              .assertReceivedMessage(subscribeWithPerformative(FAILURE))
              .assertReceivedMessage(matchContent("(failure-content)"))
              ;

        story.execute();
    }


    public void test_neverFaisWrapping_nominal() throws Exception {
        participantAgent = new DummyAgent();
        participantAgent.addBehaviour(Behaviour.thatNeverFails(createBehaviour()));

        test_receive_cancel_send_failure();
    }


    public void test_neverFaisWrapping() throws Exception {
        participantAgent = new DummyAgent();
        participantAgent.addBehaviour(Behaviour.thatNeverFails(createBehaviour()));

        participantHandlerMock.mockRegisterError(new NullPointerException("refuse-content"));

        story.record().startAgent("participant", participantAgent);

        story.record().startTester("initiator")
              .send(subscribeMessage("subscribe-content"));

        story.record()
              .addAssert(log(log, "participantHandler.handleSubscribe(souscription[subscribe-content])"));

        story.execute();
    }


    private SubscribeParticipant createBehaviour() {
        return new SubscribeParticipant(participantAgent,
                                        participantHandlerMock,
                                        matchProtocol(SubscribeProtocol.ID));
    }


    private static MessageTemplate subscribeWithPerformative(Performative performative) {
        return and(matchPerformative(performative), matchProtocol(SubscribeProtocol.ID));
    }


    private MessageBuilder subscribeMessage(String content) {
        return message(SUBSCRIBE)
              .to("participant")
              .usingProtocol(SubscribeProtocol.ID)
              .usingConversationId("conv-id")
              .withContent(content);
    }


    private MessageBuilder cancelMessage() {
        return message(CANCEL)
              .to("participant")
              .usingProtocol(SubscribeProtocol.ID)
              .usingConversationId("conv-id")
              .withContent("cancel-content");
    }


    private static class ParticipantHandlerMock implements SubscribeParticipantHandler {
        private final LogString log;
        private RefuseException refuse;
        private NotUnderstoodException notUnderstood;
        private String agree;
        private FailureException failure;
        private SubscribeParticipant.Subscription registerSubscription;
        private SubscribeParticipant.Subscription deregisterSubscription;
        private NullPointerException developmentError;


        ParticipantHandlerMock(LogString log) {
            this.log = log;
        }


        public void handleSubscribe(SubscribeParticipant.Subscription subscription)
              throws RefuseException, NotUnderstoodException {
            registerSubscription = subscription;
            log.call("handleSubscribe", "souscription[" + subscription.getMessage().getContent() + "]");
            if (refuse != null) {
                throw refuse;
            }
            else if (notUnderstood != null) {
                throw notUnderstood;
            }
            else if (developmentError != null) {
                throw developmentError;
            }
            else if (agree != null) {
                AclMessage agreeMessage = subscription.getMessage().createReply(AGREE);
                agreeMessage.setContent(agree);
                subscription.reply(agreeMessage);
            }
        }


        public void handleCancel(SubscribeParticipant.Subscription subscription)
              throws FailureException {
            deregisterSubscription = subscription;
            log.call("handleCancel", "souscription[" + subscription.getMessage().getContent() + "]");
            if (failure != null) {
                throw failure;
            }
        }


        public void mockRegisterError(RefuseException refuseException) {
            this.refuse = refuseException;
        }


        public void mockRegisterError(NotUnderstoodException notUnderstoodException) {
            this.notUnderstood = notUnderstoodException;
        }


        public void mockRegisterError(NullPointerException error) {
            this.developmentError = error;
        }


        public void mockRegisterAgree(String agreeContent) {
            this.agree = agreeContent;
        }


        public void mockDeregisterError(FailureException failureException) {
            this.failure = failureException;
        }


        public SubscribeParticipant.Subscription getRegisterSubscription() {
            return registerSubscription;
        }


        public SubscribeParticipant.Subscription getDeregisterSubscription() {
            return deregisterSubscription;
        }
    }
}
