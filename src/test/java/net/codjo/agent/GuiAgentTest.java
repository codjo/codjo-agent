package net.codjo.agent;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.test.common.LogString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GuiAgentTest {
    private AgentContainerFixture containerFixture = new AgentContainerFixture();
    private GuiAgent guiAgent;
    LogString log = new LogString();


    @Before
    public void setUp() {
        guiAgent = new GuiAgent() {
            @Override
            protected void onGuiEvent(GuiEvent event) {
                List<Object> parameters = new ArrayList<Object>();
                parameters.add(event.getSource());
                parameters.add(event.getType());
                for (Iterator it = event.getAllParameter(); it.hasNext();) {
                    parameters.add(it.next());
                }
                log.call("onGuiEvent", parameters.toArray());
            }
        };
        containerFixture.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        containerFixture.doTearDown();
    }


    @Test
    public void test_postGuiEvent() throws Exception {
        containerFixture.startNewAgent("Gui", guiAgent);

        GuiEvent guiEvent = new GuiEvent("Source", 7);
        guiEvent.addParameter("Param1");
        guiEvent.addParameter("Param2");
        guiAgent.postGuiEvent(guiEvent);

        assertUntil(new Assertion() {
            public void check() throws AssertionError {
                log.assertContent("onGuiEvent(Source, 7, Param1, Param2)");
            }
        });
    }


    private void assertUntil(Assertion assertion) {
        AssertionError exception;
        long begin = System.currentTimeMillis();
        do {
            try {
                assertion.check();
                return;
            }
            catch (AssertionError e) {
                exception = e;
                try {
                    Thread.sleep(50);
                }
                catch (InterruptedException e1) {
                    ;
                }
            }
        }
        while (System.currentTimeMillis() - begin < 1000);

        throw exception;
    }


    interface Assertion {
        void check() throws AssertionError;
    }
}
