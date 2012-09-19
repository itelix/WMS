package cma.store.data;

import java.util.List;

import cma.store.control.opt.data.Request;
import cma.store.utils.Conflict;
import cms.store.utils.Pair;

public class RouteSimple implements Route {

	Mvc mvc;
	long finalTime;
	Bot bot;
	
	public RouteSimple(){
		
	}
	
	public RouteSimple setMvc(Mvc mvc) {
		this.mvc = mvc;
		return this;
	}

	public RouteSimple setFinalTime(long finalTime) {
		this.finalTime = finalTime;
		return this;
	}
	
	public RouteSimple setBot(Bot bot) {
		this.bot = bot;
		return this;
	}

	@Override
	public Bot getBot() {
		return bot;
	}

	@Override
	public double getTotalDistance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTotalTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean exists(long time) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update(long time) {
		// TODO Auto-generated method stub

	}

	@Override
	public Pos getPos(long time, boolean nullWhenAfterRoute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getStartTime() {
		return 0;
	}

	@Override
	public long getFinalTime() {
		return finalTime;
	}

	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getRouteGroupId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setRouteGroupId(int routeGroupId) {
		// TODO Auto-generated method stub

	}

	@Override
	public Pos getFinalPos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Request getRequest() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<? extends Pos> getPos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPos(List<? extends Pos> pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Pair<Speed, Duration>> getSpeedDurList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pos getInitPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RouteType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setType(RouteType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public Double getPriority() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Conflict> getBlockers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String printRouteWithTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMvcRoute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Mvc getMvc() {
		// TODO Auto-generated method stub
		return null;
	}

}
