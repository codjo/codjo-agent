package net.codjo.agent.util;
/**
 *
 */
public class IdUtil {
    private IdUtil() {
    }


    public static String createUniqueId(Object object) {
        return new StringBuilder()
              .append(Integer.toString(System.identityHashCode(object), 36))
              .append(Integer.toString((int)(Math.random() * 1000)))
              .append(Long.toString(System.currentTimeMillis(), 36)).toString();
    }
}
