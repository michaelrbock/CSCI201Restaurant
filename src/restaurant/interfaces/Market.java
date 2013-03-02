package restaurant.interfaces;

import java.util.HashMap;
import java.util.Map;

public interface Market {
	
		public Map<String, Double> prices = new HashMap<String, Double>(4);
		
		public enum OrderState {
			received, billed, completed, unfulfilled
		};
		
		/** Message from Cashier with payment for Bill */
		public void msgPayMarketBill(Cashier c, String item, double payment);
		
		/** Message from Cook with new order */
		public void msgOrderFood(String type, int amount, Cashier csr, Cook cook);
		
		// *** EXTRA ***
		public String getName();
		
		public String toString();
}
