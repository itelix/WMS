package cma.store.control.teamplaninng;

import java.util.ArrayList;
import java.util.List;

import cma.store.data.Bot;
import cma.store.data.Route;
import cma.store.input.request.BaseRequest;

public class PlannedRequest {
	List<Route> routes= new ArrayList<Route>();
	Bot bot;
	BaseRequest baseRequest;
	
	public List<Route> getRoutes() {
		return routes;
	}
	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}
	public Bot getBot() {
		return bot;
	}
	public void setBot(Bot bot) {
		this.bot = bot;
	}
	public BaseRequest getBaseRequest() {
		return baseRequest;
	}
	public void setBaseRequest(BaseRequest baseRequest) {
		this.baseRequest = baseRequest;
	}
	
}
