package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cma.store.control.PenaltyInfo;
import cma.store.control.opt.route.RouteCreator;
import cma.store.control.opt.route.simple.BackwardsRouteCreator;
import cma.store.control.opt.route.simple.SimpleRouteCreator;
import cma.store.data.Bot;
import cma.store.data.BotFleet;
import cma.store.data.BotPositionPredictor;
import cma.store.data.LayerModel;
import cma.store.data.LayerModelFactory;
import cma.store.data.Model;
import cma.store.data.ModelFactory;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.schedule.Schedule;
import cma.store.utils.ConflictFinder;

/**
creating date: 2012-05-21
creating time: 01:04:12
autor: Czarek
 */

public class RouteChooseTest_UserDef extends  RouteChooseAbs{
	int botsCount;
	int requestsCount;
	int layout;
	static int orderId = 0;

	public RouteChooseTest_UserDef(long seed, int botsCount, int requestsCount, int layout, long testSeed ) {
		super(seed,testSeed);
		this.botsCount = botsCount;
		this.requestsCount = requestsCount;
		this.layout = layout;
		setTestCase();
	}

	protected void initTestCase() {
	}
	
	private void setTestCase() {
		env = new Environment(seed);
		Schedule schedule = new Schedule(env,null);
		PenaltyInfo info = new PenaltyInfo(env);
		env.setSchedule(schedule)
			.setPenaltyInfo(info);
		Model model;
		if(env.getModel() == null) {
			switch (layout) {
			case 0:
				model = ModelFactory.getModelFactory().getTestCaseModel_1();
				break;
			case 1:
				model = ModelFactory.getModelFactory().getTestCaseModel_3();
				break;
			case 2:
				model = ModelFactory.getModelFactory().getTestCaseModel_HMPC();
				break;
			default:
				model = ModelFactory.getModelFactory().getTestCaseModel_HMPCNonDirected();
				break;
			}
			env.setModel(model);
		}


		final RouteCreator routeCreator = new SimpleRouteCreator(env);
//		final RouteCreator routeCreator = new BackwardsRouteCreator(env);
		env.setRouteCreator(routeCreator);
		
		BotPositionPredictor botPositionPredictor = new BotPositionPredictor(env);
		
		env.setBotPositionPredictor(botPositionPredictor);
		env.getModel().setBotFleet( new BotFleet() );
		
		initStats();
	}
	
	private boolean isGoodStartPlace( Pos pos, List<Bot> bots ){
		for(Bot b: bots){
			if( ConflictFinder.isConflictPosition( pos, b.getPos(), env ) ){
				return false;
			}
		}
		return true;
	}


	@Override
	public List<Bot> createBots(){
		List<Bot> bots = new ArrayList<Bot>();
		List<PlanItem> routes = env.getModel().getLayerModel().getRoads();
		
		if( routes.size()==0 ){
			throw new RuntimeException("No aisles defined");
		}
		
		for(int i=0; i<botsCount; i++){
			
			Pos pos = null;
			for(int j=0; j<100; j++){
				PlanItem pi = routes.get( testRnd.nextInt( routes.size() ) );
				if( isGoodStartPlace(pi.getPos(),bots) ){
					pos = pi.getPos();
					break;
				}
			}
			
			if( pos==null ){
				for( PlanItem pi: routes ){
					if( isGoodStartPlace(pi.getPos(),bots) ){
						pos = pi.getPos();
						break;
					}
				}
			}
			if( pos==null ){
				throw new RuntimeException("Can't allocat so many bots in this layout");
			}
			bots.add( createBotDouble( pos.x, pos.y ) );
		}
		return bots;
	}

	@Override
	public List<BaseRequest> getBaseRequest() {
		List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();

		Mvc mvc1 = getMvc(MvcType.INPUT,0);
		Random rnd = new Random(testSeed);
		
		for(int i=0; i<requestsCount; i++){
			BaseRequest br = new BaseRequest( i, mvc1, getLocPriorities(4) );
			baseRequestList.add(br);
		}
		
		return baseRequestList;
	}



	public static void main(String[] args) {

		if( args.length < 3 ){
			System.out.println("Program required following parameters: ");
			System.out.println("Num of bots (max=10) ");
			System.out.println("Num of tasks (max=10) ");
			System.out.println("Seed Environment - optional (default is 0 - what cause creating random seed)");
			System.out.println("Seed Test - optional (default is 0 - what cause creating random seed)");
			System.out.println("Layout - value 0 (simple) or 1 (3 aleys)");
			return;
		}
		
		Integer bots = new Integer(args[0]);
		Integer requests = new Integer(args[1]);
		
		if( bots>10 ) bots=10;
//		if( requests>10 ) requests=10;
		
		long seed = 0;
		
		if( args.length >2 ){
			seed =  new Integer(args[2]);
		}
		
		long testSeed = 0;
		if( args.length >3 ){
			testSeed =  new Integer(args[3]);
		}
		
		Integer layoutParameter = 0;
		if( args.length >4 ){
			layoutParameter = new Integer(args[4]);
		}
			
		if(layoutParameter>4)
			layoutParameter = 0;
	
		
		RouteChooseTest_UserDef t = new RouteChooseTest_UserDef(seed,bots,requests,layoutParameter,testSeed );
		
		try{
			t.runTest();
		}catch(Exception e){
			e.printStackTrace();
//			System.out.println( "Seed = " + t.getSeed() );
		}
	}



}
