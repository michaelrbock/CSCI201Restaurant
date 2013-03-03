package restaurant;

import restaurant.CookAgent.OrderStatus;
import restaurant.interfaces.Waiter;
import restaurant.layoutGUI.Food;

/**
 * class to store order information. Contains the waiter, table
 * number, food item, cooktime and status.
 */
public class Order {
	public Waiter waiter;
	public int tableNum;
	public String choice;
	public OrderStatus status;
	public Food food; // a gui variable

	/**
	 * Constructor for Order class
	 * 
	 * @param waiter
	 *            waiter that this order belongs to
	 * @param tableNum
	 *            identification number for the table
	 * @param choice
	 *            type of food to be cooked
	 */
	public Order(Waiter waiter, int tableNum, String choice) {
		this.waiter = waiter;
		this.choice = choice;
		this.tableNum = tableNum;
		this.status = OrderStatus.pending;
	}
	
	//Copy constructor
	public Order(Order o) {
		this.waiter = o.waiter;
		this.choice = o.choice;
		this.tableNum = o.tableNum;
		this.status = OrderStatus.pending;
	}

	/** Represents the object as a string */
	public String toString() {
		return choice + " for " + waiter;
	}
}