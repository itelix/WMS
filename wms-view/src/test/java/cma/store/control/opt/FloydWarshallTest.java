/**
 * 
 */
package cma.store.control.opt;

import org.apache.log4j.Logger;

import cma.store.control.PenaltyInfo;
import cma.store.control.opt.route.RouteCreator;
import cma.store.control.opt.route.allshortestpaths.FloydWarshallAlgorithm;
import cma.store.control.opt.route.allshortestpaths.ShortestPathsFinder;
import cma.store.control.opt.route.simple.SimpleRouteCreator;
import cma.store.data.BotFleet;
import cma.store.data.BotPositionPredictor;
import cma.store.data.Model;
import cma.store.data.ModelFactory;
import cma.store.data.Pos;
import cma.store.env.Environment;
import cma.store.schedule.Schedule;


/**
 * @author Filip
 *
 */
public class FloydWarshallTest {
	protected static final long DEFAULT_SEED = 0;
	protected ShortestPathsFinder shortestPathsFinder;
	private long seed;
	protected Environment env;
	private int layout = 3;
	protected static Logger logger = Logger.getLogger("FloydWarshallTest");
	
	protected void initTest() {
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
		env.setRouteCreator(routeCreator);
		
		BotPositionPredictor botPositionPredictor = new BotPositionPredictor(env);
		
		env.setBotPositionPredictor(botPositionPredictor);
		env.getModel().setBotFleet( new BotFleet() );
		
	}
	
	protected void runTest() {
		Pos p0 = env.createPos(0, 0);
		Pos p1 = env.createPos(1, 0);
		Pos p2 = env.createPos(0, 11);
		Pos p3 = env.createPos(29, 15);
		Pos p4 = env.createPos(0, 15);
		Pos p5 = env.createPos(20, 0);
		Pos p6 = env.createPos(13, 0);
		Pos p7 = env.createPos(0, 9);
		Pos p8 = env.createPos(29, 15);
		Pos p9 = env.createPos(40, 16);
		logger.info("Path: (" + p1 + " => " + p4 + ") = " + shortestPathsFinder.getPath(p1, p4));
		logger.info("Path length: (" + p1 + " => " + p4 + ") = " + shortestPathsFinder.getPathLength(p1, p4));
		logger.info("Path: (" + p4 + " => " + p5 + ") = " + shortestPathsFinder.getPath(p4, p5));
		logger.info("Path length: (" + p4 + " => " + p5 + ") = " + shortestPathsFinder.getPathLength(p4, p5));
		logger.info("Path: (" + p6 + " => " + p6 + ") = " + shortestPathsFinder.getPath(p6, p6));
		logger.info("Path length: (" + p6 + " => " + p6 + ") = " + shortestPathsFinder.getPathLength(p6, p6));
		logger.info("Path: (" + p6 + " => " + p9 + ") = " + shortestPathsFinder.getPath(p6, p9));
		logger.info("Path length: (" + p6 + " => " + p9 + ") = " + shortestPathsFinder.getPathLength(p6, p9));
		logger.info("Path: (" + p2 + " => " + p8 + ") = " + shortestPathsFinder.getPath(p2, p8));
		logger.info("Path length: (" + p2 + " => " + p8 + ") = " + shortestPathsFinder.getPathLength(p2, p8));
	}
	
	public FloydWarshallTest(long seed) {
		this.seed = seed;
		initTest();
		shortestPathsFinder = FloydWarshallAlgorithm.getInstance(env);
	}
	
	public static void main(String[] args) {
		logger.debug("FloydWarshall test starting...");
		FloydWarshallTest t = new FloydWarshallTest(DEFAULT_SEED);
		t.runTest();
		logger.debug("FloydWarshall test done.");
	}
}
