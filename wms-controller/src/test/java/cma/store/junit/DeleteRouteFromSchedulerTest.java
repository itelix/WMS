package cma.store.junit;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cma.store.data.Bot;
import cma.store.data.Route;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.DefinedBaseRequestCreator;
import cma.store.schedule.ScheduleItem;

import org.hamcrest.number.OrderingComparison;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;

public class DeleteRouteFromSchedulerTest extends BaseUnitTest {
	
	@Test
	public void deleteRoute() {		
//		Assert.assertNotNull(this.controller);
		this.initData();
	}
	
	public void initData() {
		List<BaseRequest> requests = this.getBaseRequest(6);
		Assert.assertNotNull(requests);
		List<Bot> bots = createBots(2);
		Assert.assertNotNull(bots);
		
		env.getModel().getBotFleet().addBots( bots );		
		
		//definition of list of request:
		//ordered product list
		
		long uruchom1 = 0;
		baseRequestCreator.addBaseRequestList( uruchom1, requests );
		BaseRequest baseRequest = requests.get(1);
//		System.out.println("Order to delete: "+baseRequest.getOrderId());
		env.setTime(900);
//		this.controller.setPaused(true);
		this.controller.setStoped(true);
//		this.controller.acquireRequests();
//		this.controller.processRequests();
		this.controller.runSolutions();

		this.controller.updateFleet();

		int routesCount = this.env.getSchedule().getItems().size();
//		for (ScheduleItem item :  this.env.getSchedule().getItems()) {
//			BaseRequest req = item.getBaseRequest();
//			Route route = item.getRoute();
//			System.out.println("Order ID in plan: "+route.getType());
//		}
		this.env.getSchedule().deleteRouteForRequest(baseRequest);

		int afterDeleteRoutesCount = this.env.getSchedule().getItems().size();
		Assert.assertThat(Integer.valueOf(afterDeleteRoutesCount), is(lessThan(routesCount)));
		for (ScheduleItem item :  this.env.getSchedule().getItems()) {
			BaseRequest req = item.getBaseRequest();
//			System.out.println("Order ID in plan: "+req.getOrderId());
			Assert.assertThat(req.getOrderId(),
					is(lessThanOrEqualTo(baseRequest.getOrderId())));
		}
		// remove routes
		
		// startAggain
		
	}
	
}