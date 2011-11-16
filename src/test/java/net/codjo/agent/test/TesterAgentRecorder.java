/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent.test;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.agent.Aid;
import net.codjo.agent.Behaviour;
import net.codjo.agent.DFService;
import net.codjo.agent.MessageTemplate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import junit.framework.AssertionFailedError;
/**
 *
 */
public class TesterAgentRecorder {
    private List<Step> triggers = new ArrayList<Step>();
    private StoryErrorManager errorManager = new StoryErrorManager();
    private List<TesterAgentListener> listeners = new ArrayList<TesterAgentListener>();
    private boolean storyIsStarting = true;
    private Behaviour behaviour = new MyBehaviour();
    private Step previousStep;


    public <T extends Step> StepWrapper<T> perform(T step) {
        return addStep(new StepWrapper<T>(this, step));
    }


    public SendMessageStep sendMessage(AclMessage.Performative performative,
                                       Aid receiver,
                                       Serializable content) {
        return addStep(new SendMessageStep(this, performative, receiver, content));
    }


    public SendMessageStep sendMessage(AclMessage.Performative performative,
                                       String protocol,
                                       Aid receiver,
                                       String content) {
        return addStep(new SendMessageStep(this, performative, protocol, receiver, content));
    }


    public SendMessageStep sendMessage(AclMessage message) {
        return addStep(new SendMessageStep(this, message));
    }


    public SendMessageStep send(MessageBuilder messageBuilder) {
        return addStep(new SendMessageStep(this, messageBuilder.get()));
    }


    public ReceiveMessageStep receiveMessage(MessageTemplate messageTemplate) {
        return addStep(new ReceiveMessageStep(this, messageTemplate));
    }


    public ReceiveMessageStep receiveMessage() {
        return addStep(new ReceiveMessageStep(this));
    }


    /**
     * @deprecated utiliser registerToDF
     */
    @Deprecated
    public Then regsiterToDF(String type) {
        return registerToDF(type);
    }


    /**
     * @deprecated utiliser registerToDF
     */
    @Deprecated
    public Then regsiterToDF(DFService.AgentDescription agentDescription) {
        return registerToDF(agentDescription);
    }


    public Then registerToDF(String type) {
        return registerToDF(new DFService.AgentDescription(
              new DFService.ServiceDescription(type, "test-application")));
    }


    public Then release(final Semaphore semaphore) {
        addStep(new OneShotStep() {
            public void run(Agent agent) throws Exception {
                semaphore.release();
            }
        });
        return new OnlyThen();
    }


    public Then acquire(final Semaphore semaphore) {
        addStep(new OneShotStep() {
            public void run(Agent agent) throws Exception {
                semaphore.acquire();
            }
        });
        return new OnlyThen();
    }


    public Then registerToDF(final DFService.AgentDescription agentDescription) {
        addStep(new OneShotStep() {
            public void run(Agent agent) throws AssertionFailedError {
                try {
                    DFService.register(agent, agentDescription);
                }
                catch (Exception exception) {
                    throw new AssertionFailedError("Impossible de s'enregistrer auprès du DF : " + exception);
                }
            }


            @Override
            public String toString() {
                return "regsiterToDF[" + agentDescription + "]";
            }
        });
        return new OnlyThen();
    }


    <T extends Step> T addStep(T step) {
        triggers.add(step);
        return step;
    }


    public Then play(StoryPart storyPart) {
        storyPart.record(this);
        return new OnlyThen();
    }


    protected void action() {
        if (storyIsStarting) {
            fireStoryStarted();
            storyIsStarting = false;

            if (triggers.isEmpty()) {
                fireStoryFinished();
                return;
            }
        }

        try {
            Step currentStep = triggers.get(0);
            if (currentStep != previousStep) {
                fireStepStarted(currentStep);
            }
            previousStep = currentStep;
            currentStep.run(behaviour.getAgent());
            if (currentStep.done()) {
                triggers.remove(0);
                fireStepFinished(currentStep);
            }
        }
        catch (Throwable error) {
            errorManager.addError(getNickName(), error);
            triggers.clear();
        }
        finally {
            if (triggers.isEmpty()) {
                fireStoryFinished();
            }
        }
    }


    protected String getNickName() {
        return behaviour.getAgent().getAID().getLocalName();
    }


    StoryErrorManager getErrorManager() {
        return errorManager;
    }


    void setErrorManager(StoryErrorManager errorManager) {
        this.errorManager = errorManager;
    }


    void addStoryListener(TesterAgentListener listener) {
        listeners.add(listener);
    }


    Behaviour getBehaviour() {
        return behaviour;
    }


    List<Step> getSteps() {
        return java.util.Collections.unmodifiableList(triggers);
    }


    private void fireStoryStarted() {
        for (TesterAgentListener listener : listeners) {
            listener.storyStarted();
        }
    }


    private void fireStoryFinished() {
        for (TesterAgentListener listener : listeners) {
            listener.storyFinished();
        }
    }


    private void fireStepStarted(Step step) {
        for (TesterAgentListener listener : listeners) {
            listener.stepStarted(step);
        }
    }


    private void fireStepFinished(Step step) {
        for (TesterAgentListener listener : listeners) {
            listener.stepFinished(step);
        }
    }


    public interface Then {
        TesterAgentRecorder then();
    }

    public static class StepWrapper<T extends Step> implements Step, Then {
        private T step;
        private TesterAgentRecorder recorder;


        public StepWrapper(TesterAgentRecorder recorder, T step) {
            this.step = step;
            this.recorder = recorder;
        }


        public void run(Agent agent) throws Exception {
            step.run(agent);
        }


        public boolean done() {
            return step.done();
        }


        public TesterAgentRecorder then() {
            return recorder;
        }


        public T step() {
            return step;
        }
    }

    private class OnlyThen implements Then {
        public TesterAgentRecorder then() {
            return TesterAgentRecorder.this;
        }
    }

    private class MyBehaviour extends Behaviour {

        @Override
        protected void action() {
            TesterAgentRecorder.this.action();
        }


        @Override
        public boolean done() {
            return triggers.isEmpty();
        }
    }
}
