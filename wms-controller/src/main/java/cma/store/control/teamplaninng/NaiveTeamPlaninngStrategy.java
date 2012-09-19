package cma.store.control.teamplaninng;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cma.store.control.mvc.IMVCController;
import cma.store.control.opt.BotProdLocation;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.route.KirillRouteCreator;
import cma.store.control.opt.route.RouteCreator;
import cma.store.control.opt.route.allshortestpaths.FloydWarshallAlgorithm;
import cma.store.control.opt.route.allshortestpaths.ShortestPathsFinder;
import cma.store.control.utils.RoutUtils;
import cma.store.data.Bot;
import cma.store.data.Duration;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteImp;
import cma.store.data.Speed;
import cma.store.env.BaseEnvironment;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cms.store.utils.Pair;
import cms.store.utils.PositionUtils;

public class NaiveTeamPlaninngStrategy implements TeamPlaninngStrategy {
	Logger log = Logger.getLogger(getClass());
	ShortestPathsFinder pathsFinder = null;
	double speed = BaseEnvironment.DEFAULT_MAX_BOT_SPEED_MM_PER_MS;
	double deltaTime;
	double deltaDist;
	
	Environment environment;
	IMVCController mvcController;
	private RoutUtils routUtils;
	
	LinkedList<Double> nextFreeBotsList = new LinkedList<Double>();
	
	RouteCreator routeCreator;
	
	public LinkedList<Double> getNextFreeBotsList() {
		synchronized (nextFreeBotsList) {
			return nextFreeBotsList;		}
	}

	public void addTimeForNextFreeBot(Double time){
		synchronized (nextFreeBotsList) {
			nextFreeBotsList.add(time);
		}
	}
	
	public NaiveTeamPlaninngStrategy(Environment env) {
		environment = env;
		
		
		
		deltaDist = env.getLayerModel().getUnitSize();
		deltaTime = deltaDist/speed; //ms
		
		pathsFinder = new FloydWarshallAlgorithm(env);
		pathsFinder.computePaths();
		 
		routUtils = new RoutUtils(environment);
		routUtils.initBotRoutes();
		
		routeCreator = new KirillRouteCreator(environment);
	}
	
	public IMVCController getMvcController() {
		return mvcController;
	}

	public void setMvcController(IMVCController mvcController) {
		this.mvcController = mvcController;
	}

	@Override
	public BotProdLocation getCarProdLocation(List<Bot> bots,
			List<LocPriority> prodLocations, boolean[][] used) {
		// TODO Auto-generated met	hod stub
		return null;
	}

