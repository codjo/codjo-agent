/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.agent.protocol.RequestProtocol;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
/**
 * Représente un message agent.
 */
public class AclMessage {
    public static final String XML_LANGUAGE = "xml-language";
    public static final String OBJECT_LANGUAGE = "object-language";
    public static final String ZIP_ENCODING = "zip";

    private static final String SIGNATURE_USER_ID = "Signature.userId";

    private final jade.lang.acl.ACLMessage jadeMessage;
    private boolean isContentSerialized;


    public AclMessage(Performative performative) {
        jadeMessage = new jade.lang.acl.ACLMessage(performative.toJade());
    }


    public AclMessage(Performative performative, String protocol) {
        jadeMessage = new jade.lang.acl.ACLMessage(performative.toJade());
        jadeMessage.setProtocol(protocol);
    }


    AclMessage(jade.lang.acl.ACLMessage jadeMessage) {
        this.jadeMessage = jadeMessage;
    }


    public Performative getPerformative() {
        return toPerformative(jadeMessage.getPerformative());
    }


    public void setPerformative(AclMessage.Performative performative) {
        jadeMessage.setPerformative(performative.toJade());
    }


    public void addReceiver(Aid aid) {
        jadeMessage.addReceiver(aid.getJadeAID());
    }


    public Iterator getAllReceiver() {
        return new AclMessage.IteratorAdapter(jadeMessage.getAllReceiver());
    }


    public void clearReceivers() {
        jadeMessage.clearAllReceiver();
    }


    public void addReplyTo(Aid aid) {
        jadeMessage.addReplyTo(aid.getJadeAID());
    }


    public boolean removeReplyTo(Aid aid) {
        return jadeMessage.removeReplyTo(aid.getJadeAID());
    }


    public Iterator getAllReplyTo() {
        return new AclMessage.IteratorAdapter(jadeMessage.getAllReplyTo());
    }


    public void clearAllReplyTo() {
        jadeMessage.clearAllReplyTo();
    }


    public void setSender(Aid aid) {
        jadeMessage.setSender(aid.getJadeAID());
    }


    public Aid getSender() {
        if (jadeMessage.getSender() == null) {
            return null;
        }
        return new Aid(jadeMessage.getSender());
    }


    jade.lang.acl.ACLMessage getJadeMessage() {
        return jadeMessage;
    }


    public AclMessage createReply() {
        return new AclMessage(getJadeMessage().createReply());
    }


    public AclMessage createReply(Performative performative) {
        jade.lang.acl.ACLMessage jadeReply = getJadeMessage().createReply();
        jadeReply.setPerformative(performative.toJade());
        return new AclMessage(jadeReply);
    }


    public void setConversationId(String conversationId) {
        jadeMessage.setConversationId(conversationId);
    }


    public String getConversationId() {
        return jadeMessage.getConversationId();
    }


    public void setEncoding(String encoding) {
        jadeMessage.setEncoding(encoding);
    }


    public String getEncoding() {
        return jadeMessage.getEncoding();
    }


    boolean isContentSerialized() {
        return isContentSerialized;
    }


    public void setContentObject(Serializable serializable) {
        try {
            isContentSerialized = true;
            jadeMessage.setContentObject(serializable);
        }
        catch (IOException e) {
            throw new BadContentObjectException(e);
        }
    }


    public Serializable getContentObject() {
        try {
            return jadeMessage.getContentObject();
        }
        catch (jade.lang.acl.UnreadableException e) {
            throw new BadContentObjectException(e);
        }
    }


    public void setContent(String content) {
        isContentSerialized = false;
        jadeMessage.setContent(content);
    }


    public String getContent() {
        return jadeMessage.getContent();
    }


    public void setByteSequenceContent(byte[] content) {
        isContentSerialized = false;
        jadeMessage.setByteSequenceContent(content);
    }


    public byte[] getByteSequenceContent() {
        return jadeMessage.getByteSequenceContent();
    }


    public void addUserDefinedParameter(String key, String value) {
        jadeMessage.addUserDefinedParameter(key, value);
    }


    public String getUserDefinedParameter(String key) {
        String parameter = jadeMessage.getUserDefinedParameter(key);
        if (parameter == null) {
            throw new IllegalArgumentException();
        }
        return parameter;
    }


    public void setLanguage(String objectLanguage) {
        jadeMessage.setLanguage(objectLanguage);
    }


    public String getLanguage() {
        return jadeMessage.getLanguage();
    }


    public void setOntology(String ontologyName) {
        jadeMessage.setOntology(ontologyName);
    }


    public String getOntology() {
        return jadeMessage.getOntology();
    }


    public void setProtocol(String protocol) {
        jadeMessage.setProtocol(protocol);
    }


    public String getProtocol() {
        return jadeMessage.getProtocol();
    }


    public static String performativeToString(Performative performative) {
        return jade.lang.acl.ACLMessage.getPerformative(performative.toJade());
    }


    public String toFipaACLString() {
        return getJadeMessage().toString();
    }


    @Override
    public String toString() {
        return getJadeMessage().toString().replaceAll("\n", "");
    }


    public void encodeUserId(UserId userId) {
        addUserDefinedParameter(SIGNATURE_USER_ID, userId.encode());
    }


