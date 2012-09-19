package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cma.store.control.teamplaninng.HMPCStategy;
import cma.store.control.teamplaninng.TeamPlaninngContext;
import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.data.Pos;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;

public class HMPCStategyTest_00 extends RouteChooseAbs {

	private static final int TASKS_NUM = 10;
	Random rnd = new Random();
	
	public HMPCStategyTest_00(String name, long seed) {
		super(seed);
	}

	@Override
	public List<Bot> createBots(){
		List<Bot> bots = new ArrayList<Bot>();
		bots.add( createBot( 20,0) );
		bots.add( createBot( 4,0) );
		bots.add( createBot( 25,0) );
		bots.add( createBot( 35,0) );
		
//		bots.add( createBot( 39,0) );
//		bots.add( createBot( 42,0) );
//		bots.add( createBot( 50,0) );
//		bots.add( createBot( 60,0) );
		return bots;
	}

	@Override
	public List<BaseRequest> getBaseRequest() {
		int orderId = 1;
		List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();
		
		// MVCs
		Pos mvc1 = createPos(10,0);
		Pos mvc2 = createPos(30,0);
		Pos[] mvc = new Pos[2];
		mvc[0] = mvc1; mvc[1] = mvc2;
		ArrayList<Pos> loc = new ArrayList<Pos>();
//		int[] xx = {0,20,40,60,80};
		int[] xx = {0,20,39};
		for (int i = 0; i < TASKS_NUM; i++) {
//			loc.add(createPos(29*rnd.nextInt(1), 10 + rnd.nextInt(20)));
			loc.add(createPos(xx[rnd.nextInt(3)], 10 + rnd.nextInt(10)));
			
			List<LocPriority> productList = new ArrayList<LocPriority>();
			productList.add(new LocPriority(loc.get(i), 0.99));
			BaseRequest br = new BaseRequest(orderId++, new Mvc(mvc[i%2], MvcType.INPUT), productList);
			baseRequestList.add(br);
		}
		
		return baseRequestList;
	}



	public static void main(String[] args) {
		HMPCStategyTest_00 t = new HMPCStategyTest_00("Test_1",123);
		t.setTeamPlaninngContext(new TeamPlaninngContext( new HMPCStategy(t.env)));
		t.runTest();

	}

}
