package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;

import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.input.request.BaseRequest;

/**
creating date: 2012-05-21
creating time: 01:04:12
autor: Czarek
 */

public class RouteChooseTest_11 extends  RouteChooseAbs{

	public RouteChooseTest_11(long seed) {
		super(seed);
	}

	public RouteChooseTest_11() {
		super();
	}

	@Override
	public List<Bot> createBots(){
		List<Bot> bots = new ArrayList<Bot>();
		bots.add( createBot( 0,33) );
		bots.add( createBot( 0,25) );
		bots.add( createBot( 0,20) );
		bots.add( createBot( 0,18) );
		bots.add( createBot( 6,0) );
		bots.add( createBot( 8,0) );
//		bots.add( createBot( 0,0) );
//		bots.add( createBot( 5,8) );
//		bots.add( createBot( 8,8) );
		return bots;
	}


	@Override
	public List<BaseRequest> getBaseRequest() {
		List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();

		Mvc mvc1 = getMvc(MvcType.INPUT,0);
		
		for(int i=0; i<4; i++){
			BaseRequest br = new BaseRequest( 1, mvc1, getLocPriorities(4) );
			baseRequestList.add(br);
		}
		
		return baseRequestList;
	}



	public static void main(String[] args) {
		
		RouteChooseTest_11 t = new RouteChooseTest_11(123);
		//t.setTeamPlaninngContext(new TeamPlaninngContext( new NearestFreeBotStrategy(t.env)));
		t.runTest();
		

	}



}
