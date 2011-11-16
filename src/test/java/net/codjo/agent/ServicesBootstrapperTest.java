/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;
/**
 * Classe de test de {@link ServicesBootstrapper}.
 */
public class ServicesBootstrapperTest extends TestCase {
    private LogString log = new LogString();
    private ServicesBootstrapper bootstrapper;


    public void test_getName() throws Exception {
        assertEquals(ServicesBootstrapper.NAME, bootstrapper.getName());
    }


    public void test_init() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();

        containerConfiguration.addService(AGFService1.class.getName());

        initializeServices(new JadeAgentContainerMock(log), containerConfiguration);

        log.assertContent("serviceManager.activateService(" + AGFService1.class.getName()
                          + ")");

        assertEquals("running", containerConfiguration.getParameter(AGFService1.class.getName()));
    }


    private void initializeServices(JadeAgentContainerMock agentContainer,
                                    ContainerConfiguration containerConfiguration)
          throws jade.core.ProfileException, jade.core.ServiceException {
        bootstrapper.init(agentContainer, containerConfiguration.getJadeProfile());
        bootstrapper.boot(containerConfiguration.getJadeProfile());
    }


    public void test_getServiceHelper() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.addService(AGFService2.class.getName());

        JadeAgentContainerMock agentContainer = new JadeAgentContainerMock(log);
        initializeServices(agentContainer, containerConfiguration);
        jade.core.Service jadeLastService = agentContainer.getLastAddedService();

        Agent agfAgent = new AgentMock();
        jade.core.ServiceHelper jadeHelper = jadeLastService.getHelper(agfAgent.getJadeAgent());
        assertNotNull(jadeHelper);

        ServiceHelper helper =
              ((ServicesBootstrapper.JadeServiceHelperAdapter)jadeHelper)
                    .getAgfServiceHelper();

        assertEquals(AGFServiceHelper2.class, helper.getClass());
        assertSame(agfAgent, ((AGFServiceHelper2)helper).agfAgent);
    }


    public void test_getServiceHelper_noHelper() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.addService(AGFService1.class.getName());

        JadeAgentContainerMock agentContainer = new JadeAgentContainerMock(log);
        initializeServices(agentContainer, containerConfiguration);
        jade.core.Service jadeLastService = agentContainer.getLastAddedService();

        jade.core.ServiceHelper helper =
              jadeLastService.getHelper(new AgentMock().getJadeAgent());
        assertNull(helper);
    }


    public void test_getServiceHelper_notAnAgfAgent()
          throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.addService(AGFService1.class.getName());

        JadeAgentContainerMock agentContainer = new JadeAgentContainerMock(log);
        initializeServices(agentContainer, containerConfiguration);
        jade.core.Service jadeLastService = agentContainer.getLastAddedService();

        try {
            jadeLastService.getHelper(new jade.core.Agent());
            fail();
        }
        catch (ClassCastException ex) {
            ; // Ok
        }
    }


    public void test_init_multipleServices() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();

        containerConfiguration.addService(AGFService1.class.getName());
        containerConfiguration.addService(AGFService2.class.getName());

        initializeServices(new JadeAgentContainerMock(log), containerConfiguration);

        log.assertContent("serviceManager.activateService(" + AGFService1.class.getName() + ")"
                          + ", serviceManager.activateService(" + AGFService2.class.getName() + ")");
    }


    public void test_init_noServices() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();

        initializeServices(new JadeAgentContainerMock(log), containerConfiguration);

        log.assertContent("");
    }


    public void test_init_badServices() throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.addService(ServicesBootstrapperTest.class.getName());

        try {
            initializeServices(new JadeAgentContainerMock(log), containerConfiguration);
            fail();
        }
        catch (jade.core.ServiceException ex) {
            assertBeginWith("Service AGF en Erreur : net.codjo.agent.ServicesBootstrapperTest",
                            ex.getMessage());
            ; // Ok
        }
        log.assertContent("");
    }


    public void test_init_badServices_2Services()
          throws Exception {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.addService(ServicesBootstrapperTest.class.getName());
        containerConfiguration.addService(AGFService1.class.getName());

        try {
            initializeServices(new JadeAgentContainerMock(log), containerConfiguration);
            fail();
        }
        catch (jade.core.ServiceException ex) {
            assertBeginWith("Service AGF en Erreur : net.codjo.agent.ServicesBootstrapperTest",
                            ex.getMessage());
            ; // Ok
        }
    }


    public void test_picoContainerManagement() throws Exception {
        ContainerConfiguration configuration = new ContainerConfiguration();

        assertNull(ServicesBootstrapper.getPicoContainer(configuration));

        MutablePicoContainer picoContainer = new DefaultPicoContainer();
        ServicesBootstrapper.setPicoContainer(configuration, picoContainer);
        assertSame(picoContainer, ServicesBootstrapper.getPicoContainer(configuration));

        ServicesBootstrapper.removePicoContainer(configuration);
        assertNull(ServicesBootstrapper.getPicoContainer(configuration));
    }


    public void test_init_usingPicoContainer() throws Exception {
        ContainerConfiguration configuration = new ContainerConfiguration();
        configuration.addService(AGFService3.class.getName());

        DefaultPicoContainer picoContainer = new DefaultPicoContainer();
        picoContainer.registerComponentInstance(this);
        ServicesBootstrapper.setPicoContainer(configuration, picoContainer);

        initializeServices(new JadeAgentContainerMock(new LogString()), configuration);

        log.assertContent("AGFService3 has been called with the good TestCase");
    }


    private void assertBeginWith(String expected, String actual) {
        assertTrue("\nExpected: " + expected + "\nActual  : " + actual,
                   actual.startsWith(expected));
    }


    @Override
    protected void setUp() throws Exception {
        bootstrapper = new ServicesBootstrapper();
    }


    public static class AGFService1 implements Service {
        public String getName() {
            return getClass().getName();
        }


        public void boot(ContainerConfiguration containerConfiguration) throws ServiceException {
            containerConfiguration.setParameter(getClass().getName(), "running");
        }


        public ServiceHelper getServiceHelper(Agent agent) {
            return null;
        }
    }

    public static class AGFService2 extends AGFService1 {
        @Override
        public ServiceHelper getServiceHelper(Agent agent) {
            return new AGFServiceHelper2(agent);
        }
    }

    static class AGFServiceHelper2 implements ServiceHelper {
        Agent agfAgent;
        Agent initedAgent;


        AGFServiceHelper2(Agent agent) {
            this.agfAgent = agent;
        }


        public void init(Agent agent) {
            initedAgent = agent;
        }
    }

    public static class AGFService3 extends AGFService1 {
        private final ServicesBootstrapperTest testcase;


        public AGFService3(ServicesBootstrapperTest testcase) {
            this.testcase = testcase;
        }


        @Override
        public void boot(ContainerConfiguration containerConfiguration) throws ServiceException {
            testcase.log.info("AGFService3 has been called with the good TestCase");
        }
    }
}
