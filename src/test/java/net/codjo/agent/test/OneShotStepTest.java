package net.codjo.agent.test;
import net.codjo.agent.Agent;
import org.junit.Test;

import static net.codjo.test.common.matcher.JUnitMatchers.*;
/**
 *
 */
public class OneShotStepTest {
    @Test
    public void test_overridedToString() throws Exception {
        OneShotStep step = new OneShotStep("my desc") {
            public void run(Agent agent) throws Exception {
            }
        };
        assertThat(step.toString(), is("my desc"));
    }


    @Test
    public void test_defaultToString() throws Exception {
        OneShotStep step = new OneShotStep() {
            public void run(Agent agent) throws Exception {
            }
        };
        assertThat(step.toString(), containsString(OneShotStep.class.getName()));
    }
}
