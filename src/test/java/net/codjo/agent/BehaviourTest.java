/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import static net.codjo.agent.AclMessage.Performative.INFORM;
import net.codjo.agent.Behaviour.UncaughtErrorHandler;
import static net.codjo.agent.Behaviour.thatNeverFails;
import static net.codjo.agent.test.AgentAssert.log;
import net.codjo.agent.test.DummyAgent;
import static net.codjo.agent.test.MessageBuilder.message;
import net.codjo.agent.test.Semaphore;
import net.codjo.agent.test.Story;
import net.codjo.agent.test.Story.ConnectionType;
import net.codjo.test.common.LogString;
import static net.codjo.test.common.matcher.JUnitMatchers.*;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
/**
 * Classe de test de {@link Behaviour}.
 */
public class BehaviourTest {
    private Story story = new Story();

    private LogString log = new LogString();


    @Test
    public void test_setAgent() throws Exception {
        Agent agent = new AgentMock();

        Behaviour behaviour = new BehaviourMock();

        behaviour.setAgent(agent);

        assertThat(behaviour.getAgent(), is(agent));
    }


    @Test
    public void test_block() throws Exception {
        Behaviour behaviour =
              new Behaviour() {
                  @Override
                  protected void action() {
                      AclMessage message = getAgent().receive();
                      if (message == null) {
                          block();
                          return;
                      }
                      log.info("receive[" + message.getContent() + "]");
                  }


                  @Override
                  public boolean done() {
                      return false;
                  }
              };

        story.record().startAgent("hitori", new DummyAgent(behaviour));

        story.record().startTester("sender")
              .send(message(INFORM).to("hitori").withContent("myContent"));

        story.record().addAssert(log(log, "receive[myContent]"));

        story.execute();
    }


    @Test
    public void test_restart() throws Exception {
        story.getAgentContainerFixture().startContainer(ConnectionType.NO_CONNECTION);

        final Semaphore semaphore = new Semaphore();
        final Behaviour behaviour =
              new Behaviour() {
                  @Override
                  protected void action() {
                      log.call("action");
                      block(500);
                      semaphore.release();
                  }


                  @Override
                  public boolean done() {
                      return false;
                  }
              };

        Agent agent = new DummyAgent();
        story.getAgentContainerFixture().startNewAgent("hitori", agent);
        agent.addBehaviour(behaviour);

        semaphore.acquire();
        log.assertContent("action()");

        story.getAgentContainerFixture().runInAgentThread(agent,
                                                          new Runnable() {
                                                              public void run() {
                                                                  behaviour.restart();
                                                              }
                                                          });

        semaphore.acquire();
        log.assertContent("action(), action()");
    }


    @Test
    public void test_datastore() throws Exception {
        Behaviour behaviour = new BehaviourMock();

        DataStore dataStore = behaviour.getDataStore();

        assertThat(behaviour.getDataStore(), is(dataStore));
    }


    @Test
    public void test_behaviourName() throws Exception {
        Behaviour behaviour = new BehaviourMock();
        assertThat(behaviour.getBehaviourName(), is("BehaviourMock"));

        behaviour.setBehaviourName("a specific name");
        assertThat(behaviour.getBehaviourName(), is("a specific name"));
    }


    @Test
    public void test_unbreakable() throws Exception {
        Behaviour failingBehaviour = BehaviourMock.thatNeverFails("I failed but still alive");

        UncaughtErrorHandler errorHandler = new UncaughtErrorHandler() {
            public void handle(Throwable error, Behaviour behaviour) {
                log.call("handle", error.getMessage(), behaviour.getClass().getSimpleName());
            }
        };

        story.record()
              .startAgent("patrick.wilson", new DummyAgent(thatNeverFails(failingBehaviour, errorHandler)));

        story.record()
              .addAssert(log(log, "handle(I failed but still alive, BehaviourMock)"));

        story.execute();
    }


    @Test
    public void test_unbreakable_name() throws Exception {
        BehaviourMock mock = new BehaviourMock();
        assertEquals("BehaviourMock", mock.getBehaviourName());
        assertEquals("UnfailingBehaviourAdapter(BehaviourMock)",
                     Behaviour.thatNeverFails(mock).getBehaviourName());
    }


    @Test
    public void test_unbreakable_twice() throws Exception {
        BehaviourMock mock = new BehaviourMock();
        assertEquals("UnfailingBehaviourAdapter(BehaviourMock)",
                     Behaviour.thatNeverFails(Behaviour.thatNeverFails(mock)).getBehaviourName());
    }


    @Before
    public void setUp() throws Exception {
        story.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        story.doTearDown();
    }
}
