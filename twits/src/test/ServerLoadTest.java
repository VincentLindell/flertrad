package test;

import msg.client.Twit;
import msg.client.test.MessagingLog;
import msg.client.test.ServerControl;
import util.test.BeforeEach;
import util.test.Test;
import util.test.TestSuite;

public class ServerLoadTest extends TestSuite {

    @BeforeEach
    void setUp(String testName) throws Exception {
        ServerControl.restartServer(testName);
    }

    @Test
    void heavyLoadTest() throws InterruptedException {
        final int NBR_TWITS = 50;        // number of clients
        final int NBR_MESSAGES = 10000;  // number of messages per Twit
        final int MESSAGE_DELAY = 0;     // no delay between messages

        for (int i = 0; i < NBR_TWITS; i++) {
            new Twit(NBR_MESSAGES, MESSAGE_DELAY).start();
        }

        // Expect each Twit to produce all its messages
        MessagingLog.expect(NBR_TWITS, NBR_TWITS * NBR_MESSAGES);
    }

    public static void main(String[] args) {
        new ServerLoadTest().run();
    }
}
