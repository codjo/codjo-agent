package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import static net.codjo.agent.AclMessage.Performative.AGREE;
import static net.codjo.agent.AclMessage.Performative.INFORM;
import net.codjo.agent.Aid;
import static net.codjo.agent.MessageTemplate.matchContent;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import static net.codjo.agent.MessageTemplate.matchProtocol;
import net.codjo.agent.UserId;
import static net.codjo.agent.test.MessageBuilder.message;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import junit.framework.TestCase;

public class MessageBuilderTest extends TestCase {
    private Story story = new Story();


    @Override
    protected void setUp() throws Exception {
        story.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        story.doTearDown();
    }


    public void test_withContent() throws Exception {
        Serializable serializableContent = new ArrayList();
        AclMessage aclMessage =
              message(INFORM)
                    .withContent(serializableContent).get();
        assertEquals(serializableContent, aclMessage.getContentObject());
    }


    public void test_replyByDate() throws Exception {
        Date expiryDate = new Date(System.currentTimeMillis());
        AclMessage aclMessage =
              message(INFORM)
                    .replyByDate(expiryDate).get();
        assertEquals(expiryDate, aclMessage.getReplyByDate());
    }


    public void test_conversationId() throws Exception {
        AclMessage aclMessage =
              message(INFORM)
                    .usingConversationId("conv-id").get();
        assertEquals("conv-id", aclMessage.getConversationId());
    }


    public void test_encoding() throws Exception {
        AclMessage aclMessage =
              message(INFORM)
                    .usingEncoding("encoding").get();
        assertEquals("encoding", aclMessage.getEncoding());
    }


    public void test_byteSequenceContent() throws Exception {
        AclMessage aclMessage =
              message(INFORM)
                    .withByteSequenceContent(new byte[]{7}).get();
        assertEquals(7, aclMessage.getByteSequenceContent()[0]);
    }


    public void test_userDefinedParameter() throws Exception {
        AclMessage aclMessage =
              message(INFORM)
                    .addUserDefinedParameter("key", "value").get();
        assertEquals("value", aclMessage.getUserDefinedParameter("key"));
    }


    public void test_language() throws Exception {
        AclMessage aclMessage =
              message(INFORM)
                    .usingLanguage("french").get();
        assertEquals("french", aclMessage.getLanguage());
    }


    public void test_ontology() throws Exception {
        AclMessage aclMessage =
              message(INFORM)
                    .usingOntology("ontology").get();
        assertEquals("ontology", aclMessage.getOntology());
    }


    public void test_replyTo() throws Exception {
        story.record()
              .startTester("receiver1")
              .receiveMessage()
              .replyWith(AGREE, "");

        story.record()
              .startTester("anotherAgent")
              .receiveMessage()
              .assertReceivedMessage(matchPerformative(AGREE));

        story.record()
              .startTester("sender")
              .send(message(INFORM)
                    .to("receiver1")
                    .replyTo("anotherAgent"));

        story.execute();
    }


    public void test_replyTo_aid() throws Exception {
        story.record()
              .startTester("receiver1")
              .receiveMessage()
              .replyWith(AGREE, "");

        story.record()
              .startTester("anotherAgent")
              .receiveMessage()
              .assertReceivedMessage(matchPerformative(AGREE));

        story.record()
              .startTester("sender")
              .send(message(INFORM)
                    .to(new Aid("receiver1"))
                    .replyTo(new Aid("anotherAgent")));

        story.execute();
    }


    public void test_to() throws Exception {
        startReceiver("receiver1");
        startReceiver("receiver2");

        story.record()
              .startTester("sender")
              .send(message(INFORM)
                    .to("receiver1", "receiver2")
                    .usingProtocol("fipa-request")
                    .withContent("do it"));

        story.execute();
    }


    public void test_from() throws Exception {
        story.getAgentContainerFixture().startContainer();

        AclMessage aclMessage = message(INFORM).from("me").get();
        assertEquals("me", aclMessage.getSender().getLocalName());

        aclMessage = message(INFORM).from(new Aid("me")).get();
        assertEquals("me", aclMessage.getSender().getLocalName());
    }


    public void test_withUserId() throws Exception {
        AclMessage aclMessage =
              message(INFORM)
                    .withUserId(UserId.createId("login", "password")).get();
        assertEquals("login", aclMessage.decodeUserId().getLogin());
        assertEquals("password", aclMessage.decodeUserId().getPassword());
    }


    public void test_withUserId_fromLoginAndPassword() throws Exception {
        AclMessage aclMessage =
              message(INFORM)
                    .withUserId("login", "password").get();
        assertEquals("login", aclMessage.decodeUserId().getLogin());
        assertEquals("password", aclMessage.decodeUserId().getPassword());
    }


    private void startReceiver(String receiver) {
        story.record()
              .startTester(receiver)
              .receiveMessage()
              .assertReceivedMessage(matchPerformative(INFORM))
              .assertReceivedMessage(matchContent("do it"))
              .assertReceivedMessage(matchProtocol("fipa-request"));
    }
}
