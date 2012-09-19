package cma.store.control.opt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cma.store.control.Controller;
import cma.store.control.PenaltyInfo;
import cma.store.control.opt.route.KirillRouteCreator;
import cma.store.control.opt.route.RouteCreator;
import cma.store.control.opt.route.simple.SimpleRouteCreator;
import cma.store.control.teamplaninng.TeamPlaninngContext;
import cma.store.data.Bot;
import cma.store.data.BotPositionPredictor;
import cma.store.data.Model;
import cma.store.data.ModelFactory;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.env.Environment;
import cma.store.env.FinalPosTimer;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.DefinedBaseRequestCreator;
import cma.store.input.request.LocPriority;
import cma.store.output.StatisticsSaver;
import cma.store.schedule.Schedule;
import cma.store.view.DisplaySimple;

/**
creating date: 2012-05-21
creating time: 01:04:12
autor: Czarek
 */

public abstract class RouteChooseAbs {
	Controller controller;
	DisplaySimple display;
	DefinedBaseRequestCreator baseRequestCreator;
	TeamPlaninngContext teamPlaninngContext;

	Logger logger = Logger.getLogger(getClass());

	Environment env;
	long seed=0;
	long testSeed=0;
	String name;
	Random rnd;
	Random testRnd;
	boolean switchViewOn = true;

	public RouteChooseAbs(){
		this(0,0);
	}
	
	public RouteChooseAbs( long seed ){
		this(seed,0);
	}
	
	public RouteChooseAbs( long seed, long testSeed ){
		this.name = this.getClass().getSimpleName();
		setSeed(seed);
		setTestSeed(testSeed);
		initTestCase();
	}


	public TeamPlaninngContext getTeamPlaninngContext() {
		return teamPlaninngContext;
	}

	public void setTeamPlaninngContext(TeamPlaninngContext teamPlaninngContext) {
		this.teamPlaninngContext = teamPlaninngContext;
	}

	
	public void switchViewOn( boolean on ){
		this.switchViewOn = on;
	}
	
	public long getSeed(){
		return seed;
	}
	
	public void setSeed(long seed){
		this.seed = seed;
		if( this.seed==0 ) {
			this.seed = (new Random()).nextInt();
		}
		rnd = new Random(this.seed);
	}
	
	public void setTestSeed(long seed){
		this.testSeed = seed;
		if( this.testSeed==0 ) {
			this.testSeed = (new Random()).nextInt();
		}
		testRnd = new Random(this.testSeed);
	}
	
	
	//create Bots 
	public abstract List<Bot> createBots();
	
	public abstract List<BaseRequest> getBaseRequest();
	
	

	// Stats setup with valid layout, because layout is set in child class, not here
    protected void initStats() {
		Schedule schedule = new Schedule(env,null);
		schedule.initStats();
		env.setSchedule(schedule);
    }
	
	protected void initTestCase(){
		
		env = new Environment(seed);


		PenaltyInfo info = new PenaltyInfo(env);

		//RouteCreator routeCreator = new MatrixRouteCreator(env);
		//RouteCreator routeCreator = new SimpleRouteCreator(env);
		//RouteCreator routeCreator = new NoAccidentsSimpleRouteCreator(env);
//		RouteCreator routeCreator = new ShortRouteCreator(env);
	
//		env.setLayerModel(layerModel)
//			.setSchedule(schedule)
//			.setPenaltyInfo(info);
//		RouteCreator routeCreator = new KirillRouteCreator(env);// done in test initialization (setTeamPlaninngContext)


		BotPositionPredictor botPositionPredictor = new BotPositionPredictor(env);
		if(env.getModel() == null) {
			Model model = ModelFactory.getModelFactory().getTestCaseModel_1();
			env.setModel(model);
		}
		
		Schedule schedule = new Schedule(env,null);

		env.setSchedule(schedule)
			.setPenaltyInfo(info)

			.setBotPositionPredictor(botPositionPredictor);
		
		final RouteCreator routeCreator;
		
		if( Controller.CMA_CONFIG ){
			routeCreator = new SimpleRouteCreator(env);
		}else{
			routeCreator = new KirillRouteCreator(env);
		}

		env.setRouteCreator(routeCreator);
	}
	
	private void startTest(){
		
		env.setBaseRequestCreator(baseRequestCreator);
		
		
		controller = new Controller( env, baseRequestCreator );
		controller.setTeamPlaninngContext(teamPlaninngContext);

		if( switchViewOn ){
			if( env.isDebugViewMode() ){ //in the debug mode display is used for faster debuging
				display = new DisplaySimple( env, new FinalPosTimer() );
				display.start();
			}else{
				display = new DisplaySimple( env, env,controller );
				display.start();			
			}
		}
		
		
		controller.start();
		
		while(true){
			if( controller.done() ){
				break;
			}
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected Bot createBot( int x, int y ){
		return env.createBot(x, y);
	}
	
	protected Bot createBotDouble( double x, double y ){
		return env.createBotDouble(x, y);
	}
	
	protected Pos createPos( int x, int y ){
		return env.createPos(x, y);
	}
	
	public void runTest(){
		System.out.println( "Used seed = " + seed );
		logger.info("Used seed = " + seed );
//		initTestCase();

		//definition of number of cars initial positions
		//Bot bot1 = createBot(0,1);
		env.getModel().getBotFleet().getBots().clear();
		env.getModel().getBotFleet().addBots( createBots() );		
		
		//definition of list of request:
		//ordered product list
		baseRequestCreator = new DefinedBaseRequestCreator(env);
		List<BaseRequest> baseRequestList = getBaseRequest();
		long uruchom1 = 300;
		baseRequestCreator.addBaseRequestList( uruchom1, baseRequestList );
		
		//find solution
		
		startTest();
		
		System.out.println("Test "+name + " is done");
		
		printStatistics();
		
		System.out.println( "Used seed = " + seed );
		
		closeView();

	}
	
	private void closeView() {
		
		try {
			//wait before view is closed
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		if( display!=null ){
			display.stopIt();
		}
		
		
	}

	private void printStatistics() {
		System.out.println("Statistics:");
		System.out.println(""+env.getSchedule().getStat() );
		StatisticsSaver ss = new StatisticsSaver();
		try {
			ss.saveStatistics(env.getSchedule().getStat(), controller.getRequestRealizer().getClass().getSimpleName(),getClass().getSimpleName() ,seed );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ss.generateRPdfFile();
		
	}
	
	protected Mvc getMvc( MvcType type, int nr ){
		return env.getMvcFleet().getMvc(type, nr);
	}
	
	protected List<LocPriority> getLocPriorities( int count ){
		
		List<LocPriority> list = new ArrayList<LocPriority>(count);
		List<PlanItem> aisles = env.getModel().getLayerModel().getAisles();
		
		if( aisles.size()==0 ){
			throw new RuntimeException("No aisles defined");
		}
		
		for(int i=0; i<count; i++){
			PlanItem aisle = aisles.get( rnd.nextInt( aisles.size() ) );
			LocPriority lp = new LocPriority( aisle.getPos(), rnd.nextDouble() );
			list.add(lp);
			
		}
		return list;
		
	}


}
