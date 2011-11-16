/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
/**
 * Handler du comportement de l'agent 'Participant' du protocole fipa-request ou fipa-query {@link
 * jade.domain.FIPANames.InteractionProtocol}.
 *
 * @see RequestProtocol
 */
public interface RequestParticipantHandler {
    AclMessage handleRequest(AclMessage request)
          throws RefuseException, NotUnderstoodException;


    AclMessage executeRequest(AclMessage request, AclMessage agreement)
          throws FailureException;
}
