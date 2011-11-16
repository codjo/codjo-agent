/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import jade.core.Profile;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
/**
 * Configuration d'un Container.
 *
 * @see AgentContainer
 * @see AgentContainer#createContainer(ContainerConfiguration)
 */
public class ContainerConfiguration {
    private static final int REACHABLE_TIMEOUT = 2000;

    private jade.core.Profile jadeProfile = new jade.core.ProfileImpl();


    public ContainerConfiguration() {
        jadeProfile.setParameter(jade.core.Profile.SERVICES,
                                 jade.core.Profile.DEFAULT_SERVICES_NOMOBILITY);
    }


    public ContainerConfiguration(String host, int port, String containerName) {
        setHost(host);
        setPort(port);
        setContainerName(containerName);
        jadeProfile.setParameter(jade.core.Profile.SERVICES,
                                 jade.core.Profile.DEFAULT_SERVICES_NOMOBILITY);
    }


    ContainerConfiguration(jade.core.Profile profile) {
        jadeProfile = profile;
    }


    public void setHost(String host) {
        jadeProfile.setParameter(jade.core.Profile.MAIN_HOST, host);
    }


    public String getHost() {
        return jadeProfile.getParameter(jade.core.Profile.MAIN_HOST, null);
    }


    public String getLocalHost() {
        return jadeProfile.getParameter(Profile.LOCAL_HOST, null);
    }


    public void setLocalHost(String localHost) {
        jadeProfile.setParameter(Profile.LOCAL_HOST, localHost);
    }


    public void setPort(int port) {
        jadeProfile.setParameter(jade.core.Profile.MAIN_PORT, Integer.toString(port));
    }


    public int getPort() {
        return Integer.parseInt(jadeProfile.getParameter(jade.core.Profile.MAIN_PORT, "0"));
    }


    public void setLocalPort(int port) {
        jadeProfile.setParameter(jade.core.Profile.LOCAL_PORT, Integer.toString(port));
    }


    public int getLocalPort() {
        return Integer.parseInt(jadeProfile.getParameter(jade.core.Profile.LOCAL_PORT, "0"));
    }


    public void setContainerName(String containerName) {
        jadeProfile.setParameter(jade.core.Profile.CONTAINER_NAME, containerName);
    }


    public String getContainerName() {
        return jadeProfile.getParameter(jade.core.Profile.CONTAINER_NAME, null);
    }


    jade.core.Profile getJadeProfile() {
        return jadeProfile;
    }


    public void addService(String serviceClassName) {
        jadeProfile.setParameter(jade.core.Profile.SERVICES,
                                 jade.core.Profile.DEFAULT_SERVICES_NOMOBILITY + ";"
                                 + ServicesBootstrapper.class.getName());

        String previousServices =
              jadeProfile.getParameter(ServicesBootstrapper.SERVICES, null);
        if (previousServices == null) {
            jadeProfile.setParameter(ServicesBootstrapper.SERVICES, serviceClassName);
        }
        else {
            jadeProfile.setParameter(ServicesBootstrapper.SERVICES,
                                     previousServices + ";" + serviceClassName);
        }
    }


    public void setParameter(String key, String value) {
        jadeProfile.setParameter(key, value);
    }


    public String getParameter(String key) {
        return jadeProfile.getParameter(key, null);
    }


    public void loadConfig(String filePath) throws IOException {
        FileInputStream inputStream = new FileInputStream(filePath);
        try {
            loadConfig(inputStream);
        }
        finally {
            inputStream.close();
        }
    }


    public void loadConfig(InputStream inputStream)
          throws IOException {
        Properties properties = new Properties();

        properties.load(inputStream);

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            jadeProfile.setParameter((String)entry.getKey(), (String)entry.getValue());
        }
    }


    public String getJadeFileDir() {
        return jadeProfile.getParameter("file-dir", ".");
    }


    public void setJadeFileDirToTemporaryDir() {
        jadeProfile.setParameter(Profile.FILE_DIR, System.getProperty("java.io.tmpdir"));
    }


    public void resolveHostDnsName() {
        try {
            setHost(InetAddress.getByName(getHost()).getHostAddress());
        }
        catch (UnknownHostException e) {
            ;
        }
    }
}
