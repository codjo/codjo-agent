package net.codjo.agent.behaviour;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
import net.codjo.agent.JadeWrapper;
/**
 *
 */
public abstract class TickerBehaviour extends Behaviour {
    private jade.core.behaviours.TickerBehaviour tickerBehaviour;


    protected TickerBehaviour(Agent agent, long period) {
        jade.core.Agent theAgent = JadeWrapper.unwrapp(agent);
        tickerBehaviour = new TickerBehaviourAdapater(theAgent, period);
        JadeWrapper.wrapp(this, tickerBehaviour);
    }


    @Override
    protected final void action() {
        throw new UnsupportedOperationException("La méthode action sur un TickerBehaviour"
                                                + " ne doit pas être appelée directement");
    }


    public abstract void onTick();


    @Override
    public final boolean done() {
        return tickerBehaviour.done();
    }


    public void stop() {
        tickerBehaviour.stop();
    }


    public void reset() {
        tickerBehaviour.reset();
    }


    public void reset(long period) {
        tickerBehaviour.reset(period);
    }


    public int getTickCount() {
        return tickerBehaviour.getTickCount();
    }


    private class TickerBehaviourAdapater extends jade.core.behaviours.TickerBehaviour
          implements Unbreakable {
        private UncaughtErrorHandler uncaughtErrorHandler;


        TickerBehaviourAdapater(jade.core.Agent theAgent, long period) {
            super(theAgent, period);
        }


        @Override
        protected void onTick() {
            if (shouldBeFailSafe()) {
                failsafeTick();
            }
            else {
                TickerBehaviour.this.onTick();
            }
        }


        private void failsafeTick() {
            try {
                TickerBehaviour.this.onTick();
            }
            catch (Throwable e) {
                uncaughtErrorHandler.handle(e, TickerBehaviour.this);
            }
        }


        public jade.core.behaviours.Behaviour makeMeUnbreakable(UncaughtErrorHandler handler) {
            uncaughtErrorHandler = handler;
            return this;
        }


        private boolean shouldBeFailSafe() {
            return uncaughtErrorHandler != null;
        }
    }
}
