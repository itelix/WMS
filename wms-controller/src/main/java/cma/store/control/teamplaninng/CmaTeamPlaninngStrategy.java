package cma.store.control.teamplaninng;

import java.util.List;

import cma.store.control.mvc.IMVCController;
import cma.store.control.opt.BotProdLocation;
import cma.store.data.Bot;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;

/**
Warehouse optimizer.
creating date: 2012-07-26
creating time: 18:17:23
autor: Czarek
 */

public class CmaTeamPlaninngStrategy implements TeamPlaninngStrategy {

	@Override
	public void assingRequestToBots(List<BaseRequest> requests, List<Bot> availableCars) {
		// TODO Auto-generated method stub

	}

	@Override
	public BotProdLocation getCarProdLocation(List<Bot> bots, List<LocPriority> prodLocations, boolean[][] used) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMVCController(IMVCController mvcController) {
		// TODO Auto-generated method stub
		
	}

}
