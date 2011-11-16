/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.Agent;
import net.codjo.test.common.LogString;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
/**
 *
 */
public class TesterAgentRecorderTest extends TestCase {
    private TesterAgentRecorder recorder;
    private LogString log = new LogString();


    public void test_addStep() throws Exception {
        assertTrue(recorder.getBehaviour().done());

        recorder.addStep(new StepMock(log, 2));
        assertFalse(recorder.getBehaviour().done());

        recorder.action();
        assertFalse(recorder.getBehaviour().done());
        log.assertContent("run(" + recorder.getBehaviour().getAgent() + ")");
        log.clear();

        recorder.action();
        assertTrue(recorder.getBehaviour().done());

        log.assertContent("run(" + recorder.getBehaviour().getAgent() + ")");
    }


    public void test_perform_likeAddStep() throws Exception {
        assertTrue(recorder.getBehaviour().done());

        recorder.perform(new StepMock(log, 2));
        assertFalse(recorder.getBehaviour().done());

        recorder.action();
        assertFalse(recorder.getBehaviour().done());
        log.assertContent("run(" + recorder.getBehaviour().getAgent() + ")");
        log.clear();

        recorder.action();
        assertTrue(recorder.getBehaviour().done());

        log.assertContent("run(" + recorder.getBehaviour().getAgent() + ")");
    }


    public void test_perform_accessStep() throws Exception {
        StepMock original = new StepMock(log, 2);

        assertSame(original, recorder.perform(original).step());
    }


    public void test_perform_canDoThen() throws Exception {
        assertSame(recorder, recorder.perform(new StepMock(log, 2)).then());
    }


    public void test_addSeveralTriggers() throws Exception {
        recorder.addStep(new StepMock(new LogString("trigger1", log)));
        recorder.addStep(new StepMock(new LogString("trigger2", log)));

        recorder.action();
        assertFalse(recorder.getBehaviour().done());
        log.assertContent("trigger1.run(" + recorder.getBehaviour().getAgent() + ")");
        log.clear();

        recorder.action();
        assertTrue(recorder.getBehaviour().done());
        log.assertContent("trigger2.run(" + recorder.getBehaviour().getAgent() + ")");
    }


    public void test_behaviourIsDoneIfAFailureOccurs()
          throws Exception {
        StepMock failureTrigger =
              new StepMock(new LogString("triggerFailure", log));
        failureTrigger.mockRunFailure("error");

        recorder.addStep(failureTrigger);
        recorder.addStep(new StepMock(new LogString("trigger2", log)));

        recorder.action();
        assertTrue(recorder.getBehaviour().done());
        log.assertContent("triggerFailure.run(" + recorder.getBehaviour().getAgent() + ")");
        assertTrue(recorder.getErrorManager().hasError());
        assertEquals("agent 'agentNickName' : error", recorder.getErrorManager().getFirstErrorDescription());
    }


    public void test_addStoryListener() throws Exception {
        recorder.addStoryListener(new TesterAgentListenerMock(log));

        recorder.addStep(new StepMock(new LogString("trigger1", log), 1));
        recorder.addStep(new StepMock(new LogString("trigger2", log), 1));

        recorder.action();
        recorder.action();
        assertTrue(recorder.getBehaviour().done());

        log.assertContent("storyStarted()"
                          + ", stepStarted(StepMock)"
                          + ", trigger1.run(" + recorder.getBehaviour().getAgent() + ")"
                          + ", stepFinished(StepMock)"
                          + ", stepStarted(StepMock)"
                          + ", trigger2.run(" + recorder.getBehaviour().getAgent() + ")"
                          + ", stepFinished(StepMock)"
                          + ", storyFinished()");
    }


    @Override
    protected void setUp() throws Exception {
        recorder = new TesterAgentRecorder() {
            @Override
            protected String getNickName() {
                return "agentNickName";
            }
        };
        recorder.getBehaviour().setAgent(new DummyAgent());
    }


    private static class StepMock implements Step {
        private LogString log;
        private int callCountToBeDone;
        private String error;


        StepMock(LogString log) {
            this(log, 1);
        }


        StepMock(LogString log, int callCountToBeDone) {
            this.log = log;
            this.callCountToBeDone = callCountToBeDone;
        }


        public void mockRunFailure(String errorMessage) {
            this.error = errorMessage;
        }


        public void run(Agent agent) throws AssertionFailedError {
            log.call("run", agent);
            callCountToBeDone--;
            if (error != null) {
                throw new AssertionFailedError(error);
            }
        }


        public boolean done() {
            return callCountToBeDone <= 0;
        }
    }
}
