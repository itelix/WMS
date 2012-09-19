package cma.store.control.opt.route;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cma.store.control.opt.data.Request;
import cma.store.control.opt.route.allshortestpaths.FloydWarshallAlgorithm;
import cma.store.control.opt.route.allshortestpaths.ShortestPathsFinder;
import cma.store.control.opt.route.data.ShortRouteScore;
import cma.store.control.opt.route.tools.FreeRouteController;
import cma.store.control.teamplaninng.PlannedRequest;
import cma.store.control.utils.Colision;
import cma.store.control.utils.PathsContainer;
import cma.store.control.utils.RoutUtils;
import cma.store.control.utils.StandingBotColision;
import cma.store.data.Bot;
import cma.store.data.Duration;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteImp;
import cma.store.data.Speed;
import cma.store.env.Environment;
import cms.store.utils.Pair;
import cms.store.utils.PositionUtils;

public class KirillRouteCreator implements RouteCreator {
	private static final int MAX_ROUTE_SEARCH_ATTEMPTS = 50;
	private static final int MAX_PARTIAL_ROUTE_SEARCH_ATTEMPTS = MAX_ROUTE_SEARCH_ATTEMPTS;
	private static final int MAX_DELAY_MULTIPLIER = 50;//2;//50
	private static final int ALTERNATIVE_ROUTES_NUM = 3;
	private Environment env;
	private Pos[] excludedPos;
	private Random rnd;
	private double speed;
	private double deltaDist;
	private double deltaTime;
	ShortRouteScore[][] scores;
	private RoutUtils routUtils;
	private Request request;
	private Bot bot;
//	private ShortestPathsFinder pathsFinder = null;
//	private ShortestPathsFinder pathsFinderAlternative = null;
	private ShortestPathsFinder[] pathsFinders = null;
	private double startTime;
	
	private long bigDelay;
	
	Logger logger = Logger.getLogger(getClass());
	
	public KirillRouteCreator(Environment env){
		this.env = env;
//		this.rnd = new Random(env.getSeed());
		this.rnd = new Random();
		initPathFinders();
	}
	
	private void initPathFinders() {
		excludedPos = new Pos[ALTERNATIVE_ROUTES_NUM];
		excludedPos[0] = env.createPos(env.getLayerModel().getSizeX() + 1,
				env.getLayerModel().getSizeY() + 1);
		for (int i = 0; i < ALTERNATIVE_ROUTES_NUM - 1; i++) {
			excludedPos[i + 1] = env.createPos(0, 4*i);
		}
//		excludedPos[2] = env.createPos(0, 0);
//		excludedPos[1] = env.createPos(0, 4);
		// All shortest paths computation
		pathsFinders = new ShortestPathsFinder[ALTERNATIVE_ROUTES_NUM];
		for (int i = 0; i < ALTERNATIVE_ROUTES_NUM; i++) {
			List<Pos> excludedPosSet = new ArrayList<Pos>();
			excludedPosSet.add(excludedPos[i]);
			pathsFinders[i] = new FloydWarshallAlgorithm(env, excludedPosSet);
			pathsFinders[i].computePaths();
		}
	}
	
	private void init(){
		this.speed = bot.getMaxSpeed();
		this.deltaDist = env.getLayerModel().getUnitSize();
		this.deltaTime = deltaDist/speed; //ms
		
		routUtils = new RoutUtils(env);
		routUtils.initBotRoutes();
		
		// diameter of layer model
		// i.e. the longest path that bot can take between storage and deck
		bigDelay = env.getLayerModel().getSizeX() + env.getLayerModel().getSizeY();
		bigDelay = 1;
		bigDelay += 1;
	}
	
