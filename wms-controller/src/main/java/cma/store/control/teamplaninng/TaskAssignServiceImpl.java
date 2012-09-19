package cma.store.control.teamplaninng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import cma.store.control.BaseRealization;
import cma.store.control.Realization;
import cma.store.control.opt.route.allshortestpaths.FloydWarshallAlgorithm;
import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.utils.Utils;
//import cms.store.utils.BotTaskPredict;
import cms.store.utils.Pair;
import cms.store.utils.RateStorageLocation;

public class TaskAssignServiceImpl implements TaskAssignService {

	Logger log = Logger.getLogger(getClass());
	Environment env = Environment.getInstance();
	FloydWarshallAlgorithm path =  null; //FloydWarshallAlgorithm.getInstance(env);
	Random rnd = new Random(env.getSeed());
	public static final int RANDOM_LOCATION_FILE = 2;
	
	public TaskAssignServiceImpl(){
	}

	@Override
	public void assignBotToRequests2(Realization realization) {
//		SortedMap<Long, List<Pair<Bot, BaseRealization>>> result = new TreeMap<Long, List<Pair<Bot,BaseRealization>>>();
		List<Bot> bots = new ArrayList<Bot>(env.getBotFleet().getBots());
		Bot bestBot;
		double bestTime;
		long bestBotFreeTime;
		Pair<Double, LocPriority> bestNearestDestination = null;
		RateStorageLocation bestRateStorageLocation =null;
		
		for (BaseRealization rel: realization.getBaseRealList()) {
			bestBot = null;
			bestTime = Double.MAX_VALUE;
			bestBotFreeTime = Long.MAX_VALUE;
			bestRateStorageLocation =null;
			// dla kazdego bota sprawdzic odleglosc w czasie
			BaseRequest baseRequest = rel.getBaseRequest();
			for (Bot bot : bots) {
				Pos botPos = env.getSchedule().getBotPosition(bot,
						env.getSchedule().getAvailableMinTime(bot), false);
//				List<Pair<Double,LocPriority>> result = getNearesDestination(botPos,
//						baseRequest.getStorageLocations(),RANDOM_LOCATION_FILE);
				
				List<Pair<Double,LocPriority>> result = getNearesDestination(botPos,baseRequest.getStorageLocations(),RANDOM_LOCATION_FILE);
//				Pair<Double, LocPriority> nearestDestination = this
//						.getNearesDestination(botPos,
//								baseRequest.getStorageLocations());
//				int index = rnd.nextInt(result.size());
//				Pair<Double, LocPriority> nearestDestination = result.get(index);		
				Pair<Double, LocPriority> nearestDestination = getLocationByPriority(result);
				if(bestRateStorageLocation == null) {
					bestRateStorageLocation = new RateStorageLocation();
					bestRateStorageLocation.setLocPriority(nearestDestination.getT2());
					bestRateStorageLocation.setPathLength(nearestDestination.getT1());
					bestRateStorageLocation.setAvailableMinTime(env.getSchedule().getAvailableMinTime(bot));
					bestNearestDestination = nearestDestination;
					bestBot = bot;
				} else {
					RateStorageLocation rateStorageLocation = new RateStorageLocation();
					rateStorageLocation.setLocPriority(nearestDestination.getT2());
					rateStorageLocation.setPathLength(nearestDestination.getT1());
					rateStorageLocation.setAvailableMinTime(env.getSchedule().getAvailableMinTime(bot));
					
					boolean compareResult = this.compare2(bestRateStorageLocation,rateStorageLocation);
					if(compareResult) {
						bestRateStorageLocation = rateStorageLocation;
						bestNearestDestination = nearestDestination;
						bestBot = bot;
					}				
				}
//				if(nearestDestination.getT1() < bestTime ) {
//					// checkAvailable Bot Time
//					long botFreeTime = env.getSchedule().getAvailableMinTime(bot);
//					if(botFreeTime <= bestBotFreeTime) {
//						bestBot = bot;
//						bestTime = nearestDestination.getT1();	
//						bestBotFreeTime = botFreeTime;
//						bestNearestDestination = nearestDestination;
//					}
//				}
				
//				rel.setBot(bot);		
//				bestBot = bot;
//				Pos destination = baseRequest.getStorageLocations().get(0).getPos();
//				double dis = Utils.distance(botPos, destination);
//				log.info(dis);
			}
			bots.remove(bestBot);
			rel.setBot(bestBot);
			log.info("Order "+baseRequest.getOrderId() +" Location " +bestNearestDestination.getT2().getPos()+" Bot "+bestBot.getId());
		}		
	}
	
