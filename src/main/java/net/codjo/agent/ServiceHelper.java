/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Permet à un agent d'acceder au service qui y est associé.
 *
 * @see Service#getServiceHelper(Agent)
 */
public interface ServiceHelper {
    public void init(Agent agent);
}
