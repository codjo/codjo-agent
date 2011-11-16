/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.AgentMock;
import net.codjo.agent.Aid;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
/**
 * Classe de test de {@link SubscribeFailureBehaviour}.
 */
public class SubscribeFailureBehaviourTest extends TestCase {
    private LogString log = new LogString();
    private SubscribeFailureBehaviour failureBehaviour;
    private AgentContainerFixture fixture = new AgentContainerFixture();


    public void test_action() throws Exception {
        Aid amsAID = new Aid("AMS");

        AclMessage failureFromAms = new AclMessage(AclMessage.Performative.FAILURE);
        failureFromAms.setConversationId("failed-conversation-id");

        AgentMock agentMock = new AgentMock(failureBehaviour);
        agentMock.mockReceive(failureFromAms);
        agentMock.mockGetAMS(amsAID);

        failureBehaviour.action();

        agentMock.getLog().assertContent("agent.receive((( Sender AID: ( agent-identifier :name "
                                         + amsAID.getName() + " )) AND ( Perfomative: FAILURE )))");
        log.assertContent("participantHandler.removeSubscription(failed-conversation-id)");
    }


    public void test_done() throws Exception {
        assertFalse(failureBehaviour.done());
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
        fixture.startContainer();

        JobLeaderSubscribeHandlerMock participantHandler = new JobLeaderSubscribeHandlerMock(
              new LogString("participantHandler", log));

        failureBehaviour = new SubscribeFailureBehaviour(participantHandler);
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }


    private static class JobLeaderSubscribeHandlerMock
          extends AbstractSubscribeParticipantHandler {
        private final LogString log;


        JobLeaderSubscribeHandlerMock(LogString log) {
            this.log = log;
        }


        @Override
        public void removeSubscription(String conversationId) {
            log.call("removeSubscription", conversationId);
        }
    }
}
