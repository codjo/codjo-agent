/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.behaviour;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.Behaviour.UncaughtErrorHandler;
import net.codjo.agent.DFService;
import net.codjo.agent.DFService.DFServiceException;
import net.codjo.agent.test.DummyAgent;
import net.codjo.agent.test.Story;
import net.codjo.test.common.LogString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.codjo.agent.Behaviour.thatNeverFails;
import static net.codjo.agent.test.AgentAssert.log;
import static net.codjo.test.common.matcher.JUnitMatchers.assertThat;
import static net.codjo.test.common.matcher.JUnitMatchers.is;
import static org.junit.Assert.fail;
/**
 * Classe de test de {@link AmsListenerBehaviour}.
 */
public class AmsListenerBehaviourTest {
    private LogString log = new LogString();
    private Story story = new Story();
    private AmsListenerBehaviour amsListenerBehaviour = new AmsListenerBehaviour();
    private static final String AMS_LISTENER = "AMS-Listener";
    private static final String SOON_DEAD = "soon-dead";
    private static final String DUMMY_SERVICE = "dummy";


    @Before
    public void setUp() throws Exception {
        story.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        story.doTearDown();
    }


    @Test
    public void test_done() throws Exception {
        assertThat(amsListenerBehaviour.done(), is(false));
    }


    @Test
    public void test_action() throws Exception {
        amsListenerBehaviour.setAgentDeathHandler(new AmsListenerBehaviour.EventHandler() {
            public void handle(Aid agentId) {
                log.info("handle : AgentDeath on " + agentId);
            }
        });

        story.record()
              .startAgent(AMS_LISTENER, new DummyAgentWithService(amsListenerBehaviour, DUMMY_SERVICE));
        story.record().assertAgentWithService(new String[]{AMS_LISTENER}, DUMMY_SERVICE);

        story.record().startTester(SOON_DEAD);
        story.record().assertContainsAgent(SOON_DEAD);

        story.record().killAgent(SOON_DEAD);

        story.record().assertNotContainsAgent(SOON_DEAD);
        story.record().addAssert(log(log, "handle : AgentDeath on ( agent-identifier :name " + SOON_DEAD
                                          + "@localhost:-1/JADE )"));

        story.execute();
    }


    @Test
    public void test_thatNeverFails() throws Exception {
        amsListenerBehaviour.setAgentDeathHandler(new AmsListenerBehaviour.EventHandler() {
            public void handle(Aid agentId) {
                throw new NullPointerException("software bug");
            }
        });

        UncaughtErrorHandler uncaughtErrorHandler = new UncaughtErrorHandler() {
            public void handle(Throwable error, Behaviour behaviour) {
                log.info("handle-uncaught-error -> " + error.getMessage());
            }
        };

        story.record().startAgent(AMS_LISTENER,
                                  new DummyAgentWithService(thatNeverFails(amsListenerBehaviour, uncaughtErrorHandler),
                                                            DUMMY_SERVICE));
        story.record().assertAgentWithService(new String[]{AMS_LISTENER}, DUMMY_SERVICE);

        story.record().startTester(SOON_DEAD);
        story.record().killAgent(SOON_DEAD);

        story.record().addAssert(log(log, "handle-uncaught-error -> software bug"));

        story.execute();
    }


    private static class DummyAgentWithService extends DummyAgent {
        private String service;


        private DummyAgentWithService(Behaviour amsListenerBehaviour, String service) {
            super(amsListenerBehaviour);
            this.service = service;
        }


        @Override
        protected void setup() {
            super.setup();
            try {
                service = DUMMY_SERVICE;
                DFService.register(this, DFService.createAgentDescription(service));
            }
            catch (DFServiceException e) {
                fail(e.getMessage());
            }
        }
    }
}
