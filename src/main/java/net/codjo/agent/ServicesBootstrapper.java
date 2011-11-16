/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
/**
 * Service Jade permettant de lancer les Services AGF (de la couche Agent).
 */
public class ServicesBootstrapper extends jade.core.BaseService {
    public static final String SERVICES = "ServicesBootstrapper.SERVICES";
    public static final String NAME = "ServicesBootstrapper.NAME";
    private static final Map<jade.core.Profile, MutablePicoContainer> picoContainers
          = Collections.synchronizedMap(new HashMap<jade.core.Profile, MutablePicoContainer>());
    private jade.core.AgentContainer jadeAgentContainer;


    static MutablePicoContainer getPicoContainer(ContainerConfiguration configuration) {
        return picoContainers.get(configuration.getJadeProfile());
    }


    static void setPicoContainer(ContainerConfiguration configuration, MutablePicoContainer picoContainer) {
        picoContainers.put(configuration.getJadeProfile(), picoContainer);
    }


    static void removePicoContainer(ContainerConfiguration configuration) {
        picoContainers.remove(configuration.getJadeProfile());
    }


    public String getName() {
        return NAME;
    }


    @Override
    public void init(jade.core.AgentContainer agentContainer, jade.core.Profile profile)
          throws jade.core.ProfileException {
        jadeAgentContainer = agentContainer;
    }


    @Override
    public void boot(jade.core.Profile profile) throws jade.core.ServiceException {
        String serviceClassList = profile.getParameter(SERVICES, null);

        if (serviceClassList != null) {
            ContainerConfiguration containerConfiguration = new ContainerConfiguration(profile);
            String serviceClass = serviceClassList;
            int separatorIndex = serviceClassList.indexOf(";");

            try {
                while (separatorIndex != -1) {
                    serviceClass = serviceClassList.substring(0, separatorIndex);

                    activateAGFService(serviceClass, containerConfiguration);

                    serviceClassList = serviceClassList.substring(separatorIndex + 1);
                    separatorIndex = serviceClassList.indexOf(";");
                }
                activateAGFService(serviceClassList, containerConfiguration);
            }
            catch (Exception e) {
                throw new jade.core.ServiceException("Service AGF en Erreur : " + serviceClass, e);
            }
        }
    }


    private void activateAGFService(String serviceClass, ContainerConfiguration containerConfiguration)
          throws ClassNotFoundException, InstantiationException, IllegalAccessException,
                 jade.core.IMTPException, jade.core.ServiceException, ServiceException {
        Class<?> agfServiceClass = Class.forName(serviceClass);

        Service agfService = createAgfService(agfServiceClass, containerConfiguration);

        jade.core.ServiceDescriptor descriptor =
              new jade.core.ServiceDescriptor(agfService.getName(), new JadeServiceAdapter(agfService));

        jadeAgentContainer.getServiceManager().activateService(descriptor);
        agfService.boot(containerConfiguration);
    }


    private Service createAgfService(Class<?> agfServiceClass, ContainerConfiguration configuration)
          throws InstantiationException, IllegalAccessException {
        MutablePicoContainer container = getPicoContainer(configuration);
        if (container == null) {
            return (Service)agfServiceClass.newInstance();
        }
        else {
            ComponentAdapter adapter = container.registerComponentImplementation(agfServiceClass);
            return (Service)adapter.getComponentInstance(container);
        }
    }


    private static Agent toAgfAgent(jade.core.Agent agent) {
        return ((Agent.JadeAgentAdapter)agent).getAgfAgent();
    }


    static ServiceHelper getServiceHelper(Agent agent, String serviceName)
          throws jade.core.ServiceException {
        JadeServiceHelperAdapter jadeHelper =
              (JadeServiceHelperAdapter)agent.getJadeAgent().getHelper(serviceName);
        return jadeHelper.getAgfServiceHelper();
    }


    class JadeServiceAdapter extends jade.core.BaseService {
        private Service agfService;


        JadeServiceAdapter(Service agfService) {
            this.agfService = agfService;
        }


        public String getName() {
            if (agfService == null) {
                return getClass().getSimpleName();
            }
            return agfService.getName();
        }


        @Override
        public jade.core.ServiceHelper getHelper(jade.core.Agent agent) {
            Agent agfAgent = toAgfAgent(agent);
            ServiceHelper agfServiceHelper = agfService.getServiceHelper(agfAgent);
            if (agfServiceHelper == null) {
                return null;
            }
            return new JadeServiceHelperAdapter(agfServiceHelper);
        }
    }

    static class JadeServiceHelperAdapter implements jade.core.ServiceHelper {
        private ServiceHelper agfServiceHelper;


        JadeServiceHelperAdapter(ServiceHelper agfServiceHelper) {
            this.agfServiceHelper = agfServiceHelper;
        }


        public ServiceHelper getAgfServiceHelper() {
            return agfServiceHelper;
        }


        public void init(jade.core.Agent agent) {
            agfServiceHelper.init(toAgfAgent(agent));
        }
    }
}