    public UserId decodeUserId() {
        String keyValue = jadeMessage.getUserDefinedParameter(SIGNATURE_USER_ID);
        return keyValue == null ? null : UserId.decodeUserId(keyValue);
    }


    /**
     * La methode est 'caché' dans AclMessage pour éviter que IDEA tente de proposer cette methode dans la
     * completion.
     *
     * @noinspection OverlyComplexMethod
     */
    static Performative toPerformative(int jadeValue) {
        switch (jadeValue) {
            case jade.lang.acl.ACLMessage.QUERY_REF:
                return Performative.QUERY;
            case jade.lang.acl.ACLMessage.REQUEST:
                return Performative.REQUEST;
            case jade.lang.acl.ACLMessage.INFORM:
                return Performative.INFORM;
            case jade.lang.acl.ACLMessage.SUBSCRIBE:
                return Performative.SUBSCRIBE;
            case jade.lang.acl.ACLMessage.FAILURE:
                return Performative.FAILURE;
            case jade.lang.acl.ACLMessage.AGREE:
                return Performative.AGREE;
            case jade.lang.acl.ACLMessage.REFUSE:
                return Performative.REFUSE;
            case jade.lang.acl.ACLMessage.NOT_UNDERSTOOD:
                return Performative.NOT_UNDERSTOOD;
            case jade.lang.acl.ACLMessage.CANCEL:
                return Performative.CANCEL;
            case jade.lang.acl.ACLMessage.CFP:
                return Performative.CFP;
            case jade.lang.acl.ACLMessage.PROPOSE:
                return Performative.PROPOSE;
            case jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL:
                return Performative.ACCEPT_PROPOSAL;
            case jade.lang.acl.ACLMessage.REJECT_PROPOSAL:
                return Performative.REJECT_PROPOSAL;
            default:
                return new Performative(jadeValue);
        }
    }


    public void setReplyByDate(Date expiryDate) {
        jadeMessage.setReplyByDate(expiryDate);
    }


    public Date getReplyByDate() {
        return jadeMessage.getReplyByDate();
    }


    public static AclMessage createMessageForPlatformShutdown(Agent agent) {
        AclMessage message = new AclMessage(Performative.REQUEST);
        message.setProtocol(RequestProtocol.REQUEST);
        message.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
        message.setSender(agent.getAID());
        message.addReceiver(agent.getAMS());

        jade.lang.acl.ACLMessage requestMsg = JadeWrapper.unwrapp(message);
        requestMsg.setOntology(JADEManagementOntology.NAME);
        requestMsg.addReceiver(JadeWrapper.unwrapp(agent.getAMS()));

        jade.core.Agent jadeAgent = JadeWrapper.unwrapp(agent);
        jadeAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
        jadeAgent.getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);

        Action action = new Action();
        action.setActor(JadeWrapper.unwrapp(agent.getAMS()));
        action.setAction(new ShutdownPlatform());

        try {
            jadeAgent.getContentManager().fillContent(requestMsg, action);
        }
        catch (Exception exception) {
            throw new RuntimeException("Impossible de remplir le contenu du message !", exception);
        }

        return message;
    }


    private static class IteratorAdapter implements Iterator {
        private jade.util.leap.Iterator subIterator;


        IteratorAdapter(jade.util.leap.Iterator allReceiver) {
            subIterator = allReceiver;
        }


        public boolean hasNext() {
            return subIterator.hasNext();
        }


        public Object next() {
            return new Aid((jade.core.AID)subIterator.next());
        }


        public void remove() {
            subIterator.remove();
        }
    }

    public static class Performative {
        private int jadeValue;
        public static final Performative QUERY = new Performative(jade.lang.acl.ACLMessage.QUERY_REF);
        public static final Performative REQUEST = new Performative(jade.lang.acl.ACLMessage.REQUEST);
        public static final Performative INFORM = new Performative(jade.lang.acl.ACLMessage.INFORM);
        public static final Performative SUBSCRIBE = new Performative(jade.lang.acl.ACLMessage.SUBSCRIBE);
        public static final Performative FAILURE = new Performative(jade.lang.acl.ACLMessage.FAILURE);
        public static final Performative AGREE = new Performative(jade.lang.acl.ACLMessage.AGREE);
        public static final Performative REFUSE = new Performative(jade.lang.acl.ACLMessage.REFUSE);
        public static final Performative NOT_UNDERSTOOD =
              new Performative(jade.lang.acl.ACLMessage.NOT_UNDERSTOOD);
        public static final Performative CANCEL = new Performative(jade.lang.acl.ACLMessage.CANCEL);
        // CFP protocol
        public static final Performative CFP = new Performative(jade.lang.acl.ACLMessage.CFP);
        public static final Performative PROPOSE = new Performative(jade.lang.acl.ACLMessage.PROPOSE);
        public static final Performative ACCEPT_PROPOSAL = new Performative(
              jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL);
        public static final Performative REJECT_PROPOSAL = new Performative(
              jade.lang.acl.ACLMessage.REJECT_PROPOSAL);


        Performative(int value) {
            this.jadeValue = value;
        }


        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            //noinspection SimplifiableIfStatement
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            return jadeValue == ((Performative)object).jadeValue;
        }


        @Override
        public int hashCode() {
            return jadeValue;
        }


        @Override
        public String toString() {
            return AclMessage.performativeToString(this);
        }


        int toJade() {
            return jadeValue;
        }
    }
}
