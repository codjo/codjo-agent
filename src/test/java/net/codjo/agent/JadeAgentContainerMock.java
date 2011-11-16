/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.test.common.LogString;
import jade.domain.AMSEventQueueFeeder;
import jade.core.AID;
/**
 * Mock d'un {@link jade.core.AgentContainer}.
 */
class JadeAgentContainerMock implements jade.core.AgentContainer {
    private JadeServiceManagerMock jadeServiceManagerMock;


    JadeAgentContainerMock(LogString log) {
        jadeServiceManagerMock =
              new JadeServiceManagerMock(new LogString("serviceManager", log));
    }


    public jade.core.ContainerID getID() {
        return null;
    }


    public String getPlatformID() {
        return null;
    }


    public jade.core.NodeDescriptor getNodeDescriptor() {
        return null;
    }


    public jade.core.MainContainer getMain() {
        return null;
    }


    public jade.core.ServiceManager getServiceManager() {
        return jadeServiceManagerMock;
    }


    public jade.core.ServiceFinder getServiceFinder() {
        return null;
    }


    public jade.core.AID getAMS() {
        return null;
    }


    public jade.core.AID getDefaultDF() {
        return null;
    }


    public void initAgent(jade.core.AID agentID, jade.core.Agent instance,
                          jade.security.JADEPrincipal ownerPrincipal,
                          jade.security.Credentials initialCredentials) {}


    public void powerUpLocalAgent(jade.core.AID agentID) {}


    public jade.core.Agent addLocalAgent(jade.core.AID id, jade.core.Agent agent) {
        return null;
    }


    public void removeLocalAgent(jade.core.AID id) {}


    public jade.core.Agent acquireLocalAgent(jade.core.AID id) {
        return null;
    }


    public void releaseLocalAgent(jade.core.AID id) {}


    public AID[] agentNames() {
        return new AID[0];
    }


    public void fillListFromMessageQueue(jade.util.leap.List messages,
                                         jade.core.Agent agent) {}


    public void fillListFromReadyBehaviours(jade.util.leap.List behaviours,
                                            jade.core.Agent agent) {}


    public void fillListFromBlockedBehaviours(jade.util.leap.List behaviours,
                                              jade.core.Agent agent) {}


    public void addAddressToLocalAgents(String address) {}


    public void removeAddressFromLocalAgents(String address) {}


    public boolean postMessageToLocalAgent(jade.lang.acl.ACLMessage msg,
                                           jade.core.AID receiverID) {
        return false;
    }


    public boolean livesHere(jade.core.AID id) {
        return false;
    }


    public jade.core.Location here() {
        return null;
    }


    public void shutDown() {}


    public void becomeLeader(AMSEventQueueFeeder amsEventQueueFeeder) {}


    public jade.core.Service getLastAddedService() {
        return jadeServiceManagerMock.getLastAddedService();
    }
}
