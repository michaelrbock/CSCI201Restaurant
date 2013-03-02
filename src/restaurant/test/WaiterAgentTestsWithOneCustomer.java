/**
 * 
 */
package restaurant.test;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import restaurant.CookAgent;
import restaurant.WaiterAgent;
import restaurant.interfaces.*;
import restaurant.layoutGUI.Food;
import restaurant.test.mock.*;

/**
 * This is a set of tests for the normal Waiter interaction cycle. This suite
 * tests all of the waiter messages, with a single customer. Note, this does not
 * test multiple customers. These are contained in the
 * WaiterAgentTestsWithTwoCustomers. <br>
 * <br>
 * Note that the unit of test in this file is the individual message. We call
 * the appropriate functions to get the WaiterAgent ready to accept and process
 * a particular message and then make assert statements surrounding that
 * message. This breaks testing this one interaction up into 5 different test
 * methods. You could also integrate all of this into one large test method. We
 * do this when testing a WaiterAgent with multiple customers. Both styles are
 * valid and which one is best depends on the goal of your particular tests. <br>
 * This set of tests is invaluable when first writing your WaiterAgent. It will
 * validate that your waiter implements the messaging contract described in the
 * interaction diagram. In building an agent system, I would actually write
 * these unit tests before writing the WaiterAgent itself. This practice is
 * called Test Driven Development (TDD).
 * 
 * @author Sean Turner
 * 
 */
public class WaiterAgentTestsWithOneCustomer extends TestCase {

	/**
	 * This is the WaiterAgent to be tested.
	 */
	public WaiterAgent waiter;

	/**
	 * Test method for
	 * {@link restaurant.WaiterAgent#msgSitCustomerAtTable(restaurant.CustomerAgent, int)}
	 * .
	 * 
	 * This method creates a WaiterAgent and a MockCustomer. The waiter is
	 * messaged that the customer needs to be seated. The waiter's scheduler is
	 * then called. The customer should receive msgFollowMeToTable after the
	 * scheduler is called.
	 */
	@Test
	public void testMsgSitCustomerAtTable() {

		// Create a WaiterAgent
		WaiterAgent waiter = new WaiterAgent("Waiter1", null, null, null);

		// Create a MockCustomer
		MockCustomer customer = new MockCustomer("Customer1");

		// Message the waiter to sit the customer at table 3
		waiter.msgSitCustomerAtTable(customer, 3);

		// This will check that you're not messaging the customer in the
		// waiter's message reception.
		assertEquals(
				"Mock Customer should have an empty event log before the Waiter's scheduler is called. Instead, the mock customer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());

		// Call the waiter's scheduler
		waiter.pickAndExecuteAnAction();

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock customer should have received message to go to table. Event log: "
						+ customer.log.toString(), customer.log
						.containsString("Received message msgFollowMeToTable"));
		assertEquals(
				"Only 1 message should have been sent to the customer. Event log: "
						+ customer.log.toString(), 1, customer.log.size());
		assertTrue("Message should be sent from " + waiter.toString(),
				customer.log.getLastLoggedEvent().getMessage().contains(
						waiter.toString()));
	}