	private boolean compare2(RateStorageLocation bestRateStorageLocation,
			RateStorageLocation rateStorageLocation) {
		
		Double pathComparation = rateStorageLocation.getPathLength() - bestRateStorageLocation.getPathLength();
		Long timeComparation = rateStorageLocation.getAvailableMinTime() - bestRateStorageLocation.getAvailableMinTime();

		if(pathComparation <0 && timeComparation<=0)
			return true;
		
		long newEndTime = rateStorageLocation.getAvailableMinTime();
		newEndTime += (long) (rateStorageLocation.getPathLength()*env.getTimeUnitMs());
		long bestEndTime = bestRateStorageLocation.getAvailableMinTime();
		bestEndTime += (long) (bestRateStorageLocation.getPathLength()*env.getTimeUnitMs());
		if(newEndTime < bestEndTime) 
			return true;
		else
			return false;

	}
	
	private boolean compare(RateStorageLocation bestRateStorageLocation,
			RateStorageLocation rateStorageLocation) {
		Double pathComparation = rateStorageLocation.getPathLength() - bestRateStorageLocation.getPathLength();
		Long timeComparation = rateStorageLocation.getAvailableMinTime() - bestRateStorageLocation.getAvailableMinTime();
		if(pathComparation <= 0 && timeComparation < 0) {
			// shorter path and shorter time
			return true;		
		}else if(pathComparation <= 0 && timeComparation == 0) {
			// shorter path and same time
			return true;		
		} else if(pathComparation <= 0 && timeComparation > 0) {
			// shorter path and longest time
			
//			// obliczyc czy czasem szybciej nie zrealizuje zadania	
//			long timeToMakeTask = (long) (-1*pathComparation*env.getTimeUnitMs());
//			long newEndTime = rateStorageLocation.getAvailableMinTime() + timeToMakeTask;
//			long bestEndTime = bestRateStorageLocation.getAvailableMinTime();
//			if(newEndTime < bestEndTime)
//				return true;
//			else
//			long timeToMakeTask = (long) (-1*pathComparation*env.getTimeUnitMs());
			long newEndTime = rateStorageLocation.getAvailableMinTime();
			newEndTime += (long) (rateStorageLocation.getPathLength()*env.getTimeUnitMs());
			long bestEndTime = bestRateStorageLocation.getAvailableMinTime();
			bestEndTime += (long) (bestRateStorageLocation.getPathLength()*env.getTimeUnitMs());
			if(newEndTime < bestEndTime) 
				return true;
			else
				return false;
		} else if(pathComparation > 0 && timeComparation < 0) {
			// longest path and shorter time

			long newEndTime = rateStorageLocation.getAvailableMinTime();
			newEndTime += (long) (rateStorageLocation.getPathLength()*env.getTimeUnitMs());
			long bestEndTime = bestRateStorageLocation.getAvailableMinTime();
			bestEndTime += (long) (bestRateStorageLocation.getPathLength()*env.getTimeUnitMs());
			if(newEndTime < bestEndTime)
				return true;
			else
				return false;
		} else if(pathComparation >= 0 && timeComparation >= 0) {
			/// longest path and longest time
			return false;		
		}
		return false;
	}
	