	public Route applyDurationToPosList(List<Pos> path) {
		Route route = null;
		List<Pair<Speed, Duration>> speedDurList = new ArrayList<Pair<Speed, Duration>>();
		
		if (path.size() > 1) {
			double priorTime = startTime;
			Pos prior = path.get(0);
			for(int i = 1; i < path.size(); i++){
				Pos next = path.get(i);
				int xDist = Math.abs(PositionUtils.getIntX(next.x) - PositionUtils.getIntX(prior.x));
				int yDist = Math.abs(PositionUtils.getIntX(next.y) - PositionUtils.getIntX(prior.y));
				double rideTime = deltaTime*(xDist + yDist);
				if (rideTime == 0.0) {
					// We are in the same position so we apply a delay (speed = 0, rideTime = unitTime = 100)
					rideTime = env.getTimeUnitMs();
				}
				Speed s = new Speed((next.x - prior.x)/rideTime, (next.y - prior.y)/rideTime);
				double nextTime = priorTime+rideTime;
				Duration d = new Duration((long)priorTime, (long)nextTime);
				Pair<Speed, Duration> pair = new Pair<Speed, Duration>(s, d);
				speedDurList.add(pair);
				prior = next;
				priorTime = nextTime;
			}
			route = new RouteImp(bot, speedDurList, path, path.get(0), 0, null);
		}
		
		return route;
	}
	
	private Colision getCollision(List<Pos> path) {
		long timeScore = (long)startTime;
		Colision col = null;
		logger.debug("checking path: " + path);
		if (path.size() == 0) return null;
		for (Pos pos: path) {
//			AS change for 
//			col = routUtils.getCollision(bot, pos, timeScore);
			col = routUtils.getCollisionWithWorkingBot(bot,pos, timeScore);
			if (col != null) {
				logger.debug("Collision at time: " + timeScore + " pos: " + pos + " bot: " + bot.getId());
				return col;
			}
			else {
//				logger.debug("No Collisions at time: " + timeScore + " pos: " + pos + " bot: " + bot.getId());
			}
			timeScore += deltaTime;
		}
		// Check if there are collisions with standing bots after finishing bot bot task
		// XXX 08.08.2012 this code is not need more - to remove in future 
//		Pos pos = path.get(path.size() - 1);
//		long timeMargin = routUtils.getMaxEndTime(timeScore);
//		if (timeMargin < 0)
//			timeMargin = 0;
//		for (; timeScore < timeMargin; timeScore += deltaTime) {
//			col = routUtils.getCollision(bot, pos, timeScore);
//			if (col != null) {
//				return col;
//			}
//		}
		return col;
	}
	
	private List<Pos> applyDelaysImplementation(List<Pos> path) {
		List<Pos> delayedPath = new ArrayList<Pos>();

		if (path.size() > 1) {
			delayedPath.add(path.get(0));
			if (path.size() > 2) {
				for (int i = 1; i < path.size() - 1; i++) {
					Pos prev = path.get(i - 1);
					Pos curr = path.get(i);
					Pos next = path.get(i + 1);
			
					if (Math.abs(PositionUtils.getIntX(prev.x - next.x)) == 1
						&& Math.abs(PositionUtils.getIntY(prev.y - next.y)) == 1) {
						// assign delays only on turns
				
						long delay = bigDelay * rnd.nextInt(MAX_DELAY_MULTIPLIER);
//						logger.debug("pos: " + pos + " delay: " + delay);
//						delay += 1;
						while (delay-- > 0) {
							delayedPath.add(prev);
						}
					}
					delayedPath.add(curr);
				}
			}
			delayedPath.add(path.get(path.size() - 1));
		}
//
//		for (Pos pos : path) {
//			long delay = bigDelay * rnd.nextInt(MAX_DELAY_MULTIPLIER);
//			delay += 1;
//			while (delay-- > 0) {
//				delayedPath.add(pos);
//			}
//		}

		return delayedPath;
	}
	
	private List<Pos> applyDelays(List<Pos> path) {
		path = applyDelaysImplementation(path);
		Colision col = getCollision(path);
		if (col != null) {
			logger.debug("Collision " + col + ". Looking for another route...");
			logger.debug("Colliding path: " + path);
			return null;
		}
		return path;
	}

