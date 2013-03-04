package restaurant.test.mock;

import restaurant.Bill;
import restaurant.HostAgent;
import restaurant.Menu;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Waiter;
import restaurant.layoutGUI.GuiCustomer;

public class MockCustomer extends MockAgent implements Customer {

	//agent connections
	Waiter waiter;
	
	//data
	public EventLog log = new EventLog();
	public Bill bill;
	
	public MockCustomer(String name) {
		super(name);
	}
	
	public void setWaiter(Waiter w) {
		waiter = w;
	}
	
	//get bill from waiter
	@Override
	public void msgHereIsBill(Bill b) { 
		log.add(new LoggedEvent("received bill"));
		bill = b;
	}
	
	//get receipt from cashier
	@Override
	public void msgThanks(double change) {
		log.add(new LoggedEvent("received reciept from cashier with change of $"+Double.toString(change)));
	}
	
	//get work assigned from cashier

	@Override
	public void msgNotEnoughMoneyMustWorkFor(double hours) {
		log.add(new LoggedEvent("received notice to work from cashier for "+Double.toString(hours)));
	}
	
	@Override
	public void setHungry() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgFollowMeToTable(Waiter waiter, Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDecided() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgWhatWouldYouLike() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgHereIsYourFood(String choice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgDoneEating() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgThereIsWait() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHost(HostAgent host) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCashier(Cashier c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isHungry() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getHungerLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setHungerLevel(int hungerLevel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GuiCustomer getGuiCustomer() {
		// TODO Auto-generated method stub
		return null;
	}

}