	@Override
	public void assingRequestToBots(List<BaseRequest> requests,
			List<Bot> availableCars) {
		
		log.info("Starting assign tasks time: "+environment.getTimeMs());
		
		
	
		List<Pair<Long, Pair<BaseRequest, Bot>>> timesForRequests = new ArrayList<Pair<Long,Pair<BaseRequest,Bot>>>();
		int requestCounter = 0;
//		List<BaseRequest> needReshedule = new LinkedList<BaseRequest>();
		long startTime = environment.getTimeMs()+100;
		
		for (BaseRequest baseRequest : requests) {
//			requestCounter++;
			if(requestCounter < availableCars.size() && !baseRequest.isRequestPlanned()) {
				requestCounter++;
				
				for (Bot bot : availableCars) {
					
					Pair<BaseRequest, Bot> requestBotPair = new  Pair<BaseRequest, Bot>(baseRequest,bot);
					Pos destinantionPos = baseRequest.getStorageLocations().get(0).getPos();
//					List<Pos> paths= pathsFinder.getPath(bot.getPos(),destinantionPos );
//					List<Pos> pathsBack = pathsFinder.getPath(destinantionPos,baseRequest.getDestination() );
//					log.debug(paths.toString());
//					log.debug(pathsBack.toString());
					long timeToReachProduct = 0;
//					double timeToReachProduct = pathsFinder.getPathLength(
//							bot.getPos(), destinantionPos)
//							+ pathsFinder.getPathLength(destinantionPos,
//									baseRequest.getDestination());
					
					startTime = environment.getTimeMs();
					timeToReachProduct += PositionUtils.getTimeToTrevel(
							bot.getPos(), destinantionPos, bot);

					Request r1 = new Request(null,startTime,bot.getPos(),timeToReachProduct,destinantionPos, 0, null );						
					Route route =  routeCreator.createRout( r1, bot, null, true);
					
					timeToReachProduct += PositionUtils.getTimeToTrevel(
							destinantionPos, baseRequest.getDestination(), bot);
					
					Request r2 = new Request(null,route.getFinalTime()+Environment.TIME_TO_PICK_UP_PRODUCT,bot.getPos(),timeToReachProduct,baseRequest.getDestination(), 0, null );						
					Route route2 = routeCreator.createRout( r2, bot, null, true );
					timeToReachProduct =route.getFinalTime()+ route2.getFinalTime();
					log.debug("TotalTime "
							+ timeToReachProduct
							+ " for Bot "
							+ bot.getId()
							+ " task "
							+ baseRequest.getOrderId()
							+ " demanding time "
							+ mvcController.getMVCTimeForOrder(baseRequest
									.getOrderId()));
					/*
					if (timeToReachProduct < bestTimeForRequest
							|| timeToReachProduct <= mvcController
									.getMVCTimeForOrder(baseRequest.getOrderId())) {
				// new best bot for handle this request
						bestPair.setT2(bot);
						bestTimeForRequest = timeToReachProduct;
						freeBotTime.put(bot, timeToReachProduct);
					}*/
					Pair<Long, Pair<BaseRequest, Bot>> timeResultPair = new Pair<Long, Pair<BaseRequest,Bot>>(timeToReachProduct, requestBotPair);
					timesForRequests.add(timeResultPair);
				}
			}
			/*
			bestTimeForRequestList.add(bestPair);
			for (Pair<BaseRequest, Bot> pair : bestTimeForRequestList) {
				CarProdLocation cp = new CarProdLocation(pair.getT2(),
						baseRequest.getStorageLocations().get(0).getPos());
				baseRequest.setCarProdLocation(cp);
			}*/
		}
//		timesForRequests.toString();
		double bestTime = Double.MAX_VALUE;
		Double prevBestTime = null;
		Pair<BaseRequest, Bot> bestPair = null;
		BaseRequest prev = null;
		int ordersCounter = 0;
		for (Pair<Long, Pair<BaseRequest, Bot>> pair : timesForRequests) {
			log.debug("Time " + pair.getT1() + " for order "
					+ pair.getT2().getT1().getOrderId() + " and Bot "
					+ pair.getT2().getT2().getId());
			
			if(!pair.getT2().getT1().equals(prev) && prev != null){
				ordersCounter++;
				
				// tzn ze jest nastepny task
				BotProdLocation cp = new BotProdLocation(bestPair.getT2(),
						bestPair.getT1().getStorageLocations().get(0).getPos(), null);
//				baseRequest.setCarProdLocation(cp);
//				bestPair.getT1().setCarProdLocation(cp);
				log.debug("Bot "+bestPair.getT2().getId()+ " Order: "+ bestPair.getT1().getOrderId() );
				bestPair.getT2().setWork(true);
				
				reschedule(bestTime, prevBestTime, bestPair, ordersCounter,cp);
				prevBestTime = bestTime;
				bestTime = Double.MAX_VALUE;
				bestPair.getT1().setRequestPlanned(true);
			}				

			
			if(pair.getT1() < bestTime && !pair.getT2().getT2().isWork()) {
				bestTime = pair.getT1();
				bestPair = pair.getT2();
				//prevBestTime = bestTime;
			}
			
			pair.getT2().getT1();
			prev = pair.getT2().getT1();
		}
		if(timesForRequests.size() >0){
			if(requestCounter ==0) {
				requestCounter++;
			}
			BotProdLocation cp = new BotProdLocation(bestPair.getT2(),
					bestPair.getT1().getStorageLocations().get(0).getPos(), null);
			
	//		baseRequest.setCarProdLocation(cp);
//			bestPair.getT1().setCarProdLocation(cp);
			log.debug("Bot "+bestPair.getT2().getId()+ " Order: "+ bestPair.getT1().getOrderId() );
	
			reschedule(bestTime, prevBestTime, bestPair, ordersCounter, cp);
			prev.toString();
		}
		log.debug(getNextFreeBotsList());
		// pierwsze zaplanowane teraz musimy reshedulowac reszte
		for (int i = requestCounter; i < requests.size(); i++) {
			BaseRequest baseRequest = requests.get(i);
			if(!baseRequest.isRequestPlanned()) {
				if(nextFreeBotsList.size() ==0){
					baseRequest.setTimeToReschedule(3000);
							
				} else {
					baseRequest.setTimeToReschedule(environment.getTimeMs()+(nextFreeBotsList.pop().longValue()+200));
				}
			}
		}
	}

	private void reschedule(double bestTime, Double prevBestTime,
			Pair<BaseRequest, Bot> bestPair, int ordersCounter, BotProdLocation cp) {
		// check if prev time is longest then current
		if(prevBestTime != null && prevBestTime.doubleValue() >= bestTime) {
			// need resheduling
			long newTime = ((long)(prevBestTime - bestTime)*environment.getTimeUnitMs());
			bestPair.getT1().setTimeToReschedule(newTime);
			log.debug("Bot "+bestPair.getT2().getId()+ " Order: "+ bestPair.getT1().getOrderId() +" need reschedule new time: "+environment.getTimeMs() +"("+newTime+")");
//			bestPair.getT1().setCarProdLocation(cp);
			bestPair.getT1().setRequestPlanned(true);
			this.addTimeForNextFreeBot(new Double(newTime)*environment.getTimeUnitMs());
		} else {
			this.addTimeForNextFreeBot(new Double(bestTime)*environment.getTimeUnitMs());
		}
	}
	
	//XXX move this to soe util man!
	private Route applyDurationToPosList(List<Pos> path,  double startTime, Bot bot) {

		List<Pair<Speed, Duration>> speedDurList = new ArrayList<Pair<Speed,Duration>>();
		
		if (path.size() > 1) {
			double priorTime = startTime;
			Pos prior = path.get(0);
			for(int i = 1; i < path.size(); i++){
				Pos next = path.get(i);
				int xDist = Math.abs(PositionUtils.getIntX(next.x) - PositionUtils.getIntX(prior.x));
				int yDist = Math.abs(PositionUtils.getIntX(next.y) - PositionUtils.getIntX(prior.y));
				double rideTime = deltaTime*(xDist + yDist);
				Speed s = new Speed((next.x - prior.x)/rideTime, (next.y - prior.y)/rideTime);
				double nextTime = priorTime+rideTime;
				Duration d = new Duration((long)priorTime, (long)nextTime);
				Pair<Speed, Duration> pair = new Pair<Speed, Duration>(s,d);
				speedDurList.add(pair);
				prior = next;
				priorTime = nextTime;
			}
		}
		
		Route route = new RouteImp(bot, speedDurList, path, path.get(0), 0, null);

		return route;
	}

	@Override
	public void setMVCController(IMVCController mvcController) {
		this.mvcController = mvcController;
	}
}
