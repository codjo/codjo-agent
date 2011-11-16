/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
/**
 *
 */
public interface SubStep {
    public void run(Agent agent, AclMessage message) throws Exception;
}
