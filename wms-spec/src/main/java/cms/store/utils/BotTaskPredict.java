package cms.store.utils;

import cma.store.data.Bot;
import cma.store.data.Pos;

public class BotTaskPredict {

	long availabilityTime;
	Bot bot;
	Pos lastPos;

	public long getAvailabilityTime() {
		return availabilityTime;
	}
	public void setAvailabilityTime(long availabilityTime) {
		this.availabilityTime = availabilityTime;
	}
	public Bot getBot() {
		return bot;
	}
	public void setBot(Bot bot) {
		this.bot = bot;
	}
	public Pos getLastPos() {
		return lastPos;
	}
	public void setLastPos(Pos lastPos) {
		this.lastPos = lastPos;
	}
	
	   public String toString()  
	   {  
	      return com.google.common.base.Objects.toStringHelper(this)  
	                .add("availabilityTime",this.availabilityTime)  
	                .add("lastPos",this.lastPos)   
	                .add("bot",this.bot.getId()) 
	                .toString();  
	   }
	
}