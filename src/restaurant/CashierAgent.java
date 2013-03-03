package restaurant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import restaurant.Bill.*;
import restaurant.interfaces.*;
import agent.Agent;

public class CashierAgent extends Agent implements Cashier {
	
	// *** DATA ****
	//
	Menu menu = new Menu();
	private List<Bill> customerBills = Collections.synchronizedList(new ArrayList<Bill>());
	private List<Bill> marketBills = Collections.synchronizedList(new ArrayList<Bill>());

	public CashierAgent() {
		super();
	}
	
	// *** MESSAGES ***
	//
	/** Message from Customer for payment */
	public void msgPayment(Customer c, Bill b, double cash) {
		//for bill in customerBills such that b=bill
		Bill bill = null;
		synchronized(customerBills) {
			for (Bill temp: customerBills) {
				if (temp.grandTotal == b.grandTotal &&
					temp.cmr == c &&
					temp.item == b.item)
				{
					bill = temp;
					break;
				}
			}
		}
		if (bill != null) {
			bill.amountReceived = cash;
			if (bill.amountReceived < bill.grandTotal) {
				bill.status = BillState.underPaid;
				//calculate hours to work @ $8 per hour (CA min wage)
				bill.hoursNeeded = (bill.grandTotal-bill.amountReceived) / 8;
			}
			else {
				bill.status = BillState.paidInFull;
				bill.change = bill.amountReceived - bill.grandTotal;
			}
		}
		stateChanged();
	} //end msgPayment
	
	/** Message to accept bill from Market 
	 *  Market has already set self as MarketAgent */
	public void msgBillFromMarket(Market m, double cost, String type) {
		marketBills.add(new Bill(m, cost, type));
		stateChanged();
	}
	
	/** Message from waiter to get bill */
	public void msgNeedBillForCustomer(Waiter w, Customer c, String order) {
		//create bill for customer
		Bill newBill = new Bill(c, w, menu.choicesMap.get(order), order);
		customerBills.add(newBill); //still must send back to waiter
		stateChanged();
	}
	
	/** Message from Customer if needs to work */
	public void msgWillWorkFor(Customer c, Bill b, double hours) {
		//find waiter's bill and update info
		//for bill in customerBills such that b=bill
		Bill bill = null;
		synchronized(customerBills) {
			for (Bill temp: customerBills) {
				if (temp.grandTotal == b.grandTotal &&
						temp.cmr == c &&
						temp.item == b.item) 
				{
					bill = temp;
					break;
				}
			}
		}
		if (bill != null) {
			bill.hoursNeeded = hours;
			bill.status = BillState.paidByWork;
		}
		stateChanged();
	}
	
	// *** SCHEDULER ***
	//
	@Override
	protected boolean pickAndExecuteAnAction() {
		//debug: System.out.println("In cashier scheduler");
				
		//if there exists Bill b in customerBills such that b.status=unpaidAndNotSent
		Bill temp = null;
		synchronized(customerBills) {
			for (Bill b: customerBills) {
				if (b.status == BillState.unpaidAndNotSent) {
					temp = b;
					break;
				}
			}
		}
		if (temp != null) {
			sendBillToWaiter(temp);
			return true;
		}
		
		//if there exists Bill b in customerBills such that b.status=paidInFull
		temp = null;
		synchronized(customerBills) {
			for (Bill b: customerBills) {
				if (b.status == BillState.paidInFull) {
					temp = b;
					break;
				}
			}
		}
		if (temp != null) {
			sendReceipt(temp);
			return true;
		}
		
		//if there exists Bill b in customerBills such that b.status=underPaid
		temp = null;
		synchronized(customerBills) {
			for (Bill b: customerBills) {
				if (b.status == BillState.underPaid) {
					temp = b;
					break;
				}
			}
		}
		if (temp != null) {
			assignCustomerToWork(temp);
			return true;
		}
		
		//if there exists Bill b in marketBills such that b.status = unpaid
		temp = null;
		synchronized(marketBills) {
			for (Bill b: marketBills) {
				if (b.status == BillState.unpaidAndSent) {
					temp = b;
					break;
				}
			}
		}
		if (temp != null) {
			payMarketBill(temp);
			return true;
		}
		
		return false;
	}
	
	// *** ACTIONS ***
	//
	/** Send bill to waiter to give to customer */
	private void sendBillToWaiter(Bill b) {
		System.out.println(this+": sent bill to "+b.wtr+" for "+b.cmr);
		b.wtr.msgHereIsBill(b);
		b.status = BillState.unpaidAndSent;
	}
	
	/** Send receipt (change) directly to customer after payment */
	private void sendReceipt(Bill b) {
		System.out.println(this+": sent receipt to "+b.cmr);
		b.cmr.msgThanks(b.change);
		b.status = BillState.receiptGiven;
	}
	
	/** Assign hours to work to make up for underpaid bill */
	private void assignCustomerToWork(Bill b) {
		System.out.println(this+": assigned "+b.cmr+" to "+b.hoursNeeded+" of work");
		b.cmr.msgNotEnoughMoneyMustWorkFor((double)Math.round(b.hoursNeeded * 100) / 100);
		b.status = BillState.receiptGiven;
	}
	
	/** Pay a market bill after cook orders food and market sends bill */
	private void payMarketBill(Bill b) {
		System.out.println(this+": paid $"+b.grandTotal+" bill to "+b.mkt+" for "+b.item);
		b.mkt.msgPayMarketBill(this, b.item, b.grandTotal);
		b.status = BillState.paidInFull;
	}
	
	// *** EXTRA ***
	public String getName() {
		return "cashier";
	}
	
	public String toString() {
		return "cashier";
	}
}
