package cma.store.data;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import cma.store.control.opt.data.RequestType;
import cma.store.env.BaseEnvironment;
import cma.store.input.request.BaseRequestType;

public class MvcController {
	
	Logger log = Logger.getLogger(getClass());
	
	private MvcSlot slots[];
	private Hashtable<Route, MvcSlot> rout2slots; //remember 
	private Mvc mvc;
	private int slotCount;
	private long lastReserveTime;
	
	public MvcController( Mvc mvc ){
		this.mvc = mvc;
		this.slotCount = getSlotsCount();
		slots = new MvcSlot[this.slotCount];
		rout2slots = new Hashtable<Route, MvcSlot>();
		lastReserveTime = mvc.getFirstAvailableTime();
	}
	
	public long getAvailableTime() {
		return getAvailableTime(0);
	}

	
	/**
	 * Assuming that all older routes are already allocated.
	 * @param afterTime 
	 * @return
	 */
	public long getAvailableTime( long afterTime ) {
		
		if( afterTime < lastReserveTime ){
			afterTime = lastReserveTime;
		}
		
		long slotId = (getSlotId(afterTime)+1);
		long time = slotId * getSlotSize();
		if( slotId>=slotCount )  slotId%=slotCount;
		
		for(int i=0; i<slotCount; i++){
			
			if( slots[(int)slotId] == null ){
				return time;
			}
			time += getSlotSize();
			slotId++;
			if( slotId >=slotCount ){
				slotId=0;
			}
		}
		throw new RuntimeException("Overriden MVC slots. Set DEFAULT_MAX_SCHEDULING_TIME bigger." );
	}	

	public void reserveSlots( Route route ){
		//TODO it has to be modified for INPUT requests
		
		if( route.getRequest().getBaseRequest().getType() != BaseRequestType.OUTPUT ){
			throw new RuntimeException("Only OUTPUT base request type is supported now");
		}
		
		long time = route.getFinalTime();
		long id = getSlotId(time );
		int slotId = (int)( id % slots.length );
		slots[slotId] = new MvcSlot( time, slotId );
		rout2slots.put( route, slots[slotId] );
		
		lastReserveTime = time;
		
		log.debug(" Reserve slot " + slotId);
	}
	
	public void freeSlots( Route route ){
		
		if( route.getRequest().getBaseRequest().getType() != BaseRequestType.OUTPUT ){
			throw new RuntimeException("Only OUTPUT base request type is supported now");
		}
		
		MvcSlot slot = rout2slots.get( route );
		slots[slot.slotNr] = null;
		
		log.debug(" Free slot " + slot.slotNr );
	}
	
	
	private int getSlotsCount(){
		long size  = BaseEnvironment.DEFAULT_MAX_SCHEDULING_TIME / mvc.getTimeBetweenShells();
		if( size>Integer.MAX_VALUE ){
			throw new RuntimeException("Can't allocate so many MVC slots. Set DEFAULT_MAX_SCHEDULING_TIME smaller." );
		}
		return (int)(size);
	}
	

	protected long getSlotSize(){
		return mvc.getTimeBetweenShells();
	}

	protected long getFirstAvailableTime(){
		return mvc.getFirstAvailableTime();
	}
	
	/**
	 * Slot to which this time belongs
	 * @param time
	 * @return
	 */
	private long getSlotId( long time ){
		long slotId = (time-getFirstAvailableTime())/getSlotSize();
		return slotId;
	}
	
	public String toString(){
		return ""+mvc;
	}


}
