package cma.store.data;

import java.util.List;

public class ModelFactory {
	
	private ModelFactory(){
		
	}
	
	public static final ModelFactory getModelFactory(){
		return new ModelFactory();
	}
	
	public final Model setupModel(LayerModel layerModel) {
		MvcFleet mvcs = new MvcFleet(); 
		List<PlanItem> list = layerModel.getMvcLocations();
		
		for(PlanItem pi: list){
			
			if( pi.isMvcIn() ) {
				mvcs.addMvc( new Mvc(pi.getPos(), MvcType.INPUT ) );
			}else{
				mvcs.addMvc( new Mvc(pi.getPos(), MvcType.OUTPUT ) );	
			}
		}
		
		BotFleet bots = new BotFleet();  //will be created in test case

		return new Model( layerModel, bots, mvcs);
	}
	
	public final Model getTestCaseModel_1(){
		
		LayerModel layerModel = LayerModelFactory.getModelFactory().getTestCaseModel_1();
		
		return setupModel(layerModel);
	}	

	public final Model getTestCaseModel_3(){
		
		LayerModel layerModel = LayerModelFactory.getModelFactory().get3AlleysModel();
		
		return setupModel(layerModel);
	}

	public final Model getTestCaseModel_HMPCNonDirected(){
		
		LayerModel layerModel = LayerModelFactory.getModelFactory().getBasicHMPCModelNonDirected();
		
		return setupModel(layerModel);
	}

	public final Model getTestCaseModel_HMPC(){
		
		LayerModel layerModel = LayerModelFactory.getModelFactory().getBasicHMPCModel();
		
		return setupModel(layerModel);
	}
	

}
