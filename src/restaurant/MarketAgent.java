package restaurant;

import java.util.ArrayList;
import restaurant.interfaces.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.Agent;

public class MarketAgent extends Agent implements Market {
	
	// *** DATA ***
	//
	private class Order {
		public int amount;
		public String type;
		public OrderState status; //default = received
		public Cashier csr;
		public Cook cook;
		double grandTotal;
		double amountReceived;
		
		//Order constructor
		public Order(String t, int a, Cashier c, Cook ck) {
			type = t;
			amount = a;
			csr = c;
			cook = ck;
			//calculates cost of order
			grandTotal = amount*prices.get(t);
			status = OrderState.received;
		}
	}
	
	public Map<String, Double> prices = new HashMap<String, Double>(4);
	
	public enum OrderState {
		received, billed, completed, unfulfilled
	};
	
	private List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());
	
	//Map food type (string) to amount in stock
	private Map<String, Integer> inventory = new HashMap<String, Integer>(4);
	
	//Market's name
	private String name;
	
	//Constructor for MarketAgent
	public MarketAgent(String n) {
		super();
		
		name = n;
		inventory.put("Steak",   15);
		inventory.put("Chicken", 10);
		inventory.put("Salad",   5);
		inventory.put("Pizza",   8);
		
		prices.put("Steak",   8.00);
		prices.put("Chicken", 4.00);
		prices.put("Salad",   1.00);
		prices.put("Pizza",   3.00);
	}
	
	// *** MESSAGES ***
	//
	/** Message from Cashier with payment for Bill */
	public void msgPayMarketBill(Cashier c, String item, double payment) {
		//for order o such that o.bill = b
		Order temp = null;
		synchronized(orders) {
			for (Order o: orders) {
				if (o.type == item && 
					o.grandTotal == payment &&
					o.csr == c)
				{
					temp = o;
					break;
				}
			}
		}
		if (temp != null) {
			temp.amountReceived = payment;
			temp.status = OrderState.completed;
			stateChanged();
		}
	}
	
	/** Message from Cook with new order */
	public void msgOrderFood(String type, int amount, Cashier csr, Cook cook) {
		orders.add(new Order(type,amount, csr, cook)); //create new received order
		stateChanged();
	}
	
	// *** SCHEDULER ***
	//
	@Override
	protected boolean pickAndExecuteAnAction() {
		//if there exists Order o such that o.status=received and inventory has enough 
		//to fulfill the order
		Order tempSend = null;
		Order tempDecline = null;
		synchronized(orders) {
			for (Order o: orders) {
				if (o.status == OrderState.received) {
					if (inventory.get(o.type) >= o.amount) {
						tempSend = o;
					}
					//else if there exists Order o such that o.status=received and 
					//there is not enough inventory
					else if (inventory.get(o.type) < o.amount) {
						tempDecline = o;
					}
					break;
				}
			}
		}
		if (tempSend != null) {
			sendBill(tempSend);
			return true;
		}
		else if (tempDecline != null) {
			declineOrder(tempDecline);
			return true;
		}
		
		//if there exists Order o such that o.status=billed
		Order temp = null;
		synchronized(orders) {
			for (Order o: orders) {
				if (o.status == OrderState.billed) {
					temp = o;
					break;
				}
			}
		}
		if (temp != null) {
			sendOrder(temp);
			return true;
		}
		
		return false;
	}

	// *** ACTIONS ***
	//
	/** Sends bill for order received from cook to cashier */
	private void sendBill(Order o) {
		o.csr.msgBillFromMarket(this, o.grandTotal, o.type);
		o.status = OrderState.billed;
		System.out.println(this+": sent bill to "+o.csr);
	}
	
	/** Sends notice that order cannot be completed to cook */
	private void declineOrder(Order o) {
		//food delivery with 0 amount signifies declined order
		o.cook.msgFoodDelivery(this, o.type, 0);
		o.status = OrderState.unfulfilled;
		System.out.println(this+": declined order (out of inventory) for "+o.type+" to "+o.cook);
	}
	
	/** Fulfill order to Cook */
	private void sendOrder(Order o) {
		o.cook.msgFoodDelivery(this, o.type, o.amount);
		o.status = OrderState.completed;
		System.out.println(this+": sent order of "+o.amount+" "+o.type+"(s) to "+o.cook);
	}
	
	// *** EXTRA ***
	public String getName() {
		return name;
	}
	
	public String toString() {
		return "market " + name;
	}
}
