/**
 * 
 */
package cma.store.control.opt;

import java.util.List;

import cma.store.control.opt.route.allshortestpaths.TravelTimeMap;
import cma.store.data.Pos;
import cma.store.data.PosDirected;

/**
 * @author Filip
 *
 */
public class TravelTimeMapTest extends FloydWarshallTest {
	TravelTimeMap travelTimeMap;
	public TravelTimeMapTest(long seed) {
		super(seed);
		travelTimeMap = TravelTimeMap.getInstance(env);
	}
	
	private void printResult(Pos p1, Pos p2) {
		logger.info("Looking for a path from " + p1 + " to " + p2);
		logger.info("\tPath: " + travelTimeMap.getPathWithOrientation(p1, p2));
		logger.info("\tTravel time: (" + p1 + " => " + p2 + ") = " + travelTimeMap.getTravelTime(p1, p2));
	}
	
	protected void runTest() {
		Pos p0 = env.createPos(20, 8);
		Pos p1 = env.createPos(20, 9);
		Pos p2 = env.createPos(0, 4);
		Pos p3 = env.createPos(40, 0);
		Pos p4 = env.createPos(60, 6);
		Pos p5 = env.createPos(20, 0);
		Pos p6 = env.createPos(60, 0);
		Pos p7 = env.createPos(0, 9);
		Pos p8 = env.createPos(29, 8);
		printResult(p0, p2);
		printResult(p1, p3);
		printResult(p5, p0);
		printResult(p5, p1);
		printResult(p6, p6);
		printResult(p7, p4);
		printResult(p7, p6);
		printResult(p7, p3);
		printResult(p7, p8);

		Pos p10 = env.createPos(0, 0);
		Pos p11 = env.createPos(0, 1);
		Pos p12 = env.createPos(1, 0);
		Pos p13 = env.createPos(2, 0);
		Pos p14 = env.createPos(0, 2);
		printResult(p10, p11);
		printResult(p11, p12);
		printResult(p10, p13);
		printResult(p10, p14);

		Pos p20 = env.createPos(20, 4);
		Pos p21 = env.createPos(20, 5);
		Pos p22 = env.createPos(20, 6);
		Pos p23 = env.createPos(20, 7);
		Pos p24 = env.createPos(20, 8);
		Pos p25 = env.createPos(20, 9);
		Pos p26 = env.createPos(20, 10);
		printResult(p20, p21);
		printResult(p20, p22);
		printResult(p22, p24);
		printResult(p22, p23);
		printResult(p24, p25);
		printResult(p24, p26);
	}
	
	public static void main(String[] args) {
		logger.debug("TravelTimeMapTest test starting...");
		TravelTimeMapTest t = new TravelTimeMapTest(DEFAULT_SEED);
		t.runTest();
		logger.debug("TravelTimeMapTest test done.");
	}
}
