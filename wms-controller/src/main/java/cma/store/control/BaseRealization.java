package cma.store.control;

import java.util.ArrayList;
import java.util.List;

import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.schedule.ScheduleItem;

public class BaseRealization {
	
	private BaseRequest baseRequest;
	private long minimalEndTime;
	private long minimalStartTime;
	private int layer;   //layer on which task should be done
	
	private List<Route> baseRoutes;
	private List<Route> otherRoutes;
	
	//private List<BotProdLocation> carProdLocation;
	
	private List<ScheduleItem> items;
	
	private List<LocPriority> prodLocPtiority;
	
	private Bot bot;
	
	private long botAvailable;
	
	private Pos botStartPosition;
	
	private double destinationPriority;
	
	private int routesGroupId;

	
	public BaseRealization( BaseRequest baseRequest ){
		this.baseRequest = baseRequest;
		destinationPriority=0;
		this.minimalEndTime = baseRequest.getTimeToReschedule();
		clear();
	}
	
	public void clear(){
		items = new ArrayList<ScheduleItem>();
		baseRoutes = new ArrayList<Route>();
		otherRoutes = new ArrayList<Route>();
	}
	
	public double getDestinationPriority() {
		return destinationPriority;
	}

	public void setDestinationPriority(double destinationPriority) {
		this.destinationPriority = destinationPriority;
	}

	public void addItem( ScheduleItem si, boolean isBase ){
		if( isBase ){
			baseRoutes.add( si.getRoute() );
		}else{
			otherRoutes.add( si.getRoute() );
		}
		items.add( si );
	}
	
	public void removeItem( ScheduleItem si, boolean isBase ){
		if( isBase ){
			baseRoutes.remove( si.getRoute() );
		}else{
			otherRoutes.remove( si.getRoute() );
		}
		items.remove( si );
	}
	
	public List<ScheduleItem> getItems() {
		return items;
	}
	
	public Bot getBot() {
		return bot;
	}
	
	public List<LocPriority> getProdLocPtiority() {
		return prodLocPtiority;
	}

	public void setProdLocPtiority(List<LocPriority> prodLocPtiority) {
		this.prodLocPtiority = prodLocPtiority;
	}

	public Pos getBotStartPosition() {
		return botStartPosition;
	}

	public void setBotStartPosition(Pos botStartPosition) {
		this.botStartPosition = botStartPosition;
	}

	public void setBot(Bot bot) {
		this.bot = bot;
	}

	
	public long getBotAvailable() {
		return botAvailable;
	}

	public void setBotAvailable(long botAvailable) {
		this.botAvailable = botAvailable;
	}

	public long getMinimalEndTime() {
		return minimalEndTime;
	}


	public void setMinimalEndTime(long minimalEndTime) {
		this.minimalEndTime = minimalEndTime;
	}


	public BaseRequest getBaseRequest() {
		return baseRequest;
	}


	public List<Route> getBaseRoutes() {
		return baseRoutes;
	}


	public List<Route> getOtherRoutes() {
		return otherRoutes;
	}


	public int getLayer() {
		return layer;
	}


	public void setLayer(int layer) {
		this.layer = layer;
	}

	public long getMinimalStartTime() {
		return minimalStartTime;
	}

	public void setMinimalStartTime(long minimalStartTime) {
		this.minimalStartTime = minimalStartTime;
	}

	public int getRoutesGroupId() {
		return routesGroupId;
	}

	public void setRoutesGroupId(int routesGroupId) {
		this.routesGroupId = routesGroupId;
	}

	

}
