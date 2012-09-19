package cma.store.control.teamplaninng;

import java.util.List;

import cma.store.control.opt.BotProdLocation;
import cma.store.data.Bot;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;



/**
Warehouse optimizer.
creating date: 24-07-2012
creating time: 09:49:15
autor: adam
 */

public class TeamPlaninngContext {
	
	private TeamPlaninngStrategy teamPlaninngStrategy;

	public TeamPlaninngStrategy getTeamPlaninngStrategy() {
		return teamPlaninngStrategy;
	}

	public void setTeamPlaninngStrategy(TeamPlaninngStrategy teamPlaninngStrategy) {
		this.teamPlaninngStrategy = teamPlaninngStrategy;
	}
	
	public TeamPlaninngContext(TeamPlaninngStrategy strategy) {
		this.teamPlaninngStrategy = strategy;
	}
	
	public BotProdLocation findCarProdLocation(List<Bot> bots, List<LocPriority> prodLocations, boolean[][] used) {
		return teamPlaninngStrategy.getCarProdLocation(bots, prodLocations,used);
	}
	
	public void assingRequestToBots(List<BaseRequest> requests,
			List<Bot> availableCars) {
		this.teamPlaninngStrategy.assingRequestToBots(requests, availableCars);
	}
}