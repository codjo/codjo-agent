/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import org.picocontainer.MutablePicoContainer;
/**
 * Classe utilitaire permettant de passer entre la couche agent et jade et inversement.
 */
public final class JadeWrapper {
    private JadeWrapper() {
    }


    public static AclMessage wrapp(jade.lang.acl.ACLMessage jadeMessage) {
        if (jadeMessage == null) {
            return null;
        }
        return new AclMessage(jadeMessage);
    }


    public static jade.lang.acl.ACLMessage unwrapp(AclMessage message) {
        if (message == null) {
            return null;
        }
        return message.getJadeMessage();
    }


    public static jade.core.behaviours.Behaviour unwrapp(Behaviour behaviour) {
        if (behaviour == null) {
            return null;
        }
        return behaviour.getJadeBehaviour();
    }


    public static void wrapp(Behaviour behaviour,
                             jade.core.behaviours.Behaviour jadeBehaviour) {
        behaviour.setJadeBehaviour(jadeBehaviour);
    }


    public static jade.core.Agent unwrapp(Agent agent) {
        if (agent == null) {
            return null;
        }
        return agent.getJadeAgent();
    }


    public static Aid wrapp(jade.core.AID aid) {
        if (aid == null) {
            return null;
        }
        return new Aid(aid);
    }


    public static jade.core.AID unwrapp(Aid aid) {
        if (aid == null) {
            return null;
        }
        return aid.getJadeAID();
    }


    public static jade.wrapper.ContainerController unwrapp(AgentContainer container) {
        if (container == null) {
            return null;
        }
        return container.getJadeContainer();
    }


    public static MessageTemplate wrapp(jade.lang.acl.MessageTemplate jadetemplate) {
        if (jadetemplate == null) {
            return null;
        }
        return new MessageTemplate(jadetemplate);
    }


    public static jade.lang.acl.MessageTemplate unwrapp(MessageTemplate template) {
        if (template == null) {
            return null;
        }
        return template.getJadeTemplate();
    }


    public static jade.core.behaviours.DataStore unwrapp(DataStore dataStore) {
        return dataStore.getJadeDataStore();
    }


    public static DataStore wrapp(jade.core.behaviours.DataStore jadeStore) {
        return new DataStore(jadeStore);
    }


    public static MutablePicoContainer getPicoContainer(ContainerConfiguration configuration) {
        return ServicesBootstrapper.getPicoContainer(configuration);
    }


    public static void setPicoContainer(ContainerConfiguration configuration, MutablePicoContainer pico) {
        ServicesBootstrapper.setPicoContainer(configuration, pico);
    }


    public static void removePicoContainer(ContainerConfiguration configuration) {
        ServicesBootstrapper.removePicoContainer(configuration);
    }
}
