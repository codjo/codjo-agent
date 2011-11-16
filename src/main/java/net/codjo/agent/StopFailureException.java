/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Exception lancé lors de l'echec d'arret d'un conteneur.
 *
 * @see AgentContainer#stop()
 */
public class StopFailureException extends ContainerFailureException {
    public StopFailureException(Throwable cause) {
        super(cause);
    }
}
