/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.test.common.LogString;
import junit.framework.Assert;
import junit.framework.TestCase;
/**
 * Classe de test de {@link ReceiveMessageStep}.
 */
public class ReceiveMessageStepTest extends TestCase {
    private AgentContainerFixture fixture = new AgentContainerFixture();
    private LogString log = new LogString();
    private TesterAgent tester = new TesterAgent();


    public void test_addAction() throws Exception {
        tester.record().receiveMessage()
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage message) {
                      Assert.assertEquals("content", message.getContentObject());
                  }
              }).die();

        fixture.startNewAgent("tester", tester);
        fixture.sendRequestMessageTo("content", "tester");
        fixture.waitForAgentDeath("tester");
        assertFalse(tester.getErrorManager().hasError());
    }


    public void test_releaseSemaphore() throws Exception {
        Semaphore semaphore =
              new Semaphore() {
                  @Override
                  public void release() {
                      log.call("release");
                  }
              };
        tester.record().receiveMessage().releaseSemaphore(semaphore).die();

        fixture.startNewAgent("tester", tester);
        fixture.sendRequestMessageTo("content", "tester");
        fixture.waitForAgentDeath("tester");
        log.assertContent("release()");
    }


    public void test_then() throws Exception {
        assertSame(tester.record(), tester.record().receiveMessage().then());
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }
}
