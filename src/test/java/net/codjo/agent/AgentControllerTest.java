/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.DummyAgent;
import junit.framework.TestCase;
/**
 * Classe de test de {@link AgentController}.
 */
public class AgentControllerTest extends TestCase {
    private AgentContainerFixture containerFixture = new AgentContainerFixture();


    public void test_errorManagement() throws Exception {
        Agent anAgent = new DummyAgent();

        AgentController agentController =
              containerFixture.getContainer().acceptNewAgent("bobo", anAgent);

        invalidateController(agentController);

        // Check controller
        try {
            agentController.start();
            fail();
        }
        catch (BadControllerException ex) {
            assertNotNull(ex.getCause());
        }
        try {
            agentController.kill();
            fail();
        }
        catch (BadControllerException ex) {
            assertNotNull(ex.getCause());
        }
        try {
            agentController.getName();
            fail();
        }
        catch (BadControllerException ex) {
            assertNotNull(ex.getCause());
        }
        try {
            agentController.putO2AObject("");
            fail();
        }
        catch (BadControllerException ex) {
            assertNotNull(ex.getCause());
        }
    }


    public void test_putO2AObject() throws Exception {
        Agent anAgent = new DummyAgent();
        anAgent.setEnabledO2ACommunication(true, 0);

        AgentController agentController = containerFixture.startNewAgent("bobo", anAgent);

        agentController.putO2AObject("anObject");
        assertEquals("anObject", anAgent.getO2AObject());
    }


    public void test_startAndKill() throws Exception {
        AgentController agentController =
              containerFixture.startNewAgent("bobo", new DummyAgent());

        assertTrue(agentController.getName().startsWith("bobo"));
        agentController.kill();

        containerFixture.assertNotContainsAgent("bobo");
    }


    public void test_startKamikazeAgent() throws Exception {
        AgentController agentController =
              containerFixture.acceptNewAgent("bobo",
                                              new DummyAgent(new Behaviour() {
                                                  @Override
                                                  protected void action() {
                                                      getAgent().die();
                                                  }


                                                  @Override
                                                  public boolean done() {
                                                      return true;
                                                  }
                                              }));

        agentController.start();

        containerFixture.assertNotContainsAgent("bobo");
    }


    @Override
    protected void tearDown() throws Exception {
        containerFixture.doTearDown();
    }


    @Override
    protected void setUp() throws Exception {
        containerFixture.doSetUp();
    }


    private void invalidateController(AgentController agentController)
          throws BadControllerException, InterruptedException {
        agentController.start();
        Thread.sleep(100);
        containerFixture.stopContainer();
    }
}
