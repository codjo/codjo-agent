/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import junit.framework.TestCase;
/**
 * Classe de test de {@link ContainerConfiguration}.
 */
public class ContainerConfigurationTest extends TestCase {
    private ContainerConfiguration containerConfiguration;


    public void test_fileDir() throws Exception {
        assertEquals(".", containerConfiguration.getJadeFileDir());

        containerConfiguration.setJadeFileDirToTemporaryDir();

        assertEquals(System.getProperty("java.io.tmpdir"), containerConfiguration.getJadeFileDir());
    }


    public void test_resolveHostDnsName() throws Exception {
        containerConfiguration.setHost("localhost");
        containerConfiguration.resolveHostDnsName();
        assertEquals("127.0.0.1", containerConfiguration.getHost());
    }


    public void test_defaultParameters() throws Exception {
        containerConfiguration.setHost("myHost");
        containerConfiguration.setPort(1199);
        containerConfiguration.setContainerName("myContainer");

        assertProfile("myHost", "1199", "myContainer");
    }


    public void test_setParameter() throws Exception {
        containerConfiguration.setParameter("parametre", "value");
        assertEquals("value", containerConfiguration.getParameter("parametre"));
        assertEquals(null, containerConfiguration.getParameter("azzerrt"));
    }


    public void test_constructor() throws Exception {
        containerConfiguration = new ContainerConfiguration("myHost", 1199, "myContainer");
        assertProfile("myHost", "1199", "myContainer");
    }


    public void test_addService() throws Exception {
        containerConfiguration.addService("net.codjo.MyService");

        jade.core.Profile profile = containerConfiguration.getJadeProfile();

        assertEquals(jade.core.Profile.DEFAULT_SERVICES_NOMOBILITY + ";"
                     + ServicesBootstrapper.class.getName(),
                     profile.getParameter(jade.core.Profile.SERVICES, "N/A"));

        assertEquals("net.codjo.MyService",
                     profile.getParameter(ServicesBootstrapper.SERVICES, "N/A"));
    }


    public void test_addService_moreServices() throws Exception {
        containerConfiguration.addService("net.codjo.MyService");
        containerConfiguration.addService("net.codjo.MySecondService");

        jade.core.Profile profile = containerConfiguration.getJadeProfile();

        assertEquals(jade.core.Profile.DEFAULT_SERVICES_NOMOBILITY + ";"
                     + ServicesBootstrapper.class.getName(),
                     profile.getParameter(jade.core.Profile.SERVICES, "N/A"));

        assertEquals("net.codjo.MyService;net.codjo.MySecondService",
                     profile.getParameter(ServicesBootstrapper.SERVICES, "N/A"));
    }


    public void test_loadConfig() throws Exception {
        containerConfiguration.setParameter("alreadyDefinedParameter", "ok");
        containerConfiguration.setParameter("overloaded", "oldValue");

        String path =
              ContainerConfigurationTest.class.getResource("ContainerConfigTest.properties")
                    .getFile();

        containerConfiguration.loadConfig(path);

        assertEquals(25, containerConfiguration.getLocalPort());
        assertEquals("net.codjo.driver.MyDriver", containerConfiguration.getParameter("my-driver"));
        assertEquals("ok", containerConfiguration.getParameter("alreadyDefinedParameter"));
        assertEquals("newValue", containerConfiguration.getParameter("overloaded"));
    }


    public void test_loadConfig_fromStream() throws Exception {
        containerConfiguration.setParameter("alreadyDefinedParameter", "ok");
        containerConfiguration.setParameter("overloaded", "oldValue");

        containerConfiguration.loadConfig(ContainerConfigurationTest.class.getResourceAsStream(
              "ContainerConfigTest.properties"));

        assertEquals(25, containerConfiguration.getLocalPort());
        assertEquals("net.codjo.driver.MyDriver", containerConfiguration.getParameter("my-driver"));
        assertEquals("ok", containerConfiguration.getParameter("alreadyDefinedParameter"));
        assertEquals("newValue", containerConfiguration.getParameter("overloaded"));
    }


    @Override
    protected void setUp() throws Exception {
        containerConfiguration = new ContainerConfiguration();
    }


    private void assertProfile(String host, String port, String containerName) {
        jade.core.Profile profile = containerConfiguration.getJadeProfile();
        assertEquals(host, profile.getParameter(jade.core.Profile.MAIN_HOST, "N/A"));
        assertEquals(port, profile.getParameter(jade.core.Profile.MAIN_PORT, "N/A"));
        assertEquals(containerName,
                     profile.getParameter(jade.core.Profile.CONTAINER_NAME, "N/A"));
    }
}
