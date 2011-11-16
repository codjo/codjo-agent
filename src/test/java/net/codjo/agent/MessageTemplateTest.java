/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.agent.test.AgentContainerFixture;
import junit.framework.TestCase;
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
        MessageTemplate template = MessageTemplate.matchPerformative(AclMessage.Performative.AGREE);

        assertTrue(template.match(new AclMessage(AclMessage.Performative.AGREE)));
        assertFalse(template.match(new AclMessage(AclMessage.Performative.REQUEST)));
    }


    public void test_matchWith() throws Exception {
        MessageTemplate messageTemplate =
              MessageTemplate.matchWith(new MessageTemplate.MatchExpression() {
                  public boolean match(AclMessage aclMessage) {
                      return "id".equals(aclMessage.getConversationId());
                  }
              });

        AclMessage aclMessage = new AclMessage(AclMessage.Performative.REQUEST);
        aclMessage.setConversationId("id");
        assertTrue(messageTemplate.getJadeTemplate().match(aclMessage.getJadeMessage()));
        aclMessage.setConversationId("Hoho ca passe plus");
        assertFalse(messageTemplate.getJadeTemplate().match(aclMessage.getJadeMessage()));
    }


    public void test_matchConversationId() throws Exception {
        MessageTemplate messageTemplate = MessageTemplate.matchConversationId("id");

        AclMessage aclMessage = new AclMessage(AclMessage.Performative.REQUEST);

        aclMessage.setConversationId("id");
        assertTrue(messageTemplate.getJadeTemplate().match(aclMessage.getJadeMessage()));

        aclMessage.setConversationId("Hoho ca passe plus");
        assertFalse(messageTemplate.getJadeTemplate().match(aclMessage.getJadeMessage()));
    }


    public void test_matchSender() throws Exception {
        fixture.startContainer();

        MessageTemplate messageTemplate = MessageTemplate.matchSender(new Aid("aid"));

        AclMessage aclMessage = new AclMessage(AclMessage.Performative.REQUEST);

        aclMessage.setSender(new Aid("aid"));
        assertTrue(messageTemplate.getJadeTemplate().match(aclMessage.getJadeMessage()));

        aclMessage.setSender(new Aid("not_aid"));
        assertFalse(messageTemplate.getJadeTemplate().match(aclMessage.getJadeMessage()));
    }


    public void test_matchPerformative() throws Exception {
        MessageTemplate messageTemplate =
              MessageTemplate.matchPerformative(AclMessage.Performative.REQUEST);

        AclMessage aclMessage = new AclMessage(AclMessage.Performative.REQUEST);

        assertTrue(messageTemplate.getJadeTemplate().match(aclMessage.getJadeMessage()));

        aclMessage.setPerformative(AclMessage.Performative.FAILURE);
        assertFalse(messageTemplate.getJadeTemplate().match(aclMessage.getJadeMessage()));
    }


    public void test_matchProtocol() throws Exception {
        MessageTemplate messageTemplate = MessageTemplate.matchProtocol("id");

        AclMessage aclMessage = new AclMessage(AclMessage.Performative.REQUEST);

        aclMessage.setProtocol("id");
        assertTrue(messageTemplate.getJadeTemplate().match(aclMessage.getJadeMessage()));

        aclMessage.setProtocol("notId");
        assertFalse(messageTemplate.getJadeTemplate().match(aclMessage.getJadeMessage()));
    }


    public void test_matchContent() throws Exception {
        MessageTemplate template = MessageTemplate.matchContent("my-content");

        AclMessage aclMessage = new AclMessage(AclMessage.Performative.REQUEST);

        aclMessage.setContent("my-content");
        assertTrue(template.getJadeTemplate().match(aclMessage.getJadeMessage()));

        aclMessage.setContent("Hoho ca passe plus");
        assertFalse(template.getJadeTemplate().match(aclMessage.getJadeMessage()));
    }
}
