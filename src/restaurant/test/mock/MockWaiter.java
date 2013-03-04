package restaurant.test.mock;

import restaurant.Bill;
import restaurant.HostAgent;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Waiter;
import restaurant.layoutGUI.Food;

public class MockWaiter extends MockAgent implements Waiter {

	//data
	Cashier cashier;
	Customer customer;
	public Bill bill; //bill for customer
	public EventLog log = new EventLog();
	
	public MockWaiter(String name) {
		super(name);
	}
	
	//connection methods
	public void setCashier(Cashier c) {
		cashier = c;
	}
	public void setCustomer(Customer c) {
		customer = c;
	}
	
	@Override
	public void msgHereIsBill(Bill b) {
		bill = b;
		log.add(new LoggedEvent("received bill from waiter for customer"));
	}

	@Override
	public void msgSitCustomerAtTable(Customer customer, int tableNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgImReadyToOrder(Customer customer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsMyChoice(Customer customer, String choice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgOrderIsReady(int tableNum, Food f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDoneEating(Customer customer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDoneEatingAndLeaving(Customer customer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgOutOfChoice(String choice, int tableNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgNotYet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgTakeBreak() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBreakStatus(boolean state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCook(Cook cook) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHost(HostAgent host) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isOnBreak() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
