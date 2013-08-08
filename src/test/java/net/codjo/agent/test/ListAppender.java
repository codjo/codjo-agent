package net.codjo.agent.test;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class ListAppender extends AppenderSkeleton {
    private final List<String> logs = new ArrayList<String>();


    public static ListAppender createAndAddToRootLogger() {
        ListAppender result = new ListAppender();
        Logger.getRootLogger().addAppender(result);
        return result;
    }


    @Override
    protected void append(LoggingEvent event) {
        String level = event.getLevel().toString();
        logs.add(level + ": " + event.getRenderedMessage());
    }


    @Override
    public boolean requiresLayout() {
        return false;
    }


    @Override
    public void close() {
    }


    /**
     * This is a workaround since Pattern.compile. TODO upgrade java 5 ?
     */
    public boolean matchesOneLine(String regex) {
        boolean result = false;
        for (String line : logs) {
            if (line.matches(regex)) {
                result = true;
                break;
            }
        }
        return result;
    }


    public void removeFromRootLogger() {
        Logger.getRootLogger().removeAppender(this);
    }


    @Override
    public String toString() {
        return StringUtils.join(logs, '\n');
    }


    public void printTo(PrintStream out) {
        out.println("--- Actual content of logs ---\n" + logs + "\n--- end of LogString content ---");
    }
}