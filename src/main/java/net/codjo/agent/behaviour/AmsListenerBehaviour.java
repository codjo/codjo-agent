/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.behaviour;
import jade.domain.introspection.Event;
import java.util.Map;
import java.util.TreeMap;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.JadeWrapper;
/**
 *
 */
public class AmsListenerBehaviour extends Behaviour {
    private Map<String, EventHandler> eventHandlersByName = new TreeMap<String, EventHandler>();


    public AmsListenerBehaviour() {
        JadeWrapper.wrapp(this, new AMSSubscriberAdapter());
    }


    @Override
    protected void action() {
    }


    @Override
    public boolean done() {
        return false;
    }


    public void setAgentDeathHandler(final EventHandler eventHandler) {
        eventHandlersByName.put(jade.domain.introspection.IntrospectionVocabulary.DEADAGENT, eventHandler);
    }


    public interface EventHandler {
        public void handle(Aid agentId);
    }

    private class AMSSubscriberAdapter extends jade.domain.introspection.AMSSubscriber implements Unbreakable {
        private UncaughtErrorHandler uncaughtErrorHandler;


        @Override
        protected void installHandlers(Map handlersTable) {
            //noinspection unchecked
            handlersTable.put(jade.domain.introspection.IntrospectionVocabulary.DEADAGENT,
                              wrapp(new EventHandler() {
                                  public void handle(jade.domain.introspection.Event event) {
                                      jade.domain.introspection.DeadAgent deadAgent =
                                            (jade.domain.introspection.DeadAgent)event;

                                      AmsListenerBehaviour.EventHandler eventHandler =
                                            eventHandlersByName
                                                  .get(jade.domain.introspection.IntrospectionVocabulary.DEADAGENT);

                                      eventHandler.handle(JadeWrapper.wrapp(deadAgent.getAgent()));
                                  }
                              }));
        }


        private EventHandler wrapp(final EventHandler eventHandler) {
            return new EventHandler() {
                public void handle(Event event) {
                    if (shouldBeFailSafe()) {
                        failSafeHandle(event, eventHandler);
                    }
                    else {
                        eventHandler.handle(event);
                    }
                }
            };
        }


        private void failSafeHandle(Event ev, EventHandler eventHandler) {
            try {
                eventHandler.handle(ev);
            }
            catch (Throwable e) {
                uncaughtErrorHandler.handle(e, AmsListenerBehaviour.this);
            }
        }


        public jade.core.behaviours.Behaviour makeMeUnbreakable(UncaughtErrorHandler handler) {
            this.uncaughtErrorHandler = handler;
            return this;
        }


        private boolean shouldBeFailSafe() {
            return uncaughtErrorHandler != null;
        }
    }
}
