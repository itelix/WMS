package cma.store.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;

import cma.store.control.Controller;
import cma.store.control.PenaltyInfo;
import cma.store.control.opt.route.RouteCreator;
import cma.store.control.opt.route.simple.SimpleRouteCreator;
import cma.store.data.Bot;
import cma.store.data.BotPositionPredictor;
import cma.store.data.Model;
import cma.store.data.ModelFactory;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.DefinedBaseRequestCreator;
import cma.store.input.request.LocPriority;
import cma.store.schedule.Schedule;
import cma.store.utils.ConflictFinder;

public abstract class BaseUnitTest {
	Controller controller;
	DefinedBaseRequestCreator baseRequestCreator;
	Environment env;
	long seed=0;
	Random rnd = new Random(seed);
	
	@Before
	public void setUp(){
		env = new Environment(seed);
		Model model = ModelFactory.getModelFactory().getTestCaseModel_1();
		
		Schedule schedule = new Schedule(env, null);
		PenaltyInfo info = new PenaltyInfo(env);
		//RouteCreator routeCreator = new MatrixRouteCreator(env);
		//RouteCreator routeCreator = new SimpleRouteCreator(env);
		//RouteCreator routeCreator = new NoAccidentsSimpleRouteCreator(env);
		RouteCreator routeCreator = new SimpleRouteCreator(env);
		
		BotPositionPredictor botPositionPredictor = new BotPositionPredictor(env);
		baseRequestCreator = new DefinedBaseRequestCreator(env);
		
		env.setModel(model)
			.setSchedule(schedule)
			.setPenaltyInfo(info)
			.setRouteCreator(routeCreator)
			.setBotPositionPredictor(botPositionPredictor);
		
		env.setBaseRequestCreator(baseRequestCreator);
		
		controller = new Controller( env, baseRequestCreator );
	}
	
	public List<BaseRequest> getBaseRequest(int requestsCount) {
		List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();
		Mvc mvc1 =  env.getMvcFleet().getMvc(MvcType.INPUT, 0);

		rnd = new Random(seed);
		
		for(int i=0; i<requestsCount; i++){
			BaseRequest br = new BaseRequest( i, mvc1, getLocPriorities(4) );
			baseRequestList.add(br);
		}
		
		return baseRequestList;
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
	
	public List<Bot> createBots(int botsCount){
		List<Bot> bots = new ArrayList<Bot>();
		List<PlanItem> routes = env.getModel().getLayerModel().getRoads();
		
		if( routes.size()==0 ){
			throw new RuntimeException("No aisles defined");
		}
		
		for(int i=0; i<botsCount; i++){
			
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
	
	private boolean isGoodStartPlace( Pos pos, List<Bot> bots ){
		for(Bot b: bots){
			if( ConflictFinder.isConflictPosition( pos, b.getPos(), env ) ){
				return false;
			}
		}
		return true;
	}
	
	protected Bot createBotDouble( double x, double y ){
		return env.createBotDouble(x, y);
	}
	
	
}
