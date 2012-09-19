package cma.store.control.teamplaninng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cma.store.control.Direction;
import cma.store.control.mvc.IMVCController;
import cma.store.control.opt.BotProdLocation;
import cma.store.control.opt.RequestRealization;
import cma.store.data.Bot;
import cma.store.data.Duration;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteImp;
import cma.store.data.Speed;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cms.store.utils.Pair;


/**
Warehouse optimizer.
creating date: 25-07-2012
creating time: 17:09:49
autor: Filip
 */

public class MonteCarloStrategy implements TeamPlaninngStrategy {
	
	Logger log = Logger.getLogger(getClass());
	double speed;
	double deltaDist;
	double deltaTime;
	Random rnd = new Random(123);
	
	public Environment env;
	public MonteCarloStrategy(Environment env) {
		// TODO Auto-generated constructor stub
		this.env = env;
		init();
	}

	@Override
	public BotProdLocation getCarProdLocation(List<Bot> bots,
			List<LocPriority> prodLocations, boolean[][] used) {
		// TODO Auto-generated method stub
		return null;
	}

	private void printAvailableCars(List<Bot> availableCars) {
		if (availableCars.size() > 0) {
			log.info("availableCars:");
			for (Bot b : availableCars)
				log.info("\t " + b.getPos());
		}
		else
			log.info("There are no available cars!");
	}
	
	private boolean updatePosMatrix(
			List<Pos> path,
			HashMap<Double,
			List<Pos>> posMatrix,
			HashMap<Pos, Double> sleepingBots,
			long realizationStartTime) {
		for (int i = 0; i < path.size(); i++) {
			List<Pos> currentPosList = posMatrix.get((double) (i + realizationStartTime));
			if (currentPosList == null) log.error("currentPosList == null!!!!!");
			currentPosList.add(path.get(i));
			posMatrix.put((double) (i + realizationStartTime), currentPosList);
//			if (!posMatrix.put((double) (i + realizationStartTime), currentPosList)) {
//				log.error("Position: " + path.get(i) + " is already in posMatrix at time: " + (i + realizationStartTime));
//				return false;
//			}
		}
		sleepingBots.put(path.get(path.size() - 1), (double) (path.size() - 1 + realizationStartTime));
		log.info("sleepingBots Pos: " + path.get(path.size() - 1) +
				" time: " + (double) (path.size() - 1 + realizationStartTime) );
		return true;
	}
	
	private boolean isPathValid(List<Pos> path, HashMap<Double, List<Pos>> posMatrix, 
			HashMap<Pos, Double> sleepingBots, long realizationStartTime) {
		if (path == null) return false;
		for (int i = 0; i < path.size(); i++) {
			List<Pos> occupiedPosList = posMatrix.get((double) (i + realizationStartTime));
			if (occupiedPosList == null) {
				occupiedPosList = new ArrayList<Pos>();
				posMatrix.put((double) (i + realizationStartTime), occupiedPosList);
				/*
				if (!sleepingBots.containsKey(path.get(i))) {
					sleepingBots.put(path.get(i), 1000000.0);
					log.info("Added Pos: " + path.get(i) + " Time:" + 100000.0);
				}
				*/
			}/*
			if (sleepingBots.get(path.get(i)) == null) {
				sleepingBots.put(path.get(i), 5000000.0);
				log.info("Added Pos: " + path.get(i) + " Time:" + 500000.0);
			}*/
			log.info("vacantTime: " + sleepingBots.get(path.get(i)) + " < realizeTime:" + (double) (i + realizationStartTime));
			log.info("pos(i): " + path.get(i));
			if (occupiedPosList.contains(path.get(i))) return false;
			if (sleepingBots.get(path.get(i)) != null && sleepingBots.get(path.get(i)) < (double) (i + realizationStartTime)) return false;
		}
		return true;
	}
	

	private Route createRouteFromPosList( List<Pos> pp, double startTime, Pos from, Bot car) {
		
		List<Pair<Speed, Duration>> speedDurList = new ArrayList<Pair<Speed,Duration>>();

		if( pp.size()>1){
			double priorTime = startTime;
			Pos prior = pp.get(0);
			for(int i=1; i<pp.size(); i++){
				Pos next = pp.get(i);
				Speed s = new Speed( (next.x-prior.x)/deltaTime, (next.y-prior.y)/deltaTime );
				double nextTime = priorTime+deltaTime;
				Duration d = new Duration( (long)priorTime, (long)nextTime);
				Pair<Speed, Duration> pair = new Pair<Speed, Duration>(s,d);
				speedDurList.add(pair);
				prior=next;
				priorTime=nextTime;
			}
		}
		
		Route route = new RouteImp(car, speedDurList, pp, from, 0, null);
		
		return route;			
	}
	
