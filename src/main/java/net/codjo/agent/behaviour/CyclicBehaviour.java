/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.behaviour;
import net.codjo.agent.Behaviour;

/**
 *
 */
public abstract class CyclicBehaviour extends Behaviour {
    @Override
    public boolean done() {
        return false;
    }
}
