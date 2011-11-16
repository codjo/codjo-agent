/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
/**
 *
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Aid;
import net.codjo.agent.UserId;
import java.util.Iterator;
/**
 */
public abstract class MessageField {
    public static final MessageField PERFORMATIVE =
          new MessageField("performative") {
              @Override
              public String retrieveValue(AclMessage message) {
                  return AclMessage.performativeToString(message.getPerformative());
              }
          };
    public static final MessageField PROTOCOL =
          new MessageField("protocol") {
              @Override
              public String retrieveValue(AclMessage message) {
                  return message.getProtocol();
              }
          };
    public static final MessageField CONTENT =
          new MessageField("content") {
              @Override
              public String retrieveValue(AclMessage message) {
                  return "" + message.getContentObject();
              }
          };
    public static final MessageField CONVERSATION_ID =
          new MessageField("conversationId") {
              @Override
              String retrieveValue(AclMessage message) {
                  return message.getConversationId();
              }
          };
    public static final MessageField LANGUAGE =
          new MessageField("language") {
              @Override
              String retrieveValue(AclMessage message) {
                  return message.getLanguage();
              }
          };
    public static final MessageField USER_ID =
          new MessageField("userId") {
              @Override
              String retrieveValue(AclMessage message) {
                  UserId userId = message.decodeUserId();
                  return userId == null ? "null" : userId.encode();
              }
          };
    public static final MessageField SENDER =
          new MessageField("sender") {
              @Override
              String retrieveValue(AclMessage message) {
                  Aid sender = message.getSender();
                  return sender == null ? "null" : sender.getLocalName();
              }
          };
    public static final MessageField RECEIVER = new ReceiverField();
    private final String name;


    private MessageField(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return getName();
    }


    String getName() {
        return name;
    }


    abstract String retrieveValue(AclMessage message);


    private static class ReceiverField extends MessageField {
        ReceiverField() {
            super("receiver");
        }


        @Override
        String retrieveValue(AclMessage message) {
            StringBuffer buffer = new StringBuffer();
            Iterator allReceiver = message.getAllReceiver();
            while (allReceiver.hasNext()) {
                Aid aid = (Aid)allReceiver.next();
                if (buffer.length() > 0) {
                    buffer.append(", ");
                }
                buffer.append(aid.getLocalName());
            }
            return (buffer.length() == 0) ? "null" : buffer.toString();
        }
    }
}