	@Override
	public void assignBotToRequests(Realization realization) {
//		SortedMap<Long, List<Pair<Bot, BaseRealization>>> result = new TreeMap<Long, List<Pair<Bot,BaseRealization>>>();
		List<Bot> bots = new ArrayList<Bot>(env.getBotFleet().getBots());
		Bot bestBot;
		double bestTime;
		long bestBotFreeTime;
		Pair<Double, LocPriority> bestNearestDestination = null;
		Pair<Pair<Double,Double>, LocPriority> bestNearestDestination2 = null;
		
		for (BaseRealization rel: realization.getBaseRealList()) {
			bestBot = null;
			bestTime = Double.MAX_VALUE;
			bestBotFreeTime = Long.MAX_VALUE;
			// dla kazdego bota sprawdzic odleglosc w czasie
			BaseRequest baseRequest = rel.getBaseRequest();
			for (Bot bot : bots) {
				Pos botPos = env.getSchedule().getBotPosition(bot,
						env.getSchedule().getAvailableMinTime(bot), false);
//				List<Pair<Double,LocPriority>> result = getNearesDestination(botPos,
//						baseRequest.getStorageLocations(),RANDOM_LOCATION_FILE);
				
				List<Pair<Double,LocPriority>> result = getNearesDestination(botPos,baseRequest.getStorageLocations(),RANDOM_LOCATION_FILE);
//				Pair<Double, LocPriority> nearestDestination = this
//						.getNearesDestination(botPos,
//								baseRequest.getStorageLocations());
				int index = rnd.nextInt(result.size());
				Pair<Double, LocPriority> nearestDestination = result.get(index);		
//				Pair<Double, LocPriority> nearestDestination = getLocationByPriority(result);
				if(nearestDestination.getT1() < bestTime ) {
					// checkAvailable Bot Time
					long botFreeTime = env.getSchedule().getAvailableMinTime(bot);
					if(botFreeTime <= bestBotFreeTime) {
						bestBot = bot;
						bestTime = nearestDestination.getT1();	
						bestBotFreeTime = botFreeTime;
						bestNearestDestination = nearestDestination;
					}
				}
				
//				rel.setBot(bot);		
//				bestBot = bot;
//				Pos destination = baseRequest.getStorageLocations().get(0).getPos();
//				double dis = Utils.distance(botPos, destination);
//				log.info(dis);
			}
			bots.remove(bestBot);
			rel.setBot(bestBot);
			log.info("Order "+baseRequest.getOrderId() +" Location " +bestNearestDestination.getT2().getPos()+" Bot "+bestBot.getId());
		}		
	}
	/**
	 * Allow assign many task to bots
	 * @param realization
	 */
	@Override
	public void assignBotToRequests3(Realization realization) {
////		SortedMap<Long, List<Pair<Bot, BaseRealization>>> result = new TreeMap<Long, List<Pair<Bot,BaseRealization>>>();
//		List<Bot> bots = new ArrayList<Bot>(env.getBotFleet().getBots());
//		List<BotTaskPredict> botTaskPredicts = new ArrayList<BotTaskPredict>();
//		for (Bot bot : bots) {
//			BotTaskPredict botTaskPredict = new BotTaskPredict();
//			botTaskPredict.setBot(bot);
//			botTaskPredict.setAvailabilityTime(env.getSchedule().getAvailableMinTime(bot));
//			Pos botPos = env.getBotPositionPredictor().getCarPosition(bot, botTaskPredict.getAvailabilityTime(), false);
////			Pos botPos = env.getSchedule().getCarPosition(bot,
////					botTaskPredict.getAvailabilityTime(), false);
//			botTaskPredict.setLastPos(botPos);
//			botTaskPredicts.add(botTaskPredict);
//		}
//		
//		Bot bestBot;
//		double shortestPath;
//		long bestBotFreeTime;
//		Pair<Double, LocPriority> bestNearestDestination = null;
//		BotTaskPredict bestBotTaskPredict = null;
//		
//		for (BaseRealization rel: realization.getBaseRealList()) {
//			bestBot = null;
//			shortestPath = Double.MAX_VALUE;
//			bestBotFreeTime = Long.MAX_VALUE;
//			// dla kazdego bota sprawdzic odleglosc w czasie
//			BaseRequest baseRequest = rel.getBaseRequest();
//			for (BotTaskPredict botTaskPredict : botTaskPredicts) {
//				Bot bot = botTaskPredict.getBot();
//				Pos botPos = botTaskPredict.getLastPos();
////				List<Pair<Double,LocPriority>> result = getNearesDestination(botPos,
////						baseRequest.getStorageLocations(),RANDOM_LOCATION_FILE);
//				
//				List<Pair<Double,LocPriority>> result = getNearesDestination(botPos,baseRequest.getStorageLocations(),RANDOM_LOCATION_FILE);
////				Pair<Double, LocPriority> nearestDestination = this
////						.getNearesDestination(botPos,
////								baseRequest.getStorageLocations());
////				int index = rnd.nextInt(result.size());
////				Pair<Double, LocPriority> nearestDestination = result.get(index);		
//				Pair<Double, LocPriority> nearestDestination = getLocationByPriority(result);
//				if(nearestDestination.getT1() < shortestPath ) {
//					// checkAvailable Bot Time
//					long botFreeTime = botTaskPredict.getAvailabilityTime();
//					if(botFreeTime < bestBotFreeTime) {
//						bestBot = bot;
//						shortestPath = nearestDestination.getT1();	
//						bestBotFreeTime = botFreeTime;
//						bestNearestDestination = nearestDestination;
//						bestBotTaskPredict = botTaskPredict;
//					}
//				} else {
//					long botFreeTime = botTaskPredict.getAvailabilityTime();
//					if(botFreeTime < bestBotFreeTime) {
//						bestBot = bot;
//						shortestPath = nearestDestination.getT1();	
//						bestBotFreeTime = botFreeTime;
//						bestNearestDestination = nearestDestination;
//						bestBotTaskPredict = botTaskPredict;
//					}					
//				}
//				
////				rel.setBot(bot);		
////				bestBot = bot;
////				Pos destination = baseRequest.getStorageLocations().get(0).getPos();
////				double dis = Utils.distance(botPos, destination);
////				log.info(dis);
//			}
////			bots.remove(bestBot);
//			
//			bestBotTaskPredict.setAvailabilityTime(bestBotTaskPredict
//					.getAvailabilityTime() + (long)(shortestPath*env.getTimeUnitMs()));
//			bestBotTaskPredict.setLastPos(baseRequest.getMvc().getPos());
//
//			rel.setBot(bestBot);
//			log.info("Order "+baseRequest.getOrderId() +" Location " +bestNearestDestination.getT2().getPos()+" Bot "+bestBot.getId());
//		}		
	}
	
