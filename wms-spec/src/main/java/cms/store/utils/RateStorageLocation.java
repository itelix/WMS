package cms.store.utils;

import cma.store.data.Bot;
import cma.store.input.request.LocPriority;

public class RateStorageLocation {

	private LocPriority locPriority;
	private Long availableMinTime;
	private Double pathLength;	
	private Bot bestBot;

	public LocPriority getLocPriority() {
		return locPriority;
	}
	public void setLocPriority(LocPriority locPriority) {
		this.locPriority = locPriority;
	}
	public Long getAvailableMinTime() {
		return availableMinTime;
	}
	public void setAvailableMinTime(Long availableMinTime) {
		this.availableMinTime = availableMinTime;
	}
	public Double getPathLength() {
		return pathLength;
	}
	public void setPathLength(Double pathLength) {
		this.pathLength = pathLength;
	}
	public Bot getBestBot() {
		return bestBot;
	}
	public void setBestBot(Bot bestBot) {
		this.bestBot = bestBot;
	}	
	   public String toString()  
	   {  
	      return com.google.common.base.Objects.toStringHelper(this)  
	                .add("availableMinTime",this.availableMinTime)  
	                .add("pathLength",this.pathLength)   
	                .toString();  
	   } 
	
}
