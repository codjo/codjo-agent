/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import org.apache.log4j.Logger;
/**
 * Classe mère de tous les Behaviour.
 *
 * <p> Un Behaviour représente un comportement d'un agent. Un plugin peut avoir plusieurs comportements. </p>
 *
 * @see Agent#addBehaviour(Behaviour)
 */
public abstract class Behaviour {
    private Agent agent;
    private jade.core.behaviours.Behaviour jadeBehaviour;
    private DataStore dataStore;
    private boolean shouldBeUnbreakable = false;
    private UncaughtErrorHandler uncaughtErrorHandler;


    protected Behaviour() {
    }


    public void setAgent(Agent agent) {
        this.agent = agent;
    }


    public Agent getAgent() {
        return agent;
    }


    protected abstract void action();


    public abstract boolean done();


    public void block() {
        getJadeBehaviour().block();
    }


    public void block(long millis) {
        getJadeBehaviour().block(millis);
    }


    /**
     * Re-démarre le behaviour précedemment {@link #block()}.
     */
    public void restart() {
        getJadeBehaviour().restart();
    }


    jade.core.behaviours.Behaviour createJadeBehaviour() {
        return new JadeBehaviourAdapter();
    }


    jade.core.behaviours.Behaviour getJadeBehaviour() {
        if (jadeBehaviour == null) {
            setJadeBehaviour(createJadeBehaviour());
        }
        return jadeBehaviour;
    }


    void setJadeBehaviour(jade.core.behaviours.Behaviour newJadeBehaviour) {
        jadeBehaviour = newJadeBehaviour;

        if (shouldBeUnbreakable) {
            if (!(newJadeBehaviour instanceof Unbreakable)) {
                throw new UnsupportedOperationException(
                      "This behaviour does not have the fail safe capabilities.");
            }
            jadeBehaviour = ((Unbreakable)newJadeBehaviour).makeMeUnbreakable(uncaughtErrorHandler);
        }
    }


    public DataStore getDataStore() {
        if (dataStore == null) {
            dataStore = new DataStore(getJadeBehaviour().getDataStore());
        }
        return dataStore.wrapp(getJadeBehaviour().getDataStore());
    }


    public String getBehaviourName() {
        return getJadeBehaviour().getBehaviourName();
    }


    public void setBehaviourName(String name) {
        getJadeBehaviour().setBehaviourName(name);
    }


    public static Behaviour thatNeverFails(Behaviour behaviour) {
        return thatNeverFails(behaviour, new DefaultUncaughtErrorHandler());
    }


    public static Behaviour thatNeverFails(Behaviour behaviour, UncaughtErrorHandler handler) {
        behaviour.shouldBeUnbreakable = true;
        behaviour.uncaughtErrorHandler = handler;
        if (behaviour.jadeBehaviour != null) {
            behaviour.setJadeBehaviour(behaviour.jadeBehaviour);
        }
        return behaviour;
    }


    protected static String internalMessageError(Throwable error) {
        return "Internal failure '" + error.getClass().getSimpleName() + "/" + error.getMessage();
    }


    public static interface Unbreakable {
        public jade.core.behaviours.Behaviour makeMeUnbreakable(UncaughtErrorHandler handler);
    }
    private class JadeBehaviourAdapter extends jade.core.behaviours.Behaviour implements Unbreakable {
        private UnfailingBehaviourAdapter unfailingBehaviourWrapper;


        JadeBehaviourAdapter() {
            setBehaviourName(Behaviour.this.getClass().getSimpleName());
        }


        @Override
        public void action() {
            Behaviour.this.action();
        }


        @Override
        public boolean done() {
            return Behaviour.this.done();
        }


        public jade.core.behaviours.Behaviour makeMeUnbreakable(UncaughtErrorHandler handler) {
            unfailingBehaviourWrapper = new UnfailingBehaviourAdapter(handler);
            unfailingBehaviourWrapper.wrapp(this, Behaviour.this);
            return unfailingBehaviourWrapper;
        }
    }
    private static class UnfailingBehaviourAdapter extends jade.core.behaviours.Behaviour {
        private jade.core.behaviours.Behaviour jadeBehaviour;
        private Behaviour agfBehaviour;
        private UncaughtErrorHandler handler;


        UnfailingBehaviourAdapter(UncaughtErrorHandler handler) {
            setBehaviourName("UnfailingBehaviourAdapter(n/a)");
            this.handler = handler;
        }


        public void wrapp(jade.core.behaviours.Behaviour myJadeBehaviour, Behaviour behaviour) {
            this.jadeBehaviour = myJadeBehaviour;
            this.agfBehaviour = behaviour;
            setBehaviourName("UnfailingBehaviourAdapter(" + behaviour.getBehaviourName() + ")");
        }


        @Override
        public void action() {
            try {
                jadeBehaviour.action();
            }
            catch (Throwable error) {
                handler.handle(error, agfBehaviour);
            }
        }


        @Override
        public boolean done() {
            return jadeBehaviour.done();
        }
    }
    public static interface UncaughtErrorHandler {
        public void handle(Throwable error, Behaviour behaviour);
    }
    private static class DefaultUncaughtErrorHandler implements UncaughtErrorHandler {
        public void handle(Throwable error, Behaviour behaviour) {
            Logger.getLogger(Behaviour.class).error("Uncaught Error "
                                                    + "- The behaviour " + behaviour.getBehaviourName()
                                                    + " has failed but will continue to live", error);
        }
    }
}
