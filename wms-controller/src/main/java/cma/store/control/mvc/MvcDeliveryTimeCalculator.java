package cma.store.control.mvc;

import cma.store.data.Mvc;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.schedule.ScheduleItem;

public class MvcDeliveryTimeCalculator {
	
	private Environment env;
	
	public MvcDeliveryTimeCalculator( Environment env ){
		this.env = env;
	}
	
	public long getMinimalTimeToDeliver( BaseRequest firstNewPalettRequest ){
		
		int order = firstNewPalettRequest.getOrderId();
		Mvc mvc = firstNewPalettRequest.getMvc();
		
		long maxTime = 0;
		
		for( ScheduleItem si: env.getSchedule().getItems() ){
			BaseRequest br = si.getBaseRequest();
			if( br==null ) continue;
			if( br.getOrderId()>=order ) continue;
			
			Mvc mvc1 = br.getMvc();
			if( mvc1==null ) continue;
			if( mvc1!=mvc ) continue;
			
			if( si.getRoute().getFinalTime() <= maxTime ) continue;
			maxTime = si.getRoute().getFinalTime(); 
		}
		
		return maxTime;
	}
	

}
