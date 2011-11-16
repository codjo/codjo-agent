/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.test.SemaphoreByToken;
import net.codjo.test.common.LogString;
/**
 * Mock de la classe {@link InitiatorHandler}.
 */
class InitiatorHandlerMock implements InitiatorHandler {
    public static final SemaphoreByToken.Token MESSAGE_RECEIVED = new SemaphoreByToken.Token(
          "message_received");
    protected LogString log;
    protected SemaphoreByToken semaphore = new SemaphoreByToken();


    InitiatorHandlerMock(LogString log) {
        this.log = log;
    }


    public void handleAgree(AclMessage agree) {
        log.call("handleAgree", agree.getContentObject());
        semaphore.release(MESSAGE_RECEIVED);
    }


    public void handleRefuse(AclMessage refuse) {
        log.call("handleRefuse", refuse.getContentObject());
        semaphore.release(MESSAGE_RECEIVED);
    }


    public void handleInform(AclMessage inform) {
        log.call("handleInform", inform.getContentObject());
        semaphore.release(MESSAGE_RECEIVED);
    }


    public void handleFailure(AclMessage failure) {
        log.call("handleFailure", failure.getContentObject());
        semaphore.release(MESSAGE_RECEIVED);
    }


    public void handleOutOfSequence(AclMessage outOfSequence) {
        log.call("handleOutOfSequence", outOfSequence.getContentObject());
        semaphore.release(MESSAGE_RECEIVED);
    }


    public void handleNotUnderstood(AclMessage notUnderstoodMessage) {
        log.call("handleNotUnderstood", notUnderstoodMessage.getContentObject());
        semaphore.release(MESSAGE_RECEIVED);
    }


    public void waitForMessage() {
        semaphore.waitFor(MESSAGE_RECEIVED);
    }
}
