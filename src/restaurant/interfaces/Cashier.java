package restaurant.interfaces;

import restaurant.Bill;

public interface Cashier {
		/** Message from Customer for payment */
		public void msgPayment(Customer c, Bill b, double cash);
		
		/** Message to accept bill from Market 
		 *  Market has already set self as MarketAgent */
		public void msgBillFromMarket(Market m, double cost, String type);
		
		/** Message from waiter to get bill */
		public void msgNeedBillForCustomer(Waiter w, Customer c, String order);
		
		/** Message from Customer if needs to work */
		public void msgWillWorkFor(Customer c, Bill b, double hours);

		
		// *** EXTRA ***
		public String getName();
		
		public String toString();
}
