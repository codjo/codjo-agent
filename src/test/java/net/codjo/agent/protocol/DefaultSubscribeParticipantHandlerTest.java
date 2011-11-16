/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;

public class DefaultSubscribeParticipantHandlerTest extends TestCase {
    private DefaultSubscribeParticipantHandler handlerLeader;
    private LogString log = new LogString();


    public void test_handleSubscribe_sendRequestNotification()
          throws Exception {
        handlerLeader.handleSubscribe(new SubscriptionMock(new LogString("agent-subscription", log)));

        handlerLeader.sendInform("import");

        log.assertContent("agent-subscription.reply(aclMessage(INFORM, request[import]))");
    }


    public void test_handleCancel_sendNotification()
          throws Exception {
        SubscriptionMock subscription = new SubscriptionMock(new LogString("agent-subscription", log));
        handlerLeader.handleSubscribe(subscription);
        handlerLeader.handleCancel(subscription);

        handlerLeader.sendInform("import");

        log.assertContent("");
    }


    public void test_removeSubscription() throws Exception {
        SubscriptionMock subscription =
              new SubscriptionMock(new LogString("agent-subscription", log));
        subscription.mockConversationId("my-conversation-id");
        handlerLeader.handleSubscribe(subscription);

        handlerLeader.removeSubscription("my-conversation-id");
        log.assertContent("agent-subscription.close()");
        log.clear();

        handlerLeader.sendInform("import");
        log.assertContent("");
    }


    @Override
    protected void setUp() throws Exception {
        handlerLeader = new DefaultSubscribeParticipantHandler();
    }


    public static class SubscriptionMock implements SubscribeParticipant.Subscription {
        private LogString log;
        private AclMessage subscribeMessage = new AclMessage(AclMessage.Performative.SUBSCRIBE);


        public SubscriptionMock(LogString log) {
            this.log = log;
        }


        public AclMessage getMessage() {
            return subscribeMessage;
        }


        public void reply(AclMessage messageToSend) {
            log.call("reply", toString(messageToSend));
        }


        public void close() {
            log.call("close");
        }


        private String toString(AclMessage messageToSend) {
            StringBuilder buffer = new StringBuilder("aclMessage(");

            String performative = AclMessage.performativeToString(messageToSend.getPerformative());

            buffer.append(performative).append(", ");
            buffer.append("request[").append(messageToSend.getContent()).append("]");

            return buffer.append(")").toString();
        }


        public void mockConversationId(String conversationId) {
            subscribeMessage.setConversationId(conversationId);
        }
    }
}
