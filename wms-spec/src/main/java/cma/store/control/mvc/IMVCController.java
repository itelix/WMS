package cma.store.control.mvc;

public interface IMVCController {
	public void addTimeForOrder(Long time, Integer orderId);
	public Long getMVCTimeForOrder(Integer orderId);
}
