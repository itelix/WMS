package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;

import cma.store.control.teamplaninng.NoCrashStrategy;
import cma.store.control.teamplaninng.TeamPlaninngContext;
import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;

public class NaiveTaskAssign extends RouteChooseAbs {

	static Integer seed = 123;
	
	
	public NaiveTaskAssign(String name, long seed) {
		super(seed);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Bot> createBots() {
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
		
		//BOT1
		//available product
		List<LocPriority> productList1 = new ArrayList<LocPriority>(); 
		productList1.add( new LocPriority( createPos(29,15), 0.99)  ); 
		//productList1.add( new LocPriority( createPos(0,11), 0.98) ); 
		//productList1.add( new LocPriority( createPos(0,12), 0.90) ); 
		
		//destination
		Mvc mvc1 = getMvc(MvcType.INPUT,0);

		BaseRequest br1 = new BaseRequest( 1, mvc1, productList1 );
		baseRequestList.add(br1);
		
		
		//BOT2
		//available product
		List<LocPriority> productList2 = new ArrayList<LocPriority>(); 
		productList2.add( new LocPriority( createPos(0,10), 0.99) ); 
		//productList1.add( new LocPriority( createPos(0,11), 0.98) ); 
		//productList1.add( new LocPriority( createPos(0,12), 0.90) ); 
		
		//destination
		Mvc mvc2 = getMvc(MvcType.INPUT,1);
 	
		BaseRequest br2 = new BaseRequest( 2, mvc2, productList2);
		baseRequestList.add(br2);		
		
		//baseRequestList.add(br2);
		//baseRequestList.add(br3);
		
		return baseRequestList;
	}


	public static void main(String[] args) {
		NaiveTaskAssign t = new NaiveTaskAssign("NaiveTaskAssign",seed);
		t.controller.setTeamPlaninngContext(new TeamPlaninngContext(new NoCrashStrategy(t.env)));
		t.runTest();

	}
}
