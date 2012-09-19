package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;

import cma.store.control.mvc.IMVCController;
import cma.store.control.mvc.SimpleMVCController;
import cma.store.control.teamplaninng.NaiveTeamPlaninngStrategy;
import cma.store.control.teamplaninng.TeamPlaninngContext;
import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.data.Pos;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;

/**
Warehouse optimizer.
creating date: 23-07-2012
creating time: 13:20:27
autor: adam
 */

public class TaskAssignTest extends RouteChooseAbs {
	
	
	public TaskAssignTest(long seed) {
		super(seed);
	}

	public TaskAssignTest() {
		super();
	}
	
	@Override
	public List<Bot> createBots(){
		List<Bot> bots = new ArrayList<Bot>();
		bots.add( createBot( 6,0) );
		bots.add( createBot( 1,0) );
//		bots.add( createBot( 3,0) );
		return bots;
	}
		


	@Override
    public List<BaseRequest> getBaseRequest() {
        List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();


        //available product
        List<LocPriority> productList1 = new ArrayList<LocPriority>();
        productList1.add( new LocPriority( createPos(29,19), 0.99) );
//	        productList1.add( new LocPriority( createPos(0,11), 0.98) );
        //productList1.add( new LocPriority( createPos(0,12), 0.90) );

        List<LocPriority> productList2 = new ArrayList<LocPriority>();
        productList2.add( new LocPriority( createPos(0,25), 0.98) );
//	        productList1.add( new LocPriority( createPos(0,20), 0.98) );

        List<LocPriority> productList3 = new ArrayList<LocPriority>();
        productList3.add( new LocPriority( createPos(0,15), 0.98) );

        List<LocPriority> productList4 = new ArrayList<LocPriority>();
        productList4.add( new LocPriority( createPos(0,30), 0.98) );
        
        //destination
        Pos mvc1 = createPos(10,0);
        BaseRequest br1 = new BaseRequest( 1, new Mvc(mvc1, MvcType.INPUT), productList1 );
        baseRequestList.add(br1);

        Pos mvc2 = createPos(20, 0);
        BaseRequest br2 = new BaseRequest( 2, new Mvc(mvc2, MvcType.INPUT), productList2 );
        baseRequestList.add(br2);

        BaseRequest br3 = new BaseRequest( 3, new Mvc(mvc1, MvcType.INPUT), productList3 );
        baseRequestList.add(br3);
        
        BaseRequest br4 = new BaseRequest( 4, new Mvc(mvc2, MvcType.INPUT), productList4 );
        baseRequestList.add(br4);

        IMVCController mvcController = new SimpleMVCController();
        mvcController.addTimeForOrder(9000L, br1.getOrderId());
        mvcController.addTimeForOrder(10000L, br2.getOrderId());
        mvcController.addTimeForOrder(11000L, br3.getOrderId());
        this.getTeamPlaninngContext().getTeamPlaninngStrategy().setMVCController(mvcController);

        return baseRequestList;
    } 

/*
    @Override
    public List<BaseRequest> getBaseRequest() {
        List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();


        //available product
        List<LocPriority> productList1 = new ArrayList<LocPriority>();
        productList1.add( new LocPriority( createPos(0,10), 0.99) );
//        productList1.add( new LocPriority( createPos(0,11), 0.98) );
        //productList1.add( new LocPriority( createPos(0,12), 0.90) );

        List<LocPriority> productList2 = new ArrayList<LocPriority>();
        productList2.add( new LocPriority( createPos(5,4), 0.98) );
//        productList1.add( new LocPriority( createPos(0,20), 0.98) );

        //destination
        Pos mvc1 = createPos(10,0);
        BaseRequest br1 = new BaseRequest( 1, mvc1, productList1 );
        Pos mvc2 = createPos(20, 0);
        BaseRequest br2 = new BaseRequest( 2, mvc2, productList2 );
        baseRequestList.add(br1);
        baseRequestList.add(br2);

        //baseRequestList.add(br2);
        //baseRequestList.add(br3);

        return baseRequestList;
    } */

	public static void main(String[] args) {
		TaskAssignTest t = new TaskAssignTest(321l);
		t.setTeamPlaninngContext(new TeamPlaninngContext(new NaiveTeamPlaninngStrategy(t.env)));
		t.runTest();

	}
}
