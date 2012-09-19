package cma.store.control.teamplaninng;

import java.util.List;

import cma.store.control.mvc.IMVCController;
import cma.store.control.opt.BotProdLocation;
import cma.store.data.Bot;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;

/**
Warehouse optimizer.
creating date: 24-07-2012
creating time: 09:42:10
autor: adam
 */

public interface TeamPlaninngStrategy {
	/**
	 * Starategy implementation for team planning. Assign request to bot.
	 * @param bots
	 * @param prodLocations
	 * @param used
	 * @return Object represented best association bot and request
	 */
	public BotProdLocation getCarProdLocation(List<Bot> bots, List<LocPriority> prodLocations, boolean[][] used);
	public void assingRequestToBots(List<BaseRequest> requests,
			List<Bot> availableCars);
	public void setMVCController(IMVCController mvcController);
}