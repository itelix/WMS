package cma.store.input.request;

import java.util.List;

import org.apache.log4j.Logger;

import cma.store.control.opt.BotProdLocation;
import cma.store.control.opt.RequestRealization;
import cma.store.data.Mvc;
import cma.store.data.Pos;
import cms.store.utils.PositionUtils;

/**
Warehouse optimizer.
creating date: 2012-07-17
creating time: 18:43:47
autor: Czarek
 */

public class BaseRequest {
	Logger log = Logger.getLogger(getClass());
	
	private int orderId;
	private int queueId;

	private Mvc mvc;
	//private Pos destinationMvc;
	private List<LocPriority> storgeLocations;

	private BotProdLocation carProdLocation;
	private RequestRealization requestRealization;
	/**
	 * When request need reshedule this is new time to launch
	 * XXX to move in other place in future
	 */
	private long timeToReschedule;
	private boolean requestPlanned;
	
	private BaseRequestType type;

	
	public BaseRequest(int orderId, Mvc mvc, List<LocPriority> storgeLocations, int queueId) {
		super();
		this.orderId = orderId;
		this.mvc = mvc;
		this.storgeLocations = storgeLocations;
		this.queueId = queueId;
		type = BaseRequestType.OUTPUT;
		
		String message = "";
		for (LocPriority locPriority : storgeLocations) {
			message += "["+PositionUtils.getIntX(locPriority.getPos().x)+":";
			message += PositionUtils.getIntY(locPriority.getPos().y)+"]";
		}
		
		log.debug(
			"Creating order("+orderId+") with starting position at ("+message+")"
		);
	}

	
	public BaseRequest(int orderId, Mvc mvc, List<LocPriority> storgeLocations) {
		this(orderId,mvc,storgeLocations,0);
	}
	
	public String toString(){
		return ""+mvc;
	}

	
	public Mvc getMvc() {
		return mvc;
	}
	
	public int getMvcLocation(){
		return 0;
	}
	
	public Pos getDestination() {
		return mvc.getPos();
	}
	
	public List<LocPriority> getStorageLocations() {
		return storgeLocations;
	}

	public int getOrderId() {
		return orderId;
	}

//	public BotProdLocation getCarProdLocation() {
//		return carProdLocation;
//	}
//
//	public void setCarProdLocation(BotProdLocation carProdLocation) {
//		this.carProdLocation = carProdLocation;
//	}

	public RequestRealization getRequestRealization() {
		return requestRealization;
	}

	public void setRequestRealization(RequestRealization requestRealization) {
		this.requestRealization = requestRealization;
	}

	public long getTimeToReschedule() {
		return timeToReschedule;
	}

	public void setTimeToReschedule(long timeToReschedule) {
		this.timeToReschedule = timeToReschedule;
	}

	public boolean isRequestPlanned() {
		return requestPlanned;
	}

	public void setRequestPlanned(boolean requestPlanned) {
		this.requestPlanned = requestPlanned;
	}

	public int getQueueId() {
		return queueId;
	}


	public BaseRequestType getType() {
		return type;
	}
	

}
