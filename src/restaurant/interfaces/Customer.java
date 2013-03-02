package restaurant.interfaces;

import restaurant.Bill;
import restaurant.HostAgent;
import restaurant.Menu;
import restaurant.layoutGUI.GuiCustomer;

public interface Customer {

	public enum AgentState {
		DoingNothing, WaitingInRestaurant, SeatedWithMenu, WaiterCalled,
		WaitingForFood, Eating, Paying, Working
	};

	public enum AgentEvent {
		gotHungry, thereIsWait, beingSeated, decidedChoice, waiterToTakeOrder, foodDelivered, 
		doneEating, gotBill, gotReceipt, mustWork, doneWorking
	};
	
	// *** MESSAGES ***
	//
	/** Sent from GUI to set the customer as hungry */
	public void setHungry();

	/**
	 * Waiter sends this message so the customer knows to sit down
	 * 
	 * @param waiter
	 *            the waiter that sent the message
	 * @param menu
	 *            a reference to a menu
	 */
	public void msgFollowMeToTable(Waiter waiter, Menu menu);

	/** Waiter sends this message to take the customer's order */
	public void msgDecided();

	/** Waiter sends this message to take the customer's order */
	public void msgWhatWouldYouLike();

	/**
	 * Waiter sends this when the food is ready
	 * 
	 * @param choice
	 *            the food that is done cooking for the customer to eat
	 */
	public void msgHereIsYourFood(String choice);
	
	/** Message from cashier with change (receipt) */
	public void msgThanks(double change);
	
	/** Message from cashier if under-paid -- must work for hours */
	public void msgNotEnoughMoneyMustWorkFor(double hours);

	/** Timer sends this when the customer has finished eating */
	public void msgDoneEating();
	
	/** Message from Waiter with bill */
	public void msgHereIsBill(Bill b);
	/** Message from host that there is a wait */
	public void msgThereIsWait();
	
	/**
	 * establish connection to host agent.
	 * 
	 * @param host
	 *            reference to the host
	 */
	public void setHost(HostAgent host);
	
	/** Establish connection to cashier agent */
	public void setCashier(Cashier c);

	/**
	 * Returns the customer's name
	 * 
	 * @return name of customer
	 */
	public String getName();

	/**
	 * @return true if the customer is hungry, false otherwise. Customer is
	 *         hungry from time he is created (or button is pushed, until he
	 *         eats and leaves.
	 */
	public boolean isHungry();

	/** @return the hungerlevel of the customer */
	public int getHungerLevel();

	/**
	 * Sets the customer's hungerlevel to a new value
	 * 
	 * @param hungerLevel
	 *            the new hungerlevel for the customer
	 */
	public void setHungerLevel(int hungerLevel);
	
	/** @return the string representation of the class */
	public String toString();
	
	public GuiCustomer getGuiCustomer();
}
