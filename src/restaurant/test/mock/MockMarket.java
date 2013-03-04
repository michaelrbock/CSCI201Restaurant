package restaurant.test.mock;
import restaurant.interfaces.*;

public class MockMarket extends MockAgent implements Market {
	
	//log
	public EventLog log = new EventLog();

	public MockMarket(String name) {
		super(name);
	}

	@Override
	public void msgPayMarketBill(Cashier c, String item, double payment) {
		log.add(new LoggedEvent("received payment of $"+Double.toString(payment)+" from "+c+" for "+item));
	}

	@Override
	public void msgOrderFood(String type, int amount, Cashier csr, Cook cook) {
		//do not need to actually send back food, but do need to send bill to cashier
		//arbitrary cost of $5.00 per item
		double cost = amount * 5.0;
		csr.msgBillFromMarket(this, cost, type);
	}
	
}
