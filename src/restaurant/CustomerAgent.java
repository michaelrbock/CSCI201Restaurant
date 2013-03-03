package restaurant;

import restaurant.gui.RestaurantGui;
import restaurant.interfaces.*;
import restaurant.layoutGUI.*;
import agent.Agent;
import restaurant.Bill.*;
import java.util.*;
import java.awt.Color;

/**
 * Restaurant customer agent. Comes to the restaurant when he/she becomes
 * hungry. Randomly chooses a menu item and simulates eating when the food
 * arrives. Interacts with a waiter only
 */
public class CustomerAgent extends Agent implements Customer {
	private String name;
	private int hungerLevel = 5; // Determines length of meal
	private RestaurantGui gui;
	
	// ** Agent connections **
	private HostAgent host;
	private Waiter waiter;
	private Cashier cashier;
	Restaurant restaurant;
	private Menu menu;
	// ** utilities **
	Timer timer = new Timer();
	Random rand = new Random();
	GuiCustomer guiCustomer; // for gui
	// ** Agent state **
	private boolean isHungry = false; // hack for gui

	public enum AgentState {
		DoingNothing, WaitingInRestaurant, SeatedWithMenu, WaiterCalled,
		WaitingForFood, Eating, Paying, Working
	};

	// {NO_ACTION,NEED_SEATED,NEED_DECIDE,NEED_ORDER,NEED_EAT,NEED_LEAVE};
	private AgentState state = AgentState.DoingNothing;// The start state

	public enum AgentEvent {
		gotHungry, thereIsWait, beingSeated, decidedChoice, waiterToTakeOrder, foodDelivered, 
		doneEating, gotBill, gotReceipt, mustWork, doneWorking
	};

	List<AgentEvent> events = Collections.synchronizedList(new ArrayList<AgentEvent>());
	
	//customer begins with between 0 and 30 (exclusive) dollars in cash
	private double cash = (double)rand.nextInt(30);
	
	//customer's bill
	private Bill bill;
	
	//used only if needed because not enough cash
	private double hoursToWork = 0.0;

	/**
	 * Constructor for CustomerAgent class
	 * 
	 * @param name
	 *            name of the customer
	 * @param gui
	 *            reference to the gui so the customer can send it messages
	 */
	public CustomerAgent(String name, RestaurantGui gui, Restaurant restaurant) {
		super();
		this.gui = gui;
		this.name = name;
		this.restaurant = restaurant;
		guiCustomer = new GuiCustomer(name.substring(0, 2),
				new Color(0, 255, 0), restaurant);
	}

	public CustomerAgent(String name, Restaurant restaurant) {
		super();
		this.gui = null;
		this.name = name;
		this.restaurant = restaurant;
		guiCustomer = new GuiCustomer(name.substring(0, 1),
				new Color(0, 255, 0), restaurant);
	}
	
	// *** MESSAGES ***
	//
	/** Sent from GUI to set the customer as hungry */
	public void setHungry() {
		events.add(AgentEvent.gotHungry);
		isHungry = true;
		print("I'm hungry");
		stateChanged();
	}

	/**
	 * Waiter sends this message so the customer knows to sit down
	 * 
	 * @param waiter
	 *            the waiter that sent the message
	 * @param menu
	 *            a reference to a menu
	 */
	public void msgFollowMeToTable(Waiter waiter, Menu menu) {
		this.menu = menu;
		this.waiter = waiter;
		print("Received msgFollowMeToTable from" + waiter);
		events.add(AgentEvent.beingSeated);
		stateChanged();
	}

	/** Waiter sends this message to take the customer's order */
	public void msgDecided() {
		events.add(AgentEvent.decidedChoice);
		stateChanged();
	}

	/** Waiter sends this message to take the customer's order */
	public void msgWhatWouldYouLike() {
		events.add(AgentEvent.waiterToTakeOrder);
		stateChanged();
	}

	/**
	 * Waiter sends this when the food is ready
	 * 
	 * @param choice
	 *            the food that is done cooking for the customer to eat
	 */
	public void msgHereIsYourFood(String choice) {
		events.add(AgentEvent.foodDelivered);
		stateChanged();
	}
	
	/** Message from cashier with change (receipt) */
	public void msgThanks(double change) {
		System.out.println(this+": got receipt with $"+((double)Math.round(change * 100) / 100)+" of change");
		events.add(AgentEvent.gotReceipt);
		stateChanged();
	}
	
