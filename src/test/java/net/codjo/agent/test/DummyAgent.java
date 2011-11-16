/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
/**
 * Dummy agent utilisable pour faire des tests dans un container.
 *
 * <p> NB : Pour faire des tests sans container il est préférable d'utiliser le {@link
 * net.codjo.agent.AgentMock}. </p>
 */
public class DummyAgent extends Agent {
    public static final SemaphoreByToken.Token SETUP = new SemaphoreByToken.Token("setup_step");
    public static final SemaphoreByToken.Token TEARDOWN = new SemaphoreByToken.Token("teardown_step");
    private SemaphoreByToken semaphore = new SemaphoreByToken();


    public DummyAgent(Behaviour behaviour) {
        addBehaviour(behaviour);
    }


    public DummyAgent() {
    }


    public SemaphoreByToken getSemaphore() {
        return semaphore;
    }


    @Override
    protected void setup() {
        semaphore.release(SETUP);
    }


    @Override
    protected void tearDown() {
        semaphore.release(TEARDOWN);
    }
}
