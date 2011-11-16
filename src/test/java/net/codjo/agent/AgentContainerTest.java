/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.agent.test.DummyAgent;
import junit.framework.TestCase;
/**
 * Classe de test de {@link AgentContainer}.
 */
public class AgentContainerTest extends TestCase {
    private AgentContainer mainContainer = AgentContainer.createMainContainer();


    public void test_createContainer() throws Exception {
        mainContainer.start();

        ContainerConfiguration containerConfiguration =
              new ContainerConfiguration("localhost", AgentContainer.CONTAINER_PORT,
                                         "my-sub-container");
        AgentContainer standardContainer = AgentContainer.createContainer(containerConfiguration);

        standardContainer.start();
        assertTrue(standardContainer.isAlive());

        standardContainer.stop();

        assertFalse(standardContainer.isAlive());
    }


    public void test_createMainContainer_WithService() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.addService(AgentContainerTest.AGFService1.class.getName());
        containerConfiguration.setLocalPort(AgentContainer.CONTAINER_PORT);

        mainContainer = AgentContainer.createMainContainer(containerConfiguration);

        mainContainer.start();
        assertEquals(true, mainContainer.isAlive());

        assertEquals("AGFService1 is running",
                     containerConfiguration.getParameter(AGFService1.class.getName()));
    }


    public void test_createContainer_stopMainContainer()
          throws Exception {
        mainContainer.start();

        ContainerConfiguration containerConfiguration =
              new ContainerConfiguration("localhost", AgentContainer.CONTAINER_PORT,
                                         "my-sub-container");
        AgentContainer standardContainer = AgentContainer.createContainer(containerConfiguration);

        standardContainer.start();

        Thread.sleep(100);

        assertTrue(standardContainer.isAlive());

        mainContainer.stop();

        assertContainerIsDead(standardContainer);
    }


    public void test_createContainer_noMainContainer()
          throws Exception {
        ContainerConfiguration containerConfiguration =
              new ContainerConfiguration("localhost", 41000, "MyStandardContainer");
        AgentContainer standardContainer = AgentContainer.createContainer(containerConfiguration);

        try {
            standardContainer.start();

            fail("Pas de main container disponible !");
        }
        catch (StartFailureException ex) {
            ; // Ok
        }
    }


    public void test_isAlive() throws Exception {
        assertEquals(false, mainContainer.isAlive());

        mainContainer.start();
        assertEquals(true, mainContainer.isAlive());

        mainContainer.stop();
        assertEquals(false, mainContainer.isAlive());
    }


    public void test_getContainerName() throws Exception {
        assertEquals("n/a", mainContainer.getContainerName());

        mainContainer.start();
        assertEquals("Main-Container", mainContainer.getContainerName());

        mainContainer.stop();
        assertEquals("n/a", mainContainer.getContainerName());
    }


    public void test_start() throws Exception {
        mainContainer.start();

        assertContainerIsStarted(mainContainer);
    }


    public void test_start_failure() throws Exception {
        mainContainer.start();

        try {
            AgentContainer.createMainContainer().start();
            fail("Le main container est déja lancé !");
        }
        catch (StartFailureException e) {
            ;
        }

        assertContainerIsStarted(mainContainer);
    }


    public void test_stop() throws Exception {
        mainContainer.start();

        mainContainer.stop();
        assertContainerIsStopped(mainContainer);
    }


    public void test_stop_withAgents() throws Exception {
        mainContainer.start();
        AgentController adam = mainContainer.acceptNewAgent("Adam", new DummyAgent());
        adam.start();
        mainContainer.stop();

        mainContainer = AgentContainer.createMainContainer();
        mainContainer.start();
        AgentController notStartedAgent =
              mainContainer.acceptNewAgent("Adam", new DummyAgent());
        try {
            mainContainer.stop();
            fail("StopFailureException attempted.");
        }
        catch (StopFailureException e) {
            String message = e.getMessage();
            assertTrue(message.contains("Un des agents du conteneur n'a peut être pas été demarré ?!"));
        }
        notStartedAgent.kill();
        mainContainer.stop();
        assertContainerIsStopped(mainContainer);
    }


    public void test_stop_failure() throws Exception {
        mainContainer.start();
        mainContainer.stop();
        try {
            mainContainer.stop();
            fail("le main container est déjà arrêté !");
        }
        catch (StopFailureException e) {
            ;
        }
        catch (ContainerNotStartedException e) {
            ;
        }
    }


    public void test_stop_containerNotStarted() throws Exception {
        try {
            mainContainer.stop();
            fail("ContainerNotStartedException attempted.");
        }
        catch (ContainerNotStartedException ex) {
            ;
        }
    }


    public void test_acceptNewAgent() throws Exception {
        mainContainer.start();
        DummyAgent agent = new DummyAgent();
        mainContainer.acceptNewAgent("Pat", agent).start();

        agent.getSemaphore().waitFor(DummyAgent.SETUP);
        assertEquals("Pat", agent.getAID().getLocalName());
    }


    public void test_acceptNewAgent_containerNotStarted()
          throws Exception {
        try {
            mainContainer.acceptNewAgent("Pat", new DummyAgent());
            fail("ContainerNotStartedException attempted.");
        }
        catch (ContainerNotStartedException ex) {
            ;
        }
    }


    public void test_acceptNewAgent_failure() throws Exception {
        mainContainer.start();
        Agent agent = new DummyAgent();
        mainContainer.acceptNewAgent("Pat", agent).start();

        try {
            mainContainer.acceptNewAgent("Pat", agent);
            fail("Pat deja là !");
        }
        catch (ContainerFailureException ex) {
            ;
        }
    }


    public void test_getAgent() throws Exception {
        mainContainer.start();
        DummyAgent agent = new DummyAgent();
        mainContainer.acceptNewAgent("Pat", agent).start();
        agent.getSemaphore().waitFor(DummyAgent.SETUP);

        assertEquals(agent.getAID().getName(), mainContainer.getAgent("Pat").getName());
    }


    public void test_getAgentNotFound() throws Exception {
        mainContainer.start();

        try {
            mainContainer.getAgent("unknownAgent");
            fail();
        }
        catch (ContainerFailureException ex) {
            assertEquals("jade.wrapper.ControllerException: Agent unknownAgent not found.",
                         ex.getMessage());
        }
    }


    @Override
    protected void tearDown() throws Exception {
        silentStop();
    }


    private void silentStop() {
        try {
            mainContainer.stop();
        }
        catch (Throwable e) {
            ;
        }
    }


    private static void assertContainerIsStopped(AgentContainer container) {
//        assertEquals(PlatformState.cPLATFORM_STATE_KILLED,
//            container.getJadeContainer().getState().getCode());
        assertFalse(container.isAlive());
    }


    private static void assertContainerIsStarted(AgentContainer container) {
//        assertEquals(PlatformState.cPLATFORM_STATE_READY,
//            container.getJadeContainer().getState().getCode());
        assertTrue(container.isAlive());
    }


    private static void assertContainerIsDead(AgentContainer standardContainer)
          throws Exception {
        jade.wrapper.AgentController agentController =
              standardContainer.getJadeContainer().acceptNewAgent("ee",
                                                                  new jade.core.Agent());
        try {
            agentController.suspend();

            fail("Should be dead !");
        }
        catch (jade.wrapper.StaleProxyException ex) {
            ; // Ok
        }
    }


    public static class AGFService1 implements Service {
        public String getName() {
            return getClass().getName();
        }


        public void boot(ContainerConfiguration containerConfiguration) throws ServiceException {
            containerConfiguration.setParameter(getName(), "AGFService1 is running");
        }


        public ServiceHelper getServiceHelper(Agent agent) {
            return null;
        }
    }
}
