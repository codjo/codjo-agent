/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
/**
 * Exception levée pour signifier l'échec lors de l'annulation d'une souscription.
 */
public class FailureException extends jade.domain.FIPAAgentManagement.FailureException {
    public FailureException(String message) {
        super(message);
    }

/*
    @Override
    public jade.lang.acl.ACLMessage getACLMessage() {
        jade.lang.acl.ACLMessage aclMessage = super.getACLMessage();
        try {
            aclMessage.setContentObject(aclMessage.getContent());
        }
        catch (IOException e) {
            ;
        }
        return aclMessage;
    }
*/
}
