/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
/**
 * Conteneur d'Agent.
 *
 * <p> Il y a 2 types de conteneur : Conteneur principal, et Conteneur simple. Le conteneur simple doit être
 * attaché à un conteneur principal. </p>
 */
public abstract class AgentContainer {
    public static final int CONTAINER_PORT = 35700;
    private jade.wrapper.ContainerController jadeContainer;


    protected AgentContainer() {
    }


    protected AgentContainer(jade.wrapper.ContainerController containerController) {
        this.jadeContainer = containerController;
    }


    public static AgentContainer createMainContainer() {
        ContainerConfiguration containerConfiguration = new ContainerConfiguration();
        containerConfiguration.setLocalPort(AgentContainer.CONTAINER_PORT);
        return new MainContainer(containerConfiguration);
    }


    public static AgentContainer createMainContainer(ContainerConfiguration containerConfiguration) {
        return new MainContainer(containerConfiguration);
    }


    public static AgentContainer createContainer(ContainerConfiguration containerConfiguration) {
        return new Container(containerConfiguration);
    }


    public void start() throws ContainerFailureException {
        jadeContainer = startJade();

        assertJadeContainerIsStarted();
    }


    public void stop() throws ContainerFailureException {
        assertJadeContainerIsStarted();
        try {
            // UGLY : Petite pause pour éviter un arret trop rapide dans le cadre des tests
            Thread.sleep(50);
            jadeContainer.kill();
        }
        catch (Exception e) {
            throw new StopFailureException(new Exception(
                  "Un des agents du conteneur n'a peut être pas été demarré ?!", e));
        }
        assertJadeContainerIsStopped();
    }


    public abstract boolean isAlive();


    public void startRMA() throws ContainerFailureException {
        assertJadeContainerIsStarted();
        try {
            jadeContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]).start();
        }
        catch (jade.wrapper.StaleProxyException e) {
            throw new ContainerFailureException(e);
        }
    }


    public AgentController acceptNewAgent(String nickname, final Agent agent)
          throws ContainerFailureException {
        assertJadeContainerIsStarted();
        try {
            return new AgentController(jadeContainer.acceptNewAgent(nickname, agent.getJadeAgent()));
        }
        catch (jade.wrapper.StaleProxyException e) {
            throw new ContainerFailureException(e);
        }
    }


    public AgentController getAgent(String localName)
          throws ContainerFailureException {
        try {
            return new AgentController(jadeContainer.getAgent(localName));
        }
        catch (jade.wrapper.ControllerException e) {
            throw new ContainerFailureException(e);
        }
    }


    public String getContainerName() {
        if (jadeContainer == null) {
            return "n/a";
        }
        try {
            return jadeContainer.getContainerName();
        }
        catch (jade.wrapper.ControllerException e) {
            return "n/a";
        }
    }


    abstract jade.wrapper.ContainerController startJade()
          throws StartFailureException;


    jade.wrapper.ContainerController getJadeContainer() {
        return jadeContainer;
    }


    boolean same(jade.wrapper.ContainerController otherJadeContainer)
          throws jade.wrapper.ControllerException {
        jade.wrapper.PlatformController platformController = jadeContainer.getPlatformController();
        return jadeContainer.getPlatformName().equals(otherJadeContainer.getPlatformName())
               && (platformController.getState() == otherJadeContainer.getPlatformController().getState());
    }


    private void assertJadeContainerIsStarted() throws ContainerNotStartedException {
        try {
            assertJadeContainerIsNotNull();
        }
        catch (NullPointerException e) {
            throw new ContainerNotStartedException(e);
        }
        if (jadeContainer.getPlatformName() == null) {
            throw new ContainerNotStartedException();
        }

//        if (PlatformState.cPLATFORM_STATE_READY != jadeContainer.getState().getCode()) {
//            throw new ContainerNotStartedException();
//        }
    }


    private void assertJadeContainerIsStopped() throws StopFailureException {
        try {
            assertJadeContainerIsNotNull();
        }
        catch (NullPointerException e) {
            throw new StopFailureException(e);
        }

//        if (PlatformState.cPLATFORM_STATE_KILLED != jadeContainer.getState().getCode()) {
//            throw new StopFailureException(new Exception("Container not killed !"));
//        }
    }


    private void assertJadeContainerIsNotNull() throws NullPointerException {
        if (jadeContainer == null) {
            throw new NullPointerException("AgentContainer.jadeContainer");
        }
    }


    private static final class MainContainer extends AgentContainer {
        private ContainerConfiguration containerConfiguration;


        MainContainer(ContainerConfiguration containerConfiguration) {
            this.containerConfiguration = containerConfiguration;
        }


        @Override
        public jade.wrapper.AgentContainer startJade()
              throws StartFailureException {
            jade.core.Runtime runtime = jade.core.Runtime.instance();

            jade.wrapper.AgentContainer jade =
                  runtime.createMainContainer(containerConfiguration.getJadeProfile());
            if (jade == null) {
                throw new StartFailureException();
            }

            // Bizarre, il n'est pas nécessaire de démarrer le container pour qu'il marche ?!?
            //    try { jade.start(); }
            //    catch (ControllerException e) { throw new StartFailureException(e);}
            return jade;
        }


        @Override
        public boolean isAlive() {
            if (getJadeContainer() == null) {
                return false;
            }

            // Contournement BUG du kill qui ne met pas platformController a null
            //      cf. getState()
            if (getJadeContainer().getPlatformName() == null) {
                return false;
            }
            // END Contournement BUG

            try {
                jade.wrapper.PlatformController controller = getJadeContainer().getPlatformController();
                return jade.wrapper.PlatformState.cPLATFORM_STATE_READY == controller.getState().getCode();
            }
            catch (jade.wrapper.ControllerException e) {
                return false;
            }
        }
    }

    private static final class Container extends AgentContainer {
        private final ContainerConfiguration containerConfiguration;


        Container(ContainerConfiguration containerConfiguration) {
            this.containerConfiguration = containerConfiguration;
        }


        @Override
        public jade.wrapper.AgentContainer startJade()
              throws StartFailureException {
            jade.core.Runtime runtime = jade.core.Runtime.instance();
            jade.wrapper.AgentContainer jade =
                  runtime.createAgentContainer(containerConfiguration.getJadeProfile());
            if (jade == null) {
                throw new StartFailureException();
            }
            return jade;
        }


        @Override
        public boolean isAlive() {
            try {
                getJadeContainer().getContainerName();
                return true;
            }
            catch (jade.wrapper.ControllerException e) {
                return false;
            }
        }
    }

    public static class Wrapper extends AgentContainer {
        public Wrapper(jade.wrapper.AgentContainer agentContainer) {
            super(agentContainer);
            if (agentContainer == null) {
                throw new NullPointerException("Wrapper#Wrapper.agentContainer");
            }
        }


        @Override
        public boolean isAlive() {
            try {
                getJadeContainer().getContainerName();
                return true;
            }
            catch (jade.wrapper.ControllerException e) {
                return false;
            }
        }


        @Override
        jade.wrapper.ContainerController startJade() throws StartFailureException {
            if (getJadeContainer() == null) {
                throw new StartFailureException();
            }
            return getJadeContainer();
        }


        @Override
        public String toString() {
            return "Wrapper{" + getJadeContainer().getPlatformName() + "}";
        }
    }
}
