package restaurant;

import java.util.ArrayList;
import java.util.List;

import restaurant.Bill.BillState;

import agent.Agent;

public class CashierAgent extends Agent {
	
	Menu menu = new Menu();
	private List<Bill> customerBills = new ArrayList<Bill>();
	private List<Bill> marketBills = new ArrayList<Bill>();
	
	//Name of Cashier
	private String name;

	public CashierAgent(String name) {
		super();
		
		this.name = name;
	}
	
	// *** MESSAGES ***
	/** Message from Customer for payment */
	public void msgPayment(CustomerAgent c, Bill b, double cash) {
		//for bill in customerBills such that b=bill
		for (Bill bill: customerBills) {
			if (bill.grandTotal == b.grandTotal &&
				bill.cmr == c &&
				bill.item == b.item) 
			{
				bill.amountReceived = cash;
				if (bill.amountReceived < bill.grandTotal) {
					bill.status = BillState.underPaid;
					//calculate hours to work @ $8 per hour (CA min wage)
					bill.hoursNeeded = (bill.grandTotal-bill.amountReceived) / 8;
					stateChanged();
				}
				else {
					bill.status = BillState.paidInFull;
					bill.change = bill.amountReceived - bill.grandTotal;
					stateChanged();
				}
				break;
			}
		}
	} //end msgPayment
	
	/** Message to accept bill from Market 
	 *  Market has already set self as MarketAgent 
	 */
	public void msgBillFromMarket(MarketAgent m, Bill b) {
		marketBills.add(new Bill(b.mkt, b));
		stateChanged();
	}
	
	/** Message from waiter to get bill
	 */
	public void msgNeedBillForCustomer(CustomerAgent c, String order) {
		//TODO: method stub
	}
	
	/** Message from Customer if needs to work */
	public void msgWillWorkFor(CustomerAgent c, Bill b, double hours) {
		//find waiter's bill and update info
		//for bill in customerBills such that b=bill
		for (Bill bill: customerBills) {
			if (bill.grandTotal == b.grandTotal &&
				bill.cmr == c &&
				bill.item == b.item) 
			{
				bill.hoursNeeded = hours;
				bill.status = BillState.paidInFull;
				stateChanged();
				break;
			}
		}
	}
	
	// *** SCHEDULER ***
	@Override
	protected boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		return false;
	}
	
	// *** ACTIONS ***
	
}
