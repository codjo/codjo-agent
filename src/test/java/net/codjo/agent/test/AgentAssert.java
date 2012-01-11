package net.codjo.agent.test;
import java.util.regex.Pattern;
import junit.framework.Assert;
import net.codjo.agent.Behaviour;
import net.codjo.test.common.LogString;

import static net.codjo.test.common.matcher.JUnitMatchers.*;

public class AgentAssert {
    private AgentAssert() {
    }


    public interface Assertion {

        public void check() throws Throwable;
    }


    public static Assertion behaviourDone(final Behaviour behaviour) {
        return new Assertion() {
            public void check() {
                Assert.assertTrue(behaviour.done());
            }
        };
    }


    public static Assertion behaviourNotDone(final Behaviour behaviour) {
        return new Assertion() {
            public void check() {
                Assert.assertFalse(behaviour.done());
            }
        };
    }


    public static Assertion log(final LogString logString, final String expected) {
        return new Assertion() {
            public void check() {
                logString.assertContent(expected);
            }
        };
    }


    public static Assertion log(final LogString logString, final Pattern pattern) {
        return new Assertion() {
            public void check() {
                logString.assertContent(pattern);
            }
        };
    }


    static Assertion logContains(final LogString log, final String... expectedParts) {
        return new Assertion() {
            public void check() {
                for (String expectedPart : expectedParts) {
                    assertThat(log.getContent(), containsString(expectedPart));
                }
            }
        };
    }


    public static Assertion logAndClear(final LogString logString, final String expected) {
        return new Assertion() {
            public void check() {
                logString.assertAndClear(expected);
            }
        };
    }


    public static Assertion logAndClear(final LogString logString, final Pattern pattern) {
        return new Assertion() {
            public void check() {
                logString.assertAndClear(pattern);
            }
        };
    }
}
