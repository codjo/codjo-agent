package net.codjo.agent;
import static net.codjo.agent.AMSService.AMS;
import net.codjo.agent.AMSService.AMSServiceException;
import static net.codjo.agent.AclMessage.Performative.INFORM;
import static net.codjo.agent.test.MessageBuilder.message;
import net.codjo.agent.test.Story;
import net.codjo.agent.test.SubStep;
import static net.codjo.test.common.matcher.JUnitMatchers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/**
 *
 */
public class AMSServiceTest {
    private Story story = new Story();


    @Test
    public void test_getFailedReceiver() throws Exception {
        story.record().startTester("tester")
              .send(message(INFORM).to("unknown-agent"))
              .then()
              .receiveMessage()
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage message) throws Exception {
                      assertThat(AMSService.getFailedReceiver(agent, message), is(new Aid("unknown-agent")));
                  }
              });

        story.execute();
    }


    @Test
    public void test_getFailedReceiver_error() throws Exception {
        story.record().startTester("tester")
              .receiveMessage()
              .add(new SubStep() {
                  public void run(Agent agent, AclMessage message) throws Exception {
                      try {
                          AMSService.getFailedReceiver(agent, message);
                          fail();
                      }
                      catch (AMSServiceException e) {
                          assertThat(e.getMessage(), is("Invalid AMS FAILURE message"));
                      }
                  }
              });

        story.record().startTester("so-called ams")
              .send(message(INFORM).from(AMS).to("tester"));

        story.execute();
    }


    @Before
    public void setUp() throws Exception {
        story.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        story.doTearDown();
    }
}
