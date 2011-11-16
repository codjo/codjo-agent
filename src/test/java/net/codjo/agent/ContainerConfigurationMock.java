/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.test.common.LogString;
/**
 *
 */
public class ContainerConfigurationMock extends ContainerConfiguration {
    private final LogString log;


    public ContainerConfigurationMock(LogString log) {
        this.log = log;
    }


    @Override
    public void addService(String serviceClassName) {
        log.call("addService", serviceClassName);
    }
}
