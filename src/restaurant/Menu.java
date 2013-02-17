package restaurant;

import java.util.HashMap;
import java.util.Map;


public class Menu {

	public Map<String,Double> choicesMap = new HashMap<String,Double>(4);

	public String choices[] = new String[]
			{ "Steak"  ,
			"Chicken", 
			"Salad"  , 
			"Pizza"  };

	public double prices[] = new double[] {15.99, 10.99, 5.99, 8.99};
	
	public Menu() {
		choicesMap.put("Steak",   15.99);
		choicesMap.put("Chicken", 10.99);
		choicesMap.put("Salad",    5.99);
		choicesMap.put("Pizza",    8.99);
	}
}

