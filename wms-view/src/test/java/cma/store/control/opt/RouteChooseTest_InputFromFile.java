/**
 * 
 */
package cma.store.control.opt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import cma.store.data.Model;
import cma.store.data.ModelFactory;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.data.PlanItem;
import cma.store.data.PlanItemType;
import cma.store.data.Pos;
import cma.store.env.BaseEnvironment;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.schedule.Schedule;
import cma.store.utils.ConflictFinder;
import cms.store.utils.PositionUtils;

/**
 * @author Filip
 *
 */
public class RouteChooseTest_InputFromFile extends RouteChooseAbs {
	private static final String FOLDER_NAME = ".";
	private static final String DEFAULT_FILE_NAME = "input.txt";
	private static final String SPACE = " ";
	String fileName;
	int botsCount;
	List<Pos> botLoc;
	List<Pos> prodLoc;
	List<Integer> mvcNum;
	int requestsCount;
	int layout;
	static int orderId = 0;
	Logger logger = Logger.getLogger(getClass());
	
	private void readRequests(int count, BufferedReader in) throws IOException {
		List<PlanItem> aisles = env.getModel().getLayerModel().getAisles();
		for (int i = 0; i < count; i++) {
			String s = in.readLine();
			if (s == null)
				throw new RuntimeException("Bad input file format!");
			String coord[] = s.split(SPACE);
			if (coord.length != 3)
				throw new RuntimeException("This is not a valid position! (" + s + ")");
			int alley = Integer.parseInt(coord[0]);
			int bay =  Integer.parseInt(coord[1]);
			LayerModel m = env.getLayerModel();
			//Check if alley and bay are valid
			int maxAlley = m.getColsNum() - 1;
			if (alley < 0 || alley > maxAlley)
				throw new RuntimeException("Invalid product location! (alley = " + alley
					+ " can't be outside [0, " + maxAlley + "]");
//			int maxBay = m.getSizeY() - m.getDistanceBetweenAvenues()*(m.getNumberOfAvenues() - 1) - 2;
			int maxBay = m.getSizeY() - m.getRow3() - 2;
			if (bay < 0 || bay > maxBay)
				throw new RuntimeException("Invalid product location! (bay = " + bay
					+ " can't be outside [0, " + maxBay + "]");
			
			int x = m.getDistanceBetweenCols()*alley;
//			int y = m.getDistanceBetweenAvenues()*(m.getNumberOfAvenues() - 1) + 1 + bay;
			int y = m.getRow3() + 1 + bay;
			Pos p = createPos(x, y);
			PlanItem pi = new PlanItem(x, y, PlanItemType.AISLE, BaseEnvironment.DEFAULT_DIST_UNIT_SIZE_MM);
			if (!aisles.contains(pi))
				throw new RuntimeException("Invalid product location! (alley: " + alley + ", bay: " + bay + ")");
			logger.debug("pos: " + p);
			prodLoc.add(p);
			mvcNum.add(Integer.parseInt(coord[2]));
		}
	}
	
