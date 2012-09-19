package cma.store.env;

import java.util.Random;

import cma.store.control.PenaltyInfoIfc;
import cma.store.control.mvc.MvcDeliveryTimeCalculator;
import cma.store.control.opt.route.RouteCreator;
import cma.store.data.Bot;
import cma.store.data.BotFleet;
import cma.store.data.BotPositionPredictor;
import cma.store.data.LayerModel;
import cma.store.data.Model;
import cma.store.data.MvcFleet;
import cma.store.data.Pos;
import cma.store.input.request.BaseRequestCreator;
import cma.store.schedule.Schedule;
import cma.store.stat.Statistics;

/**
Warehouse optimizer.
creating date: 2012-07-17
creating time: 14:25:45
autor: Czarek
 */

public class Environment extends BaseEnvironment implements Timer {
	
	public static boolean DEBUG = true;
	
	public static boolean DEBUG_VIEW = false;
//	
	public static boolean ALLOW_ROUTES_COLISIONS = true;
	
	public static boolean DEEP_CHECK = false;
	
	public static long DEFAULT_TIME_ACCURACY = BaseEnvironment.DEFAULT_TIME_ACCURACY;
	
	private long timeUnitMs = BaseEnvironment.DEFAULT_TIME_UNIT_MS;
	
	private long currentTimeMs;
	private Random rnd;

	private Model model;
	
//	private BotFleet fleet;
	private BaseRequestCreator baseRequestCreator;
//	private LayerModel layerModel;
	private Schedule schedule;
	private PenaltyInfoIfc info;
	private RouteCreator routeCreator;
	private BotPositionPredictor botPositionPredictor;
//	private Statistics statistics;
	private long seed;
	private MvcDeliveryTimeCalculator mvcDeliveryTimeCalculator;
	
	public static Environment env;
	
	public Environment( long seed ){
		this.seed = seed;
		rnd = new Random(seed);
//		statistics = new Statistics(this);
		this.env = this;
	}
	
	public boolean isDebugMode(){
		return DEBUG;
	}
	
	public boolean isDebugViewMode(){
		return DEBUG_VIEW;
	}
	
	public boolean isDeepCheck(){
		return DEEP_CHECK;
	}
	
	public boolean allowCollision(){
		return ALLOW_ROUTES_COLISIONS;
	}
	
	public static Environment getInstance() {
		return env;
	}
	
	//distans which prohibit colision
	public double getSaveDistance(){
		return getLayerModel().getUnitSize()*1.5;
	}
	
	public double getAccuracy(){
		return 0.1;
	}

	
	public long getSeed(){
		return seed;
	}
	
//	public Statistics getStatistics(){
//		return statistics;
//	}
	
	
	public void setTime( long timeMs ){
		currentTimeMs = timeMs;
	}

	/**
	 * We will process with system state in discret time snapshots.
	 * @return minimal time units
	 */
	public long getTimeUnitMs(){
		return timeUnitMs;
	}
	
	public long getTimeMs(){
		return currentTimeMs;
	}
	
	public Model getModel() {
		return model;
	}

	public Environment setModel(Model model) {
		this.model = model;
		return this;
	}

	public BotFleet getBotFleet() {
		return model.getBotFleet();
	}
	
	public MvcFleet getMvcFleet() {
		return model.getMvcFleet();
	}


	public BaseRequestCreator getBaseRequestCreator() {
		return baseRequestCreator;
	}


	public Environment setBaseRequestCreator(BaseRequestCreator baseRequestCreator) {
		this.baseRequestCreator = baseRequestCreator;
		return this;
	}


	public LayerModel getLayerModel() {
		return model.getLayerModel();
	}
	

	public RouteCreator getRouteCreator() {
		return routeCreator;
	}


	public Environment setRouteCreator(RouteCreator routeCreator) {
		this.routeCreator = routeCreator;
		return this;
	}


	public BotPositionPredictor getBotPositionPredictor() {
		return botPositionPredictor;
	}


	public Environment setBotPositionPredictor(BotPositionPredictor botPositionPredictor) {
		this.botPositionPredictor = botPositionPredictor;
		return this;
	}
	
	public Schedule getSchedule(){
		return schedule;
	}
	

	public Environment setSchedule(Schedule schedule) {
		this.schedule = schedule;
		return this;
	}
	
	public PenaltyInfoIfc getPenaltyInfo(){
		return info;
	}
	
	public Environment setPenaltyInfo( PenaltyInfoIfc info){
		 this.info=info;
		 return this;
	}	

	public Bot createBot( int x, int y ){
		return model.getLayerModel().createBot(x, y);
	}
	
	public Bot createBotDouble( double x, double y ){
		return model.getLayerModel().createBotDouble(x, y);
	}
	
	public Pos createPos( int x, int y ){
		return model.getLayerModel().createPos(x, y);
	}	
	
	public MvcDeliveryTimeCalculator getMvcDeliveryTimeCalculator(){
		if( mvcDeliveryTimeCalculator==null ){
			mvcDeliveryTimeCalculator = new MvcDeliveryTimeCalculator(this);
		}
		return mvcDeliveryTimeCalculator;
	}	

}
