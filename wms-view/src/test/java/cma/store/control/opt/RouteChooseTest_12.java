package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;

import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;

/**
creating date: 2012-05-21
creating time: 01:04:12
autor: Czarek
 */

public class RouteChooseTest_12 extends  RouteChooseAbs{

	public RouteChooseTest_12(long seed) {
		super(seed);
	}

	public RouteChooseTest_12() {
		super();
	}
	@Override
	public List<Bot> createBots(){
		List<Bot> bots = new ArrayList<Bot>();
		bots.add( createBot( 0,33) );
		bots.add( createBot( 0,25) );
		bots.add( createBot( 0,20) );
//		bots.add( createBot( 0,18) );
//		bots.add( createBot( 6,0) );
//		bots.add( createBot( 8,0) );
//		bots.add( createBot( 0,0) );
//		bots.add( createBot( 5,8) );
//		bots.add( createBot( 8,8) );
		return bots;
	}


	@Override
	public List<BaseRequest> getBaseRequest() {
		List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();
		
		int maxX = env.getModel().getLayerModel().getSizeX()-1;
		//destination
		Mvc mvc1 = getMvc(MvcType.INPUT,0);
		
		List<LocPriority> productList1 = new ArrayList<LocPriority>(); 
		productList1.add( new LocPriority( createPos(maxX,30), 0.99) ); 
		BaseRequest br1 = new BaseRequest( 1, mvc1, productList1 );
		baseRequestList.add(br1);
			
		List<LocPriority> productList2 = new ArrayList<LocPriority>(); 
		productList2.add( new LocPriority( createPos(maxX,30), 0.99) ); 
		BaseRequest br2 = new BaseRequest( 1, mvc1, productList2 );
		baseRequestList.add(br2);
		
		
		List<LocPriority> productList3 = new ArrayList<LocPriority>(); 
		productList3.add( new LocPriority( createPos(maxX,30), 0.99) ); 
		BaseRequest br3 = new BaseRequest( 1, mvc1, productList3 );
		baseRequestList.add(br3);
		
		return baseRequestList;
	}



	public static void main(String[] args) {
		
		
		RouteChooseTest_12 t = new RouteChooseTest_12(123);
		//t.setTeamPlaninngContext(new TeamPlaninngContext( new NearestFreeBotStrategy(t.env)));
		t.runTest();
		
		

	}



}
