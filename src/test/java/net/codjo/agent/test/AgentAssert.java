package net.codjo.agent.test;
import java.util.regex.Pattern;
import junit.framework.Assert;
import net.codjo.agent.Behaviour;
import net.codjo.test.common.LogString;
import org.apache.commons.lang.StringUtils;

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


            @Override
            public String toString() {
                return "behaviourDone(" + behaviour + ')';
            }
        };
    }


    public static Assertion behaviourNotDone(final Behaviour behaviour) {
        return new Assertion() {
            public void check() {
                Assert.assertFalse(behaviour.done());
            }


            @Override
            public String toString() {
                return "behaviourNotDone(" + behaviour + ')';
            }
        };
    }


    public static Assertion log(final LogString logString, final String expected) {
        return new Assertion() {
            public void check() {
                logString.assertContent(expected);
            }


            @Override
            public String toString() {
                return "log(" + expected + ')';
            }
        };
    }


    public static Assertion log(final LogString logString, final Pattern pattern) {
        return new Assertion() {
            public void check() {
                logString.assertContent(pattern);
            }


            @Override
            public String toString() {
                return "log(" + pattern + ')';
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


            @Override
            public String toString() {
                return "logContains({" + StringUtils.join(expectedParts, ',') + "})";
            }
        };
    }


    public static Assertion logAndClear(final LogString logString, final String expected) {
        return new Assertion() {
            public void check() {
                logString.assertAndClear(expected);
            }


            @Override
            public String toString() {
                return "logAndClear(" + expected + ')';
            }
        };
    }


    public static Assertion logAndClear(final LogString logString, final Pattern pattern) {
        return new Assertion() {
            public void check() {
                logString.assertAndClear(pattern);
            }


            @Override
            public String toString() {
                return "logAndClear(" + pattern + ')';
            }
        };
    }
}
