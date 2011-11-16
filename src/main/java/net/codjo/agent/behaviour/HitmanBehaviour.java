/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.behaviour;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.JadeWrapper;
import org.apache.log4j.Logger;
/**
 * Comportement permettant à un agent de demander à l'AMS d'en tuer un autre.
 */
public class HitmanBehaviour extends Behaviour {
    private static final Logger LOG = Logger.getLogger(HitmanBehaviour.class.getName());
    private final Aid agentToKillAid;


    public HitmanBehaviour(Agent killerAgent, Aid agentToKillAid) {
        this.agentToKillAid = agentToKillAid;
        final jade.lang.acl.ACLMessage killRequest =
              createKillRequestMessage(killerAgent);
        JadeWrapper.wrapp(this,
                          new AMSRequesterBehaviour(JadeWrapper.unwrapp(killerAgent), killRequest));
    }


    public Aid getAgentToKillAid() {
        return agentToKillAid;
    }


    @Override
    protected void action() {}


    @Override
    public boolean done() {
        return JadeWrapper.unwrapp(this).done();
    }


    private jade.lang.acl.ACLMessage createKillRequestMessage(Agent killerAgent) {
        jade.domain.JADEAgentManagement.KillAgent killAgentAction =
              new jade.domain.JADEAgentManagement.KillAgent();
        killAgentAction.setAgent(JadeWrapper.unwrapp(agentToKillAid));

        jade.content.onto.basic.Action action = new jade.content.onto.basic.Action();
        action.setActor(JadeWrapper.unwrapp(killerAgent).getAMS());
        action.setAction(killAgentAction);

        return createKillRequest(action, killerAgent);
    }


    private jade.lang.acl.ACLMessage createKillRequest(jade.content.onto.basic.Action action,
                                                       Agent killerAgent) {
        jade.content.ContentManager contentManager = JadeWrapper.unwrapp(killerAgent).getContentManager();
        contentManager.registerLanguage(new jade.content.lang.sl.SLCodec(),
                                        jade.domain.FIPANames.ContentLanguage.FIPA_SL0);
        contentManager.registerOntology(jade.domain.JADEAgentManagement.JADEManagementOntology.getInstance());

        jade.lang.acl.ACLMessage requestMsg = new jade.lang.acl.ACLMessage(jade.lang.acl.ACLMessage.REQUEST);
        requestMsg.setSender(JadeWrapper.unwrapp(killerAgent.getAID()));
        requestMsg.addReceiver(JadeWrapper.unwrapp(killerAgent).getAMS());
        requestMsg.setProtocol(jade.domain.FIPANames.InteractionProtocol.FIPA_REQUEST);
        requestMsg.setLanguage(jade.domain.FIPANames.ContentLanguage.FIPA_SL0);
        requestMsg.setOntology(jade.domain.JADEAgentManagement.JADEManagementOntology.NAME);
        try {
            contentManager.fillContent(requestMsg, action);
        }
        catch (Exception e) {
            LOG.error("Impossible de construire le message pour tuer " + agentToKillAid, e);
        }
        return requestMsg;
    }


    protected static class AMSRequesterBehaviour extends jade.proto.SimpleAchieveREInitiator {
        AMSRequesterBehaviour(jade.core.Agent owner, jade.lang.acl.ACLMessage request) {
            super(owner, request);
        }
    }
}
