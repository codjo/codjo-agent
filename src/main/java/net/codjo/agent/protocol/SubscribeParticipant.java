/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.protocol;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
import net.codjo.agent.JadeWrapper;
import net.codjo.agent.MessageTemplate;
import java.util.HashMap;
import java.util.Map;
/**
 * Agent 'Participant' du protocole 'fipa-subscribe'.
 *
 * @see net.codjo.agent.protocol.SubscribeProtocol
 */
public class SubscribeParticipant extends Behaviour {
    private SubscribeParticipantHandler participantHandler;


    public SubscribeParticipant(Agent participant,
                                SubscribeParticipantHandler handler,
                                MessageTemplate template) {
        this.participantHandler = handler;

        jade.proto.SubscriptionResponder responder =
              new SubscriptionResponder(JadeWrapper.unwrapp(participant),
                                        JadeWrapper.unwrapp(template),
                                        new SubscriptionManagerAdapter());

        JadeWrapper.wrapp(this, responder);
    }


    @Override
    protected final void action() {
    }


    @Override
    public final boolean done() {
        return false;
    }


    public static interface Subscription {
        /**
         * Retourne le message de souscription.
         *
         * @return le message de souscription.
         */
        public AclMessage getMessage();


        public void reply(AclMessage messageToSend);


        public void close();
    }

    private class SubscriptionResponder extends jade.proto.SubscriptionResponder implements Unbreakable {
        SubscriptionResponder(jade.core.Agent agent,
                              jade.lang.acl.MessageTemplate template,
                              SubscriptionManager manager) {
            super(agent, template, manager);
        }


        public jade.core.behaviours.Behaviour makeMeUnbreakable(UncaughtErrorHandler handler) {
            participantHandler = new FailSafeWrapper(participantHandler, handler);
            return this;
        }
    }
    private class SubscriptionManagerAdapter implements jade.proto.SubscriptionResponder.SubscriptionManager {
        private Map<jade.proto.SubscriptionResponder.Subscription, Subscription> subscriptions
              = new HashMap<jade.proto.SubscriptionResponder.Subscription, Subscription>();


        public boolean register(jade.proto.SubscriptionResponder.Subscription jadeSubscription)
              throws jade.domain.FIPAAgentManagement.RefuseException,
                     jade.domain.FIPAAgentManagement.NotUnderstoodException {
            Subscription subscription = new JadeSubscriptionWrapper(jadeSubscription);
            participantHandler.handleSubscribe(subscription);
            subscriptions.put(jadeSubscription, subscription);
            return true;
        }


        public boolean deregister(jade.proto.SubscriptionResponder.Subscription jadeSubscription)
              throws jade.domain.FIPAAgentManagement.FailureException {
            Subscription subscription = subscriptions.get(jadeSubscription);
            participantHandler.handleCancel(subscription);
            subscriptions.remove(jadeSubscription);
            return true;
        }
    }
    private static class JadeSubscriptionWrapper implements Subscription {
        private final jade.proto.SubscriptionResponder.Subscription jadeSubscription;


        JadeSubscriptionWrapper(jade.proto.SubscriptionResponder.Subscription jadeSubscription) {
            this.jadeSubscription = jadeSubscription;
        }


        public AclMessage getMessage() {
            return JadeWrapper.wrapp(jadeSubscription.getMessage());
        }


        public void reply(AclMessage agreeMessage) {
            jadeSubscription.notify(JadeWrapper.unwrapp(agreeMessage));
        }


        public void close() {
            jadeSubscription.close();
        }
    }
    private class FailSafeWrapper implements SubscribeParticipantHandler {
        private SubscribeParticipantHandler handler;
        private UncaughtErrorHandler uncaughtErrorHandler;


        private FailSafeWrapper(SubscribeParticipantHandler handler,
                                UncaughtErrorHandler uncaughtErrorHandler) {
            this.handler = handler;
            this.uncaughtErrorHandler = uncaughtErrorHandler;
        }


        public void handleSubscribe(Subscription subscription) throws RefuseException,
                                                                      NotUnderstoodException {
            try {
                handler.handleSubscribe(subscription);
            }
            catch (RefuseException e) {
                throw e;
            }
            catch (NotUnderstoodException e) {
                throw e;
            }
            catch (Throwable error) {
                uncaughtErrorHandler.handle(error, SubscribeParticipant.this);
                throw new NotUnderstoodException(internalMessageError(error));
            }
        }


        public void handleCancel(Subscription subscription) throws FailureException {
            try {
                handler.handleCancel(subscription);
            }
            catch (FailureException e) {
                throw e;
            }
            catch (Throwable error) {
                uncaughtErrorHandler.handle(error, SubscribeParticipant.this);
                throw new FailureException(internalMessageError(error));
            }
        }
    }
}
