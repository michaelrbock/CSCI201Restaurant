package restaurant;

public class Bill {
	
	double grandTotal;
	String item;
	CustomerAgent cmr;
	MarketAgent mkt; //TODO: include this
	double amountReceived;
	double change;
	BillState status; //default = unpaid
	double hoursNeeded; //default = 0
	
	public enum BillState {
		unpaid, paidInFull, underPaid, receiptGiven
	};
	
	//Constructor for a Customer's Bill
	public Bill(CustomerAgent c) {
		cmr = c;
		hoursNeeded = 0;
		sharedConstructor();
	}
	
	//Constructor for a Bill to pay to Market
	//Create new bill from bill
	public Bill(MarketAgent m, Bill b) {
		//this.
		mkt = m;
		grandTotal = b.grandTotal;
		item = b.item;
		sharedConstructor();
	}
	
	//used by constructors
	private void sharedConstructor() {
		status = BillState.unpaid;
	}
}
