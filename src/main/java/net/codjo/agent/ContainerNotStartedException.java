/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Exception lancé lorsqu'une opération nécessitant que le {@link AgentContainer} soit démarré alors qu'il ne
 * l'est pas.
 *
 * @see AgentContainer#acceptNewAgent(String, Agent)
 */
class ContainerNotStartedException extends ContainerFailureException {
    ContainerNotStartedException() {}


    ContainerNotStartedException(Throwable cause) {
        super(cause);
    }
}
