package cma.store.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.management.RuntimeErrorException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import cma.store.config.SettingProperties;
import cma.store.control.opt.RandomRealizer;
import cma.store.control.opt.RealizationException;
import cma.store.control.opt.Realizer;
import cma.store.control.opt.RealizerVer1;
import cma.store.control.opt.RealizerVer2;
import cma.store.control.opt.RealizerVer3;
import cma.store.control.opt.RealizerVer4;
import cma.store.control.opt.TeemRequestRealizer;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.route.allshortestpaths.ShortestPathsContext;
import cma.store.control.teamplaninng.TeamPlaninngContext;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.env.Environment;
import cma.store.exception.NoFreeBotException;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.BaseRequestCreator;
import cma.store.input.request.DefinedBaseRequestCreator;
import cma.store.schedule.Schedule;
import cma.store.serialization.FileSerializer;
import cma.store.utils.Conflict;
import cma.store.utils.ConflictFinder;

/**
Warehouse optimizer.
creating date: 2012-07-17
creating time: 16:31:22
autor: Czarek
 */

public class Controller extends Thread{
	public static final boolean CMA_CONFIG = true;
	private static double SPEED_UP = 0.8; //1 ==> real time
	private static final String throwOnBadOrderError = "throw.on.bad.order.error";
	private static final String throwOnCollisionError = "throw.on.collision.error";
	private static final String MODEL_SPEED = "model.speed";
	public static final String REALISTIC_TIMES = "use.realistic.times";
	
	Environment env;
	BaseRequestCreator requestCreator;
	List<BaseRequest> requests;
	TeemRequestRealizer teamRealizer;
	Realizer requestRealizer;
	private boolean work = true;
	TeamPlaninngContext teamPlaninngContext;
	List<BaseRequest> requestsArchive;
	PropertiesConfiguration config;
	
	Logger logger = Logger.getLogger(getClass());
	private Boolean paused = false;
	
	public TeamPlaninngContext getTeamPlaninngContext() {
		return teamPlaninngContext;
	}

	public void setTeamPlaninngContext(TeamPlaninngContext teamPlaninngContext) {
		this.teamPlaninngContext = teamPlaninngContext;
	}

	ShortestPathsContext shortestPathsContext;
	boolean throwOnBadOrder;
	boolean throwOnCollision;
	boolean stoped;
	
	public Controller( Environment env, BaseRequestCreator requestCreator ){
		this.env = env;
		this.requestCreator = requestCreator;
		
		//this.teamPlaninngContext = new TeamPlaninngContext(new KirillStrategy(env));
		//this.shortestPathsContext = new ShortestPathsContext(new FloydWarshallAlgorithm(env));
		this.shortestPathsContext = null;
		
		requests = new ArrayList<BaseRequest>();
		requestsArchive = new ArrayList<BaseRequest>();
		
		Properties properties = new Properties();
		try {
			URL url =  ClassLoader.getSystemResource(FileSerializer.fileName);
		    properties.load(new FileInputStream(new File(url.getFile())));
		} catch (IOException e) {
			logger.error("", e);
		}
		throwOnBadOrder = Boolean.parseBoolean(properties.getProperty(throwOnBadOrderError, "false"));
		throwOnCollision = Boolean.parseBoolean(properties.getProperty(throwOnCollisionError, "false"));
		
		try {
			config = new PropertiesConfiguration(FileSerializer.fileName);
		} catch (ConfigurationException e) {
			logger.error("",e);
		}
		Double speed = (Double.parseDouble((String) SettingProperties
				.getInstance().getValue("external.properties",
						Controller.MODEL_SPEED)));
		setSpeed(speed);
	}
	
	private void stopIt(){
		work=false;
	}
	
	public boolean done(){
		return !work;
	}

	public void run(){
		env.setTime(0);

		this.runSolutions();
	}
	
