/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import jade.lang.acl.ACLMessage;
import junit.framework.TestCase;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.Story.ConnectionType;

import static net.codjo.agent.AclMessage.Performative.FAILURE;
import static net.codjo.agent.AclMessage.Performative.REQUEST;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import static net.codjo.agent.MessageTemplate.not;
/**
 * Classe de test de {@link MessageTemplate}.
 */
public class MessageTemplateTest extends TestCase {
    private AgentContainerFixture fixture = new AgentContainerFixture();


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }


    public void test_match() throws Exception {
        MessageTemplate template = matchPerformative(AclMessage.Performative.AGREE);

        assertTrue(template.match(new AclMessage(AclMessage.Performative.AGREE)));
        assertFalse(template.match(new AclMessage(REQUEST)));
    }


    public void test_matchWith() throws Exception {
        MessageTemplate messageTemplate =
              MessageTemplate.matchWith(new MessageTemplate.MatchExpression() {
                  public boolean match(AclMessage aclMessage) {
                      return "id".equals(aclMessage.getConversationId());
                  }
              });

        AclMessage aclMessage = new AclMessage(REQUEST);
        aclMessage.setConversationId("id");
        assertTrue(template(messageTemplate).match(message(aclMessage)));
        aclMessage.setConversationId("Hoho ca passe plus");
        assertFalse(template(messageTemplate).match(message(aclMessage)));
    }


    public void test_matchConversationId() throws Exception {
        MessageTemplate messageTemplate = MessageTemplate.matchConversationId("id");

        AclMessage aclMessage = new AclMessage(REQUEST);

        aclMessage.setConversationId("id");
        assertTrue(template(messageTemplate).match(message(aclMessage)));

        aclMessage.setConversationId("Hoho ca passe plus");
        assertFalse(template(messageTemplate).match(message(aclMessage)));
    }


    public void test_matchSender() throws Exception {
        fixture.startContainer(ConnectionType.NO_CONNECTION);

        MessageTemplate messageTemplate = MessageTemplate.matchSender(new Aid("aid"));

        AclMessage aclMessage = new AclMessage(REQUEST);

        aclMessage.setSender(new Aid("aid"));
        assertTrue(template(messageTemplate).match(message(aclMessage)));

        aclMessage.setSender(new Aid("not_aid"));
        assertFalse(template(messageTemplate).match(message(aclMessage)));
    }


    public void test_matchPerformative() throws Exception {
        MessageTemplate messageTemplate =
              matchPerformative(REQUEST);

        AclMessage aclMessage = new AclMessage(REQUEST);

        assertTrue(template(messageTemplate).match(message(aclMessage)));

        aclMessage.setPerformative(AclMessage.Performative.FAILURE);
        assertFalse(template(messageTemplate).match(message(aclMessage)));
    }


    public void test_matchProtocol() throws Exception {
        MessageTemplate messageTemplate = MessageTemplate.matchProtocol("id");

        AclMessage aclMessage = new AclMessage(REQUEST);

        aclMessage.setProtocol("id");
        assertTrue(template(messageTemplate).match(message(aclMessage)));

        aclMessage.setProtocol("notId");
        assertFalse(template(messageTemplate).match(message(aclMessage)));
    }


    public void test_matchContent() throws Exception {
        MessageTemplate template = MessageTemplate.matchContent("my-content");

        AclMessage aclMessage = new AclMessage(REQUEST);

        aclMessage.setContent("my-content");
        assertTrue(template(template).match(message(aclMessage)));

        aclMessage.setContent("Hoho ca passe plus");
        assertFalse(template(template).match(message(aclMessage)));
    }


    public void test_not() throws Exception {
        MessageTemplate notRequest = not(matchPerformative(REQUEST));

        assertTrue(template(notRequest).match(message(new AclMessage(FAILURE))));

        assertFalse(template(notRequest).match(message(new AclMessage(REQUEST))));
    }


    private static ACLMessage message(AclMessage aclMessage) {
        return aclMessage.getJadeMessage();
    }


    private jade.lang.acl.MessageTemplate template(MessageTemplate notRequest) {
        return notRequest.getJadeTemplate();
    }
}
