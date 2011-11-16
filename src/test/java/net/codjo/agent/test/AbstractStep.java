/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.test.common.LogString;
import java.util.ArrayList;
import java.util.List;
/**
 *
 */
public abstract class AbstractStep implements Step, TesterAgentRecorder.Then {
    private List<SubStep> actionList = new ArrayList<SubStep>();
    private boolean runPerformedOnce;
    protected final TesterAgentRecorder recorder;


    protected AbstractStep(TesterAgentRecorder recorder) {
        this.recorder = recorder;
    }


    protected abstract void doRun(Agent agent);


    protected abstract boolean doRunDone();


    protected abstract AclMessage getMessage();


    public final void run(Agent agent) throws Exception {
        if (!runPerformedOnce || !doRunDone()) {
            doRun(agent);
            runPerformedOnce = true;
        }
        else {
            for (SubStep subStep : actionList) {
                subStep.run(agent, getMessage());
            }
            actionList.clear();
        }
    }


    protected AbstractStep addSubStepImpl(SubStep subStep) {
        actionList.add(subStep);
        return this;
    }


    public final boolean done() {
        return doRunDone() && actionList.isEmpty();
    }


    protected void addLog(LogString log, String info) {
        addSubStepImpl(new LogSubStep(log, info));
    }


    public void die() {
        addSubStepImpl(new SubStep() {
            public void run(Agent agent, AclMessage message) {
                agent.die();
            }
        });
    }


    public TesterAgentRecorder then() {
        return recorder;
    }


    private static class LogSubStep implements SubStep {
        private final LogString log;
        private final String info;


        LogSubStep(LogString log, String info) {
            this.log = log;
            this.info = info;
        }


        public void run(Agent agent, AclMessage message) {
            log.info(info);
        }
    }
}
