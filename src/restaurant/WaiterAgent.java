package restaurant;

import java.awt.Color;
import restaurant.interfaces.*;
import restaurant.gui.RestaurantGui;
import restaurant.layoutGUI.*;
import restaurant.Bill.*;
import agent.Agent;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import astar.*;
import java.util.*;

/**
 * Restaurant Waiter Agent. Sits customers at assigned tables and takes their
 * orders. Takes the orders to the cook and then returns them when the food is
 * done. Cleans up the tables after the customers leave. Interacts with
 * customers, host, and cook
 */
public class WaiterAgent extends Agent implements Waiter {

	// State variables for Waiter
	public enum BreakState {
		none, wantBreak, askedForBreak, mustWaitForBreak, canTake, onBreak
	};
	
	private BreakState breakState; //default: none

	// State constants for Customers

	public enum CustomerState {
		NEED_SEATED, READY_TO_ORDER, ORDER_PENDING, ORDER_READY,
		IS_DONE_EATING, HAS_BILL, IS_COMPLETELY_DONE, NO_ACTION
	};

	Timer timer = new Timer();

	/**
	 * Private class to hold information for each customer. Contains a reference
	 * to the customer, his choice, table number, and state
	 */
	private class MyCustomer {
		public CustomerState state;
		public Customer cmr;
		public String choice;
		public int tableNum;
		public Bill bill; // from cashier
		public boolean askedForBill;
		public Food food; // gui thing
		public boolean choiceIsOut; //default false

		/**
		 * Constructor for MyCustomer class.
		 * 
		 * @param cmr
		 *            reference to customer
		 * @param num
		 *            assigned table number
		 */
		public MyCustomer(Customer cmr, int num) {
			this.cmr = cmr;
			tableNum = num;
			state = CustomerState.NO_ACTION;
			choiceIsOut = false;
			askedForBill = false;
		}
	}

	// Name of waiter
	private String name;
	
	//Menu
	private Menu menu;

	// All the customers that this waiter is serving
	private List<MyCustomer> customers = Collections.synchronizedList(new ArrayList<MyCustomer>());

	private HostAgent host;
	private Cook cook;
	private Cashier cashier;
	
	//Access to the revolving stand if it is a shared data waiter
	boolean useRevolvingStand;
	protected List<Order> revolvingStand; 

	// Animation Variables
	AStarTraversal aStar;
	Restaurant restaurant; // the gui layout
	GuiWaiter guiWaiter;
	Position currentPosition;
	Position originalPosition;
	Table[] tables; // the gui tables

	/**
	 * Constructor for WaiterAgent class
	 * 
	 * @param name
	 *            name of waiter
	 * @param gui
	 *            reference to the gui
	 */
	public WaiterAgent(String name, AStarTraversal aStar, 
			Restaurant restaurant, Table[] tables, List<Order> revStand) {
		super();

		this.name = name;
		breakState = BreakState.none;

		// initialize all the animation objects
		this.aStar = aStar;
		this.restaurant = restaurant;// the layout for astar
		guiWaiter = new GuiWaiter(name.substring(0, 2), new Color(255, 0, 0),
				restaurant);
		currentPosition = new Position(guiWaiter.getX(), guiWaiter.getY());
		currentPosition.moveInto(aStar.getGrid());
		originalPosition = currentPosition;// save this for moving into
		this.tables = tables;
		
		if (revStand != null) {
			this.useRevolvingStand = true;
			this.revolvingStand = revStand;
		}
		else {
			this.useRevolvingStand = false;
		}
	}

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
	public void msgSitCustomerAtTable(Customer customer, int tableNum) {
		MyCustomer c = new MyCustomer(customer, tableNum);
		c.state = CustomerState.NEED_SEATED;
		customers.add(c);
		stateChanged();
	}

	/**
	 * Customer sends this when they are ready.
	 * 
	 * @param customer
	 *            customer who is ready to order.
	 */
	public void msgImReadyToOrder(Customer customer) {
		// print("received msgImReadyToOrder from:"+customer);
		MyCustomer temp = null;
		synchronized(customers) {
			for (int i = 0; i < customers.size(); i++) {
				// if(customers.get(i).cmr.equals(customer)){
				if (customers.get(i).cmr == customer) {
					temp = customers.get(i);
					break;
				}
			}
		}
		if (temp != null) {
			temp.state = CustomerState.READY_TO_ORDER;
			stateChanged();
			return;
		}
		
		System.out
				.println("msgImReadyToOrder in WaiterAgent, didn't find him?");
	}

