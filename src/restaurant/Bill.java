package restaurant;

public class Bill {
	
	double grandTotal;
	String item;
	CustomerAgent cmr;
	MarketAgent mkt;
	WaiterAgent wtr;
	double amountReceived;
	double change;
	BillState status; //default = unpaid
	double hoursNeeded; //default = 0
	
	public enum BillState {
		unpaidAndNotSent, unpaidAndSent, paidInFull, underPaid, receiptGiven, paidByWork
	};
	
	//Constructor for a Customer's Bill
	public Bill(CustomerAgent c, WaiterAgent w, double price, String choice) {
		cmr = c;
		wtr = w;
		grandTotal = price;
		item = choice;
		hoursNeeded = 0;
		status = BillState.unpaidAndNotSent;
	}
	
	//Constructor for a Bill to pay to Market
	//Create new bill from bill
	public Bill(MarketAgent m, double total, String it) {
		//this.
		mkt = m;
		grandTotal = total;
		item = it;
		status = BillState.unpaidAndSent;
	}
}