	@Override
	public void assingRequestToBots(List<BaseRequest> requests,
			List<Bot> availableCars) {
		//Pos[][], List<double> posMatrix;
//		HashMap<Pos, List<Double>> posMatrix = new HashMap<Pos, List<Double>>();
		HashMap<Double, List<Pos>> posMatrix = new HashMap<Double, List<Pos>>();
		HashMap<Pos, Double> sleepingBots = new HashMap<Pos, Double>();
		List<Route> routesFound;
		for (BaseRequest baseRequest : requests) {
			printAvailableCars(availableCars);
			Pos destinationPos = baseRequest.getStorageLocations().get(0).getPos();
			List<Pos> randomPath = null;
			List<Pos> reverseRandom = null;
			while(randomPath == null) {
				randomPath = findRandomPath(destinationPos, resolvePosFromBot(availableCars));
				if (randomPath == null) log.info("NULL route found!");
				if (randomPath != null) {
					reverseRandom = new ArrayList<Pos>(randomPath);
					Collections.reverse(reverseRandom);
				}
				if (!isPathValid(reverseRandom, posMatrix, sleepingBots, 0 /* hardcoded realizationStartTime */)) {
					randomPath = null;
					log.info("Coliding route found!");
				}
			}
			log.info("route for product " + destinationPos + " found!");
			if (!updatePosMatrix(reverseRandom, posMatrix, sleepingBots, 0)) log.error("updatePosMatrix failed");
			Bot usedBot = null;
			for (Bot b : availableCars) {
				if (b.getPos().equals(randomPath.get(randomPath.size() - 1))) {
					usedBot = b;
					availableCars.remove(b);
					break;
				}
			}
			printAvailableCars(availableCars);
//			Collections.reverse( randomPath);
			Route r = createRouteFromPosList( reverseRandom, 1200,
					reverseRandom.get(0) /*usedBot.getPos()*/, usedBot);
			List<Route> rrList = new ArrayList<Route>();
			rrList.add(r);
			RequestRealization rr = new RequestRealization(rrList);
			baseRequest.setRequestRealization(rr);
			
		}
		
//		List<Bot> availableBots = availableCars;[150.0:50.0, 650.0:50.0]

		
//		void public refreshPosMatrix(PosList) {
//		
//		}
		
		
		//for (i = 0; i < MAX_ITER; i++) {
		//lista (sciezka, bot)
		//petla po dotyczaca T
		//currentTime = 0; // start B
		//Pos act = poczatkowa pozycja bota(i)// baseRequest.getStorageLocations().get(0).getPos();
		//List<Pos> currentRoute = {}; 
		//	petla po act
		//		if (act == bot.getPos() for some bot) break;
		//		if (!posMatrix[currentTime].contains(act))
		//			act = getNextPos(currentRoute.getLast());
		//	currentRoute.add(act);
		
	}
	
//	findRandomPath(startPos, destPosList)
//	Pos act = startPos
//	List<Pos> currentRoute = {}; 
//	while (!destPosList.contains(act)) {
//		if (currentRoute.containts(act)) {
//			currentRoute.delLast();		
//		}
//		else {
//			currentRoute.add(act);
//			act = nextPos(currentRoute.getLast());
//		}
//}	
	private List<Pos> findRandomPath(Pos destinationPos,
			List<Pos> startPosList) {
		Pos current = destinationPos;
		List<Pos> currentRoute = new ArrayList<Pos>();
		currentRoute.add(current);
		int loopCounter = 0;
		int maxLoops= 50;
		List<Integer> indexes = new ArrayList<Integer>();
		while (!startPosList.contains(current) && loopCounter < maxLoops) {
//			log.info("loopCounter ="+loopCounter);
		
//				log.info("routes"+currentRoute);
				//indexes.add(index);

				Pos tmp = null;
				while (tmp == null || currentRoute.contains(tmp) || !env.getLayerModel().isRoadGood( tmp )) {
					loopCounter++;
					if (loopCounter > maxLoops) break;
					int index = rnd.nextInt( Direction.values().length );
//					if(loopCounter == 1) {//TODO
//						index=3;
//					}
					tmp = getNextPos(current,  Direction.values()[index]);
//					log.info("index set to "+index + " tmp " + tmp + " currentRoute.contains(tmp) = " + currentRoute.contains(tmp) +
//							" !env.getLayerModel().isRoadGood( tmp ) = " + !env.getLayerModel().isRoadGood( tmp ));
					//log.info("tmp ="+tmp);
				}
				current = tmp;
				currentRoute.add(current);
			
		}
		if(loopCounter == maxLoops){
//			log.info("return"+indexes);
			log.info("routes"+currentRoute);
			return null;
		}
		if (!startPosList.contains(current)) return null;
		log.info("RETURN current = "+current + " currentRoute = " + currentRoute);
		return currentRoute;
	}

	private List<Pos> resolvePosFromBot(List<Bot> availableCars) {
		List<Pos> resultPos = new ArrayList<Pos>();
		for (Bot bot : availableCars) {
			resultPos.add(bot.getPos());
		}
		return resultPos;
	}

	private Pos getNextPos( Pos old, Direction d){
		
		switch(d){
			case RIGHT: return new Pos(old.x+deltaDist,old.y);
			case LEFT: 	return new Pos(old.x-deltaDist,old.y);
			case UP: 	return new Pos(old.x,old.y+deltaDist);
			case DOWN: 	return  new Pos(old.x,old.y-deltaDist);
			default: throw new RuntimeException( "Direction isn't support yet");
		}
	}
	
	private void init(){
		speed = env.DEFAULT_MAX_BOT_SPEED_MM_PER_MS;
		deltaDist = env.getLayerModel().getUnitSize();
		deltaTime = deltaDist/speed; //ms
	}

	@Override
	public void setMVCController(IMVCController mvcController) {
		// TODO Auto-generated method stub
		
	}

}
