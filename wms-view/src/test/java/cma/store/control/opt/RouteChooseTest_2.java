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

public class RouteChooseTest_2 extends  RouteChooseAbs{
	
	public RouteChooseTest_2() {
		super();
	}


	public RouteChooseTest_2(long seed) {
		super(seed);
	}

	@Override
	public List<Bot> createBots(){
		List<Bot> bots = new ArrayList<Bot>();
		bots.add( createBot( 1,0) );
		bots.add( createBot( 4,0) );
		//bots.add( createBot( 6,0) );
		//bots.add( createBot( 8,0) );
		return bots;
	}
		


	@Override
	public List<BaseRequest> getBaseRequest() {
		List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();
		

		//available product
		List<LocPriority> productList1 = new ArrayList<LocPriority>(); 
		productList1.add( new LocPriority( createPos(0,10), 0.99) ); 
		//productList1.add( new LocPriority( createPos(0,11), 0.98) ); 
		//productList1.add( new LocPriority( createPos(0,12), 0.90) ); 
		
		//destination
		Mvc mvc1 = getMvc(MvcType.INPUT,0);

		BaseRequest br1 = new BaseRequest( 1, mvc1, productList1 );
		baseRequestList.add(br1);
			
		//baseRequestList.add(br2);
		//baseRequestList.add(br3);
		
		return baseRequestList;
	}



	public static void main(String[] args) {
		RouteChooseTest_2 t = new RouteChooseTest_2(123);
		t.runTest();

	}



}
