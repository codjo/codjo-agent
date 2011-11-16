/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Behaviour;
import net.codjo.agent.MessageTemplate;
import static net.codjo.agent.MessageTemplate.and;
import static net.codjo.agent.MessageTemplate.matchPerformative;
import static net.codjo.agent.MessageTemplate.matchSender;
/**
 * Dans le protocole 'fipa-subscribe', ce comportement permet de gérer le cas où 'Initiator' meurt après
 * souscription.
 *
 * @see DefaultSubscribeParticipantHandler
 */
public class SubscribeFailureBehaviour extends Behaviour {
    private final AbstractSubscribeParticipantHandler leaderHandler;
    private MessageTemplate failureTemplate;


    public SubscribeFailureBehaviour(AbstractSubscribeParticipantHandler leaderHandler) {
        this.leaderHandler = leaderHandler;
    }


    @Override
    protected void action() {
        if (failureTemplate == null) {
            failureTemplate = createFailureTemplate();
        }

        AclMessage failure = getAgent().receive(failureTemplate);

        if (failure == null) {
            block();
            return;
        }

        leaderHandler.removeSubscription(failure.getConversationId());
    }


    @Override
    public boolean done() {
        return false;
    }


    private MessageTemplate createFailureTemplate() {
        return and(matchSender(getAgent().getAMS()),
                   matchPerformative(AclMessage.Performative.FAILURE));
    }
}
