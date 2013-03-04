/**
 * 
 */
package restaurant.test.mock;

import restaurant.interfaces.Cashier;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Market;
import restaurant.interfaces.Waiter;

/**
 * @author Sean Turner
 * 
 */
public class MockCook extends MockAgent implements Cook {
	
	//Hold real or mock agents to mock up implementation
	Cashier cashier;
	Market market;

	public MockCook(String name) {
		super(name);
	}

	public EventLog log = new EventLog();

	public void msgHereIsAnOrder(Waiter waiter, int tableNum, String choice) {
		log.add(new LoggedEvent(
				"Received message msgHereIsAnOrder from waiter "
						+ waiter.toString() + " for table number " + tableNum
						+ " to cook item " + choice + "."));

	}
	
	public void order(String type, int amount) {
		market.msgOrderFood(type, amount, cashier, this);
	}

	@Override
	public void msgFoodDelivery(Market m, String foodType, int amount) {
		// must accept this message but mock need not do anything
	}

	@Override
	public void setCashier(Cashier c) {
		this.cashier = c;
	}
	
	public void setMarket(Market m) {
		this.market = m;
	}

}