	private void readPosList(int count, BufferedReader in, List<Pos> posList) throws IOException {
		for (int i = 0; i < count; i++) {
			String s = in.readLine();
			if (s == null)
				throw new RuntimeException("Bad input file format!");
			String coord[] = s.split(" ");
			if (coord.length != 2)
				throw new RuntimeException("This is not a valid position! (" + s + ")");
			Pos p = createPos(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
			logger.debug("pos: " + p);
			posList.add(p);
		}
	}
	
	private void writeParsedInput() {
		logger.debug("\t Input parsed from file: " + fileName);
		logger.debug("\t\t layout: " + layout);
		logger.debug("\t\t botsNum: " + botsCount);
		logger.debug("\t\t requestsNum: " + requestsCount);
		logger.debug("\t\t Bots: " + botLoc);
		logger.debug("\t\t Request: product locations: " + prodLoc + " mvcs => " + mvcNum);
	}
	
	private void processInputFile() throws IOException {
		File file = new File(FOLDER_NAME, fileName);
		FileReader inputFile = new FileReader(file);
		BufferedReader in = new BufferedReader(inputFile);

		// Layout
		String s = in.readLine();
		layout = Integer.parseInt(s);
		if (layout > 3 || layout < 0) {
			in.close();
			throw new IOException("Invalid layout type (" + layout + " not in {0, 1, 2, 3} set)");
		}
		setTestCase();
		
		// Bots
		s = in.readLine();
		botsCount = Integer.parseInt(s);
		if (botsCount > 10) {
			in.close();
			throw new IOException("Invalid number of bots (" + botsCount + " > 10)");
		}
		readPosList(botsCount, in, botLoc);
		
		// Products
		s = in.readLine();
		requestsCount = Integer.parseInt(s);
		readRequests(requestsCount, in);
		
		in.close();
		
		writeParsedInput();
	}
	
	public RouteChooseTest_InputFromFile(String name, String fileName) throws IOException {
		super(0);
		this.fileName = fileName;
		botLoc = new ArrayList<Pos>();
		prodLoc = new ArrayList<Pos>();
		mvcNum = new ArrayList<Integer>();
//		setTestCase();
		processInputFile();
	}

	private boolean isGoodStartPlace(Pos pos, List<Bot> bots){
		List<PlanItem> aisles = env.getModel().getLayerModel().getAisles();
		for (Bot b: bots){
			if (ConflictFinder.isConflictPosition(pos, b.getPos(), env)){
				return false;
			}
		}
		PlanItem pi = new PlanItem(PositionUtils.getIntX(pos.x),
				PositionUtils.getIntY(pos.y),
				PlanItemType.AISLE, BaseEnvironment.DEFAULT_DIST_UNIT_SIZE_MM);
		if (!aisles.contains(pi) && !env.getModel().getLayerModel().isRoad(pos))
			return false;
		return true;
	}
	
	@Override
	public List<Bot> createBots() {
		List<Bot> bots = new ArrayList<Bot>();
		
		for (int i=0; i<botsCount; i++){
			Pos pos = botLoc.get(i);
			if (!isGoodStartPlace(pos, bots)){
				throw new RuntimeException("Invalid bot's start position! (" + pos + ")");
			}
			bots.add(createBotDouble(pos.x, pos.y));
		}
		return bots;
	}

	public List<BaseRequest> getBaseRequest() {
		List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();
		
		Random rnd = new Random(seed);
		
		for(int i = 0; i < requestsCount; i++) {
			LocPriority lp = new LocPriority(prodLoc.get(i), rnd.nextDouble());
			List<LocPriority> list = new ArrayList<LocPriority>();
			list.add(lp);
			Mvc mvc = getMvc(MvcType.INPUT, mvcNum.get(i));
			if (mvc == null)
				throw new RuntimeException("Bad MVC number (" + mvcNum.get(i) + ")");
			BaseRequest br = new BaseRequest(i, mvc, list);
			baseRequestList.add(br);
		}
		
		return baseRequestList;
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

		
//		final RouteCreator routeCreator = new SimpleRouteCreator(env);
		final RouteCreator routeCreator = new BackwardsRouteCreator(env);
		env.setRouteCreator(routeCreator);
		
		BotPositionPredictor botPositionPredictor = new BotPositionPredictor(env);
		
		env.setBotPositionPredictor(botPositionPredictor);
		env.getModel().setBotFleet( new BotFleet() );
		
		initStats();
	}
	
	public static void main(String[] args) {
		String inputFile = DEFAULT_FILE_NAME;
		
		if( args.length != 1 ) {
			System.out.println("Program requires file name parameter!");
			System.out.println("Using default file name (" + DEFAULT_FILE_NAME +")");
		}
		else {
			inputFile = args[0];
		}
		
		RouteChooseTest_InputFromFile t;
		try {
			t = new RouteChooseTest_InputFromFile("RouteChooseTest_InputFromFile", inputFile);
			t.runTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
