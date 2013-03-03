package restaurant;

import agent.Agent;
import restaurant.interfaces.*;
import java.util.*;

import restaurant.MarketAgent;
import restaurant.WaiterAgent.CustomerState;
import restaurant.layoutGUI.*;

import java.awt.Color;

/**
 * Cook agent for restaurant. Keeps a list of orders for waiters and simulates
 * cooking them. Interacts with waiters only.
 */
public class CookAgent extends Agent implements Cook {

	// List of all the orders and inventory of food items
	private List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());
	private Map<String, FoodData> inventory = Collections.synchronizedMap(new HashMap<String, FoodData>());

	public enum OrderStatus {
		pending, cooking, done
	}; // order status

	// Name of the cook
	private String name;
	
	//Agent Connections
	private Cashier csr;

	// Timer for simulation
	Timer timer = new Timer();
	Restaurant restaurant; // Gui layout
	Random rand = new Random(); //utility
	
	//List of Markets to order from
	private List<MyMarket> markets = new ArrayList<MyMarket>();
	
	private boolean marketOrderIsPlaced; //default false

	/**
	 * Constructor for CookAgent class
	 * 
	 * @param name
	 *            name of the cook
	 */
	public CookAgent(String name, Restaurant restaurant) {
		super();

		this.name = name;
		this.restaurant = restaurant;
		// Create the restaurant's inventory: name, cookTime, amount in inventory
		inventory.put("Steak", new FoodData("Steak",     5, 0));
		inventory.put("Chicken", new FoodData("Chicken", 4, 2));
		inventory.put("Pizza", new FoodData("Pizza",     3, 2));
		inventory.put("Salad", new FoodData("Salad",     2, 2));
		
		marketOrderIsPlaced = false;
	}

	/**
	 * Private class to store information about food. Contains the food type,
	 * its cooking time, and ...
	 */
	private class FoodData {
		public String type; // kind of food
		public double cookTime;

		// other things ...
		public int amount;
		
		public FoodData(String type, double cookTime, int amount) {
			this.type = type;
			this.cookTime = cookTime;
			this.amount = amount;
		}
		//for use when market delivers food
		public void addToInventory(int amount) {
			this.amount += amount;
		}
		public String getType() {
			return type;
		}
	}

	/**
	 * Private class to store order information. Contains the waiter, table
	 * number, food item, cooktime and status.
	 */
	private class Order {
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

		/** Represents the object as a string */
		public String toString() {
			return choice + " for " + waiter;
		}
	}
	
	/**
	 * Private class to hold information for each market. Contains a reference
	 * to the MarketAgent, and his known inventory state for each food item
	 */
	private class MyMarket {
		public Market mkt;
		public Map<String,MarketInventoryStatus> inventoryStatus = 
				new HashMap<String,MarketInventoryStatus>();

		//Constructor for MyMarket
		public MyMarket(Market m) {
			this.mkt = m;
			
			//start all markets with all unknown inventory status for each item
			//update as orders are placed and info is gleaned
			inventoryStatus.put("Steak",   MarketInventoryStatus.unknown);
			inventoryStatus.put("Chicken", MarketInventoryStatus.unknown);
			inventoryStatus.put("Salad",   MarketInventoryStatus.unknown);
			inventoryStatus.put("Pizza",   MarketInventoryStatus.unknown);
		}
		//method to set an inventory status in map
		public void setInventoryStatusFor(String food, MarketInventoryStatus status) {
			inventoryStatus.put(food, status);
		}
	}
	
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
	public void msgHereIsAnOrder(Waiter waiter, int tableNum, String choice) {
		orders.add(new Order(waiter, tableNum, choice));
		stateChanged();
	}
	
	/** Message from Market of food delivery */
	public void msgFoodDelivery(Market m, String foodType, int amount) {
		//if order is empty (market is out of stock), change market status
		if (amount == 0) {
			for (MyMarket myMarket: markets) {
				if (myMarket.mkt == m) {
					myMarket.setInventoryStatusFor(foodType, MarketInventoryStatus.out);
					break;
				}
			}
		}
		//add received food to inventory
		else {
			synchronized(inventory) {
				inventory.get(foodType).addToInventory(amount);
			}
		}
		marketOrderIsPlaced = false;
		stateChanged();
	}
	
	// *** SCHEDULER ***
	//
	/** Scheduler. Determine what action is called for, and do it. */
	protected boolean pickAndExecuteAnAction() {
		//if there exists food in inventory such that the amount is 0, order more
		FoodData foodTemp = null;
		synchronized(inventory) {
			for (FoodData foodData : inventory.values()) {
				//debug: System.out.println(foodData.amount);
				if (foodData.amount == 0 && !marketOrderIsPlaced) 
				{
					foodTemp = foodData;
					break;
				}
			}
		}
		if (foodTemp != null) {
			int randomNum = (rand.nextInt(5) + 1);
			orderMoreFromMarket(foodTemp.type, randomNum); //order 1-5 more
			return true;
		}
		
		// If there exists an order o whose status is done, place o.
		Order temp = null;
		synchronized(orders) {
			for (Order o : orders) {
				if (o.status == OrderStatus.done) {
					temp = o;
					break;
				}
			}
		}
		if (temp != null) {
			placeOrder(temp);
			return true;
		}
		
		// If there exists an order o whose status is pending, cook o.
		temp = null;
		synchronized(orders) {
			for (Order o : orders) {
				if (o.status == OrderStatus.pending) {
					temp = o;
					break;
				}
			}
		}
		if (temp != null) {
			cookOrder(temp);
			return true;
		}

		// we have tried all our rules (in this case only one) and found
		// nothing to do. So return false to main loop of abstract agent
		// and wait.
		return false;
	}

	// *** ACTIONS ***
	//
	/**
	 * Starts a timer for the order that needs to be cooked.
	 * 
	 * @param order
	 */
	private void cookOrder(Order order) {
		DoCooking(order); //<< print happens there
		order.status = OrderStatus.cooking;
	}

	private void placeOrder(Order order) {
		DoPlacement(order); //<< print happens there
		order.waiter.msgOrderIsReady(order.tableNum, order.food);
		orders.remove(order);
	}
	
	/** Order more food from market */
	private void orderMoreFromMarket(String foodType, int amount) {
		//check that markets have been set
		if (markets.size()==0) {
			return;
		}
		//find market to order from that has food stocked
		for (MyMarket market: markets) {
			//if market is not out of that type of food
			if (market.inventoryStatus.get(foodType) != MarketInventoryStatus.out) {
				System.out.println(this+": ordered "+amount+" of "+foodType+" from "+market.mkt);
				market.mkt.msgOrderFood(foodType, amount, csr, this);
				marketOrderIsPlaced = true;
				return;
			}
		}
		//if there are no markets out...retry random to see if market has replenished
		int randomNum = rand.nextInt(10);
		System.out.println(this+": ordered "+amount+" of "+foodType+" from random market");
		markets.get(randomNum).mkt.msgOrderFood(foodType, amount, csr, this);
		marketOrderIsPlaced = true;
	}

	// *** EXTRA -- all the simulation routines***
	//
	/** Returns the name of the cook */
	public String getName() {
		return name;
	}
	
	public String toString() {
		return "cook " + name;
	}
	
	/** establish connection to cashier agent. */
	public void setCashier(Cashier c) {
		this.csr = c;
	}
	
	/** establish connection to market agents */
	public void setMarkets(Vector<MarketAgent> ms) {
		for (MarketAgent m: ms) {
			this.markets.add(new MyMarket(m));
		}
	}

	private void DoCooking(final Order order) {
		print("Cooking: " + order + " for table:" + (order.tableNum + 1));
		// put it on the grill. gui stuff
		order.food = new Food(order.choice.substring(0, 2), new Color(0, 255,
				255), restaurant);
		order.food.cookFood();

		timer.schedule(new TimerTask() {
			public void run() {// this routine is like a message reception
				order.status = OrderStatus.done;
				stateChanged();
			}
		}, (int) (inventory.get(order.choice).cookTime * 1000));
	}

	public void DoPlacement(Order order) {
		print("Order finished: " + order + " for table:" + (order.tableNum + 1));
		order.food.placeOnCounter();
	}
}
