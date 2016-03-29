package metric;

import java.util.HashMap;

public class CountMap extends HashMap<Integer,Integer>{

	public CountMap(){
		super();
	}
	
	public void increment(int index){
		Integer previousValue = get(index);
		if(previousValue==null){
			put(index,1);
		}else{
			put(index,previousValue+1);
		}
	}
}
