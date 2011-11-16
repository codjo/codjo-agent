/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.DummyAgent;
import net.codjo.agent.test.Story.ConnectionType;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import junit.framework.TestCase;
/**
 * Classe de test de {@link AclMessage}.
 */
public class AclMessageTest extends TestCase {
    private AgentContainerFixture containerFixture = new AgentContainerFixture();


    public void test_userDefinedParameter() throws Exception {
        AclMessage aclMessage = new AclMessage(AclMessage.Performative.INFORM);
        aclMessage.addUserDefinedParameter("key", "value");
        String parameter = aclMessage.getUserDefinedParameter("key");
        assertEquals("value", parameter);
    }


    public void test_userDefinedParameter_withBadValue()
          throws Exception {
        AclMessage aclMessage = new AclMessage(AclMessage.Performative.INFORM);

        try {
            aclMessage.addUserDefinedParameter("key", null);
            fail();
        }
        catch (NullPointerException ex) {
        }

        try {
            aclMessage.addUserDefinedParameter(null, "ee");
            fail();
        }
        catch (NullPointerException ex) {
        }

        try {
            aclMessage.getUserDefinedParameter("eeee");
            fail();
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            aclMessage.getUserDefinedParameter(null);
            fail();
        }
        catch (NullPointerException ex) {
        }
    }


    public void test_constructor() throws Exception {
        AclMessage msg = new AclMessage(AclMessage.Performative.REQUEST);
        assertEquals(AclMessage.Performative.REQUEST, msg.getPerformative());
        msg.setPerformative(AclMessage.Performative.AGREE);
        assertEquals(AclMessage.Performative.AGREE, msg.getPerformative());

        final Serializable serializable = new ArrayList();
        msg.setContentObject(serializable);
        assertNotSame(serializable, msg.getContentObject());
        assertEquals(serializable, msg.getContentObject());

        msg.addReceiver(new Aid("toto"));
        assertReceiverLocalName("toto", msg);

        assertNull(msg.getSender());
        msg.setSender(new Aid("titi"));
        assertAIDLocalName("titi", msg.getSender());
    }


    public void test_constructor_withProtocol() throws Exception {
        AclMessage msg = new AclMessage(AclMessage.Performative.REQUEST, "my-protocol");
        assertEquals("my-protocol", msg.getProtocol());
        assertEquals(AclMessage.Performative.REQUEST, msg.getPerformative());
    }


    public void test_clearReceivers() throws Exception {
        AclMessage message = new AclMessage(AclMessage.Performative.INFORM);
        assertNoReceiver(message);

        message.addReceiver(new Aid("receiverAID"));
        assertReceiverLocalName("receiverAID", message);

        message.clearReceivers();
        assertNoReceiver(message);
    }


    public void test_addReplyTo() throws Exception {
        AclMessage message = new AclMessage(AclMessage.Performative.INFORM);
        assertNoReplyToReceivers(message);

        message.addReplyTo(new Aid("receiverAID"));
        assertReplyToLocalName("receiverAID", message);

        message.clearAllReplyTo();
        assertNoReplyToReceivers(message);
    }


    public void test_removeReplyTo() throws Exception {
        AclMessage message = new AclMessage(AclMessage.Performative.INFORM);
        assertNoReplyToReceivers(message);

        message.addReplyTo(new Aid("receiverAID"));
        assertReplyToLocalName("receiverAID", message);

        assertFalse(message.removeReplyTo(new Aid("receiverAID_NonExistant")));

        assertTrue(message.removeReplyTo(new Aid("receiverAID")));
        assertNoReplyToReceivers(message);
    }


    public void test_setProtocol() throws Exception {
        AclMessage msg = new AclMessage(AclMessage.Performative.REQUEST);

        msg.setProtocol("my-protocol");
        assertEquals("my-protocol", msg.getProtocol());
    }


    public void test_setLanguage() throws Exception {
        AclMessage msg = new AclMessage(AclMessage.Performative.REQUEST);

        msg.setLanguage("my-language");
        assertEquals("my-language", msg.getLanguage());
    }


    public void test_setContent() throws Exception {
        AclMessage msg = new AclMessage(AclMessage.Performative.REQUEST);

        assertNull(msg.getContent());

        msg.setContent("content");
        assertEquals("content", msg.getContent());

        msg.setContent(null);
        assertNull(msg.getContent());
    }


    public void test_setContentObject_Error() throws Exception {
        AclMessage msg = new AclMessage(AclMessage.Performative.REQUEST);

        try {
            Serializable notReallySerializable = new Serializable() {
            };
            msg.setContentObject(notReallySerializable);

            fail();
        }
        catch (BadContentObjectException ex) {
            assertTrue(ex instanceof RuntimeException);
            assertTrue(ex.getCause() instanceof NotSerializableException);
        }
    }


