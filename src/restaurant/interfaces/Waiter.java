package restaurant.interfaces;

import restaurant.Bill;
import restaurant.HostAgent;

import restaurant.layoutGUI.Food;


public interface Waiter {
	// State variables for Waiter
	public enum BreakState {
		none, wantBreak, askedForBreak, mustWaitForBreak, canTake, onBreak
	};

	public enum CustomerState {
		NEED_SEATED, READY_TO_ORDER, ORDER_PENDING, ORDER_READY,
		IS_DONE_EATING, HAS_BILL, IS_COMPLETELY_DONE, NO_ACTION
	};

	// *** MESSAGES ***
	//
	/**
	 * Host sends this to give the waiter a new customer.
	 * 
	 * @param customer
	 *            customer who needs seated.
	 * @param tableNum
	 *            identification number for table
	 */
	public void msgSitCustomerAtTable(Customer customer, int tableNum);
	
	/**
	 * Customer sends this when they are ready.
	 * 
	 * @param customer
	 *            customer who is ready to order.
	 */
	public void msgImReadyToOrder(Customer customer);

	/**
	 * Customer sends this when they have decided what they want to eat
	 * 
	 * @param customer
	 *            customer who has decided their choice
	 * @param choice
	 *            the food item that the customer chose
	 */
	public void msgHereIsMyChoice(Customer customer, String choice);

	/**
	 * Cook sends this when the order is ready.
	 * 
	 * @param tableNum
	 *            identification number of table whose food is ready
	 * @param f
	 *            is the guiFood object
	 */
	public void msgOrderIsReady(int tableNum, Food f);

	/**
	 * Customer sends this when they are done eating.
	 * 
	 * @param customer
	 *            customer who is leaving the restaurant.
	 */
	public void msgDoneEating(Customer customer);

	public void msgDoneEatingAndLeaving(Customer customer);

	/** Message from Cook that customer's choice is out */
	public void msgOutOfChoice(String choice, int tableNum);

	/** Message sent from cashier with bill for customer */
	public void msgHereIsBill(Bill b);

	/** Message sent from Host that waiter cannot take a break yet */
	public void msgNotYet();
	
	/** Message from Host that waiter can now take a break */
	public void msgTakeBreak();

	/**
	 * Sent from GUI to control breaks
	 * 
	 * @param state
	 *            true when the waiter should go on break and false when the
	 *            waiter should go off break Is the name onBreak right? What
	 *            should it be?
	 *            true sets breakState to want
	 */
	public void setBreakStatus(boolean state);

	/** @return name of waiter */
	public String getName();

	/** @return string representation of waiter */
	public String toString();
	
	/** Hack to set the cook for the waiter */
	public void setCook(Cook cook);

	/** Hack to set the host for the waiter */
	public void setHost(HostAgent host);

	/** Hack to set the cashier for the waiter */
	public void setCashier(Cashier c);

	/** @return true if the waiter is on break, false otherwise */
	public boolean isOnBreak();
}
