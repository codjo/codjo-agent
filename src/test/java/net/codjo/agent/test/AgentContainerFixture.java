/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.AgentController;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.DFService;
import net.codjo.agent.JadeWrapper;
import net.codjo.agent.MessageTemplate;
import net.codjo.agent.behaviour.OneShotBehaviour;
import net.codjo.agent.test.Story.ConnectionType;
import net.codjo.test.common.fixture.Fixture;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
/**
 * Fixture pour l'utilisation d'un {@link net.codjo.agent.AgentContainer}.
 *
 * @noinspection OverlyCoupledClass
 */
public class AgentContainerFixture implements Fixture {
    private AgentContainer container;
    private int maxTryBeforeFailure = 40;
    private long assertTimeout = 20;


    public AgentContainer getContainer() {
        if (container == null) {
            startContainer();
        }
        return container;
    }


    public boolean doesContainsAgent(String agentLocalName) {
        return doesContainsAgent(getContainer(), agentLocalName);
    }


    public boolean doesContainsAgent(AgentContainer agentContainer, String agentLocalName) {
        try {
            agentContainer.getAgent(agentLocalName);
            return true;
        }
        catch (ContainerFailureException ex) {
            return false;
        }
    }


    public void sendRequestMessageTo(String content, String destLocalName) throws ContainerFailureException {
        AclMessage message = new AclMessage(AclMessage.Performative.REQUEST);
        message.addReceiver(new Aid(destLocalName));
        message.setContentObject(content);
        sendMessage(message);
    }


    public void sendMessage(final AclMessage message) throws ContainerFailureException {
        final DummyAgent agent = new DummyAgent();
        startNewAgent(this.toString(), agent);
        runInAgentThread(agent,
                         new java.lang.Runnable() {
                             public void run() {
                                 agent.send(message);
                                 agent.die();
                             }
                         });
    }


    public void sendMessage(final Agent agent, final AclMessage message) {
        runInAgentThread(agent,
                         new java.lang.Runnable() {
                             public void run() {
                                 agent.send(message);
                             }
                         });
    }


    public AclMessage receiveMessage(Agent receiver) {
        return receiveMessage(receiver, MessageTemplate.matchAll());
    }


    public AclMessage receiveMessage(Agent receiver, final MessageTemplate template) {
        jade.lang.acl.ACLMessage aclMessage =
              JadeWrapper.unwrapp(receiver).blockingReceive(JadeWrapper.unwrapp(template), 1000);
        return JadeWrapper.wrapp(aclMessage);
    }


    public Aid[] searchAgentWithService(String type) {
        try {
            DFService.AgentDescription searchTemplate = new DFService.AgentDescription();
            searchTemplate.addService(new DFService.ServiceDescription(type));
            Agent searcherAgent = new DummyAgent();
            startNewAgent("Searcher" + searcherAgent.hashCode(), searcherAgent);
            DFService.AgentDescription[] descriptions = DFService.search(searcherAgent, searchTemplate);
            Aid[] results = new Aid[descriptions.length];
            for (int i = 0; i < descriptions.length; i++) {
                results[i] = descriptions[i].getAID();
            }

            return results;
        }
        catch (Exception e) {
            IllegalStateException exception =
                  new IllegalStateException("searchAgentWithService en erreur " + e.toString());
            exception.initCause(e);
            throw exception;
        }
    }


    public void runInAgentThread(Agent agent, final java.lang.Runnable runnable) {
        final Semaphore semaphore = new Semaphore();
        agent.addBehaviour(new Behaviour() {
            @Override
            protected void action() {
                runnable.run();
                semaphore.release();
            }


            @Override
            public boolean done() {
                return true;
            }
        });
        semaphore.acquire();
    }


    public void runWithTimeout(final Runnable runnable) throws Exception {
        final Semaphore semaphore = new Semaphore();

        Runner runner = new Runner(runnable, semaphore);
        new Thread(runner).start();

        semaphore.acquire();

        if (runner.hasError()) {
            throw runner.getException();
        }
    }


    public void doSetUp() {
    }


    public void doTearDown() throws Exception {
        if (container != null && container.isAlive()) {
            container.stop();
            Assert.assertFalse(container.isAlive());
            container = null;
        }
    }