	public void runSolutions(){
		
		long timeUnit = env.getTimeUnitMs();
		long sleep = (long)(timeUnit/getSpeed());
		
		
		if( CMA_CONFIG ){
			requestRealizer = new RealizerVer4(env);
//			requestRealizer = new BackwardsRealizer(env);
			teamRealizer = new TeemRequestRealizer(env);
		}
		
		
		while(work){
			sleep = (long)(timeUnit/getSpeed());
			updateFleet();   	//fleet position

			updateSchedule();	//update paths of cars depends of their current positions

			if( CMA_CONFIG ){
				acquireRequests();  //new requests
			}else{
				acquireRequestsWithTeamPlanning();
			}
			
			if( CMA_CONFIG ){
				processRequests();  //translate new requests into rovers orders
			}else{
				processRequestsWithTeamPlanning();				
			}
			
			checkCollisions();

			//CMA change
//			monteCarloProcessRequests();
//			updateStatistics(); //update statististics
			
			if(isStoped()) {
				this.stopIt();
			}
			try {
				while(isPaused()){
					Thread.sleep(50);					
				}
				if(SPEED_UP != 0) {
					Thread.sleep(sleep);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error("", e);
			}
			

			env.setTime( env.getTimeMs()+timeUnit );
//			logger.debug(""+requestCreator.getRequestsWithoutDelete().size());
			//Nothing more to do
			if( requestCreator.done() && env.getSchedule().done() && (requests==null || requests.size()==0)){
				work=false;
				logger.debug("work=false");
				logger.debug(" requestCreator.done() : " +  requestCreator.done());
				logger.debug(" env.getSchedule().done() : " +  env.getSchedule().done());
			}

			//zmiana w shedulerze wynika nie tylko z failures
			//ale rowniez z roznicach w predkosci botow.
			//jezeli polozenie bota rozni sie od zalozonego o 
			//wiecej niz zakladana tolerancja
			//nalezy zmodyfikowac schedule.
			//boty nic nie robiace stanowia przeszkody rowniez i to powazniejsze niz boty ruchome.
		}

		checkFinalOrder();
	}
	
	private void checkCollisions(){
		long time = env.getTimeMs();
		Conflict conflict =  ConflictFinder.getConflict( time, env.getSchedule(), env );
		if( conflict!=null ){
			logger.error("Collision " + conflict );
			ConflictFinder.getConflict( time, env.getSchedule(), env );
			if (throwOnCollision)
				throw new RuntimeException("Collision in runtime detected: " + conflict);
		}
	}

	private List<BaseRequest> getRequestsForMvc(Mvc mvc, List<BaseRequest> reqList) {
		List<BaseRequest> ret = new ArrayList<BaseRequest>();
		for (BaseRequest br : reqList) {
			if (br.getMvc().equals(mvc)) {
				ret.add(br);
			}
		}
		return ret;
	}
	
	private boolean checkFinalOrderForMVC(Mvc mvc) {
		logger.debug("Checking realizations order for MVC " + mvc);
		List<BaseRequest> initList = requestsArchive;
		List<BaseRequest> finalList = env.getSchedule().getRealizedRequests();
		initList = getRequestsForMvc(mvc, initList);
		finalList = getRequestsForMvc(mvc, finalList);
		int size = initList.size();
		if (size != finalList.size()) {
			logger.error("Error in checkFinalOrderForMVC: sizes of init (" + size
					+ ") and final (" + finalList.size() + ") are different!");
			return false;
		}
		for (int i = 0; i < size; i++) {
			if (initList.get(i).getOrderId() != finalList.get(i).getOrderId()) {
				logger.error("Error in checkFinalOrderForMVC: order(" + finalList.get(i).getOrderId()
						+ ") is before order(" + initList.get(i).getOrderId() + ")");
				return false;
			}
		}
		return true;
	}
	
	private void checkFinalOrder() {
		logger.debug("Final realizations order:");
		List<BaseRequest> rr = env.getSchedule().getRealizedRequests();
		for (BaseRequest br : rr) {
			logger.debug("\torder(" + br.getOrderId() + ")");
		}
		List<Mvc> mvcs = env.getMvcFleet().getInputMvcs(); // TODO: this should be OUTPUT, but everywhere in the code we have switch
		for (Mvc m : mvcs) {
			if (!checkFinalOrderForMVC(m)) {
				String error = "Order for mvc (" + m + ") is broken";
				logger.error(error);
				if (throwOnBadOrder) {
					throw new RuntimeException(error);
				}
				
				env.getSchedule().getStat().notifyWrongOrder();
			}
			else {
				logger.debug("Order for mvc (" + m + ") is fine");
			}
		}
		
		return;
	}
	
//	private void updateStatistics() {
//		// TODO Auto-generated method stub
//		
//	}
	
	public void processRequests() {

		if( requests == null || requests.size()==0) return;
		
		try {
			//teamRealizer.findRealizations(requests);
			requestRealizer.findRealization(requests);
			requests=null;
		}catch( RealizationException e ){
			e.printStackTrace();
		}

//		if( requests == null ) return;
//		
//		for(BaseRequest br: requests){
//			RequestRealization rr;
//			try {
//				logger.debug("request realization " +env.getTimeMs()+" "+br.getOrderId());
//				
//				rr = requestRealizer.realize(br);
//			} catch (NoFreeBotException e) {
//				List<BaseRequest> request = new ArrayList<BaseRequest>();
//				request.add(br);
//				
//				((DefinedBaseRequestCreator)requestCreator).addBaseRequestList(env.getTimeMs()+br.getTimeToReschedule(), request);	
//				br.setTimeToReschedule(0);
//				logger.error(e);
//				//System.out.println("Can't process BaseRequest " + br);
//				continue;
//			}
//		}
	}


	
	private void processRequestsWithTeamPlanning() {
		logger.debug("processRequestsWithTeamPlanning Time: "+env.getTimeMs()+" Requests size: - "+requests.size());
		if( requests == null ) return;
		if(requests.size() > 0){
			if(teamPlaninngContext != null) {
				logger.info("startTeamPlaninng");
				teamPlaninngContext.assingRequestToBots(requests, env.getSchedule().getAvailableCars());	
			} else {
				logger.info("No TeamPlaninngContext");
			}
		}
//		try {
////			teamRealizer.findRealizations(requests);
//		} catch (NoFreeBotException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		for(int i = 0; i < requests.size(); i++) {
//			BaseRequest br = requests.get(i);
////			logger.debug("i: " + i + " br: " + br);
//			if (br.getRequestRealization() != null) {
//				// br was evaluated
//				logger.debug("Remove Order ID : "+ br.getOrderId());
//				env.getSchedule().addRoutes( br.getRequestRealization().getRoutes() );
//				requests.remove(i);
//				i--;				
//			}
//		}

	}
	
	private void monteCarloProcessRequests() {
		startTeamPlaninng();
		for(BaseRequest br: requests){
			env.getSchedule().addRoutes( br.getRequestRealization().getRoutes() );
		}
	}

	public void acquireRequests() {
//		if( stop ) {
//			requests=null;
//
//			return;
//		}
		requests = requestCreator.getRequests();
		

//		int botsCount =0;
		int botsCount = env.getModel().getBotFleet().getBots().size();
//		int botsCount = env.getSchedule().getBotsReadyToRambo().size();

//				int requestsProcessingMax = Math.max( 10, botsCount*3 );
		int requestsProcessingMax = botsCount*2;
//		int requestsProcessingMax = botsCount;	
		
		if(requestsArchive.size() > 0 && requests.size() > 0) {
			int requestToBackward = Math.round(requestsProcessingMax/2);
			List<BaseRequest> backList = new ArrayList<BaseRequest> (requestsArchive.subList(
					requestsArchive.size() - requestToBackward,
					requestsArchive.size()));
			if(backList.size()>0) {
				BaseRequest first = backList.get(0);
				env.getSchedule().deleteRouteForRequest(first);
				Collections.reverse(backList);
				
				for (BaseRequest baseRequest : backList) {
					requests.add(0, baseRequest);
					requestsArchive.remove(baseRequest);
				}
			}
			
		}
		
		if(requests.size() > requestsProcessingMax) {
			List<BaseRequest> reshedule = new ArrayList<BaseRequest>(requests.subList(requestsProcessingMax,requests.size()));
			requests = new ArrayList<BaseRequest>(requests.subList(0, requestsProcessingMax));
			((DefinedBaseRequestCreator) requestCreator).addBaseRequestListAtBeginning(env.getTimeMs()+1000, reshedule);		
		}
		
		for (BaseRequest br : requests) {
			if (!requestsArchive.contains(br))
				requestsArchive.add(br);
		}
//		startTeamPlaninng();

//		if( requests!=null && requests.size()>0 ){
//			stop=true;
//		}	
	}
	
	private void acquireRequestsWithTeamPlanning() {
		logger.debug("acquireRequestsWithTeamPlanning");
		List<BaseRequest> pendingRequests = requestCreator.getRequests();
		requests.addAll(0, pendingRequests);
	}
	
	private void startTeamPlaninng() {
		if(teamPlaninngContext != null) {
			logger.info("startTeamPlaninng");
			teamPlaninngContext.assingRequestToBots(requests, env.getSchedule().getAvailableCars());	
		} else {
			logger.info("No TeamPlaninngContext");
		}
	}

	public void updateSchedule() {
		// TODO Auto-generated method stub
		
	}

	public void updateFleet() {
		
		env.getSchedule().updateFleet();
		
	}

	public Realizer getRequestRealizer() {
		return requestRealizer;
	}
	
	public synchronized void increaseSpeed() {
		setSpeed(getSpeed() + 0.35);
	}
	
	public synchronized void reduceSpeed() {
		double newVal =getSpeed() - 0.35;
		if(newVal<0) {
			newVal = 0.01;
		}
		setSpeed(newVal);		
	}

	public static double getSpeed() {
		return SPEED_UP;
	}

	public static void setSpeed(double speed) {
		SPEED_UP = speed;
	}

	public synchronized void pausePlay() {
		if(isPaused()) {
			setPaused(false);
		} else {
			setPaused(true);
		}
	}

	public Boolean isPaused() {
		return paused;
	}

	public void setPaused(Boolean paused) {
		this.paused = paused;
	}

	public boolean isStoped() {
		return stoped;
	}

	public void setStoped(boolean stoped) {
		this.stoped = stoped;
	}	
}
