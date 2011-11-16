/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Controller d'agent.
 */
public class AgentController {
    private jade.wrapper.AgentController jadeController;


    AgentController(jade.wrapper.AgentController agentController) {
        jadeController = agentController;
    }


    public void start() throws BadControllerException {
        try {
            jadeController.start();
        }
        catch (jade.wrapper.StaleProxyException e) {
            throw new BadControllerException(e);
        }
    }


    public String getName() throws BadControllerException {
        try {
            return jadeController.getName();
        }
        catch (jade.wrapper.StaleProxyException e) {
            throw new BadControllerException(e);
        }
    }


    public void putO2AObject(Object object) throws BadControllerException {
        try {
            jadeController.putO2AObject(object, jade.wrapper.AgentController.ASYNC);
        }
        catch (jade.wrapper.StaleProxyException e) {
            throw new BadControllerException(e);
        }
    }


    public void kill() throws BadControllerException {
        try {
            jadeController.kill();
        }
        catch (jade.wrapper.StaleProxyException e) {
            throw new BadControllerException(e);
        }
    }
}
