/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.test.common.LogString;
import java.util.Iterator;
/**
 * Mock de {@link Agent}.
 *
 * <p> NB : Pour faire des tests dans un container il faut utiliser le {@link net.codjo.agent.test.DummyAgent}.
 * </p>
 *
 * @see net.codjo.agent.test.DummyAgent
 */
public class AgentMock extends Agent {
    private AclMessage aclMessage;
    private AclMessage lastSentMessage;
    private LogString log = new LogString();
    private Aid amsAID;
    private Aid agentAID;
    private ServiceHelper serviceHelperMock;
    private ServiceException getHelperFailure;


    public AgentMock() {
    }


    public AgentMock(LogString log) {
        this.log = log;
    }


    public AgentMock(Behaviour behaviour) {
        addBehaviour(behaviour);
    }


    @Override
    public Aid getAID() {
        return agentAID;
    }


    @Override
    public Aid getAMS() {
        return amsAID;
    }


    public LogString getLog() {
        return log;
    }


    @Override
    public void die() {
        log.call("die");
    }


    @Override
    protected void setup() {
        log.call("setup");
    }


    @Override
    protected void tearDown() {
        log.call("tearDown");
    }


    @Override
    public void send(AclMessage message) {
        log.call("agent.send", receiverToString(message), message.getConversationId(),
                 message.isContentSerialized() ?
                 message.getContentObject().toString() :
                 new String(message.getByteSequenceContent()));
        lastSentMessage = message;
    }


    public static String receiverToString(AclMessage aclMessage) {
        Iterator allReceiver = aclMessage.getAllReceiver();
        StringBuffer result = new StringBuffer();
        while (allReceiver.hasNext()) {
            result.append(((Aid)allReceiver.next()).getLocalName());
        }
        return result.toString();
    }


    @Override
    public AclMessage receive(MessageTemplate template) {
        log.call("agent.receive", template);
        return aclMessage;
    }


    @Override
    public void putO2AObject(Object object, boolean blocking)
          throws InterruptedException {
        log.call("putO2AObject", object, Boolean.toString(blocking));
    }


    @Override
    public AclMessage receive() {
        log.call("agent.receive");
        return aclMessage;
    }


    @Override
    public ServiceHelper getHelper(String serviceName)
          throws ServiceException {
        log.call("getHelper", serviceName);
        if (getHelperFailure != null) {
            getHelperFailure.fillInStackTrace();
            throw getHelperFailure;
        }
        return serviceHelperMock;
    }


    public void mockReceive(AclMessage message) {
        this.aclMessage = message;
    }


    public void mockResponse(AclMessage message) {
        this.aclMessage = message;
    }


    public AclMessage getLastSentMessage() {
        return lastSentMessage;
    }


    public void mockGetAMS(Aid aid) {
        this.amsAID = aid;
    }


    public void mockGetAID(Aid aid) {
        this.agentAID = aid;
    }


    public void mockGetHelper(ServiceHelper serviceHelper) {
        serviceHelperMock = serviceHelper;
    }


    public void mockGetHelperFailure(ServiceException exception) {
        getHelperFailure = exception;
    }
}
