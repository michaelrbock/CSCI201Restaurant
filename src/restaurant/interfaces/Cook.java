package restaurant.interfaces;

public interface Cook {
		
	/** enum for the MarketInventory status known to the cook for ordering purposes */
	public enum MarketInventoryStatus {unknown, stocked, out};

	// *** MESSAGES ***
	//
	/**
	 * Message from a waiter giving the cook a new order.
	 * 
	 * @param waiter
	 *            waiter that the order belongs to
	 * @param tableNum
	 *            identification number for the table
	 * @param choice
	 *            type of food to be cooked
	 */
	public void msgHereIsAnOrder(Waiter waiter, int tableNum, String choice);
	
	/** Message from Market of food delivery */
	public void msgFoodDelivery(Market m, String foodType, int amount);

	// *** EXTRA -- all the simulation routines***
	//
	/** Returns the name of the cook */
	public String getName();
	
	public String toString();
	
	/** establish connection to cashier agent. */
	public void setCashier(Cashier c);

}