	/** Message from cashier if under-paid -- must work for hours */
	public void msgNotEnoughMoneyMustWorkFor(double hours) {
		//add working event and hours to work
		hoursToWork = hours;
		events.add(AgentEvent.mustWork);
		stateChanged();
	}

	/** Timer sends this when the customer has finished eating */
	public void msgDoneEating() {
		events.add(AgentEvent.doneEating);
		stateChanged();
	}
	
	/** Message from Waiter with bill */
	public void msgHereIsBill(Bill b) {
		//add bill to internal data
		bill = b;
		events.add(AgentEvent.gotBill);
		stateChanged();
	}
	
	/** Message from host that there is a wait */
	public void msgThereIsWait() {
		//will decide to leave or stay in actions
		events.add(AgentEvent.thereIsWait);
		stateChanged();
	}
	
	// *** SCHEDULER ***
	//
	/** Scheduler. Determine what action is called for, and do it. */
	protected boolean pickAndExecuteAnAction() {
		//debug: print(" events: "+events.toString()); 
		//debug: System.out.println("In customer scheduler");
				
		if (events.isEmpty())
			return false;
		AgentEvent event = events.remove(0); // pop first element

		// Simple finite state machine
		if (state == AgentState.DoingNothing) {
			if (event == AgentEvent.gotHungry) {
				state = AgentState.WaitingInRestaurant;
				goingToRestaurant();
				return true;
			}
			// elseif (event == xxx) {}
		}
		if (state == AgentState.WaitingInRestaurant) {
			if (event == AgentEvent.thereIsWait) {
				decideToWaitOrLeave(); //changes states, deletes all events
				return true;
			}
			else if (event == AgentEvent.beingSeated) {
				makeMenuChoice();
				state = AgentState.SeatedWithMenu;
				return true;
			}
		}
		if (state == AgentState.SeatedWithMenu) {
			if (event == AgentEvent.decidedChoice) {
				callWaiter();
				state = AgentState.WaiterCalled;
				return true;
			}
		}
		if (state == AgentState.WaiterCalled) {
			if (event == AgentEvent.waiterToTakeOrder) {
				orderFood();
				state = AgentState.WaitingForFood;
				return true;
			}
		}
		if (state == AgentState.WaitingForFood) {
			//if the cook is out of something, the waiter will send
			//another msgWhatWouldYouLike which adds a waiterToTakeOrder event
			if (event == AgentEvent.waiterToTakeOrder) {
				orderFood(); //order again
				return true;
			}
			else if (event == AgentEvent.foodDelivered) {
				eatFood();
				state = AgentState.Eating;
				return true;
			}
		}
		if (state == AgentState.Eating) {
			if (event == AgentEvent.doneEating) {
				doneEating();
				state = AgentState.Paying;
				return true;
			}
		}
		if (state == AgentState.Paying) {
			if (event == AgentEvent.gotBill) {
				payBill();
				return true;
			}
			else if (event == AgentEvent.gotReceipt) {
				leaveRestaurant();
				return true;
			}
			else if (event == AgentEvent.mustWork) {
				work();
				return true;
			}
		}
		if (state == AgentState.Working) {
			if (event == AgentEvent.doneWorking) {
				leaveRestaurant();
				return true;
			}
		}

		print("No scheduler rule fired, should not happen in FSM, event="
				+ event + " state=" + state);
		return false;
	}

	// *** ACTIONS ***
	//
	/** Goes to the restaurant when the customer becomes hungry */
	private void goingToRestaurant() {
		print("Going to restaurant");
		state = AgentState.WaitingInRestaurant;
		guiCustomer.appearInWaitingQueue();
		host.msgIWantToEat(this);// send him our instance, so he can respond to
									// us
		stateChanged();
	}
	
	/** Decides to wait or leave when there is a wait */
	private void decideToWaitOrLeave() {
		int choice = rand.nextInt(100);
		choice = choice % 2;
		//stay and wait
		if (choice == 0) {
			host.msgIWillWait(this);
			state = AgentState.WaitingInRestaurant;
			System.out.println(this+": I will wait");
		}
		//leave
		else if (choice == 1) {
			host.msgThatIsTooLongIAmLeaving(this);
			isHungry = false;
			bill = null;
			hoursToWork = 0;
			cash = (double)rand.nextInt(30);
			state = AgentState.DoingNothing;
			System.out.println(this+": I will not wait");
			
			//gui
			guiCustomer.leave(); // for the animation
			gui.setCustomerEnabled(this); // Message to gui to enable hunger button
			// hack to keep customer getting hungry. Only for non-gui customers
			if (gui == null)
				becomeHungryInAWhile();// set a timer to make us hungry
		}
		stateChanged();
	}