	private List<Pair<Integer, Integer>> findDelays(List<Pos> path) {
		List<Pair<Integer, Integer>> ret = new ArrayList<Pair<Integer, Integer>>();
		boolean sequenceStarted = false;
		int sequenceLen = 0;
		int sequenceStart = 0;
		
		int pathSize = path.size();
		if (pathSize > 0) {
			Pos curr;
			Pos prev = path.get(0);
			for (int i = 1; i < pathSize; i++) {
				curr = path.get(i);
				if (curr.equals(prev)) {
					if (!sequenceStarted) {
						sequenceStarted = true;
						sequenceLen = 1;
						sequenceStart = i - 1;
					}
					sequenceLen++;
				} else {
					if (sequenceStarted) {
						Pair<Integer, Integer> pair =
								new Pair<Integer, Integer>(sequenceStart, sequenceLen);
						ret.add(pair);
					}
					sequenceStarted = false;
				}
				prev = curr;
					
			}
		}
		return ret;
	}
	
	
	private List<Pos> decreaseDelaysImplementation(List<Pos> path) {
		// decrease delays
		int delayLen; int newDelayLen;
		int delayIndex = 0;
		
		List<Pair<Integer, Integer>> delayIndx = findDelays(path);//TODO take it from applyDelays!!!
		logger.debug("delayIdx: " + delayIndx);
		List<Pos> compressedPath = new ArrayList<Pos>();
		int lastDelayEndIdx = 0;
		
		for (Pair<Integer, Integer> delay : delayIndx) {
			delayLen = delay.getT2();
			delayIndex = delay.getT1();
			
			newDelayLen = delayLen / 2;
			if (newDelayLen == 0) continue;
			
			if (delayIndex > 0)
				compressedPath.addAll(path.subList(lastDelayEndIdx, delayIndex));
			compressedPath.addAll(path.subList(delayIndex, delayIndex + newDelayLen));
			
			lastDelayEndIdx = delayIndex + delayLen;
			delayLen = newDelayLen;
		}
		if (compressedPath.size() == 0) return null;
		compressedPath.addAll(path.subList(lastDelayEndIdx, path.size()));
		
		logger.debug("compressedPath: " + compressedPath);
		
		Colision col = getCollision(compressedPath);
		if (col != null) {
			logger.debug("Collision " + col + ". decreaseDelays failed...");
			logger.debug("Colliding path: " + compressedPath);
			return null;
		}
		else {
			logger.debug("decreaseDelays worked fine...");
		}
		
		return compressedPath;
	}
	
	private List<Pos> decreaseDelays(List<Pos> path) {
		List<Pos> compressedPath = null;
		while ((compressedPath = decreaseDelaysImplementation(path)) != null) {
			path = compressedPath;
		}
		return path;
	}
	
	public void initializeCreator(Request request, Bot bot) {
		this.bot = bot;
		this.request = request;
		this.startTime = request.getFromTime();
		logger.debug("createRout bot: " + bot.getId() + " startTime: " + startTime + " from: "
				+ request.getFrom() + " to: " + request.getTo());
		// setup of task's parameters
		init();
		// assign bots to routes
		routUtils.initBotRoutes();
	}
	
	private List<Pos> createRoutImplementation(int attempt, PathsContainer foundRoutes) {
		Pos from = request.getFrom();
		Pos to = request.getTo();
		
		int excludedIdx = attempt % ALTERNATIVE_ROUTES_NUM;
		if (from.equals(excludedPos[excludedIdx])
				|| to.equals(excludedPos[excludedIdx])) {
			// route that starts of ends in excludedPos doesn't exist in this graph
			return null;
		}
		
		List<Pos> path = pathsFinders[excludedIdx].getPath(from, to);
		
		logger.debug("attempt: " + attempt + " path: " + path);
		if (path == null) {
			logger.error("NULL path! Can't happen");
			return null;
		}
		if (path.size() <= 1) {
			logger.error("Empty path! We are already at destination");
			return path;
		}
		
		
		if ((path = applyDelays(path)) == null)
			return null;
		
//		path = decreaseDelays(path);
		
		if (foundRoutes != null) {
			Colision col = foundRoutes.add(path, (long) startTime, bot);
			if (col != null) {
				logger.debug("foundRoutes collision: " + col);
				return null;
			}
			// TODO: Delete executed/scheduled routes
		}

		return path;
	}

	public Route createRoutWithDelays(Request request, Bot car, PathsContainer foundRoutes) {
//		int attempt = 0;
		List<Pos> path = getBestPathForOrder(request,car,foundRoutes);
		
//		initializeCreator(request, car);
//
//		while (attempt < MAX_ROUTE_SEARCH_ATTEMPTS) {
//			path = createRoutImplementation(attempt, foundRoutes);
//			if (path != null) break;
//			attempt = attempt + 1;
//		}
		/*
		if (route == null) {
			attempt = 0;
			while (attempt < MAX_PARTIAL_ROUTE_SEARCH_ATTEMPTS) {
				route = createRoutPartialSolution(attempt);
				if (route != null) break;
				attempt = attempt + 1;
			}
		}
		*/
		if (path == null)
			return null;
		Route route = applyDurationToPosList(path);
		route.setPos(path);
		return route;
	}
	
