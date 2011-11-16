/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Exception lancée lorsqu'une erreur est levé dans un service .
 *
 * @see Service
 * @see Service#boot(ContainerConfiguration)
 */
public class ServiceException extends Exception {
    public ServiceException(Throwable cause) {
        super(cause);
    }


    public ServiceException(String message) {
        super(message);
    }


    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