	/**
	 * Return LocPriority with probability including location priority 
	 * @param source
	 * @return
	 */
	
	private Pair<Double, LocPriority> getLocationByPriority(
			List<Pair<Double, LocPriority>> source) {
//		List<Pair<Double, LocPriority>> result = new ArrayList<Pair<Double,LocPriority>>();
		// counting sum of all values
		double sum =0;
		for (Pair<Double,LocPriority> pair : source) {
			sum += pair.getT2().getPriority();
		}
		// preparing distribution
	
		double rangeStart = 0;
		List<Range<Double>> ranges = new ArrayList<Range<Double>>();
//		for (Pair<Double,LocPriority> pair : source) {
//			double value = (Math.round((pair.getT2().getPriority()/sum)*100.0)/100.0)% 1;
////			if(value >= random && value <=random)
////				return pair;
//		//	result.add(new Pair<Double, LocPriority>(value, pair.getT2()));
//			Range<Double> range = Ranges.open(rangeStart, value);
//			ranges.add(range);
//			rangeStart = value;
//		}
		for (Iterator iterator = source.iterator(); iterator.hasNext();) {
			Pair<Double,LocPriority> pair = (Pair<Double,LocPriority>) iterator.next();
			double value = (Math.round((pair.getT2().getPriority()/sum)*100.0)/100.0)% 1;
			Range<Double> range;
			if(iterator.hasNext()) {
				if(value< rangeStart) {
					range = Ranges.closedOpen(value, rangeStart);					
				} else
					range = Ranges.closedOpen(rangeStart, value);
			} else {
				range = Ranges.openClosed(rangeStart, 100d);
			}
			ranges.add(range);
			rangeStart = value;
		}
		
		double random = rnd.nextDouble();

		int counter = 0;
		for (Range<Double> range : ranges) {
			if(range.contains(random)) {
				return source.get(counter);
			}
			counter++;
		}
	
		return null;
	}