    public void startContainer() {
        startContainer(ConnectionType.DEFAULT_CONNECTION);
    }


    public void startContainer(ConnectionType connectionType) {
        container = AgentContainer.createMainContainer(Story.containerConfigurationFor(connectionType));
        try {
            container.start();
        }
        catch (ContainerFailureException e) {
            throw new IllegalStateException("Impossible de démarrer le main-container " + e.toString());
        }
    }


    public void startContainer(ContainerConfiguration configuration) {
        container = AgentContainer.createMainContainer(configuration);
        try {
            container.start();
        }
        catch (ContainerFailureException e) {
            throw new IllegalStateException("Impossible de démarrer le main-container " + e.toString());
        }
    }


    public void stopContainer() {
        if (container == null) {
            return;
        }
        try {
            container.stop();
            container = null;
        }
        catch (ContainerFailureException e) {
            throw new IllegalStateException("Impossible de Stopper le container " + e.toString());
        }
    }


    public boolean isContainerStarted() {
        return container != null;
    }


    public void waitForAgentDeath(final String agentLocalAID) {
        assertUntilOk(new AgentAssert.Assertion() {
            public void check() {
                Assert.assertFalse(doesContainsAgent(agentLocalAID));
            }
        });
    }


    public void waitForAgentDeath(Agent agentToKill) {
        waitForAgentDeath(agentToKill.getAID().getLocalName());
    }


    public AgentController acceptNewAgent(String name, Agent agent) throws ContainerFailureException {
        return getContainer().acceptNewAgent(name, agent);
    }


    public AgentController startNewAgent(String nickName, Agent agent) throws ContainerFailureException {
        final Semaphore semaphore = new Semaphore();
        agent.addBehaviour(new OneShotBehaviour() {
            @Override
            protected void action() {
                semaphore.release();
            }
        });

        AgentController controller = acceptNewAgent(nickName, agent);
        controller.start();
        semaphore.acquire();
        return controller;
    }


    public void killAgent(String nickName) {
        try {
            container.getAgent(nickName).kill();
        }
        catch (ContainerFailureException e) {
            throw new IllegalStateException("Impossible de démarrer le main-container " + e.toString());
        }
    }


    public void killAgent(Agent agent) throws ContainerFailureException {
        killAgent(agent.getAID().getLocalName());
    }


    public void startContainerWithJdbcService() {
//        JdbcFixture jdbcFixture = JdbcFixture.newSybaseFixture();
//        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
//        containerConfiguration.setLocalPort(AgentContainer.CONTAINER_PORT);
//        containerConfiguration.setParameter(JdbcService.DRIVER_PARAMETER, jdbcFixture.getDriver());
//        containerConfiguration.setParameter(JdbcService.URL_PARAMETER, jdbcFixture.getUrl());
//        containerConfiguration.setParameter(JdbcService.CATALOG_PARAMETER, jdbcFixture.getCatalog());
//        containerConfiguration.addService(JdbcService.class.getName());
    }


