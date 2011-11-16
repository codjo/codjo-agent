package net.codjo.agent.test;
import net.codjo.test.common.LogString;
/**
 *
 */
public class TesterAgentListenerMock implements TesterAgentListener {
    private final LogString log;


    public TesterAgentListenerMock() {
        this.log = new LogString();
    }


    public TesterAgentListenerMock(LogString log) {
        this.log = log;
    }


    public void storyStarted() {
        log.call("storyStarted");
    }


    public void stepStarted(Step step) {
        log.call("stepStarted", toSimpleClassName(step.getClass().getName()));
    }


    public void stepFinished(Step step) {
        log.call("stepFinished", toSimpleClassName(step.getClass().getName()));
    }


    private String toSimpleClassName(String name) {
        char character = '.';
        if (name.indexOf("$") != -1) {
            character = '$';
        }
        return name.substring(name.lastIndexOf(character) + 1, name.length());
    }


    public void storyFinished() {
        log.call("storyFinished");
    }
}