	/**
	 * Customer sends this when they have decided what they want to eat
	 * 
	 * @param customer
	 *            customer who has decided their choice
	 * @param choice
	 *            the food item that the customer chose
	 */
	public void msgHereIsMyChoice(Customer customer, String choice) {
		MyCustomer temp = null;
		synchronized(customers) {
			for (MyCustomer c : customers) {
				if (c.cmr.equals(customer)) {
					temp = c;
					break;
				}
			}
		}
		if (temp != null) {
			temp.choice = choice;
			temp.state = CustomerState.ORDER_PENDING;
			stateChanged();
			return;
		}
	}

	/**
	 * Cook sends this when the order is ready.
	 * 
	 * @param tableNum
	 *            identification number of table whose food is ready
	 * @param f
	 *            is the guiFood object
	 */
	public void msgOrderIsReady(int tableNum, Food f) {
		MyCustomer temp = null;
		synchronized(customers) {
			for (MyCustomer c : customers) {
				if (c.tableNum == tableNum) {
					temp = c;
					break;
				}
			}
		}
		if (temp != null) {
			temp.state = CustomerState.ORDER_READY;
			temp.food = f; // so that later we can remove it from the table.
			stateChanged();
			return;
		}
	}

	/**
	 * Customer sends this when they are done eating.
	 * 
	 * @param customer
	 *            customer who is leaving the restaurant.
	 */
	public void msgDoneEating(Customer customer) {
		MyCustomer temp = null;
		synchronized(customers) {
			for (MyCustomer c : customers) {
				if (c.cmr.equals(customer)) {
					temp = c;
					break;
				}
			}
		}
		if (temp != null) {
			temp.state = CustomerState.IS_DONE_EATING;
			stateChanged();
			return;
		}
	}
	
	public void msgDoneEatingAndLeaving(Customer customer) {
		MyCustomer temp = null;
		synchronized(customers) {
			for (MyCustomer c : customers) {
				if (c.cmr.equals(customer)) {
					temp = c;
					break;
				}
			}
		}
		if (temp != null) {
			temp.state = CustomerState.IS_COMPLETELY_DONE;
			stateChanged();
			return;
		}
	}
	
	/** Message from Cook that customer's choice is out */
	public void msgOutOfChoice(String choice, int tableNum) {
		//find customer and set state that his choice is out
		MyCustomer temp = null;
		synchronized(customers) {
			for (MyCustomer myCustomer: customers) {
				if (myCustomer.tableNum == tableNum) {
					temp = myCustomer;
					break;
				}
			}
		}
		if (temp != null) {
			temp.choiceIsOut = true;
			stateChanged();
		}
	}
	
	/** Message sent from cashier with bill for customer */
	public void msgHereIsBill(Bill b) {
		//add bill to MyCustomer object for correct customer
		MyCustomer temp = null;
		synchronized(customers) {
			for (MyCustomer myCustomer: customers) {
				//find customer associated with bill
				if (myCustomer.cmr == b.cmr) {
					temp = myCustomer;
				}
			}
		}
		if (temp != null) {
			temp.bill = b;
			stateChanged();
			return;
		}
	}
	
	/** Message sent from Host that waiter cannot take a break yet */
	public void msgNotYet() {
		breakState = BreakState.mustWaitForBreak;
		stateChanged();
	}
	
	/** Message from Host that waiter can now take a break */
	public void msgTakeBreak() {
		breakState = BreakState.canTake;
		stateChanged();
	}

	/**
	 * Sent from GUI to control breaks
	 * 
	 * @param state
	 *            true when the waiter should go on break and false when the
	 *            waiter should go off break Is the name onBreak right? What
	 *            should it be?
	 *            true sets breakState to want
	 */
	public void setBreakStatus(boolean state) {
		if (state) {
			breakState = BreakState.wantBreak;
		}
		else {
			breakState = BreakState.none;
		}
		stateChanged();
	}
	
