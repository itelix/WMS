package cma.store.exception;

import cma.store.input.request.BaseRequest;

/**
Warehouse optimizer.
creating date: 25-07-2012
creating time: 11:20:57
autor: adam
 */

public class NoFreeBotException extends SMBException {
	
	public BaseRequest baseRequest;
	private String reason="";
	
	public NoFreeBotException(BaseRequest baseRequest, String reason){
		this.baseRequest = baseRequest;
		this.baseRequest = baseRequest;
	}
	
	public NoFreeBotException(BaseRequest baseRequest) {
		this(baseRequest,"");
	}
	
	public String toString() {
		return "No free bot for Order("+baseRequest.getOrderId()+") need reschedule. " + reason;
	}
}