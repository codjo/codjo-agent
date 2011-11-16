/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
/**
 * Protocole fipa-request et/ou fipa-query.
 *
 * @see <a href="http://4ddev02/vqwiki/jsp/Wiki?ProtocolesDeCommunicationFipa&highlight=FIPARequest">request</a>.
 * @see <a href="http://4ddev02/vqwiki/jsp/Wiki?ProtocolesDeCommunicationFipa&highlight=FIPAQuery">query</a>.
 */
public interface RequestProtocol {
    public static final String REQUEST = jade.domain.FIPANames.InteractionProtocol.FIPA_REQUEST;
    public static final String QUERY = jade.domain.FIPANames.InteractionProtocol.FIPA_QUERY;
}
