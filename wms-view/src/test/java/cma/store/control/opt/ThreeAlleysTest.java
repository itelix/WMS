package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cma.store.control.PenaltyInfo;
import cma.store.control.teamplaninng.KirillStrategy;
import cma.store.control.teamplaninng.TeamPlaninngContext;
import cma.store.data.Bot;
import cma.store.data.BotFleet;
import cma.store.data.BotPositionPredictor;
import cma.store.data.LayerModel;
import cma.store.data.LayerModelFactory;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.schedule.Schedule;

public class ThreeAlleysTest extends RouteChooseAbs {

	private static final int TASKS_NUM = 5;
	private Random rnd;
	Logger logger = Logger.getLogger(getClass());

	public ThreeAlleysTest(long seed) {
		super(seed);
		rnd = new Random(seed);
		initTestCase();
	}
	
	public ThreeAlleysTest() {
		super();
	}

	protected void initTestCase(){
		
		System.out.println("inintTestCase 3 alley");
		env = new Environment(seed);
		LayerModel layerModel = LayerModelFactory.getModelFactory().get3AlleysModel();
		Schedule schedule = new Schedule(env,null);
		PenaltyInfo info = new PenaltyInfo(env);
		env.setSchedule(schedule)
			.setPenaltyInfo(info);
		env.getModel().setLayerModel(layerModel);

		BotPositionPredictor botPositionPredictor = new BotPositionPredictor(env);
		
		env.setBotPositionPredictor(botPositionPredictor);
		env.getModel().setBotFleet( new BotFleet() );
	}
	
	@Override
	public List<Bot> createBots() {
		List<Bot> bots = new ArrayList<Bot>();
		bots.add( createBot( 20,0) );
		bots.add( createBot( 4,0) );
		return bots;
	}

	@Override
	public List<BaseRequest> getBaseRequest() {
		int orderId = 1;
		List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();
		
		LayerModel model = env.getLayerModel();
		// MVCs
		List<PlanItem> mvcPlanItems = model.getMvcLocations();
		Pos[] mvc = new Pos[mvcPlanItems.size()];
		for (int i = 0; i < mvcPlanItems.size(); i++) {
			mvc[i] = mvcPlanItems.get(i).getPos();
		}
		ArrayList<Pos> loc = new ArrayList<Pos>();
		for (int i = 0; i < TASKS_NUM; i++) {
//			int nextRnd = rnd.nextInt(model.getNumberOfAlleys());
//			loc.add(createPos((nextRnd == 0) ? 0 : model.getDistanceBetweenAlleys()*nextRnd - 1,
//					10 + rnd.nextInt(20)));
			loc.add(createPos(0, 10 + rnd.nextInt(20)));
			List<LocPriority> productList = new ArrayList<LocPriority>();
			productList.add(new LocPriority(loc.get(i), 0.99));
			BaseRequest br = new BaseRequest(orderId++, new Mvc(mvc[i%2], MvcType.INPUT), productList);
			baseRequestList.add(br);
		}
		
		return baseRequestList;
	}
	
	public static void main(String[] args) {
		ThreeAlleysTest t = new ThreeAlleysTest(123l);
		t.setTeamPlaninngContext(new TeamPlaninngContext( new KirillStrategy(t.env)));
		t.runTest();

	}

}
