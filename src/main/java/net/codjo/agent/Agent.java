/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Un Agent.
 */
public class Agent {
    private jade.core.Agent jadeAgent = createJadeAgentAdapter();


    public Agent() {
    }


    public Agent(Behaviour behaviour) {
        addBehaviour(behaviour);
    }


    protected void setup() {
    }


    protected void tearDown() {
    }


    public Aid getAID() {
        return JadeWrapper.wrapp(jadeAgent.getAID());
    }


    public Aid getAMS() {
        return JadeWrapper.wrapp(jadeAgent.getAMS());
    }


    public void addBehaviour(Behaviour behaviour) {
        behaviour.setAgent(this);
        jadeAgent.addBehaviour(behaviour.getJadeBehaviour());
    }


    public void removeBehaviour(Behaviour behaviour) {
        jadeAgent.removeBehaviour(behaviour.getJadeBehaviour());
    }


    public void send(AclMessage aclMessage) {
        jadeAgent.send(aclMessage.getJadeMessage());
    }


    public void doWait(long millis) {
        jadeAgent.doWait(millis);
    }


    public AclMessage receive() {
        jade.lang.acl.ACLMessage jadeMessage = jadeAgent.receive();
        if (jadeMessage == null) {
            return null;
        }
        return new AclMessage(jadeMessage);
    }


    public AclMessage receive(MessageTemplate template) {
        jade.lang.acl.ACLMessage jadeMessage =
              jadeAgent.receive(template.getJadeTemplate());
        if (jadeMessage == null) {
            return null;
        }
        return new AclMessage(jadeMessage);
    }


    @Deprecated
    public AclMessage blockingReceive() {
        jade.lang.acl.ACLMessage jadeMessage = jadeAgent.blockingReceive();
        if (jadeMessage == null) {
            return null;
        }
        return new AclMessage(jadeMessage);
    }


    @Deprecated
    public AclMessage blockingReceive(MessageTemplate template) {
        jade.lang.acl.ACLMessage jadeMessage =
              jadeAgent.blockingReceive(template.getJadeTemplate());
        if (jadeMessage == null) {
            return null;
        }
        return new AclMessage(jadeMessage);
    }


    @Deprecated
    public AclMessage blockingReceive(long timeout) {
        jade.lang.acl.ACLMessage jadeMessage =
              jadeAgent.blockingReceive(timeout);
        if (jadeMessage == null) {
            return null;
        }
        return new AclMessage(jadeMessage);
    }


    @Deprecated
    public AclMessage blockingReceive(MessageTemplate template, long timeout) {
        jade.lang.acl.ACLMessage jadeMessage =
              jadeAgent.blockingReceive(template.getJadeTemplate(), timeout);
        if (jadeMessage == null) {
            return null;
        }
        return new AclMessage(jadeMessage);
    }


    public void die() {
        jadeAgent.doDelete();
    }


    /**
     * Indique si un agent accepte la communication <tt>object-to-plugin</tt>. Cette communication ne
     * fonctionne que dans la même JVM. Par défaut la communication est désactivé.
     *
     * @param enabled   <code>true</code> active la communication.
     * @param queueSize Dans la cas ou la communication est activé, ce paramètre donne la taille de la liste
     *                  du buffer (si la valeur est 0, le buffer n'a pas de limite).
     *
     * @see #putO2AObject(Object,boolean)
     * @see #getO2AObject()
     */
    public void setEnabledO2ACommunication(boolean enabled, int queueSize) {
        getJadeAgent().setEnabledO2ACommunication(enabled, queueSize);
    }


    /**
     * Place un objet dans le flux <tt>object-to-agent</tt>.
     *
     * @param object   l'objet mis dans le flux
     * @param blocking Envoie synchronisé (bloque jusqu'a l'appel de getO2AObject).
     *
     * @see #getO2AObject()
     */
    public void putO2AObject(Object object, boolean blocking)
          throws InterruptedException {
        getJadeAgent().putO2AObject(object, blocking);
    }


    public Object getO2AObject() {
        return getJadeAgent().getO2AObject();
    }


    public ServiceHelper getHelper(String serviceName)
          throws ServiceException {
        try {
            return ServicesBootstrapper.getServiceHelper(this, serviceName);
        }
        catch (jade.core.ServiceException e) {
            throw new ServiceException(e);
        }
    }


    public String getAgentStateName() {
        return jadeAgent.getAgentState().getName();
    }


    jade.core.Agent createJadeAgentAdapter() {
        return new JadeAgentAdapter();
    }


    jade.core.Agent getJadeAgent() {
        return jadeAgent;
    }


    public AgentContainer getAgentContainer() {
        return new AgentContainer.Wrapper(getJadeAgent().getContainerController());
    }


    class JadeAgentAdapter extends jade.core.Agent {
        @Override
        protected void setup() {
            Agent.this.setup();
        }


        @Override
        protected void takeDown() {
            Agent.this.tearDown();
        }


        Agent getAgfAgent() {
            return Agent.this;
        }
    }
}
