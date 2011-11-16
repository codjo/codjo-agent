/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.Agent;
/**
 * net.codjo.agent.util.ResponderAgent & AgentMock
 */
public class TesterAgent extends Agent {
    private TesterAgentRecorder recorder = new TesterAgentRecorder();


    @Override
    protected void setup() {
        addBehaviour(record().getBehaviour());
    }


    @Override
    protected void tearDown() {
    }


    public void addStoryListener(TesterAgentListener listener) {
        recorder.addStoryListener(listener);
    }


    public TesterAgentRecorder record() {
        return recorder;
    }


    public StoryErrorManager getErrorManager() {
        return recorder.getErrorManager();
    }


    void setErrorManager(StoryErrorManager assertionManager) {
        recorder.setErrorManager(assertionManager);
    }


    public void assertNoError() {
        getErrorManager().assertNoError();
    }


    public boolean isStoryFinished() {
        return recorder.getBehaviour().done();
    }
}
