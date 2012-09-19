package cma.store.control.opt.data;

import java.util.List;

import cma.store.data.Mvc;
import cma.store.data.Pos;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;

/**
Warehouse optimizer.
creating date: 2012-05-17
creating time: 19:38:31
autor: Czarek
 */

public class Request {
	
	private RequestType type;
	
	private Pos from;
	private Pos to;
	private List<LocPriority> prodLocPtiority; //
	private Long fromTime;
	private Long toTime;
	private BaseRequest baseRequest;
	private long stayAtFinal;

	public Request( RequestType type, Long fromTime, Pos from, Long toTime, Pos to, long stayAtFinal, BaseRequest baseRequest){
		this(type, fromTime, from, toTime, to, null, stayAtFinal, baseRequest);
	}

	
	public Request( RequestType type, Long fromTime, Pos from, Long toTime, Pos to, List<LocPriority> prodLocPtiority, long stayAtFinal, BaseRequest baseRequest){
		this.type = type;
		this.fromTime = fromTime;
		this.from = from;
		this.toTime = toTime;
		this.to = to;
		this.prodLocPtiority = prodLocPtiority;
		this.stayAtFinal = stayAtFinal;
		this.baseRequest = baseRequest;
	}


	public Pos getFrom() {
		return from;
	}

	public Pos getTo() {
		return to;
	}
	
	public List<LocPriority> getProdLocPtiority() {
		return prodLocPtiority;
	}

	public Long getFromTime() {
		return fromTime;
	}

	public Long getToTime() {
		return toTime;
	}

	public RequestType getType() {
		return type;
	}
	

	public long getStayAtFinal() {
		return stayAtFinal;
	}

	public BaseRequest getBaseRequest() {
		return baseRequest;
	}

	public void setBaseRequest(BaseRequest b) {
		baseRequest = b;
	}

	public Mvc getMvc() {
		if( baseRequest==null ) return null;
		return baseRequest.getMvc();
	}

	
}