	/**
	 * Test method for
	 * {@link restaurant.WaiterAgent#msgImReadyToOrder(restaurant.CustomerAgent)}
	 * . Tests that once a customer messages he is ready to order and the
	 * waiter's scheduler is called, the waiter messages the customer to ask
	 * msgWhatWouldYouLike.
	 */
	@Test
	public void testMsgImReadyToOrder() {
		WaiterAgent waiter = new WaiterAgent("Waiter1", null, null, null);

		MockCustomer customer = new MockCustomer("Customer1");

		waiter.msgSitCustomerAtTable(customer, 3);

		assertEquals(
				"Mock Customer should have an empty event log before the Waiter's scheduler is called. Instead, the mock customer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());

		waiter.pickAndExecuteAnAction();

		waiter.msgImReadyToOrder(customer);

		assertFalse(
				"Mock customer should not have received msgWhatIsYourOrder before scheduler call.",
				customer.log.containsString("msgWhatWouldYouLike"));

		waiter.pickAndExecuteAnAction();

		assertTrue("Mock customer should have received msgWhatIsYourOrder.",
				customer.log.containsString("msgWhatWouldYouLike"));
	}

	/**
	 * Test method for
	 * {@link restaurant.WaiterAgent#msgHereIsMyChoice(restaurant.CustomerAgent, java.lang.String)}
	 * .
	 */
	@Test
	public void testMsgHereIsMyChoice() {
		WaiterAgent waiter = new WaiterAgent("Waiter1", null, null, null);
		MockCook cook = new MockCook("Cook1");
		waiter.setCook(cook);

		MockCustomer customer = new MockCustomer("Customer1");

		waiter.msgSitCustomerAtTable(customer, 3);

		waiter.pickAndExecuteAnAction();

		waiter.msgImReadyToOrder(customer);

		waiter.pickAndExecuteAnAction();

		waiter.msgHereIsMyChoice(customer, "Salad");

		assertEquals(
				"Cook's log should be empty before waiter scheduler called. Instead, it is "
						+ cook.log.toString(), 0, cook.log.size());

		waiter.pickAndExecuteAnAction();

		assertTrue("Cook should have received message to cook order.", cook.log
				.containsString("msgHereIsAnOrder"));

		assertTrue("Order should be for table 3", cook.log.getLastLoggedEvent()
				.getMessage().contains("3"));

		assertTrue("Order should be for salad.", cook.log.getLastLoggedEvent()
				.getMessage().contains("Salad"));
	}

	/**
	 * Test method for
	 * {@link restaurant.WaiterAgent#msgOrderIsReady(int, restaurant.layoutGUI.Food)}
	 * .
	 */
	@Test
	public void testMsgOrderIsReady() {
		WaiterAgent waiter = new WaiterAgent("Waiter1", null, null, null);
		MockCook cook = new MockCook("Cook1");
		waiter.setCook(cook);

		MockCustomer customer = new MockCustomer("Customer1");

		waiter.msgSitCustomerAtTable(customer, 3);

		waiter.pickAndExecuteAnAction();

		waiter.msgImReadyToOrder(customer);

		waiter.pickAndExecuteAnAction();

		waiter.msgHereIsMyChoice(customer, "Salad");

		waiter.pickAndExecuteAnAction();

		waiter.msgOrderIsReady(3, new Food("Salad", Color.BLUE, null));

		assertFalse(customer.log.containsString("msgHereIsYourFood"));

		waiter.pickAndExecuteAnAction();

		assertTrue("Waiter should deliver food to customer.", customer.log
				.containsString("msgHereIsYourFood"));

	}

	/**
	 * Test method for
	 * {@link restaurant.WaiterAgent#msgDoneEatingAndLeaving(restaurant.CustomerAgent)}
	 * .
	 */
	@Test
	public void testMsgDoneEatingAndLeaving() {
		WaiterAgent waiter = new WaiterAgent("Waiter1", null, null, null);
		MockCook cook = new MockCook("Cook1");
		MockHost host = new MockHost("Host");
		waiter.setCook(cook);
		waiter.setHost(host);

		MockCustomer customer = new MockCustomer("Customer1");

		waiter.msgSitCustomerAtTable(customer, 3);

		waiter.pickAndExecuteAnAction();

		waiter.msgImReadyToOrder(customer);

		waiter.pickAndExecuteAnAction();

		waiter.msgHereIsMyChoice(customer, "Salad");

		waiter.pickAndExecuteAnAction();

		waiter.msgOrderIsReady(3, new Food("Salad", Color.BLUE, null));

		waiter.pickAndExecuteAnAction();

		waiter.msgDoneEatingAndLeaving(customer);

		assertEquals(
				"Host log should be empty before scheduler is called. Instead, log reads "
						+ host.log.toString(), 0, host.log.size());

		waiter.pickAndExecuteAnAction();

		// It takes a while for the waiter to actually clear the table. Because
		// this action happens on a separate thread, we need to wait for this
		// thread to complete its action before we return. However, it is
		// possible that there is an error in the waiter code that would never
		// actually clear the table. We want to write a test that will wait a
		// short time for the table to be cleared, and fail if the test doesn't
		// complete in that period of time.
		int timer = 0;
		int timeout = 1000 * 7;
		while (timer < timeout && !host.log.containsString("table 3 is free")) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			timer += 50;
		}

		assertTrue("Host should have been messaged that table is free.",
				host.log.containsString("table 3 is free"));

	}

}
