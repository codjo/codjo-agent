package net.codjo.agent;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GuiEventTest {
    private GuiEvent guiEvent = new GuiEvent("Source", 7);


    @Test
    public void test_constructor() throws Exception {
        assertEquals("Source", guiEvent.getSource());
        assertEquals(7, guiEvent.getType());
    }


    @Test
    public void test_getParameter() throws Exception {
        guiEvent.addParameter("Param1");
        guiEvent.addParameter("Param2");

        assertEquals("Param1", guiEvent.getParameter(0));
        assertEquals("Param2", guiEvent.getParameter(1));
    }
}
