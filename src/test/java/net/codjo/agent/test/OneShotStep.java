package net.codjo.agent.test;
/**
 *
 */
public abstract class OneShotStep implements Step {
    private String description;


    protected OneShotStep() {
    }


    protected OneShotStep(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        if (description == null) {
            return super.toString();
        }
        return description;
    }


    public boolean done() {
        return true;
    }
}
