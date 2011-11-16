/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Exception lancé lors de l'utilisation d'un controller invalide.
 *
 * @see AgentController
 */
public class BadControllerException extends ContainerFailureException {
    public BadControllerException(Throwable cause) {
        super(cause);
    }
}
