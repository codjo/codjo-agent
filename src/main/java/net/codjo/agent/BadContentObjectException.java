/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Exception lancé lors de l'utilisation des méthode get/set Content Object de {@link AclMessage}.
 *
 * @see AclMessage#getContentObject()
 * @see AclMessage#setContentObject(java.io.Serializable)
 */
public class BadContentObjectException extends RuntimeException {
    public BadContentObjectException(Throwable error) {
        super(error);
    }
}