	/** Starts a timer to simulate the customer thinking about the menu */
	private void makeMenuChoice() {
		print("Deciding menu choice...(3000 milliseconds)");
		timer.schedule(new TimerTask() {
			public void run() {
				msgDecided();
			}
		}, 3000);// how long to wait before running task
		stateChanged();
	}

	private void callWaiter() {
		print("I decided!");
		waiter.msgImReadyToOrder(this);
		stateChanged();
	}

	/** Picks a random choice from the menu and sends it to the waiter */
	private void orderFood() {
		String choice = menu.choices[(int) (Math.random() * 4)];
		print("Ordering the " + choice);
		waiter.msgHereIsMyChoice(this, choice);
		stateChanged();
	}

	/** Starts a timer to simulate eating */
	private void eatFood() {
		print("Eating for " + hungerLevel * 1000 + " milliseconds.");
		timer.schedule(new TimerTask() {
			public void run() {
				msgDoneEating();
			}
		}, getHungerLevel() * 1000);// how long to wait before running task
		stateChanged();
	}
	
	/** Tell waiter that customer is done eating */
	private void doneEating() {
		System.out.println(this+": told "+waiter+" I am done eating");
		waiter.msgDoneEating(this);
		stateChanged();
	}

	/** When the customer is done eating, he leaves the restaurant */
	private void leaveRestaurant() {
		print("Leaving the restaurant");
		guiCustomer.leave(); // for the animation
		waiter.msgDoneEatingAndLeaving(this);
		isHungry = false;
		bill = null;
		hoursToWork = 0;
		cash = (double)rand.nextInt(30);
		state = AgentState.DoingNothing;
		stateChanged();
		gui.setCustomerEnabled(this); // Message to gui to enable hunger button

		// hack to keep customer getting hungry. Only for non-gui customers
		if (gui == null)
			becomeHungryInAWhile();// set a timer to make us hungry.
	}
	
	/** To pay the bill */
	private void payBill() {
		System.out.println(this+": payed the cashier $"+cash);
		cashier.msgPayment(this, bill, cash);
	}
	
	/** To work when underpaid */
	private void work() {
		state = AgentState.Working;
		cashier.msgWillWorkFor(this, bill, hoursToWork);
		System.out.println(this+": washing dishes for "+(hoursToWork*1000)+"ms");
		timer.schedule( new TimerTask() {
			public void run() {
				doneWorking();
			}
		}, (long)(hoursToWork*1000) );// how long to wait before running task
		//stateChanged();
	}
	
	/** When done working */
	private void doneWorking() {
		events.add(AgentEvent.doneWorking);
		stateChanged();
	}

	/**
	 * This starts a timer so the customer will become hungry again. This is a
	 * hack that is used when the GUI is not being used
	 */
	private void becomeHungryInAWhile() {
		timer.schedule(new TimerTask() {
			public void run() {
				setHungry();
			}
		}, 15000);// how long to wait before running task
	}

	// *** EXTRA ***

	/**
	 * establish connection to host agent.
	 * 
	 * @param host
	 *            reference to the host
	 */
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	/** Establish connection to cashier agent */
	public void setCashier(Cashier c) {
		this.cashier = c;
	}

	/**
	 * Returns the customer's name
	 * 
	 * @return name of customer
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return true if the customer is hungry, false otherwise. Customer is
	 *         hungry from time he is created (or button is pushed, until he
	 *         eats and leaves.
	 */
	public boolean isHungry() {
		return isHungry;
	}

	/** @return the hungerlevel of the customer */
	public int getHungerLevel() {
		return hungerLevel;
	}

	/**
	 * Sets the customer's hungerlevel to a new value
	 * 
	 * @param hungerLevel
	 *            the new hungerlevel for the customer
	 */
	public void setHungerLevel(int hungerLevel) {
		this.hungerLevel = hungerLevel;
	}

	public GuiCustomer getGuiCustomer() {
		return guiCustomer;
	}
	
	/** @return the string representation of the class */
	public String toString() {
		return "customer " + getName();
	}

}