	/**
	 * Old implementation of method getNearesDestination not oplimalize please use getNearesDestination
	 * @param botPos
	 * @param storageLocations
	 * @param number wanted size
	 * @return List of nearest storage location to botBos
	 */
	private List<Pair<Double,LocPriority>> getNearesDestination2(Pos botPos,List<LocPriority> storageLocations, int number) {
		List<Pair<Double,LocPriority>> result = new ArrayList<Pair<Double,LocPriority>>();
		List<LocPriority> storageLocationsTmp = new ArrayList<LocPriority>(storageLocations);
		//result.add(getNearesDestination)
		for (int i = 0; i < number; i++) {
			Pair<Double,LocPriority> first = getNearesDestination(botPos,storageLocationsTmp);
			storageLocationsTmp.remove(first.getT2());
			result.add(first);			
		}
//		Pair<Double,LocPriority> first = getNearesDestination(botPos,storageLocationsTmp);
//		storageLocationsTmp.remove(first.getT2());
//		Pair<Double,LocPriority> second = getNearesDestination(botPos,storageLocationsTmp);
//		result.add(first);
//		result.add(second);
		return result;
	}
	
	/**
	 * 
	 * @param botPos
	 * @param storageLocations
	 * @param number size of returned List
	 * @return List of nearest storage location to botBos
	 */
	private List<Pair<Double,LocPriority>> getNearesDestination(Pos botPos,List<LocPriority> storageLocations, int number) {
		List<Pair<Double,LocPriority>> bestResult = new ArrayList<Pair<Double,LocPriority>>();
		
		for (LocPriority locPriority : storageLocations) {
			
			double dis = path.getPathLength(botPos, locPriority.getPos());
			Pair<Double, LocPriority>  haveBetter = haveWorstResult(dis,bestResult);
			if(haveBetter == null && bestResult.size() < number) {
				bestResult.add(new Pair<Double, LocPriority>(dis, locPriority));
			} else if(bestResult.size() < number) {
				bestResult.add(new Pair<Double, LocPriority>(dis, locPriority));
			} else if(haveBetter != null) {
				bestResult.remove(haveBetter);
				bestResult.add(new Pair<Double, LocPriority>(dis, locPriority));
			}			
		}		
		return bestResult;
	}

	/**
	 * For value newDis return worst value in bestResult
	 * @param newDis
	 * @param bestResult
	 * @return Pair of value and storage location
	 */
	private Pair<Double, LocPriority> haveWorstResult(double newDis,
			List<Pair<Double, LocPriority>> bestResult) {
		Pair<Double, LocPriority> worst = null;
		double worstWalue = 0d;
		for (Pair<Double, LocPriority> locPriority : bestResult) {
			double dis = locPriority.getT1();
			if(dis > worstWalue) {
				worst = locPriority;	
				worstWalue = locPriority.getT1();
			}
		}
		if(newDis < worstWalue){
			return worst;
		}
		return null;		
	}

	/**
	 * Return single nearest sotrage location to botPos
	 * @param botPos
	 * @param storageLocations
	 * @return
	 */
	private Pair<Double,LocPriority> getNearesDestination(Pos botPos,
			List<LocPriority> storageLocations) {
		LocPriority best = null;
		double bestDis = Double.MAX_VALUE;
		for (LocPriority locPriority : storageLocations) {
			double dis = path.getPathLength(botPos, locPriority.getPos());
//			List<Pos> poss= path.getPath(botPos, locPriority.getPos());
//			log.info("Location "+);
			if(dis < bestDis) {
				bestDis = dis;
				best = locPriority;
			}
		}
		return new Pair<Double, LocPriority>(bestDis, best);
	}
}