/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Aid;
import net.codjo.agent.UserId;
import junit.framework.TestCase;
/**
 *
 */
public class MessageFieldTest extends TestCase {
    private AgentContainerFixture fixture = new AgentContainerFixture();
    private AclMessage message;


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }


    public void test_retrieveValue() throws Exception {
        message = new AclMessage(AclMessage.Performative.AGREE);
        message.setProtocol("my protocol");
        message.setContentObject("my content");
        message.setConversationId("my conversationId");
        message.setLanguage("my language");
        message.encodeUserId(UserId.createId("login", "password"));

        assertRetrieveValue("AGREE", MessageField.PERFORMATIVE);
        assertRetrieveValue(message.getProtocol(), MessageField.PROTOCOL);
        assertRetrieveValue(message.getContentObject(), MessageField.CONTENT);
        assertRetrieveValue(message.getConversationId(), MessageField.CONVERSATION_ID);
        assertRetrieveValue(message.getLanguage(), MessageField.LANGUAGE);
        assertRetrieveValue(message.decodeUserId().encode(), MessageField.USER_ID);

        message = new AclMessage(AclMessage.Performative.AGREE);
        assertRetrieveValue("null", MessageField.USER_ID);
    }


    public void test_aidFields() throws Exception {
        fixture.startContainer();

        message = new AclMessage(AclMessage.Performative.AGREE);
        assertRetrieveValue("null", MessageField.SENDER);

        message.setSender(new Aid("sender"));
        assertRetrieveValue("sender", MessageField.SENDER);

        assertRetrieveValue("null", MessageField.RECEIVER);

        message.addReceiver(new Aid("dest1"));
        message.addReceiver(new Aid("dest2"));
        assertRetrieveValue("dest1, dest2", MessageField.RECEIVER);
    }


    private void assertRetrieveValue(Object expected, MessageField field) {
        assertEquals(expected, field.retrieveValue(message));
    }
}