    public void test_createReply() throws Exception {
        AclMessage msg = new AclMessage(AclMessage.Performative.REQUEST);
        msg.setContentObject("msg content");
        msg.addReceiver(new Aid("toto"));
        msg.setSender(new Aid("titi"));
        msg.setLanguage("fipa-sl");
        msg.setOntology("fipa-management");
        msg.setProtocol("fipa-request");
        msg.addUserDefinedParameter("bobo", "value");

        AclMessage reply = msg.createReply();

        assertNotNull(reply);
        assertReceiverLocalName("titi", reply);
        assertEquals("value", reply.getUserDefinedParameter("bobo"));
        assertEquals("fipa-sl", msg.getLanguage());
        assertEquals("fipa-management", msg.getOntology());
        assertEquals("fipa-request", msg.getProtocol());
    }


    public void test_createReply_withPerformative()
          throws Exception {
        AclMessage msg = new AclMessage(AclMessage.Performative.REQUEST);

        AclMessage reply = msg.createReply(AclMessage.Performative.INFORM);

        assertNotNull(reply);
        assertEquals(AclMessage.Performative.INFORM, reply.getPerformative());
    }


    public void test_performativeToString() throws Exception {
        assertEquals("AGREE", AclMessage.performativeToString(AclMessage.Performative.AGREE));
    }


    public void test_messageToString() throws Exception {
        AclMessage aclMessage = new AclMessage((AclMessage.Performative.AGREE));
        aclMessage.setLanguage("french");
        assertEquals("(AGREE\n :language  french )", aclMessage.toFipaACLString());
        assertEquals("(AGREE :language  french )", aclMessage.toString());
    }


    public void test_encodeUserId() throws Exception {
        UserId userId = UserId.createId("login", "pwd");

        AclMessage aclMessage = new AclMessage(AclMessage.Performative.INFORM);
        aclMessage.encodeUserId(userId);

        assertEquals(userId, aclMessage.decodeUserId());
    }


    public void test_decodeUserId_fromNonEncodedMessage() throws Exception {
        AclMessage aclMessage = new AclMessage(AclMessage.Performative.INFORM);
        assertNull(aclMessage.decodeUserId());
    }


    public void test_decodeUserId_inARepliedMessage() {
        UserId userId = UserId.createId("login", "pwd");
        AclMessage message = new AclMessage(AclMessage.Performative.INFORM);
        message.encodeUserId(userId);

        AclMessage reply = message.createReply();

        assertEquals(userId, reply.decodeUserId());
    }


    public void test_setReplyByDate() throws Exception {
        AclMessage message = new AclMessage(AclMessage.Performative.INFORM);

        assertEquals(null, message.getReplyByDate());

        long timeout = 500;
        Date expiryDate = new Date(System.currentTimeMillis() + timeout);

        message.setReplyByDate(expiryDate);
        assertEquals(expiryDate, message.getReplyByDate());
    }


    public void test_createAclMessageForPlatformShutdown() throws Exception {
        DummyAgent agent = new DummyAgent() {
            @Override
            public Aid getAID() {
                return new Aid("dummy");
            }


            @Override
            public Aid getAMS() {
                return new Aid("AMS");
            }
        };

        AclMessage message = AclMessage.createMessageForPlatformShutdown(agent);

        ACLMessage aclMessage = JadeWrapper.unwrapp(message);

        assertEquals(FIPANames.InteractionProtocol.FIPA_REQUEST, aclMessage.getProtocol());
        assertEquals(FIPANames.ContentLanguage.FIPA_SL0, aclMessage.getLanguage());
        assertEquals(JADEManagementOntology.NAME, aclMessage.getOntology());
        assertEquals(agent.getAID().getLocalName(), aclMessage.getSender().getLocalName());

        Aid ams = (Aid)message.getAllReceiver().next();
        assertEquals(JadeWrapper.unwrapp(agent.getAMS()).getName(), ams.getName());
    }


    @Override
    protected void setUp() throws Exception {
        containerFixture.doSetUp();
        containerFixture.startContainer(ConnectionType.NO_CONNECTION);
    }


    @Override
    protected void tearDown() throws Exception {
        containerFixture.doTearDown();
    }


    private void assertReceiverLocalName(String expected, AclMessage msg) {
        Iterator allReceiver = msg.getAllReceiver();
        assertTrue(allReceiver.hasNext());
        assertAIDLocalName(expected, ((Aid)allReceiver.next()));
    }


    private void assertReplyToLocalName(String expected, AclMessage msg) {
        Iterator allReplyTo = msg.getAllReplyTo();
        assertTrue(allReplyTo.hasNext());
        assertAIDLocalName(expected, ((Aid)allReplyTo.next()));
    }


    private void assertNoReceiver(AclMessage msg) {
        Iterator allReceiver = msg.getAllReceiver();
        assertFalse(allReceiver.hasNext());
    }


    private void assertNoReplyToReceivers(AclMessage msg) {
        Iterator allReplyTo = msg.getAllReplyTo();
        assertFalse(allReplyTo.hasNext());
    }


    private void assertAIDLocalName(String expected, Aid aid) {
        assertEquals(expected, aid.getLocalName());
    }
}
