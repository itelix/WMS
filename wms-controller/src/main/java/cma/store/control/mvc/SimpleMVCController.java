package cma.store.control.mvc;

import java.util.HashMap;


public class SimpleMVCController implements IMVCController {
	
	/**
	 * Int - order ID Long - time
	 */
	HashMap<Integer, Long> timeRequests = new HashMap<Integer, Long>();

	public void addTimeForOrder(Long time, Integer orderId){
		timeRequests.put(orderId, time);
	}
	
	public Long getMVCTimeForOrder(Integer orderId){
		return timeRequests.get(orderId);
	}
}