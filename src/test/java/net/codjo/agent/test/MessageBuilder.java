package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Aid;
import net.codjo.agent.UserId;
import java.io.Serializable;
import java.util.Date;

public class MessageBuilder {
    private AclMessage aclMessage;


    private MessageBuilder(AclMessage.Performative performative) {
        aclMessage = new AclMessage(performative);
    }


    public static MessageBuilder message(AclMessage.Performative performative) {
        return new MessageBuilder(performative);
    }


    public MessageBuilder from(String sender) {
        return from(new Aid(sender));
    }


    public MessageBuilder from(Aid aid) {
        aclMessage.setSender(aid);
        return this;
    }


    public MessageBuilder to(String... receivers) {
        for (String receiver : receivers) {
            aclMessage.addReceiver(new Aid(receiver));
        }
        return this;
    }


    public MessageBuilder to(Aid... receivers) {
        for (Aid receiver : receivers) {
            aclMessage.addReceiver(receiver);
        }
        return this;
    }


    public MessageBuilder replyTo(String... receivers) {
        for (String receiver : receivers) {
            aclMessage.addReplyTo(new Aid(receiver));
        }
        return this;
    }


    public MessageBuilder replyTo(Aid... receivers) {
        for (Aid receiver : receivers) {
            aclMessage.addReplyTo(receiver);
        }
        return this;
    }


    public MessageBuilder usingProtocol(String protocol) {
        aclMessage.setProtocol(protocol);
        return this;
    }


    public MessageBuilder usingEncoding(String encoding) {
        aclMessage.setEncoding(encoding);
        return this;
    }


    public MessageBuilder usingLanguage(String language) {
        aclMessage.setLanguage(language);
        return this;
    }


    public MessageBuilder usingOntology(String ontologyName) {
        aclMessage.setOntology(ontologyName);
        return this;
    }


    public MessageBuilder usingConversationId(String conversationId) {
        aclMessage.setConversationId(conversationId);
        return this;
    }


    public MessageBuilder withContent(String content) {
        aclMessage.setContent(content);
        return this;
    }


    public MessageBuilder withContent(Serializable serializableContent) {
        aclMessage.setContentObject(serializableContent);
        return this;
    }


    public MessageBuilder withByteSequenceContent(byte[] content) {
        aclMessage.setByteSequenceContent(content);
        return this;
    }


    public MessageBuilder replyByDate(Date expiryDate) {
        aclMessage.setReplyByDate(expiryDate);
        return this;
    }


    public MessageBuilder addUserDefinedParameter(String key, String value) {
        aclMessage.addUserDefinedParameter(key, value);
        return this;
    }


    public MessageBuilder withUserId(UserId userId) {
        aclMessage.encodeUserId(userId);
        return this;
    }


    public MessageBuilder withUserId(String login, String password) {
        return withUserId(UserId.createId(login, password));
    }


    public AclMessage get() {
        return aclMessage;
    }
}
