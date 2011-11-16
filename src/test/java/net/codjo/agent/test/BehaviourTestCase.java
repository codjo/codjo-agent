/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.Agent;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.test.Story.ConnectionType;
import junit.framework.TestCase;
/**
 * Classe permettant de faciliter l'écriture d'un test de Behaviour.
 */
public abstract class BehaviourTestCase extends TestCase {
    protected AgentContainerFixture containerFixture;


    @Override
    protected final void setUp() throws Exception {
        containerFixture = new AgentContainerFixture();
        containerFixture.doSetUp();

        containerFixture.startContainer(ConnectionType.NO_CONNECTION);
        doSetUp();
    }


    protected void doSetUp() throws Exception {
    }


    @Override
    protected final void tearDown() throws Exception {
        containerFixture.doTearDown();
        doTearDown();
    }


    protected void doTearDown() throws Exception {
    }


    protected void acceptAgent(String nickname, Agent agent) throws ContainerFailureException {
        containerFixture.startNewAgent(nickname, agent);
    }
}