    public void assertNumberOfAgentWithService(final int expectedCount, final String serviceType) {
        assertUntilOk(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                Aid[] aids = searchAgentWithService(serviceType);
                Assert.assertEquals("Nombre d'agent avec service '" + serviceType + "'",
                                    expectedCount, aids.length);
            }
        });
    }


    public void assertAgentWithService(final String[] expectedLocalNames, final String serviceType) {
        assertUntilOk(new AgentAssert.Assertion() {
            public void check() throws AssertionFailedError {
                List expected = Arrays.asList(expectedLocalNames);

                Aid[] aids = searchAgentWithService(serviceType);
                Assert.assertEquals("Nombre d'agent avec service '" + serviceType + "'",
                                    expectedLocalNames.length, aids.length);
                for (Aid aid : aids) {
                    Assert.assertTrue(aid.getLocalName() + " is not expected",
                                      expected.contains(aid.getLocalName()));
                }
            }
        });
    }


    public void assertBehaviourDone(final Behaviour behaviour) {
        assertUntilOk(AgentAssert.behaviourDone(behaviour));
    }


    public void assertBehaviourNotDone(final Behaviour behaviour) {
        assertUntilOk(AgentAssert.behaviourNotDone(behaviour));
    }


    public void assertContainsAgent(final String localName) {
        assertUntilOk(new AgentAssert.Assertion() {
            public void check() {
                Assert.assertTrue("Agent '" + localName + "' existe",
                                  doesContainsAgent(localName));
            }
        });
    }


    public void assertContainsAgent(final AgentContainer oneContainer, final String name) {
        assertUntilOk(new AgentAssert.Assertion() {
            public void check() {
                Assert.assertTrue("Agent '" + name + "' existe",
                                  doesContainsAgent(oneContainer, name));
            }
        });
    }


    public void assertNotContainsAgent(final String localName) {
        assertUntilOk(new AgentAssert.Assertion() {
            public void check() {
                Assert.assertFalse("Agent '" + localName + "' n'existe pas",
                                   doesContainsAgent(localName));
            }
        });
    }


    public void assertMessage(AclMessage message,
                              String protocol,
                              AclMessage.Performative performative,
                              Serializable content) {
        Assert.assertNotNull(message);
        Assert.assertEquals(protocol, message.getProtocol());
        Assert.assertEquals(AclMessage.performativeToString(performative),
                            AclMessage.performativeToString(message.getPerformative()));
        Assert.assertEquals(content, message.getContentObject());
    }


    public void assertStringMessage(AclMessage message,
                                    String protocol,
                                    AclMessage.Performative performative,
                                    String content) {
        Assert.assertNotNull(message);
        Assert.assertEquals(protocol, message.getProtocol());
        Assert.assertEquals(AclMessage.performativeToString(performative),
                            AclMessage.performativeToString(message.getPerformative()));
        Assert.assertEquals(content, message.getContent());
    }


    public void assertMessage(AclMessage message, String protocol, AclMessage.Performative performative) {
        Assert.assertNotNull(message);
        Assert.assertEquals(protocol, message.getProtocol());
        Assert.assertEquals(AclMessage.performativeToString(performative),
                            AclMessage.performativeToString(message.getPerformative()));
    }


    public void assertReceivedMessage(Agent agent,
                                      String protocol,
                                      AclMessage.Performative performative,
                                      Serializable content) {
        assertMessage(receiveMessage(agent), protocol, performative, content);
    }


    public void assertUntilOk(AgentAssert.Assertion assertion) {
        AssertionFailedError error = checkAssertionIsOk(assertion);
        if (error == null) {
            return;
        }

        int tryCount = 0;
        do {
            try {
                Thread.sleep(assertTimeout);
            }
            catch (InterruptedException e) {
                ;
            }
            tryCount++;
            error = checkAssertionIsOk(assertion);
        }
        while (tryCount < maxTryBeforeFailure - 1 && error != null);

        if (error != null) {
            throw error;
        }
    }


    public void setMaxTryBeforeFailure(int maxTryBeforeFailure) {
        this.maxTryBeforeFailure = maxTryBeforeFailure;
    }


    private AssertionFailedError checkAssertionIsOk(AgentAssert.Assertion assertion) {
        try {
            assertion.check();
            return null;
        }
        catch (AssertionFailedError error) {
            return error;
        }
        catch (Throwable error) {
            AssertionFailedError failedError = new AssertionFailedError(error.getLocalizedMessage());
            failedError.initCause(error);
            return failedError;
        }
    }


    public long getAssertTimeout() {
        return assertTimeout;
    }


    public void setAssertTimeout(long assertTimeout) {
        this.assertTimeout = assertTimeout;
    }


    /**
     * Pour compatibilité ascendante seulement.
     */
    public interface Assertion extends AgentAssert.Assertion {
    }

    public interface Runnable {
        public void run() throws Exception;
    }

    private static class Runner implements java.lang.Runnable {
        private final Runnable runnable;
        private final Semaphore semaphore;
        private Exception exception;


        Runner(Runnable runnable, Semaphore semaphore) {
            this.runnable = runnable;
            this.semaphore = semaphore;
        }


        public void run() {
            try {
                runnable.run();
            }
            catch (Exception e) {
                exception = e;
            }
            finally {
                semaphore.release();
            }
        }


        public boolean hasError() {
            return getException() != null;
        }


        public Exception getException() {
            return exception;
        }
    }
}
