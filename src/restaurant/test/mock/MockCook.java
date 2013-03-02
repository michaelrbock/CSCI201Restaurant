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

	@Override
	public void msgFoodDelivery(Market m, String foodType, int amount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCashier(Cashier c) {
		// TODO Auto-generated method stub
		
	}

}
