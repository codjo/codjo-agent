package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
/**
 *
 */
public abstract class AbstractRequestParticipantHandler implements RequestParticipantHandler {
    public AclMessage handleRequest(AclMessage request) throws RefuseException, NotUnderstoodException {
        return null;
    }
}
