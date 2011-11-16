/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Template permettant de filtrer des messages par conversationId ou par MatchExpression.
 *
 * @see Agent#receive(MessageTemplate)
 */
public final class MessageTemplate {
    private jade.lang.acl.MessageTemplate jadeTemplate;


    MessageTemplate(jade.lang.acl.MessageTemplate template) {
        this.jadeTemplate = template;
    }


    public final boolean match(AclMessage message) {
        return jadeTemplate.match(message.getJadeMessage());
    }


    public static MessageTemplate matchConversationId(String value) {
        return new MessageTemplate(jade.lang.acl.MessageTemplate.MatchConversationId(
              value));
    }


    public static MessageTemplate matchContent(String content) {
        return new MessageTemplate(jade.lang.acl.MessageTemplate.MatchContent(content));
    }


    public static MessageTemplate matchSender(Aid aid) {
        return new MessageTemplate(jade.lang.acl.MessageTemplate.MatchSender(
              JadeWrapper.unwrapp(aid)));
    }


    public static MessageTemplate matchPerformative(AclMessage.Performative performative) {
        return new MessageTemplate(jade.lang.acl.MessageTemplate.MatchPerformative(performative.toJade()));
    }


    public static MessageTemplate matchProtocol(String protocol) {
        return new MessageTemplate(jade.lang.acl.MessageTemplate.MatchProtocol(protocol));
    }


    public static MessageTemplate matchWith(MatchExpression matchExpression) {
        return new MessageTemplate(new jade.lang.acl.MessageTemplate(
              new MessageTemplate.JadeDelegator(matchExpression)));
    }


    public static MessageTemplate matchLanguage(String value) {
        return new MessageTemplate(jade.lang.acl.MessageTemplate.MatchLanguage(value));
    }


    public static MessageTemplate matchOntology(String value) {
        return new MessageTemplate(jade.lang.acl.MessageTemplate.MatchOntology(value));
    }


    public static MessageTemplate and(MessageTemplate left, MessageTemplate right) {
        return new MessageTemplate(jade.lang.acl.MessageTemplate.and(left.jadeTemplate,
                                                                     right.jadeTemplate));
    }


    public static MessageTemplate or(MessageTemplate left, MessageTemplate right) {
        return new MessageTemplate(jade.lang.acl.MessageTemplate.or(left.jadeTemplate,
                                                                    right.jadeTemplate));
    }


    jade.lang.acl.MessageTemplate getJadeTemplate() {
        return jadeTemplate;
    }


    @Override
    public String toString() {
        return getJadeTemplate().toString();
    }


    public static MessageTemplate matchAll() {
        return new MessageTemplate(jade.lang.acl.MessageTemplate.MatchAll());
    }


    public static interface MatchExpression {
        public boolean match(AclMessage aclMessage);
    }

    /**
     * delegation de jade vers la couche agent
     */
    private static class JadeDelegator
          implements jade.lang.acl.MessageTemplate.MatchExpression {
        private final MatchExpression matchExpression;


        private JadeDelegator(MatchExpression matchExpression) {
            this.matchExpression = matchExpression;
        }


        public boolean match(jade.lang.acl.ACLMessage aclMessage) {
            return matchExpression.match(new AclMessage(aclMessage));
        }


        @Override
        public String toString() {
            return matchExpression.toString();
        }
    }
}
