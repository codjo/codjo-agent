/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.behaviour;
import junit.framework.TestCase;
/**
 * Classe de test de {@link CyclicBehaviour}.
 */
public class CyclicBehaviourTest extends TestCase {
    public void test_done() throws Exception {
        CyclicBehaviour behaviour = new CyclicBehaviour() {
            @Override
            public void action() {
            }
        };

        assertFalse(behaviour.done());
    }
}
