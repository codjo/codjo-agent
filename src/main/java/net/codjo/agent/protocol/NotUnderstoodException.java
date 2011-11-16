/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
/**
 * Exception levée pour signifier une imcompréhension lors d'une interaction.
 */
public class NotUnderstoodException
      extends jade.domain.FIPAAgentManagement.NotUnderstoodException {
    public NotUnderstoodException(String message) {
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
