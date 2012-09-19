package cma.store.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MvcControllerTest {
	
	Mvc mvc;
	MvcController controller;

	@Before
	public void setUp() throws Exception {
		MvcType type = MvcType.OUTPUT;
		Pos pos = new Pos(1,2);
		mvc = new Mvc( pos, type );
		controller = new MvcController(mvc);
	}
	

	@Test
	public void testReserveSlots() {
		
		long firstAvailableTime = controller.getFirstAvailableTime();
		long x1 = controller.getAvailableTime( firstAvailableTime );
		assertTrue("Next available time is to small", x1>firstAvailableTime);
				
		Route route = new RouteSimple().setFinalTime(x1);
		controller.reserveSlots(route);
		
		long x2 = controller.getAvailableTime( firstAvailableTime );
		assertTrue("Next available time is to small", x2>x1 );

	}

	@Test
	public void testFreeSlots() {
	}

	@Test
	public void testGetSlotId() {
	}

	@Test
	public void testGetFirstAvailableTime() {
	}

}
