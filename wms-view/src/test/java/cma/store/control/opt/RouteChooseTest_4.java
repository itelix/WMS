package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cma.store.control.teamplaninng.KirillStrategy;
import cma.store.control.teamplaninng.TeamPlaninngContext;
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

public class RouteChooseTest_4 extends  RouteChooseAbs{

	public RouteChooseTest_4(long seed) {
		super(seed);
	}

	public RouteChooseTest_4() {
		super();
	}
	@Override
	public List<Bot> createBots(){
		List<Bot> bots = new ArrayList<Bot>();
		bots.add( createBot( 20,0) );
		bots.add( createBot( 4,0) );
//		bots.add( createBot( 28,0) );
		return bots;
	}
		


	@Override
	public List<BaseRequest> getBaseRequest() {
		List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();
		
		//destination
		Pos mvc1 = createPos(10,0); 
		Pos mvc2 = createPos(20,0);
		Pos mvc3 = createPos(23,0);
		Pos mvc4 = createPos(5,0);
		Pos loc1 = createPos(0,15);
		Pos loc2 = createPos(0,15);
		Pos loc3 = createPos(29,9);
		Pos loc4 = createPos(29,12);
		
		/*
		//BOT1
		//available product
		List<LocPriority> productList1 = new ArrayList<LocPriority>(); 
		productList1.add( new LocPriority( loc1, 0.99)  );

//		BaseRequest br1 = new BaseRequest( 1, mvc2, productList1 );
		BaseRequest br1 = new BaseRequest( 3, new Mvc(mvc1, MvcType.INPUT), productList1 ); // Wrocic do tego przypadku!
		baseRequestList.add(br1);
//		baseRequestList.add(br4);

		//BOT2
		//available product
		List<LocPriority> productList2 = new ArrayList<LocPriority>(); 
//		productList2.add( new LocPriority( createPos(29,15), 0.99) ); 
		productList2.add( new LocPriority( loc2, 0.99) ); // Wrocic do tego przypadku!

		//destination
		BaseRequest br2 = new BaseRequest( 6, new Mvc(mvc2, MvcType.INPUT), productList2 );
//		BaseRequest br3 = new BaseRequest( 3, mvc2, productList2 );
		baseRequestList.add(br2);
//		baseRequestList.add(br3);

		List<LocPriority> productList3 = new ArrayList<LocPriority>(); 
		productList3.add( new LocPriority( loc3, 0.99) );
		BaseRequest br3 = new BaseRequest( 2, new Mvc(mvc3, MvcType.INPUT), productList3 );
		baseRequestList.add(br3);

		List<LocPriority> productList4 = new ArrayList<LocPriority>(); 
		productList4.add( new LocPriority( loc4, 0.99) );
		BaseRequest br4 = new BaseRequest( 11, new Mvc(mvc4, MvcType.INPUT), productList4 );
		baseRequestList.add(br4);
		
		*/

		
		Pos[] mvc =new  Pos[2];
		mvc[0] = mvc1;
		mvc[1] = mvc2;

		List<LocPriority> productList1 = new ArrayList<LocPriority>(); 
		productList1.add( new LocPriority( createPos(0,19), 0.99)  );
		
		
		return baseRequestList;
	}



	public static void main(String[] args) {
		RouteChooseTest_4 t = new RouteChooseTest_4(123);
		t.setTeamPlaninngContext(new TeamPlaninngContext( new KirillStrategy(t.env)));
		t.runTest();

	}



}
