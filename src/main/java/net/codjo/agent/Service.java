/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Décrit un service associé à un container.
 */
public interface Service {
    /**
     * Retourne le nom du service (unique).
     *
     * @return un nom
     */
    public String getName();


    /**
     * Initialisation du service.
     *
     * @param containerConfiguration
     *
     * @throws ServiceException
     */
    public void boot(ContainerConfiguration containerConfiguration) throws ServiceException;


    /**
     * Retourne le serviceHelper associé à l'agent passé en paramètre
     *
     * @param agent
     *
     * @return un serviceHelper
     */
    public ServiceHelper getServiceHelper(Agent agent);
}
