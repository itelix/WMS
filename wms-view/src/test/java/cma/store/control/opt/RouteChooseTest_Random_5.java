package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.data.Pos;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;

/**
creating date: 2012-05-21
creating time: 01:04:12
autor: Czarek
 */

public class RouteChooseTest_Random_5 extends  RouteChooseAbs{

	public RouteChooseTest_Random_5(long seed) {
		super(seed);
	}

	public RouteChooseTest_Random_5() {
		super();
	}
	@Override
	public List<Bot> createBots(){
		List<Bot> bots = new ArrayList<Bot>();
		bots.add( createBot( 10,0) );
		bots.add( createBot( 4,0) );
		bots.add( createBot( 6,0) );
		//bots.add( createBot( 8,0) );
		return bots;
	}
		


	@Override
	public List<BaseRequest> getBaseRequest() {
		List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();
		
		//BOT1
		//available product
		List<LocPriority> productList1 = new ArrayList<LocPriority>(); 
		productList1.add( new LocPriority( createPos(0,15), 0.99)  );
//		productList1.add( new LocPriority( createPos(15,8), 0.99)  ); 
		
		//destination
		Mvc mvc1 = getMvc(MvcType.INPUT,0);
		BaseRequest br1 = new BaseRequest( 1, mvc1, productList1 );
//		BaseRequest br4 = new BaseRequest( 1, mvc2, productList1 );
//		baseRequestList.add(br1);
//		baseRequestList.add(br4);
		
		//BOT2
		//available product
		List<LocPriority> productList2 = new ArrayList<LocPriority>(); 
		productList2.add( new LocPriority( createPos(0,10), 0.99) ); 
//		productList2.add( new LocPriority( createPos(7, 4), 0.99) ); 
		
		//destination
		Mvc mvc2 = getMvc(MvcType.INPUT,1);

		BaseRequest br2 = new BaseRequest( 2, mvc2, productList2 );
//		BaseRequest br3 = new BaseRequest( 3, mvc2, productList2 );
//		baseRequestList.add(br2);		
//		baseRequestList.add(br3);		
		
		//baseRequestList.add(br2);
		//baseRequestList.add(br3);
		
		
		Pos mvc11 = createPos(10,0); 
		Pos mvc22 = createPos(20,0);
		
		Mvc[] mvc =new  Mvc[2];
		mvc[0] = mvc1;
		mvc[1] = mvc2;
		Random rndSeed = new Random();
		int seed = rndSeed.nextInt(300);
	
		Random rnd = new Random(rndSeed.nextInt(154));
		int taskNumer = 10;
		for (int i = 0; i < taskNumer; i++) {
			List<LocPriority> productList11 = new ArrayList<LocPriority>(); 
			productList11.add( new LocPriority( createPos(0,rnd.nextInt(29)), 0.99)  );
			
			BaseRequest br11 = new BaseRequest( i, mvc[rnd.nextInt(2)], productList11 );
			baseRequestList.add(br11);
		}
		
		return baseRequestList;
	}



	public static void main(String[] args) {
		RouteChooseTest_Random_5 t = new RouteChooseTest_Random_5(123);
		//t.setTeamPlaninngContext(new TeamPlaninngContext( new NearestFreeBotStrategy(t.env)));
		t.runTest();

	}



}