	public List<Pos> getBestPathForOrder(Request request, Bot car, PathsContainer foundRoutes) {
		int attempt = 0;
		List<Pos> path = null;
		
		initializeCreator(request, car);

		while (attempt < MAX_ROUTE_SEARCH_ATTEMPTS) {
			path = createRoutImplementation(attempt, foundRoutes);
			if (path != null) break;
			attempt = attempt + 1;
		}

		return path;
	}
	
	@Override
	public Route createRout(Request r, Bot bot, FreeRouteController freeRouteController, boolean forward) {
		return createRoutWithDelays(r,bot,null);
	}

	
	public void checkAndResolveColisionWithStandingBot(List<PlannedRequest> plannedRequests, PathsContainer foundRoutes) {
//		Pos pos = path.get(path.size() - 1);
//		long timeMargin = routUtils.getMaxEndTime(timeScore);
//		if (timeMargin < 0)
//			timeMargin = 0;
//		for (; timeScore < timeMargin; timeScore += deltaTime) {
//			col = routUtils.getCollision(bot, pos, timeScore);
//			if (col != null) {
//				return col;
//			}
//		}
//		List<PlannedRequest> tmpPlannedRequests = new ArrayList<PlannedRequest>(plannedRequests);
		
		for (PlannedRequest plannedRequest : plannedRequests) {
			// sprawdzenie czy bot bedzie stal w czas
			
			Bot bot = plannedRequest.getBot();
			Route route = plannedRequest.getRoutes().get(
					plannedRequest.getRoutes().size()-1);
			List<Pos> plannedPath = (List<Pos>) route.getPos();
			StandingBotColision col = foundRoutes.isColiding(plannedPath.get(plannedPath.size()-1), route.getFinalTime(),bot);
			if(col != null) {
//				// utworzenie ruta
				long startTime = route.getFinalTime()-2000;
				long timeToReachProduct =  startTime + 
						PositionUtils.getTimeToTrevel(col.getPos(), env.createPos(29, 0), col.getConflictedBot());
				
//				Request r1 = new Request(startTime, col.getPos(),
//						timeToReachProduct, env.createPos(29, 0));
//				this.initializeCreator(r1, col.getConflictedBot());
//				Route moveRoute = this.createRout(r1, col.getConflictedBot(), foundRoutes);
//				
//
//								List<Pos> poss = new ArrayList<Pos>();
//				poss.add(env.createPos(20, 0));
//				poss.add(env.createPos(21, 0));
//				Route moveRoute = this.getR(poss, route.getEndTime()-2000,col.getConflictedBot());
//				plannedRequest.getRoutes().add(moveRoute);
				logger.info(col);				
			}
			
		}
	}
	
	public Route getR(List<Pos> path, long startTime2, Bot bot2) {
		List<Pair<Speed, Duration>> speedDurList = new ArrayList<Pair<Speed, Duration>>();
		
		if (path.size() > 1) {
			double priorTime = startTime2;
			Pos prior = path.get(0);
			for(int i = 1; i < path.size(); i++){
				Pos next = path.get(i);
				int xDist = Math.abs(PositionUtils.getIntX(next.x) - PositionUtils.getIntX(prior.x));
				int yDist = Math.abs(PositionUtils.getIntX(next.y) - PositionUtils.getIntX(prior.y));
				double rideTime = deltaTime*(xDist + yDist);
				if (rideTime == 0.0) {
					// We are in the same position so we apply a delay (speed = 0, rideTime = unitTime = 100)
					rideTime = env.getTimeUnitMs();
				}
				Speed s = new Speed((next.x - prior.x)/rideTime, (next.y - prior.y)/rideTime);
				double nextTime = priorTime+rideTime;
				Duration d = new Duration((long)priorTime, (long)nextTime);
				Pair<Speed, Duration> pair = new Pair<Speed, Duration>(s, d);
				speedDurList.add(pair);
				prior = next;
				priorTime = nextTime;
			}
			return new RouteImp(bot2, speedDurList, path, path.get(0), 0, null);
		}
		return null;
	}




}
