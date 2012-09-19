package cma.store.schedule;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import cma.store.control.opt.data.Request;
import cma.store.data.Bot;
import cma.store.data.BotFleet;
import cma.store.data.Duration;
import cma.store.data.Model;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteSimple;
import cma.store.data.RouteImp;
import cma.store.data.RouteType;
import cma.store.data.Speed;
import cma.store.env.Environment;
import cms.store.utils.Pair;

public class ScheduleTest {
	Environment env;
	Schedule schedule;
	List<Bot> bots;
	List<Pos> initPositions;
	Random rnd = new Random(123);
	BotFleet fleet;
	Model model;
	Bot bot1,bot2,bot3;
	Pos initPos1,initPos2,initPos3;
	

	@Before
	public void setUp() throws Exception {
		env = new Environment(100);
		schedule = new Schedule(env,null);
	}
	
	private void init( int botsCount ){
		
		fleet = new BotFleet();
		bots = new ArrayList<Bot>();
		initPositions = new ArrayList<Pos>();
		
		for( int i=0; i<botsCount; i++){
			Pos pos = new Pos( rnd.nextDouble()*100, rnd.nextDouble()*100 );
			Bot bot = new Bot(pos.x, pos.y);
			
			bots.add( bot );
			initPositions.add(pos);
			
			fleet.addBot(bot);
		}
		
		if( botsCount>0 ){
			bot1=bots.get(0);
			initPos1 = initPositions.get(0);
		}
		if( botsCount>1 ){
			bot2=bots.get(1);
			initPos2 = initPositions.get(1);
		}
		if( botsCount>2 ){
			bot3=bots.get(2);
			initPos3 = initPositions.get(2);
		}
		model = new Model(null,fleet,null);
		env.setModel(model);
	}
	
	private Route createRoute( Bot bot, Pos initPos ){
		
		List<Pair<Speed,Duration>> speedDurList = new ArrayList<Pair<Speed,Duration>>();
		Speed s = new Speed(1,2);
		int start = 1+rnd.nextInt(300);
		Duration d = new Duration(start,start+10+rnd.nextInt(100));
		Pair<Speed,Duration> speed = new Pair<Speed, Duration>(s, d);
		speedDurList.add( speed );
		
		Route route = new RouteImp( bot, speedDurList, null, initPos, 100, null);
		route.setType(RouteType.ROUTE_1);
		ScheduleItem item = new ScheduleItem(route);
		schedule.add(item);
		
		return route;
		
	}



	@Test
	public void testAdd() {
		
		init(1);
		
		Route route1a = createRoute( bot1, initPos1 );
		
		Route route1b = schedule.getLastRoute(bot1);
		
		assertTrue("Wrong last route", route1a==route1b);
		
		long endTime = route1a.getFinalTime() + 1000;
		
		env.setTime( endTime );
		
		Pos endPos = route1a.getFinalPos();
		
		schedule.updateFleet();
		
		Pos p = schedule.getBotPosition(bot1, endTime, false);
		
		assertEquals( "Wrong bot position ", p.x, endPos.x, 0.0001 );
		assertEquals( "Wrong bot position ", p.y, endPos.y, 0.0001 );
		

	}
	


	@Test
	public void testGetLastPos() {
		init(2);
				
		Pos pos1 = schedule.getLastPos(bot1);
		assertEquals( "Wrong bot final position", pos1.x, initPos1.x, 0.0001);
		assertEquals( "Wrong bot final position", pos1.y, initPos1.y, 0.0001);
		
		createRoute( bot2, initPos2 );
		pos1 = schedule.getLastPos(bot1);
		assertEquals( "Wrong bot final position", pos1.x, initPos1.x, 0.0001);
		assertEquals( "Wrong bot final position", pos1.y, initPos1.y, 0.0001);
		
		Route r = createRoute( bot1, initPos1 );
		Pos p = r.getFinalPos();
		pos1 = schedule.getLastPos(bot1);
		assertEquals( "Wrong bot final position", pos1.x, p.x, 0.0001);
		assertEquals( "Wrong bot final position", pos1.y, p.y, 0.0001);
		
		ScheduleItem si = schedule.getItems().get(1);
		schedule.remove(si);

		pos1 = schedule.getLastPos(bot1);
		assertEquals( "Wrong bot final position", pos1.x, initPos1.x, 0.0001);
		assertEquals( "Wrong bot final position", pos1.y, initPos1.y, 0.0001);	
		
	}


	@Test
	public void testGetLastRouteBot() {
		init(2);
		
		Route r1_b2 = createRoute( bot2, initPos2 );
		Route r1_b1 = createRoute( bot1, initPos1 );
		Route r2_b2 = createRoute( bot1, r1_b2.getFinalPos() );
		
		assertEquals( "Wrong bot last route", r1_b1, schedule.getLastRoute(bot1));
		assertEquals( "Wrong bot last route", r2_b2, schedule.getLastRoute(bot2));

	}
	

	@Test
	public void testGetMvcAvailableTime() {
		
	}

	@Test
	public void testUpdateRealization() {
		
	}

	@Test
	public void testUpdateSchedule() {
		
	}

	@Test
	public void testAddItems() {
		
	}

	@Test
	public void testAddRoutes() {
		
	}

	@Test
	public void testGetAvailableMinTime() {
		
	}

	@Test
	public void testGetCarPosition() {
		
	}

	@Test
	public void testGetRoute() {
		
	}

	@Test
	public void testGetMvcUseEndTime() {
		
	}

	@Test
	public void testGetAvailableCars() {
		
	}

	@Test
	public void testGetAvailableCarsLongPosListOfBot() {
		
	}

	@Test
	public void testIsBotConflit1() {
		
	}

	@Test
	public void testGetConflitedRoute() {
		
	}

	@Test
	public void testGetItems() {
		
	}

	@Test
	public void testDeleteItem() {
		
	}

	@Test
	public void testUpdateFleet() {
		
	}

	@Test
	public void testRemove() {
		
	}

	@Test
	public void testRemoveAll() {
		
	}


	@Test
	public void testGetBotsReadyToRambo() {
		
	}

	@Test
	public void testDeleteRouteForRequest() {
		
	}

}
