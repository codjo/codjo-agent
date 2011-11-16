/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import static net.codjo.agent.AclMessage.Performative.SUBSCRIBE;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.DummyAgent;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
/**
 * Classe de test de {@link SubscribeInitiator}.
 */
public class SubscribeInitiatorTest extends TestCase {
    private LogString log;
    private SubscribeInitiator initiator;
    private InitiatorHandlerMock subscribeHandlerMock;
    private AgentContainerFixture fixture = new AgentContainerFixture();
    private Agent participantAgent;
    private Agent initiatorAgent;


    @Override
    protected void setUp() throws Exception {
        log = new LogString();
        subscribeHandlerMock = new InitiatorHandlerMock(new LogString("initiator", log));
        participantAgent = new DummyAgent();
        initiatorAgent = new DummyAgent();
        fixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }


    public void test_send_subscribe() throws Exception {
        fixture.startNewAgent("responder", participantAgent);

        AclMessage sentMessage = new AclMessage(SUBSCRIBE);
        sentMessage.setContentObject("content");
        sentMessage.addReceiver(new Aid("responder"));

        initiator =
              new SubscribeInitiator(initiatorAgent, subscribeHandlerMock, sentMessage);
        initiatorAgent.addBehaviour(initiator);
        fixture.startNewAgent("initiator", initiatorAgent);

        AclMessage subscribeMessage = fixture.receiveMessage(participantAgent);

        fixture.assertMessage(subscribeMessage, null, SUBSCRIBE, sentMessage.getContentObject());
    }


    public void test_send_cancel() throws Exception {
        initializeProtocol();
        assertFalse(initiator.done());

        fixture.runInAgentThread(initiatorAgent,
                                 new Runnable() {
                                     public void run() {
                                         initiator.cancel(participantAgent.getAID(), true);
                                     }
                                 });

        fixture.assertBehaviourDone(initiator);
    }


    public void test_receive_agree() throws Exception {
        AclMessage subscribeMessage = initializeProtocol();

        AclMessage agree = subscribeMessage.createReply(AclMessage.Performative.AGREE);
        agree.setContentObject("agree-content");

        participantAgent.send(agree);

        subscribeHandlerMock.waitForMessage();

        log.assertContent("initiator.handleAgree(agree-content)");
        assertFalse(initiator.done());
    }


    public void test_receive_refuse() throws Exception {
        AclMessage subscribeMessage = initializeProtocol();

        AclMessage refuse = subscribeMessage.createReply(AclMessage.Performative.REFUSE);
        refuse.setContentObject("refuse-content");

        participantAgent.send(refuse);

        subscribeHandlerMock.waitForMessage();

        log.assertContent("initiator.handleRefuse(refuse-content)");

        fixture.assertBehaviourDone(initiator);
    }


    public void test_receive_inform() throws Exception {
        AclMessage subscribeMessage = initializeProtocol();

        AclMessage inform = subscribeMessage.createReply(AclMessage.Performative.INFORM);
        inform.setContentObject("inform-content");

        participantAgent.send(inform);

        subscribeHandlerMock.waitForMessage();

        log.assertContent("initiator.handleInform(inform-content)");
    }


    public void test_receive_failure() throws Exception {
        AclMessage subscribeMessage = initializeProtocol();

        AclMessage failure = subscribeMessage.createReply(AclMessage.Performative.FAILURE);
        failure.setContentObject("handleFailure-content");

        participantAgent.send(failure);

        subscribeHandlerMock.waitForMessage();

        log.assertContent("initiator.handleFailure(handleFailure-content)");
        assertFalse(initiator.done());
    }


    public void test_receive_notUnderstood() throws Exception {
        AclMessage subscribeMessage = initializeProtocol();

        AclMessage notUnderstood =
              subscribeMessage.createReply(AclMessage.Performative.NOT_UNDERSTOOD);
        notUnderstood.setContentObject("handleOutOfSequence-content");

        participantAgent.send(notUnderstood);

        subscribeHandlerMock.waitForMessage();

        log.assertContent("initiator.handleNotUnderstood(handleOutOfSequence-content)");

        fixture.assertBehaviourDone(initiator);
    }


    public void test_receive_outOfSequence() throws Exception {
        AclMessage subscribeMessage = initializeProtocol();

        AclMessage notUnderstood = subscribeMessage.createReply(AclMessage.Performative.REQUEST);
        notUnderstood.setContentObject("handleOutOfSequence-content");

        participantAgent.send(notUnderstood);

        subscribeHandlerMock.waitForMessage();

        log.assertContent("initiator.handleOutOfSequence(handleOutOfSequence-content)");
        assertFalse(initiator.done());
    }


    private AclMessage initializeProtocol() throws ContainerFailureException {
        fixture.startNewAgent("responder", participantAgent);

        AclMessage sentMessage = new AclMessage(SUBSCRIBE);
        sentMessage.setContentObject("content");
        sentMessage.addReceiver(new Aid("responder"));

        initiator =
              new SubscribeInitiator(initiatorAgent, subscribeHandlerMock, sentMessage);
        initiatorAgent.addBehaviour(initiator);
        fixture.startNewAgent("initiator", initiatorAgent);

        return fixture.receiveMessage(participantAgent);
    }


    public void test_neverFailsWrapping_unsupported() throws Exception {
        initiator = new SubscribeInitiator(initiatorAgent, subscribeHandlerMock, new AclMessage(SUBSCRIBE));

        try {
            new DummyAgent().addBehaviour(Behaviour.thatNeverFails(initiator));
            fail();
        }
        catch (UnsupportedOperationException ex) {
            ;
        }
    }
}