	// *** SCHEDULER ***
	//
	/** Scheduler. Determine what action is called for, and do it. */
	protected boolean pickAndExecuteAnAction() {
		// print("in waiter scheduler");
		//debug: System.out.println("In waiter scheduler");
		
		//Waiter on break does nothing
		if (breakState == BreakState.onBreak) {
			return false;
		}

		// Runs through the customers for each rule, so
		// the waiter doesn't serve only one customer at a time
		if (!customers.isEmpty()) {
			// System.out.println("in scheduler, customers not empty:");
			// Gives food to customer if the order is ready
			MyCustomer temp = null;
			synchronized(customers) {
				for (MyCustomer c : customers) {
					if (c.state == CustomerState.ORDER_READY) {
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				giveFoodToCustomer(temp);
				return true;
			}
			
			// Clears the table if the customer has left
			temp = null;
			synchronized(customers) {
				for (MyCustomer c : customers) {
					if (c.state == CustomerState.IS_COMPLETELY_DONE) {
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				clearTable(temp);
				return true;
			}

			// Seats the customer if they need it
			temp = null;
			synchronized(customers) {
				for (MyCustomer c : customers) {
					if (c.state == CustomerState.NEED_SEATED) {
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				seatCustomer(temp);
				return true;
			}
			temp = null;
			
			// Gives customer bill if done and need it
			MyCustomer tempSend = null;
			MyCustomer tempGet = null;
			synchronized(customers) {
				for (MyCustomer c: customers) {
					if (c.state == CustomerState.IS_DONE_EATING && 
						c.bill != null) 
					{
						tempSend = c;
						break;
					}
					else if (c.state == CustomerState.IS_DONE_EATING) {
						if (!c.askedForBill) {
							tempGet = c;
							break;
						}
					}
				}
			}
			if (tempSend != null) {
				sendBillToCustomer(tempSend);
				return true;
			}
			else if (tempGet != null) {
				getBillFromCashierFor(tempGet);
				return true;
			}

			// Gives all pending orders to the cook
			temp = null;
			synchronized(customers) {
				for (MyCustomer c : customers) {
					if (c.state == CustomerState.ORDER_PENDING) {
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				giveOrderToCook(temp);
				return true;
			}

			// Takes new orders for customers that are ready
			temp = null;
			synchronized(customers) {
				for (MyCustomer c : customers) {
					// print("testing for ready to order"+c.state);
					if (c.state == CustomerState.READY_TO_ORDER) {
						temp = c;
						break;
					}
				}
			}
			if (temp != null) {
				takeOrder(temp);
				return true;
			}
			
			//Gives bill to customers that are done eating!
		}
		
		
		if (!currentPosition.equals(originalPosition)) {
			DoMoveToOriginalPosition();// Animation thing
			return true;
		}
		
		//Break scenarios -- only do after dealing with customers
		if (breakState == BreakState.wantBreak) {
			askForBreak();
			return true;
		}
		else if (breakState == BreakState.mustWaitForBreak) {
			return false;
		}
		else if (breakState == BreakState.canTake) {
			takeBreak();
			return true;
		}

		// we have tried all our rules and found nothing to do.
		// So return false to main loop of abstract agent and wait.
		// print("in scheduler, no rules matched:");
		return false;
	}

	// *** ACTIONS ***
	//
	/**
	 * Seats the customer at a specific table
	 * 
	 * @param customer
	 *            customer that needs seated
	 */
	private void seatCustomer(MyCustomer customer) {
		DoSeatCustomer(customer); // animation
		customer.state = CustomerState.NO_ACTION;
		customer.cmr.msgFollowMeToTable(this, new Menu());
		stateChanged();
	}

	/**
	 * Takes down the customers order
	 * 
	 * @param customer
	 *            customer that is ready to order
	 */
	private void takeOrder(MyCustomer customer) {
		DoTakeOrder(customer); // animation
		customer.state = CustomerState.NO_ACTION;
		customer.cmr.msgWhatWouldYouLike();
		stateChanged();
	}

	/**
	 * Gives any pending orders to the cook
	 * 
	 * @param customer
	 *            customer that needs food cooked
	 */
	private void giveOrderToCook(MyCustomer customer) {
		// In our animation the waiter does not move to the cook in
		// order to give him an order. We assume some sort of electronic
		// method implemented as our message to the cook. So there is no
		// animation analog, and hence no DoXXX routine is needed.
		if (!useRevolvingStand) { //normal waiter
			print("Giving " + customer.cmr + "'s choice of " + customer.choice
					+ " to cook");
	
			customer.state = CustomerState.NO_ACTION;
			cook.msgHereIsAnOrder(this, customer.tableNum, customer.choice);
			stateChanged();
		}
		else { //shared data waiter
			print("Putting " + customer.cmr + "'s choice of " + customer.choice
					+ " on revolving stand");
			
			customer.state = CustomerState.NO_ACTION;
			//add order directly to shared revolving stand
			revolvingStand.add(new Order(this, customer.tableNum, customer.choice));
			//cook.addToRevolvingStand(this, customer.tableNum, customer.choice);
			stateChanged();
		}

		// Here's a little animation hack. We put the first two
		// character of the food name affixed with a ? on the table.
		// Simply let's us see what was ordered.
		tables[customer.tableNum].takeOrder(customer.choice.substring(0, 2)
				+ "?");
		restaurant.placeFood(tables[customer.tableNum].foodX(),
				tables[customer.tableNum].foodY(), new Color(255, 255, 255),
				customer.choice.substring(0, 2) + "?");
	}

	/**
	 * Gives food to the customer
	 * 
	 * @param customer
	 *            customer whose food is ready
	 */
	private void giveFoodToCustomer(MyCustomer customer) {
		DoGiveFoodToCustomer(customer);// Animation
		customer.state = CustomerState.NO_ACTION;
		customer.cmr.msgHereIsYourFood(customer.choice);
		stateChanged();
	}

	/**
	 * Starts a timer to clear the table
	 * 
	 * @param customer
	 *            customer whose table needs cleared
	 */
	private void clearTable(MyCustomer customer) {
		DoClearingTable(customer);
		customer.state = CustomerState.NO_ACTION;
		stateChanged();
	}
	
	/** Get bill for customer from cashier */
	private void getBillFromCashierFor(MyCustomer c) {
		System.out.println(this+": asked cashier for bill for "+c.cmr);
		cashier.msgNeedBillForCustomer(this, c.cmr, c.choice);
		c.askedForBill = true;
	}
	
	/** Sends bill to finished customer */
	private void sendBillToCustomer(MyCustomer c) {
		System.out.println(this+": sent bill to "+c.cmr);
		c.cmr.msgHereIsBill(c.bill);
		c.state = CustomerState.HAS_BILL;
		stateChanged();
	}
	
	/** Ask the host if can take break when want to */
	private void askForBreak() {
		host.msgCanITakeBreak(this);
		breakState = BreakState.askedForBreak;
		System.out.println(this+": asking host for break");
		stateChanged();
	}
	
	/** Go on break when allowed by host and no customer stuff to do */
	private void takeBreak() {
		host.msgGoingOnBreak(this);
		breakState = BreakState.onBreak;
		System.out.println(this+": going on break for 10000ms");
		timer.schedule(new TimerTask() {
			public void run() {
				goOffBreak();
			}
		}, 10000);
		stateChanged();
	}
	
	/** Going off break after timer is fired */
	private void goOffBreak() {
		host.msgGoingOffBreak(this);
		breakState = BreakState.none;
		System.out.println(this+": going off break");
		stateChanged();
	}

	// Animation Actions
	void DoSeatCustomer(MyCustomer customer) {
		print("Seating " + customer.cmr + " at table "
				+ (customer.tableNum + 1));
		// move to customer first.
		GuiCustomer guiCustomer = customer.cmr.getGuiCustomer();
		guiMoveFromCurrentPostionTo(new Position(guiCustomer.getX() + 1,
				guiCustomer.getY()));
		guiWaiter.pickUpCustomer(guiCustomer);
		Position tablePos = new Position(tables[customer.tableNum].getX() - 1,
				tables[customer.tableNum].getY() + 1);
		guiMoveFromCurrentPostionTo(tablePos);
		guiWaiter.seatCustomer(tables[customer.tableNum]);
	}

	void DoTakeOrder(MyCustomer customer) {
		print("Taking " + customer.cmr + "'s order.");
		Position tablePos = new Position(tables[customer.tableNum].getX() - 1,
				tables[customer.tableNum].getY() + 1);
		guiMoveFromCurrentPostionTo(tablePos);
	}

	void DoGiveFoodToCustomer(MyCustomer customer) {
		print("Giving finished order of " + customer.choice + " to "
				+ customer.cmr);
		Position inFrontOfGrill = new Position(customer.food.getX() - 1,
				customer.food.getY());
		guiMoveFromCurrentPostionTo(inFrontOfGrill);// in front of grill
		guiWaiter.pickUpFood(customer.food);
		Position tablePos = new Position(tables[customer.tableNum].getX() - 1,
				tables[customer.tableNum].getY() + 1);
		guiMoveFromCurrentPostionTo(tablePos);
		guiWaiter.serveFood(tables[customer.tableNum]);
	}

	void DoClearingTable(final MyCustomer customer) {
		print("Clearing table " + (customer.tableNum + 1)
				+ " (1500 milliseconds)");
		timer.schedule(new TimerTask() {
			public void run() {
				endCustomer(customer);
			}
		}, 1500);
	}

	/**
	 * Function called at the end of the clear table timer to officially remove
	 * the customer from the waiter's list.
	 * 
	 * @param customer
	 *            customer who needs removed from list
	 */
	private void endCustomer(MyCustomer customer) {
		print("Table " + (customer.tableNum + 1) + " is cleared!");
		customer.food.remove(); // remove the food from table animation
		host.msgTableIsFree(customer.tableNum);
		customers.remove(customer);
		stateChanged();
	}

	private void DoMoveToOriginalPosition() {
		print("Nothing to do. Moving to original position=" + originalPosition);
		guiMoveFromCurrentPostionTo(originalPosition);
	}

	// this is just a subroutine for waiter moves. It's not an "Action"
	// itself, it is called by Actions.
	void guiMoveFromCurrentPostionTo(Position to) {
		// System.out.println("[Gaut] " + guiWaiter.getName() + " moving from "
		// + currentPosition.toString() + " to " + to.toString());

		AStarNode aStarNode = (AStarNode) aStar.generalSearch(currentPosition,
				to);
		List<Position> path = aStarNode.getPath();
		Boolean firstStep = true;
		Boolean gotPermit = true;

		for (Position tmpPath : path) {
			// The first node in the path is the current node. So skip it.
			if (firstStep) {
				firstStep = false;
				continue;
			}

			// Try and get lock for the next step.
			int attempts = 1;
			gotPermit = new Position(tmpPath.getX(), tmpPath.getY())
					.moveInto(aStar.getGrid());

			// Did not get lock. Lets make n attempts.
			while (!gotPermit && attempts < 3) {
				// System.out.println("[Gaut] " + guiWaiter.getName() +
				// " got NO permit for " + tmpPath.toString() + " on attempt " +
				// attempts);

				// Wait for 1sec and try again to get lock.
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}

				gotPermit = new Position(tmpPath.getX(), tmpPath.getY())
						.moveInto(aStar.getGrid());
				attempts++;
			}

			// Did not get lock after trying n attempts. So recalculating path.
			if (!gotPermit) {
				// System.out.println("[Gaut] " + guiWaiter.getName() +
				// " No Luck even after " + attempts +
				// " attempts! Lets recalculate");
				guiMoveFromCurrentPostionTo(to);
				break;
			}

			// Got the required lock. Lets move.
			// System.out.println("[Gaut] " + guiWaiter.getName() +
			// " got permit for " + tmpPath.toString());
			currentPosition.release(aStar.getGrid());
			currentPosition = new Position(tmpPath.getX(), tmpPath.getY());
			guiWaiter.move(currentPosition.getX(), currentPosition.getY());
		}
		/*
		 * boolean pathTaken = false; while (!pathTaken) { pathTaken = true;
		 * //print("A* search from " + currentPosition + "to "+to); AStarNode a
		 * = (AStarNode)aStar.generalSearch(currentPosition,to); if (a == null)
		 * {//generally won't happen. A* will run out of space first.
		 * System.out.println("no path found. What should we do?"); break; //dw
		 * for now } //dw coming. Get the table position for table 4 from the
		 * gui //now we have a path. We should try to move there List<Position>
		 * ps = a.getPath(); Do("Moving to position " + to + " via " + ps); for
		 * (int i=1; i<ps.size();i++){//i=0 is where we are //we will try to
		 * move to each position from where we are. //this should work unless
		 * someone has moved into our way //during our calculation. This could
		 * easily happen. If it //does we need to recompute another A* on the
		 * fly. Position next = ps.get(i); if (next.moveInto(aStar.getGrid())){
		 * //tell the layout gui guiWaiter.move(next.getX(),next.getY());
		 * currentPosition.release(aStar.getGrid()); currentPosition = next; }
		 * else { System.out.println("going to break out path-moving");
		 * pathTaken = false; break; } } }
		 */
	}

	// *** EXTRA ***

	/** @return name of waiter */
	public String getName() {
		return name;
	}

	/** @return string representation of waiter */
	public String toString() {
		return "waiter " + getName();
	}

	/** Hack to set the cook for the waiter */
	public void setCook(Cook cook) {
		this.cook = cook;
	}

	/** Hack to set the host for the waiter */
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	/** Hack to set the cashier for the waiter */
	public void setCashier(Cashier c) {
		this.cashier = c;
	}
	
	/** Hack to set the revolving stand from cook
	public void setRevolvingStand(List<Order> rs) {
		
	}
	*/

	/** @return true if the waiter is on break, false otherwise */
	public boolean isOnBreak() {
		if (breakState == BreakState.onBreak) {
			return true;
		}
		else {
			return false;
		}
	}

}
