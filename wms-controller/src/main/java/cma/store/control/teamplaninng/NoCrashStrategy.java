package cma.store.control.teamplaninng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cma.store.control.Direction;
import cma.store.control.opt.BotProdLocation;
import cma.store.control.opt.RequestRealization;
import cma.store.control.opt.route.RouteCreator;
import cma.store.data.Bot;
import cma.store.data.Bot.State;
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
creating date: 26-07-2012
creating time: 08:12:02
autor: adam
 */

public class NoCrashStrategy extends MonteCarloStrategy implements TeamPlaninngStrategy {
	
	RouteCreator routeCreator;
	int maxRouteLen = 2000;
	
	public NoCrashStrategy(Environment env) {
		super(env);
		//routeCreator = new ShortRouteCreatorAdam(env);
		init();
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
		
		if(requests.size() >0){			
			List<Pos> nextPosition = new ArrayList<Pos>();
			List<Pos> prevPosition = new ArrayList<Pos>();
			List<Pos> doneBotsPositions = new ArrayList<Pos>();
			
			HashMap<Bot, List<Pos>> result = new HashMap<Bot, List<Pos>>();
			List<Pos> placeToVisit = new ArrayList<Pos>();
			
			for (BaseRequest baseRequest : requests) {				
				placeToVisit.add(baseRequest.getStorageLocations().get(0).getPos());
			}
			boolean isAllDone = false;
			doneBotsPositions.clear();
			while(!isAllDone) {
				nextPosition.clear();
				prevPosition.clear();
				for (Bot bot : availableCars) {
					if(bot.isAvailable()) {
						if(result.get(bot) == null) {
							result.put(bot, new ArrayList<Pos>());
						}
						Pos currentBotPos;
						if(bot.getNextPos() == null) {
							currentBotPos = bot.getPos();
						} else {
							currentBotPos = bot.getNextPos();
						}					
						while(true) {
							int index = rnd.nextInt( Direction.values().length );
							// get next position
							Pos nextPos = getNextPos(currentBotPos,  Direction.values()[index]);
							if(env.getLayerModel().isRoadGood( nextPos )){
								if(nextPosition.contains(nextPos) || prevPosition.contains(nextPos)) {
									continue;
								} else {
									// dodajemy i lecimy do nastepnego bota
									if (result.get(bot).size() > 0)
									prevPosition.add(result.get(bot).get(result.get(bot).size() - 1));
									nextPosition.add(nextPos);
									bot.setNextPos(nextPos);
									result.get(bot).add(nextPos);
									if(result.get(bot).size() == maxRouteLen){
										log.error("Bot "+bot.getId()+ " route to long");
										result.get(bot).clear();
										bot.setNextPos(null);
									}
									// sprawdzenie czy moze juz czasem nie doszlismy do celu
									boolean onPlace = onPlace(nextPos, placeToVisit);
									if(onPlace) {
										log.info("onPlace");
										bot.setAvailable(false);
										placeToVisit.remove(nextPos);
										doneBotsPositions.add(nextPos);
										placeToVisit.toString();
										bot.setState(State.GOTOMVC);
									}
									break;
								}				
							}					
						}
					}
				}
				if(placeToVisit.size()== 0) {
					isAllDone = true;
				}
			}
			Iterator it = result.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry)it.next();
				Bot bot = (Bot) pairs.getKey();
				List<Pos> movments = (List<Pos>) pairs.getValue();
				Pos destination = movments.get(movments.size()-1);
				for (BaseRequest baseRequest : requests) {
					if(baseRequest.getStorageLocations().get(0).getPos().equals(destination)) {
						Route route = this.createRouteFromPosList(movments, env.getTimeMs(), bot.getPos(), bot);
						List<Route> routes = new ArrayList<Route>();
						routes.add(route);
						RequestRealization rr = new RequestRealization(routes);
						baseRequest.setRequestRealization(rr);
					}
				}				
			}
			
		}
	}

	
	private Route createRouteFromPosList( List<Pos> pp, double startTime, Pos from, Bot car) {
		log.info("pp: " + pp);
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
	
	/*
	int carNumber = 0;
	List<Route> routes = new ArrayList<Route>();
	//TODO dodac petle po produktach
	while(true) {
		Bot bot = availableCars.get(carNumber);
		Pos destination = requests.get(0).getStorageLocations().get(0).getPos();
		long startTime = env.getTimeMs()+1000; 
		long timeToReachProduct = startTime + 
				getTimeToTrevel( bot.getPos(), destination, bot );
		
		Request r1 = new Request(startTime,bot.getPos(),timeToReachProduct,destination );
		Route route1  = routeCreator.createRout( r1, bot );
		Route route2  = routeCreator.createRout( r1, bot );

		if( route1==null) continue;
		
		int routeGroupId = Route.getNextRouteId();
		
		route1.setRouteGroupId(routeGroupId);
		//route2.setRouteGroupId(routeGroupId);
		
		routes.add(route1);
		//routes.add(route2);
		
		requests.get(0).setRequestRealization( new RequestRealization(routes));
		//return new RequestRealization(routes);
		int time= 500;
		while(time < 20000) {
			Pos inTime = posInTime(time, route1.getInitPosition(),route1.getSpeedDurList());
			time += 100;
			log.debug("inTime "+inTime);
		}
		
		
	
		break;
	}
	*/	
	
	private boolean onPlace(Pos nextPos, List<Pos> placeToVisit) {
		for (Pos pos : placeToVisit) {
			return onPlace(nextPos, pos);
		}
		return false;
	}

	private boolean onPlace(Pos nextPos, Pos pos) {
		return nextPos.equals(pos);		
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
	 

}
