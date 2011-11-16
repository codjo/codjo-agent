/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.test.common.LogString;
/**
 * Classe mock d'un {@link jade.core.ServiceManager} Jade.
 */
public class JadeServiceManagerMock implements jade.core.ServiceManager {
    private LogString log = new LogString();
    private jade.core.Service lastAddedService;


    public JadeServiceManagerMock(LogString log) {
        this.log = log;
    }


    public String getPlatformName() {
        return null;
    }


    public void addAddress(String addr) {}


    public void removeAddress(String addr) {}


    public String getLocalAddress() {
        return null;
    }


    public void addNode(jade.core.NodeDescriptor desc,
                        jade.core.ServiceDescriptor[] services) {}


    public void removeNode(jade.core.NodeDescriptor desc) {}


    public void activateService(jade.core.ServiceDescriptor desc) {
        log.call("activateService", desc.getService().getName());
        lastAddedService = desc.getService();
    }


    public void deactivateService(String string) {}


    public jade.core.Service getLastAddedService() {
        return lastAddedService;
    }
}
