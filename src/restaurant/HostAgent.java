package restaurant;

import agent.Agent;
import java.util.*;

/**
 * Host agent for restaurant. Keeps a list of all the waiters and tables.
 * Assigns new customers to waiters for seating and keeps a list of waiting
 * customers. Interacts with customers and waiters.
 */
public class HostAgent extends Agent {

	/**
	 * Private class storing all the information for each table, including table
	 * number and state.
	 */
	private class Table {
		public int tableNum;
		public boolean occupied;

		/**
		 * Constructor for table class.
		 * 
		 * @param num
		 *            identification number
		 */
		public Table(int num) {
			tableNum = num;
			occupied = false;
		}
	}

	/** Private class to hold waiter information and state */
	private class MyWaiter {
		public WaiterAgent wtr;
		public WaiterBreakState waiterBreakState;

		/**
		 * Constructor for MyWaiter class
		 * 
		 * @param waiter: WaiterAgent
		 */
		public MyWaiter(WaiterAgent waiter) {
			wtr = waiter;
			waiterBreakState = WaiterBreakState.none;
		}
		//returns true if waiter is working (aka not onBreak)
		public boolean working() {
			if (waiterBreakState != WaiterBreakState.onBreak) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	//waiter is working in all states except onBreak
	public enum WaiterBreakState {
		none, wantsBreak, needsToWait, toldOkToBreak, onBreak
	};

	// List of all the customers that need a table
	private List<CustomerAgent> waitList = Collections
			.synchronizedList(new ArrayList<CustomerAgent>());

	// List of all waiter that exist.
	private List<MyWaiter> waiters = Collections
			.synchronizedList(new ArrayList<MyWaiter>());
	private int nextWaiter = 0; // The next waiter that needs a customer

	// List of all the tables
	int nTables;
	private Table tables[];

	// Name of the host
	private String name;

	/**
	 * Constructor for HostAgent class
	 * 
	 * @param name
	 *            name of the host
	 */
	public HostAgent(String name, int ntables) {
		super();
		this.nTables = ntables;
		tables = new Table[nTables];

		for (int i = 0; i < nTables; i++) {
			tables[i] = new Table(i);
		}
		this.name = name;
	}

	// *** MESSAGES ***
	//
	/**
	 * Customer sends this message to be added to the wait list
	 * 
	 * @param customer
	 *            customer that wants to be added
	 */
	public void msgIWantToEat(CustomerAgent customer) {
		waitList.add(customer);
		stateChanged();
	}

	/**
	 * Waiter sends this message after the customer has left the table
	 * 
	 * @param tableNum
	 *            table identification number
	 */
	public void msgTableIsFree(int tableNum) {
		tables[tableNum].occupied = false;
		stateChanged();
	}
	
	/** Message from Waiter asking to take a break */
	public void msgCanITakeBreak(WaiterAgent w) {
		for (MyWaiter myWaiter: waiters) {
			if (myWaiter.wtr == w) {
				myWaiter.waiterBreakState = WaiterBreakState.wantsBreak;
				stateChanged();
				break;
			}
		}
	}
	
	/** Message from Waiter signifying starting break */
	public void msgGoingOnBreak(WaiterAgent w) {
		for (MyWaiter myWaiter: waiters) {
			if (myWaiter.wtr == w) {
				myWaiter.waiterBreakState = WaiterBreakState.onBreak;
				stateChanged();
				break;
			}
		}
	}
	
	/** Message from Waiter signifying break is over */
	public void msgGoingOffBreak(WaiterAgent w) {
		for (MyWaiter myWaiter: waiters) {
			if (myWaiter.wtr == w) {
				myWaiter.waiterBreakState = WaiterBreakState.none;
				stateChanged();
				break;
			}
		}
	}
	
	/** Message from customer who does not want to wait if there is long wait */
	public void msgThatIsTooLongIAmLeaving(CustomerAgent c) {
		//do not sit this customer, remove him from wait list
		for (CustomerAgent customerAgent: waitList) {
			if (customerAgent == c) {
				waitList.remove(c);
				stateChanged();
			}
		}
	}
	
	/** Message from customer who is willing to wait */
	public void msgIWillWait(CustomerAgent c) {
		//keep customer on waitlist...so change nothing about list
		stateChanged();
	}
	
	// *** SCHEDULER ***
	//
	/** Scheduler. Determine what action is called for, and do it. */
	protected boolean pickAndExecuteAnAction() {
		
		//tell customer about wait if there is one
		if (!waitList.isEmpty()) {
			//check if all tables are occupied
			boolean allOccupied = true;
			for (Table t: tables) {
				if (t.occupied == false) {
					allOccupied = false; //at least one table is open
					break;
				}
			}
			//if all tables are occupied, tell customer about wait
			if (allOccupied) {
				tellCustomerThereIsWait(waitList.get(0));
				return true;
			}
		}

		if (!waitList.isEmpty() && !waiters.isEmpty()) {
			synchronized (waiters) {
				// Finds the next waiter that is working
				while (!waiters.get(nextWaiter).working()) {
					nextWaiter = (nextWaiter + 1) % waiters.size();
				}
			}
			print("picking waiter number:" + nextWaiter);
			// Then runs through the tables and finds the first unoccupied
			// table and tells the waiter to sit the first customer at that
			// table
			for (int i = 0; i < nTables; i++) {

				if (!tables[i].occupied) {
					synchronized (waitList) {
						tellWaiterToSitCustomerAtTable(waiters.get(nextWaiter),
								waitList.get(0), i);
					}
					return true;
				}
			}
		}
		
		//waiter break scenarios, only dealt with after customers
		//if there exists Waiter w such that w.wantsBreak = true
		for (MyWaiter myWaiter: waiters) {
			//if a waiter wants a break and there are no waiting customers
			//and there is at least one more waiter, allow break
			//check for waiting waiters first, then any others that want
			if (myWaiter.waiterBreakState == WaiterBreakState.needsToWait &&
				waitList.isEmpty() &&
				isAtLestOneOtherWorkingWaiter(myWaiter))
			{
				sendWaiterOnBreak(myWaiter);
				return true;
			}
			else if (myWaiter.waiterBreakState == WaiterBreakState.wantsBreak &&
					 waitList.isEmpty() &&
					 isAtLestOneOtherWorkingWaiter(myWaiter))
			{
					sendWaiterOnBreak(myWaiter);
					return true;
			}
			else if (myWaiter.waiterBreakState == WaiterBreakState.wantsBreak &&
					 (!waitList.isEmpty() ||
					  !isAtLestOneOtherWorkingWaiter(myWaiter)))
			{
				tellWaiterToWaitForBreak(myWaiter);
				return true;
			}
		}
		
		// we have tried all our rules (in this case only one) and found
		// nothing to do. So return false to main loop of abstract agent
		// and wait.
		return false;
	}

	// *** ACTIONS ***
	//
	/**
	 * Assigns a customer to a specified waiter and tells that waiter which
	 * table to sit them at.
	 * 
	 * @param waiter
	 * @param customer
	 * @param tableNum
	 */
	private void tellWaiterToSitCustomerAtTable(MyWaiter waiter,
			CustomerAgent customer, int tableNum) {
		print("Telling " + waiter.wtr + " to sit " + customer + " at table "
				+ (tableNum + 1));
		waiter.wtr.msgSitCustomerAtTable(customer, tableNum);
		tables[tableNum].occupied = true;
		waitList.remove(customer);
		nextWaiter = (nextWaiter + 1) % waiters.size();
		stateChanged();
	}
	
	/** Tell waiter it is a good time to break */
	private void sendWaiterOnBreak(MyWaiter waiter) {
		waiter.wtr.msgTakeBreak();
		//waiter's break state is not changed to onBreak until message received
		//for now set state that waiter was told break OK
		waiter.waiterBreakState = WaiterBreakState.toldOkToBreak;
		System.out.println(this+": told "+waiter.wtr+" he can go on break");
		stateChanged();
	}
	
	/** Tell waiter that he must wait to take a break and set status */
	private void tellWaiterToWaitForBreak(MyWaiter waiter) {
		waiter.wtr.msgNotYet();
		//change waiter break state to waiting for break
		waiter.waiterBreakState = WaiterBreakState.needsToWait;
		System.out.println(this+": told "+waiter.wtr+" he needs to wait to take break");
		stateChanged();
	}
	
	/** Tell the customer that there is a wait (if there is) after customer approaches */
	private void tellCustomerThereIsWait(CustomerAgent c) {
		c.msgThereIsWait();
		System.out.println(this+": told "+c+" that there is a wait");
		//stateChanged();
	}
	
	// *** EXTRA ***
	//
	/**
	 * Returns the name of the host
	 * 
	 * @return name of host
	 */
	public String getName() {
		return name;
	}
	
	public String toString() {
		return "host " + name;
	}
	
	/** Utility method for checking if there is at least one other working waiter
	 *  besides the one passed in
	 *  @return true if there is at least one working waiter
	 */
	private boolean isAtLestOneOtherWorkingWaiter(MyWaiter w) {
		for (MyWaiter myWaiter: waiters) {
			//skip the passed in waiter
			if (myWaiter == w) {
				continue;
			}
			if (myWaiter.waiterBreakState != WaiterBreakState.onBreak) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Hack to enable the host to know of all possible waiters
	 * 
	 * @param waiter
	 *            new waiter to be added to list
	 */
	public void setWaiter(WaiterAgent waiter) {
		waiters.add(new MyWaiter(waiter));
		stateChanged();
	}

	// Gautam Nayak - Gui calls this when table is created in animation
	public void addTable() {
		nTables++;
		Table[] tempTables = new Table[nTables];
		for (int i = 0; i < nTables - 1; i++) {
			tempTables[i] = tables[i];
		}
		tempTables[nTables - 1] = new Table(nTables - 1);
		tables = tempTables;
	}
}
