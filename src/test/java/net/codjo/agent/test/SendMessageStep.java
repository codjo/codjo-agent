/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.test.common.LogString;
import java.io.Serializable;
/**
 *
 */
public class SendMessageStep extends AbstractStep {
    private AclMessage message;


    public SendMessageStep(TesterAgentRecorder recorder, AclMessage message) {
        super(recorder);
        this.message = message;
    }


    public SendMessageStep(TesterAgentRecorder recorder,
                           AclMessage.Performative performative,
                           String protocol,
                           Aid receiver,
                           String content) {
        this(recorder, createAclMessage(performative, protocol, receiver, content));
    }


    public SendMessageStep(TesterAgentRecorder recorder,
                           AclMessage.Performative performative,
                           Aid receiver,
                           Serializable content) {
        this(recorder, createAclMessage(performative, null, receiver, content));
    }


    @Override
    protected void doRun(Agent agent) {
        agent.send(getMessage());
    }


    @Override
    protected boolean doRunDone() {
        return true;
    }


    @Override
    protected AclMessage getMessage() {
        return message;
    }


    @Deprecated
    public SendMessageStep addSubStep(SubStep subStep) {
        return (SendMessageStep)addSubStepImpl(subStep);
    }


    public SendMessageStep add(SubStep subStep) {
        return (SendMessageStep)addSubStepImpl(subStep);
    }


    public SendMessageStep log(LogString log, String info) {
        addLog(log, info);
        return this;
    }


    private static AclMessage createAclMessage(AclMessage.Performative performative,
                                               String protocol,
                                               Aid receiver,
                                               Serializable content) {
        AclMessage message = createAclMessage(performative, protocol, receiver);
        message.setContentObject(content);
        return message;
    }


    private static AclMessage createAclMessage(AclMessage.Performative performative,
                                               String protocol,
                                               Aid receiver,
                                               String content) {
        AclMessage message = createAclMessage(performative, protocol, receiver);
        message.setContent(content);
        return message;
    }


    private static AclMessage createAclMessage(AclMessage.Performative performative,
                                               String protocol,
                                               Aid receiver) {
        AclMessage message = new AclMessage(performative);
        message.setProtocol(protocol);
        message.addReceiver(receiver);
        return message;
    }


    @Override
    public String toString() {
        return "SendMessageStep[" + message + ']';
    }
}
