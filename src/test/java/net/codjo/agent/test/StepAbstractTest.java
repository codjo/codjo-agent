/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.test.common.LogString;
import junit.framework.TestCase;
/**
 *
 */
public class StepAbstractTest extends TestCase {
    private LogString log = new LogString();
    private Agent agent = new DummyAgent();
    private AclMessage message = new AclMessage(AclMessage.Performative.INFORM);


    public void test_triggerIsDoneAfterRunIsPerformedAndNoAction()
          throws Exception {
        MyStep trigger = new MyStep(0);
        assertTrue(trigger.done());

        trigger.run(agent);
        assertTrue(trigger.done());
        log.assertContent("MyStep.doRun(" + agent + ")");
    }


    public void test_triggerIsDoneAfterRunAndActionsArePerformed() throws Exception {
        MyStep trigger = new MyStep();
        trigger.addAction(new SubStepMock(new LogString("action1", log)));
        trigger.addAction(new SubStepMock(new LogString("action2", log)));

        assertFalse(trigger.done());
        trigger.run(agent);

        assertFalse(trigger.done());
        log.assertContent("MyStep.doRun(" + agent + ")");
        log.clear();

        trigger.run(agent);
        assertTrue(trigger.done());
        log.assertContent("action1.run(" + agent + ", " + message + "), action2.run("
                          + agent + ", " + message + ")");
    }


    public void test_triggerIsDoneAfter2Calls() throws Exception {
        MyStep trigger = new MyStep(2);
        trigger.addAction(new SubStepMock(new LogString("action1", log)));

        assertFalse(trigger.done());

        trigger.run(agent);
        assertFalse(trigger.done());
        log.assertContent("MyStep.doRun(" + agent + ")");
        log.clear();

        trigger.run(agent);
        assertFalse(trigger.done());
        log.assertContent("MyStep.doRun(" + agent + ")");
        log.clear();

        trigger.run(agent);
        assertTrue(trigger.done());
        log.assertContent("action1.run(" + agent + ", " + message + ")");
    }


    private class MyStep extends AbstractStep {
        private int callCountToBeDone;


        MyStep() {
            this(1);
        }


        MyStep(int callCountToBeDone) {
            super(null);
            this.callCountToBeDone = callCountToBeDone;
        }


        @Override
        protected void doRun(Agent agent) {
            log.call("MyStep.doRun", agent);
            callCountToBeDone--;
        }


        @Override
        protected boolean doRunDone() {
            return callCountToBeDone <= 0;
        }


        @Override
        protected AclMessage getMessage() {
            return message;
        }


        public void addAction(SubStep subStep) {
            addSubStepImpl(subStep);
        }
    }

    private class SubStepMock implements SubStep {
        private LogString log;


        private SubStepMock(LogString log) {
            this.log = log;
        }


        public void run(Agent agent, AclMessage message) {
            log.call("run", agent, message);
        }
    }
}
