package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cma.store.control.Controller;
import cma.store.control.PenaltyInfo;
import cma.store.control.opt.route.KirillRouteCreator;
import cma.store.control.opt.route.RouteCreator;
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

public class HMPCLayoutTest extends RouteChooseAbs {
	private static final int BOTS_NUM = 4;
	private static final int REQUESTS_NUM = 6;
	static int orderId = 0;

	public HMPCLayoutTest(String name, long seed) {
		super(seed);
	}
	
	protected void initTestCase() {
		env = new Environment(seed);
		Schedule schedule = new Schedule(env,null);
		PenaltyInfo info = new PenaltyInfo(env);
		env.setSchedule(schedule)
			.setPenaltyInfo(info);
		if(env.getModel() == null) {
			Model model = ModelFactory.getModelFactory().getTestCaseModel_HMPC();
			env.setModel(model);
		}

		final RouteCreator routeCreator = new SimpleRouteCreator(env);
		env.setRouteCreator(routeCreator);
		
		BotPositionPredictor botPositionPredictor = new BotPositionPredictor(env);
		
		env.setBotPositionPredictor(botPositionPredictor);
		env.getModel().setBotFleet( new BotFleet() );
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
		
		for(int i=0; i<BOTS_NUM; i++){
			
			Pos pos = null;
			for(int j=0; j<100; j++){
				PlanItem pi = routes.get( rnd.nextInt( routes.size() ) );
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
		Random rnd = new Random(seed);
		
		for(int i=0; i<REQUESTS_NUM; i++){
			BaseRequest br = new BaseRequest( i, mvc1, getLocPriorities(4) );
			baseRequestList.add(br);
		}
		
		return baseRequestList;
	}



	public static void main(String[] args) {
		HMPCLayoutTest t = new HMPCLayoutTest("Test_1", 123);
		
		try{
			t.runTest();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


}
