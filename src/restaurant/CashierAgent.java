package restaurant;

import java.util.ArrayList;
import java.util.List;

import restaurant.Bill.BillState;

import agent.Agent;

public class CashierAgent extends Agent {
	
	// *** DATA ****
	//
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
	//
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
	 *  Market has already set self as MarketAgent */
	public void msgBillFromMarket(MarketAgent m, double cost, String type) {
		marketBills.add(new Bill(m, cost, type));
		stateChanged();
	}
	
	/** Message from waiter to get bill */
	public void msgNeedBillForCustomer(WaiterAgent w, CustomerAgent c, String order) {
		//create bill for customer
		Bill newBill = new Bill(c, w, menu.choicesMap.get(order), order);
		customerBills.add(newBill); //still must send back to waiter
		stateChanged();
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
	//
	@Override
	protected boolean pickAndExecuteAnAction() {
		//if there exists Bill b in customerBills such that b.status=unpaidAndNotSent
		for (Bill b: customerBills) {
			if (b.status == BillState.unpaidAndNotSent) {
				sendBillToWaiter(b);
				return true;
			}
		}
		//if there exists Bill b in customerBills such that b.status=paidInFull
		for (Bill b: customerBills) {
			if (b.status == BillState.paidInFull) {
				sendReceipt(b);
				return true;
			}
		}
		//if there exists Bill b in customerBills such that b.status=underPaid
		for (Bill b: customerBills) {
			if (b.status == BillState.underPaid) {
				assignCustomerToWork(b);
				return true;
			}
		}
		//if there exists Bill b in marketBills such that b.status = unpaid
		for (Bill b: marketBills) {
			if (b.status == BillState.unpaidAndSent) {
				payMarketBill(b);
				return true;
			}
		}
		
		return false;
	}
	
	// *** ACTIONS ***
	//
	/** */
	private void sendBillToWaiter(Bill b) {
		b.wtr.msgHereIsBill(b);
		b.status = BillState.unpaidAndSent;
	}
	
	/** */
	private void sendReceipt(Bill b) {
		b.cmr.msgThanks(b.change);
		b.status = BillState.receiptGiven;
	}
	
	/** */
	private void assignCustomerToWork(Bill b) {
		b.cmr.msgNotEnoughMoneyMustWorkFor(b.hoursNeeded);
		b.status = BillState.receiptGiven;
	}
	
	/** */
	private void payMarketBill(Bill b) {
		b.mkt.msgPayMarketBill(this, b.item, b.grandTotal);
		b.status = BillState.paidInFull;
	}
}
