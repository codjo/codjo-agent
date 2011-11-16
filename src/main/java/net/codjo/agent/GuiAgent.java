package net.codjo.agent;
import java.util.Iterator;

public abstract class GuiAgent extends Agent {

    protected GuiAgent() {
    }


    protected GuiAgent(Behaviour behaviour) {
        super(behaviour);
    }


    public void postGuiEvent(GuiEvent event) {
        jade.gui.GuiEvent guiEvent = new jade.gui.GuiEvent(event.getSource(), event.getType());
        for (Iterator it = event.getAllParameter(); it.hasNext();) {
            guiEvent.addParameter(it.next());
        }
        getJadeGuiAgent().postGuiEvent(guiEvent);
    }


    protected abstract void onGuiEvent(GuiEvent event);


    jade.gui.GuiAgent getJadeGuiAgent() {
        return (jade.gui.GuiAgent)getJadeAgent();
    }


    @Override
    jade.core.Agent createJadeAgentAdapter() {
        return new JadeGuiAgentAdapter();
    }


    private class JadeGuiAgentAdapter extends jade.gui.GuiAgent {

        @Override
        protected void setup() {
            GuiAgent.this.setup();
        }


        @Override
        protected void takeDown() {
            GuiAgent.this.tearDown();
        }


        @Override
        protected void onGuiEvent(jade.gui.GuiEvent event) {
            GuiAgent.this.onGuiEvent(new GuiEvent(event));
        }
    }
}
