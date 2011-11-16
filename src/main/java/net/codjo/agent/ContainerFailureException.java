/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Ecehc dans le {@link AgentContainer}.
 */
public class ContainerFailureException extends Exception {
    public ContainerFailureException() {
    }


    public ContainerFailureException(Throwable cause) {
        super(cause);
    }


    public ContainerFailureException(String cause) {
        super(cause);
    }
}
