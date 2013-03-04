package restaurant.test;

import static org.junit.Assert.*;
import junit.framework.*;
import org.junit.Test;
import agent.*;
import restaurant.*;
import restaurant.interfaces.*;
import restaurant.test.mock.*;

public class FullCashierTest extends TestCase {
	
	@Test
	public void testMarketOrderBilling() {
		// created needed agents and mocks
		CashierAgent cashier = new CashierAgent();
		MockMarket market = new MockMarket("market");
		//set up cook
		MockCook cook = new MockCook("cook");
		cook.setCashier(cashier);
		cook.setMarket(market);
		//start thread
		cashier.startThread();
		
		//have cook order some inventory
		cook.order("Steak", 4);
		//market receives order and sends bill to cashier
		
		//wait for cashier to act on its own
		try { Thread.sleep(1000); } catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
		
		//cashier should now send payment to market
		assertTrue("Market should have received payment", market.log.containsString("received payment of $20.0 from cashier for Steak"));
	}
	
	@Test
	public void testCustomerBillingEnoughMoney() {
		// created needed agents and mocks
		CashierAgent cashier = new CashierAgent();
		MockWaiter waiter = new MockWaiter("waiter");
		MockCustomer customer = new MockCustomer("customer");
		
		//connect mocks and agents
		waiter.setCashier(cashier);
		waiter.setCustomer(customer);
		customer.setCashier(cashier);
		customer.setWaiter(waiter);
		
		//start cashier thread
		cashier.startThread();
		
		// have waiter ask cashier to send bill
		cashier.msgNeedBillForCustomer(waiter, customer, "Pizza");
		
		//wait for cashier to act on its own
		try { Thread.sleep(1000); } catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
		
		//check that the waiter received the bill from the cashier
		assertTrue("Check that waiter received bill for customer from cashier", 
				waiter.log.containsString("received bill from waiter for customer"));
		
		//send the bill to the customer from waiter
		customer.msgHereIsBill(waiter.bill);
		
		//check that customer received correct bill
		assertTrue("check that customer received corret bill", 
				customer.bill.equals(waiter.bill));
		
		// have customer send payment - with enough money
		cashier.msgPayment(customer, customer.bill, 30.0);
		
		//wait for cashier to act on its own
		try { Thread.sleep(1000); } catch(InterruptedException ex) { Thread.currentThread().interrupt(); }
		
		//check that the customer received his receipt and correct change
		assertTrue("check that the customer received his receipt and correct change",
				customer.log.containsString("received reciept from cashier with change of $"));
	}
	
	@Test
	public void testCustomerBillingNotEnoughMoney() {
		// created needed agents and mocks
		CashierAgent cashier = new CashierAgent();
		MockWaiter waiter = new MockWaiter("waiter");
		MockCustomer customer = new MockCustomer("customer");

		//connect mocks and agents
		waiter.setCashier(cashier);
		waiter.setCustomer(customer);
		customer.setCashier(cashier);
		customer.setWaiter(waiter);

		//start cashier thread
		cashier.startThread();

		// have waiter ask cashier to send bill
		cashier.msgNeedBillForCustomer(waiter, customer, "Pizza");

		//wait for cashier to act on its own
		try { Thread.sleep(1000); } catch(InterruptedException ex) { Thread.currentThread().interrupt(); }

		//check that the waiter received the bill from the cashier
		assertTrue("Check that waiter received bill for customer from cashier", 
				waiter.log.containsString("received bill from waiter for customer"));

		//send the bill to the customer from waiter
		customer.msgHereIsBill(waiter.bill);

		//check that customer received correct bill
		assertTrue("check that customer received corret bill", 
				customer.bill.equals(waiter.bill));

		// have customer send payment - without enough money
		cashier.msgPayment(customer, customer.bill, 5.00);

		//wait for cashier to act on its own
		try { Thread.sleep(1000); } catch(InterruptedException ex) { Thread.currentThread().interrupt(); }

		//check that customer received work notice
		assertTrue("check that customer received work notice"
				,customer.log.containsString("received notice to work from cashier for"));
		
		//customer now "works"
		
		//customer sends will work msg to cashier
		cashier.msgWillWorkFor(customer, customer.bill, 0.49875);
		//customer then works and leaves, cashier trusts customer
	}

}
