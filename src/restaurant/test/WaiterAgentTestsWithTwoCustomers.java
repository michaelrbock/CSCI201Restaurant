/**
 * 
 */
package restaurant.test;

import org.junit.Test;

import restaurant.WaiterAgent;
import restaurant.test.Mock.*;
import junit.framework.TestCase;

/**
 * This is a second approach to testing an agent. Here, the unit under test is
 * the interaction scenario (a Waiter interacting with its customers, with no on
 * break code). Instead of having our test methods test a single message, it
 * runs an entire scenario and makes assertions as you go along. This strategy
 * is better suited to testing the interactions of multiple agents. For this
 * series of tests, we are testing one waiter with two customers.
 * 
 * @author Sean Turner
 * 
 */
public class WaiterAgentTestsWithTwoCustomers extends TestCase {

	public WaiterAgent waiter;
	public MockCustomer customer1;
	public MockCustomer customer2;
	public MockHost host;
	public MockCook cook;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		waiter = new WaiterAgent("Waiter1", null, null, null);

		customer1 = new MockCustomer("Customer1");
		customer2 = new MockCustomer("Customer2");
		
		host = new MockHost("Host1");
		
		cook = new MockCook("Cook1");
		
		waiter.setCook(cook);
		waiter.setHost(host);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {

	}

	/**
	 * This test should test the normal scenario for two customers. It makes
	 * sure that each customer is only messaged once for each action, and that
	 * only one action is taken per scheduler execution. Note that some assert
	 * conditions (such as taking action in message reception) are not checked
	 * in these tests because the WaiterAgentTestsWithOneCustomer takes care of
	 * these conditions. <br>
	 * Also note that this method isn't finished yet. You should complete the
	 * rest of the scenario.
	 */
	@Test
	public void testNormalScenarioForTwoCustomers() {
		waiter.msgSitCustomerAtTable(customer1, 1);
		waiter.msgSitCustomerAtTable(customer2, 2);

		waiter.pickAndExecuteAnAction();

		assertTrue("With 1 scheduler call, only 1 customer should be seated."
				+ getLogs(), customer1.log.containsString("msgFollowMeToTable")
				^ customer2.log.containsString("msgFollowMeToTable"));

		waiter.pickAndExecuteAnAction();

		assertTrue("Both customers should be seated now." + getLogs(),
				customer1.log.containsString("msgFollowMeToTable")
						&& customer2.log.containsString("msgFollowMeToTable"));

		assertTrue("Both customers should have only received 1 message."
				+ getLogs(), customer1.log.size() == 1
				&& customer2.log.size() == 1);

		assertFalse(
				"Calling scheduler again should have no action and return false.",
				waiter.pickAndExecuteAnAction());

		assertTrue("Both customers should have only received 1 message."
				+ getLogs(), customer1.log.size() == 1
				&& customer2.log.size() == 1);
		
		waiter.msgImReadyToOrder(customer1);
		
		waiter.pickAndExecuteAnAction();
		
		assertTrue("Customer 1 should have been asked for order." + getLogs(), customer1.log.containsString("msgWhatWouldYouLike"));
		
		assertTrue("Customer 2 should not have been asked for order." + getLogs(), !customer2.log.containsString("msgWhatWouldYouLike"));
		
		waiter.msgHereIsMyChoice(customer1, "Salad");
		
		waiter.msgImReadyToOrder(customer2);
		
		waiter.pickAndExecuteAnAction();
		waiter.pickAndExecuteAnAction();
		
		assertTrue("Customer 2 should have been asked for order." + getLogs(), customer2.log.containsString("msgWhatWouldYouLike"));
		
		assertTrue("Cook should have received order." + getLogs(), cook.log.containsString("msgHereIsAnOrder"));
		assertTrue("Order should be for table 1 " + getLogs(), cook.log.containsString("table number 1"));
		assertTrue("Order should be for salad." + getLogs(), cook.log.containsString("Salad"));
		assertTrue("Cook should only have received 1 order." + getLogs(), cook.log.size()==1);
		
		fail("Rest of test not implemented. Please finish.");
	}

	/**
	 * This is a helper function to print out the logs from the two MockCustomer
	 * objects. This should help to assist in debugging.
	 * 
	 * @return a string containing the logs from the two mock customers
	 */
	public String getLogs() {
		StringBuilder sb = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		sb.append("-------Customer 1 Log-------");
		sb.append(newLine);
		sb.append(customer1.log.toString());
		sb.append(newLine);
		sb.append("-------End Customer 2 Log-------");

		sb.append(newLine);

		sb.append("-------Customer 2 Log-------");
		sb.append(newLine);
		sb.append(customer2.log.toString());
		sb.append("-------End Customer 2 Log-------");
		
		sb.append(newLine);
		
		sb.append("------Cook Log------");
		sb.append(newLine);
		sb.append(cook.log.toString());
		sb.append("-------End Cook Log-------");
		
		sb.append(newLine);
		
		sb.append("-------Host Log------");
		sb.append(newLine);
		sb.append(host.log.toString());
		sb.append("-------End Host Log-------");

		return sb.toString();

	}

}
