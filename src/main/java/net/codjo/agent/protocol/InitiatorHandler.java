/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
/**
 * Handler du comportement de l'agent initiant un protocole fipa-subscribe {@link
 * jade.domain.FIPANames.InteractionProtocol}.
 *
 * @see net.codjo.agent.protocol.SubscribeProtocol
 */
public interface InitiatorHandler {
    public void handleAgree(AclMessage agree);


    public void handleRefuse(AclMessage refuse);


    public void handleInform(AclMessage inform);


    public void handleFailure(AclMessage failure);


    public void handleOutOfSequence(AclMessage outOfSequenceMessage);


    public void handleNotUnderstood(AclMessage notUnderstoodMessage);
}
