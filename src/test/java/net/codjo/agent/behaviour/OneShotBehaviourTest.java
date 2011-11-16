/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.behaviour;
import junit.framework.TestCase;
/**
 * Classe de test de {@link OneShotBehaviour}.
 */
public class OneShotBehaviourTest extends TestCase {
    public void test_done() throws Exception {
        OneShotBehaviour behaviour = new OneShotBehaviour() {
            @Override
            public void action() {
            }
        };

        assertTrue(behaviour.done());
    }
}
