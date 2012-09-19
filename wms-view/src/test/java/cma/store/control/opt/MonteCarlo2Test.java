package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;

/**
Warehouse optimizer.
creating date: 26-07-2012
creating time: 08:53:05
autor: adam
 */

public class MonteCarlo2Test extends RouteChooseAbs {

	public MonteCarlo2Test(String name, long seed) {
		super(seed);
	}


	@Override
	public List<Bot> createBots(){
		List<Bot> bots = new ArrayList<Bot>();
		Random rand = new Random();
		bots.add( createBot( rand.nextInt(6),0) );
		bots.add( createBot(  rand.nextInt(20),0) );
		//bots.add( createBot( 3,0) );
		return bots;
	}
		

	@Override
    public List<BaseRequest> getBaseRequest() {
        List<BaseRequest> baseRequestList = new ArrayList<BaseRequest>();
        

        //available product
        List<LocPriority> productList1 = new ArrayList<LocPriority>();
        productList1.add( new LocPriority( createPos(0,20), 0.99) );
//	        productList1.add( new LocPriority( createPos(0,11), 0.98) );
        //productList1.add( new LocPriority( createPos(0,12), 0.90) );

        List<LocPriority> productList2 = new ArrayList<LocPriority>();
        productList2.add( new LocPriority( createPos(0,21), 0.98) );
//	        productList1.add( new LocPriority( createPos(0,20), 0.98) );

        //List<LocPriority> productList3 = new ArrayList<LocPriority>();
        //productList3.add( new LocPriority( createPos(0,15), 0.98) );

        
        //destination
		Mvc mvc1 = getMvc(MvcType.INPUT,0);

        BaseRequest br1 = new BaseRequest( 1, mvc1, productList1 );
        baseRequestList.add(br1);

		Mvc mvc2 = getMvc(MvcType.INPUT,1);

        BaseRequest br2 = new BaseRequest( 2, mvc2, productList2 );
        //baseRequestList.add(br2);

        //BaseRequest br3 = new BaseRequest( 3, mvc2, productList3 );
        //baseRequestList.add(br3);
        baseRequestList.add(br2);
        //baseRequestList.add(br3);

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
		MonteCarlo2Test t = new MonteCarlo2Test("Test_2",321);
		t.runTest();

	}
}
