package net.codjo.agent.test;
/**
 *
 */
interface TesterAgentListener {
    public void storyStarted();


    public void stepStarted(Step step);


    public void stepFinished(Step step);


    public void storyFinished();
}
