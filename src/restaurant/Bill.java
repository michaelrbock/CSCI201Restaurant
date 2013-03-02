package restaurant;

import restaurant.interfaces.*;

public class Bill {
	
	double grandTotal;
	String item;
	Customer cmr;
	Market mkt;
	Waiter wtr;
	double amountReceived;
	double change;
	BillState status; //default = unpaid
	double hoursNeeded; //default = 0
	
	public enum BillState {
		unpaidAndNotSent, unpaidAndSent, paidInFull, underPaid, receiptGiven, paidByWork
	};
	
	//Constructor for a Customer's Bill
	public Bill(Customer c, Waiter w, double price, String choice) {
		cmr = c;
		wtr = w;
		grandTotal = price;
		item = choice;
		hoursNeeded = 0;
		status = BillState.unpaidAndNotSent;
	}
	
	//Constructor for a Bill to pay to Market
	//Create new bill from bill
	public Bill(Market m, double total, String it) {
		//this.
		mkt = m;
		grandTotal = total;
		item = it;
		status = BillState.unpaidAndSent;
	}
}
