/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.UserId;
import net.codjo.test.common.LogString;
import java.io.Serializable;
import junit.framework.AssertionFailedError;
/**
 *
 */
public class ReceiveMessageStep extends AbstractStep {
    private MessageTemplate messageTemplate;
    private AclMessage message;


    public ReceiveMessageStep(TesterAgentRecorder recorder, MessageTemplate messageTemplate) {
        super(recorder);
        this.messageTemplate = messageTemplate;
    }


    public ReceiveMessageStep(TesterAgentRecorder recorder) {
        this(recorder, MessageTemplate.matchAll());
    }


    @Override
    protected void doRun(Agent agent) {
        message = agent.receive(messageTemplate);
    }


    @Override
    protected boolean doRunDone() {
        return message != null;
    }


    @Override
    protected AclMessage getMessage() {
        return message;
    }


    public ReceiveMessageStep replyWithByteSequence(final AclMessage.Performative performative,
                                                    final byte[] content) {
        return add(new SubStep() {
            public void run(Agent agent, AclMessage message) {
                AclMessage reply = message.createReply(performative);
                reply.setByteSequenceContent(content);
                agent.send(reply);
            }
        });
    }


    public ReceiveMessageStep replyWithContent(final AclMessage.Performative performative,
                                               final Serializable content) {
        return add(new SubStep() {
            public void run(Agent agent, AclMessage message) {
                AclMessage reply = message.createReply(performative);
                reply.setContentObject(content);
                agent.send(reply);
            }
        });
    }


    public ReceiveMessageStep replyWith(final AclMessage.Performative performative, final String content) {
        return add(new SubStep() {
            public void run(Agent agent, AclMessage message) {
                AclMessage reply = message.createReply(performative);
                reply.setContent(content);
                agent.send(reply);
            }
        });
    }


    public ReceiveMessageStep log(LogString log, String info) {
        addLog(log, info);
        return this;
    }


    public ReceiveMessageStep assertReceivedMessage(final MessageTemplate expected) {
        return add(new SubStep() {
            public void run(Agent agent, AclMessage message) throws AssertionFailedError {
                if (!expected.match(message)) {
                    //noinspection UseOfSystemOutOrSystemErr
                    System.out.println("Received message :\n" + message.toFipaACLString());
                    throw new AssertionFailedError(
                          "Received message does not match the template: '" + expected + "'");
                }
            }
        });
    }


    public ReceiveMessageStep assertReceivedMessageUserId(final UserId expected) {
        return add(new SubStep() {
            public void run(Agent agent, AclMessage message) throws AssertionFailedError {
                UserId actual = message.decodeUserId();

                if (expected == actual
                    || (expected != null && expected.equals(actual))) {
                    return;
                }

                throw new AssertionFailedError("Received UserId does not match the expected one \n"
                                               + "\texpected : '" + userIdToString(expected) + "'\n"
                                               + "\tactual   : '" + userIdToString(actual) + "'");
            }
        });
    }


    @Deprecated
    public ReceiveMessageStep addSubStep(SubStep subStep) {
        return (ReceiveMessageStep)addSubStepImpl(subStep);
    }


    public ReceiveMessageStep add(SubStep subStep) {
        return (ReceiveMessageStep)addSubStepImpl(subStep);
    }


    public ReceiveMessageStep releaseSemaphore(final Semaphore semaphore) {
        return add(new SubStep() {
            public void run(Agent agent, AclMessage message)
                  throws AssertionFailedError {
                semaphore.release();
            }
        });
    }


    @Override
    public String toString() {
        return "ReceiveMessageStep[" + messageTemplate + ']';
    }


    String userIdToString(UserId expected) {
        return (expected == null ? "null" : expected.encode());
    }
}
