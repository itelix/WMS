package cma.store.data;

public class Model {
	

	private LayerModel layerModel;
	private BotFleet botFleet;
	private MvcFleet mvcFleet;
	
	public Model(LayerModel layerModel,  BotFleet botFleet, MvcFleet mvcFleet) {
		super();
		this.botFleet = botFleet;
		this.layerModel = layerModel;
		this.mvcFleet = mvcFleet;
	}
	
	public LayerModel getLayerModel() {
		return layerModel;
	}
	public BotFleet getBotFleet() {
		return botFleet;
	}
	public MvcFleet getMvcFleet() {
		return mvcFleet;
	}
	
	public void setLayerModel(LayerModel layerModel) {
		this.layerModel = layerModel;
	}

	public void setBotFleet(BotFleet botFleet) {
		this.botFleet = botFleet;
	}

	public void setMvcFleet(MvcFleet mvcFleet) {
		this.mvcFleet = mvcFleet;
	}
	

}
