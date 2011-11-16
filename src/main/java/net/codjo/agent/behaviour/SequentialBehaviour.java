package net.codjo.agent.behaviour;
import net.codjo.agent.Agent;
import net.codjo.agent.Behaviour;
import net.codjo.agent.JadeWrapper;
import java.util.ArrayList;
import java.util.List;
/**
 * <p> Un SequentialBehaviour représente une liste de comportements d'un agent. </p>
 *
 * @see net.codjo.agent.Behaviour
 */
public class SequentialBehaviour extends Behaviour {
    private jade.core.behaviours.SequentialBehaviour sequentialBehaviour;
    private List<Behaviour> subBehaviours = new ArrayList<Behaviour>();


    public SequentialBehaviour() {
        sequentialBehaviour = new jade.core.behaviours.SequentialBehaviour();
        JadeWrapper.wrapp(this, sequentialBehaviour);
    }


    @Override
    protected void action() {
        sequentialBehaviour.action();
    }


    @Override
    public boolean done() {
        return sequentialBehaviour.done();
    }


    public void addSubBehaviour(Behaviour subBehaviour) {
        subBehaviours.add(subBehaviour);
        subBehaviour.setAgent(getAgent());
        sequentialBehaviour.addSubBehaviour(JadeWrapper.unwrapp(subBehaviour));
    }


    @Override
    public void setAgent(Agent agent) {
        super.setAgent(agent);
        for (Behaviour subBehaviour : subBehaviours) {
            subBehaviour.setAgent(agent);
        }
    }


    public void skipNext() {
        sequentialBehaviour.skipNext();
    }


    public static SequentialBehaviour wichStartsWith(Behaviour behaviour) {
        SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
        return sequentialBehaviour.andThen(behaviour);
    }


    public SequentialBehaviour andThen(Behaviour behaviour) {
        addSubBehaviour(behaviour);
        return this;
    }
}
