package net.codjo.agent;
/**
 *
 */
public class ServiceMock implements Service, ServiceHelper {
    public String getName() {
        return getClass().getSimpleName();
    }


    public void boot(ContainerConfiguration containerConfiguration) throws ServiceException {
    }


    public ServiceHelper getServiceHelper(Agent agent) {
        return this;
    }


    public void init(Agent agent) {
    }
}
