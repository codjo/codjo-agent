package net.codjo.agent;
import java.util.Iterator;

public class GuiEvent {
    private jade.gui.GuiEvent jadeGuiEvent;


    GuiEvent(jade.gui.GuiEvent event) {
        jadeGuiEvent = event;
    }


    public GuiEvent(Object eventSource, int eventType) {
        jadeGuiEvent = new jade.gui.GuiEvent(eventSource, eventType);
    }


    public int getType() {
        return jadeGuiEvent.getType();
    }


    public Object getSource() {
        return jadeGuiEvent.getSource();
    }


    public void addParameter(Object param) {
        jadeGuiEvent.addParameter(param);
    }


    public Object getParameter(int number) {
        return jadeGuiEvent.getParameter(number);
    }


    public Iterator getAllParameter() {
        return jadeGuiEvent.getAllParameter();
    }
}
