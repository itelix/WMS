package cma.store.control.teamplaninng;

import java.util.List;

import cma.store.control.cost.TaskCostService;
import cma.store.control.cost.TaskCostServiceImpl;
import cma.store.control.mvc.IMVCController;
import cma.store.control.opt.BotProdLocation;
import cma.store.data.Bot;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;

public class HMPCStategy implements TeamPlaninngStrategy {

	Environment env;
	TaskCostService taskCostService = new TaskCostServiceImpl();
	
	public HMPCStategy(Environment env) {
		super();
		this.env = env;
	}

	@Override
	public BotProdLocation getCarProdLocation(List<Bot> bots,
			List<LocPriority> prodLocations, boolean[][] used) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void assingRequestToBots(List<BaseRequest> requests,
			List<Bot> availableCars) {
		requests.toString();

	}

	@Override
	public void setMVCController(IMVCController mvcController) {
		// TODO Auto-generated method stub

	}

}
