/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
/**
 * Handler du comportement de l'agent 'Participant' du protocole fipa-subscribe {@link
 * jade.domain.FIPANames.InteractionProtocol}.
 *
 * @see net.codjo.agent.protocol.SubscribeProtocol
 */
public interface SubscribeParticipantHandler {
    public void handleSubscribe(SubscribeParticipant.Subscription subscription)
          throws RefuseException, NotUnderstoodException;


    public void handleCancel(SubscribeParticipant.Subscription subscription)
          throws FailureException;
}
