/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
/**
 * Exception levée pour refuser une interaction.
 */
public class RefuseException extends jade.domain.FIPAAgentManagement.RefuseException {
    public RefuseException(String message) {
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
