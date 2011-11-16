/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.behaviour;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
import net.codjo.agent.JadeWrapper;
import net.codjo.agent.test.AgentAssert;
import static net.codjo.agent.test.AgentAssert.behaviourDone;
import static net.codjo.agent.test.AgentAssert.behaviourNotDone;
import static net.codjo.agent.test.AgentAssert.log;
import net.codjo.agent.test.Story;
import net.codjo.agent.test.TesterAgent;
import net.codjo.test.common.LogString;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 * Classe de test de {@link TickerBehaviour}.
 */
public class TickerBehaviourTest extends TestCase {
    private static final int PERIOD = 1;
    private final LogString log = new LogString();
    private Story story;
    private TesterAgent testerAgent;


    public void test_onTick() throws Exception {
        final TickerBehaviourMock behaviour = new TickerBehaviourMock(testerAgent, PERIOD);

        story.record().startAgent("ticker", agentWith(behaviour));

        story.record().addAssert(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                log.assertContent("onTick()");
                assertEquals(3, behaviour.getTickCount());
                assertTrue("Behaviour done", behaviour.done());
            }
        });

        story.execute();
    }


    public void test_stop() throws Exception {
        final TickerBehaviour behaviour =
              new TickerBehaviour(testerAgent, PERIOD) {
                  @Override
                  public void onTick() {
                      if (getTickCount() > 2) {
                          stop();
                      }
                      else {
                          log.call("onTick", getTickCount());
                      }
                  }
              };

        story.record().startAgent("ticker", agentWith(behaviour));

        story.record().addAssert(log(log, "onTick(1), onTick(2)"));
        story.record().addAssert(behaviourDone(behaviour));

        story.execute();
    }


    public void test_reset() throws Exception {
        final TickerBehaviour behaviour =
              new TickerBehaviour(testerAgent, PERIOD) {
                  private boolean isReset = false;


                  @Override
                  public void onTick() {
                      if (isReset) {
                          log.call("reset");
                          stop();
                          return;
                      }
                      if (getTickCount() < 3) {
                          log.call("onTick", getTickCount());
                      }
                      else {
                          isReset = true;
                          reset();
                      }
                  }
              };

        story.record().startAgent("ticker", agentWith(behaviour));

        story.record().addAssert(behaviourNotDone(behaviour));
        story.record().addAssert(behaviourDone(behaviour));

        story.execute();

        log.assertContent("onTick(1), onTick(2), reset()");
    }


    public void test_jadeBehaviour() throws Exception {
        final TickerBehaviour behaviour = new TickerBehaviourMock(testerAgent, PERIOD);
        assertTrue(JadeWrapper.unwrapp(behaviour) instanceof jade.core.behaviours.TickerBehaviour);
    }


    public void test_action_unsupportedOperation() throws Exception {
        TickerBehaviourMock mock = new TickerBehaviourMock(testerAgent, PERIOD);
        try {
            mock.action();
            fail();
        }
        catch (UnsupportedOperationException ex) {
            assertEquals("La méthode action sur un TickerBehaviour ne doit pas être appelée directement",
                         ex.getMessage());
        }
    }


    public void test_neverFaisWrapping_nominal() throws Exception {
        final TickerBehaviour behaviour =
              new TickerBehaviour(testerAgent, PERIOD) {
                  @Override
                  public void onTick() {
                      if (getTickCount() > 3) {
                          stop();
                      }
                      else if (getTickCount() == 2) {
                          throw new NullPointerException();
                      }
                      else {
                          log.call("onTick", getTickCount());
                      }
                  }
              };

        story.record().startAgent("ticker", agentWith(behaviour));

        story.record().addAssert(AgentAssert.log(log, "onTick(1), onTick(3)"));
        story.record().addAssert(AgentAssert.behaviourDone(behaviour));

        story.execute();
    }


    @Override
    protected void setUp() throws Exception {
        story = new Story();
        story.doSetUp();
        testerAgent = new TesterAgent();
    }


    @Override
    protected void tearDown() throws Exception {
        log.clear();
        story.doTearDown();
    }


    private Agent agentWith(TickerBehaviour behaviour) {
        testerAgent.addBehaviour(Behaviour.thatNeverFails(behaviour));
        return testerAgent;
    }


    private class TickerBehaviourMock extends TickerBehaviour {

        TickerBehaviourMock(TesterAgent testerAgent, int period) {
            super(testerAgent, period);
        }


        @Override
        public void onTick() {
            if (getTickCount() == 3) {
                log.call("onTick");
                stop();
            }
        }
    }
}
