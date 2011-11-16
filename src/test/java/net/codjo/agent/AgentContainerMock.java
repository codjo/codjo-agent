/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.test.common.LogString;
/**
 * Mock d'un {@link AgentContainer}.
 */
public class AgentContainerMock extends AgentContainer {
    protected LogString log;
    private String lastNickname;
    private Agent lastAgent;


    public AgentContainerMock(LogString log) {
        this.log = log;
    }


    public String getLastNickname() {
        return lastNickname;
    }


    public Agent getLastAgent() {
        return lastAgent;
    }


    @Override
    public AgentController acceptNewAgent(String nickname, Agent agent)
          throws ContainerFailureException {
        log.call("acceptNewAgent", nickname, toSimpleName(agent.getClass()));
        lastNickname = nickname;
        lastAgent = agent;
        return new AgentControllerMock(new LogString(nickname, log));
    }


    private String toSimpleName(Class aClass) {
        String name = aClass.getName();
        return name.substring(name.lastIndexOf(".") + 1);
    }


    @Override
    public boolean isAlive() {
        return true;
    }


    @Override
    jade.wrapper.AgentContainer startJade() throws StartFailureException {
        return null;
    }


    private static class AgentControllerMock extends AgentController {
        private LogString log;


        AgentControllerMock(LogString logString) {
            super(null);
            this.log = logString;
        }


        @Override
        public void start() throws BadControllerException {
            log.call("start");
        }


        @Override
        public void kill() throws BadControllerException {
            log.call("kill");
        }
    }
}
