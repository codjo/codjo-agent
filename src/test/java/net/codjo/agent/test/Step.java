/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.Agent;
/**
 *
 */
public interface Step {
    void run(Agent agent) throws Exception;


    boolean done();
}
