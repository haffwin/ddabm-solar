package solarPanelAdoption;

import repast.simphony.essentials.RepastEssentials;
/**
 * The class represents a basic Updater agent
 * @author Haifeng Zhang, Computational Economics Lab, EECS, Vanderbilt University
 *
 */
public class Updater {

	double expNumAdopt=0;//expected accumulative adoption, i.e., sum over adoption probabilities
	int numAdopters=0; //number of adopters
	double remain_budget; //budget remaining for seeding experiment
	double deviance; // Deviance of full model, i.e., -2loglike
	double deviance_null; //Deviance of null model

	public Updater (int intialNumOfAdopters ) {		
		expNumAdopt=intialNumOfAdopters;
		numAdopters=intialNumOfAdopters;
		deviance=0;
		deviance_null=0;
	}

	
	/*Methods below will be called by Repast S to generate output statistics*/
	
	//GET expected number of adoption
	public double getExpNum(){
		return expNumAdopt;		
	}

	//GET actual adoption 
	public int getRealPath(){		
		int tick=(int) RepastEssentials.GetTickCount();
		//System.out.print(""+tick);
		int crm=tick+solarPanelAdoptionBuilder.fromMonth-1;
		if (tick==-1)  return solarPanelAdoptionBuilder.ACTUAL_ADOPTERS[solarPanelAdoptionBuilder.fromMonth-1];		
		else return solarPanelAdoptionBuilder.ACTUAL_ADOPTERS[crm];		
	}

	//GET actual CSI program step
	public int getRealStep(){	
		int tick=(int) RepastEssentials.GetTickCount();

		int crm=tick+solarPanelAdoptionBuilder.fromMonth-1;
		if (tick==-1) 			
			return solarPanelAdoptionBuilder.ACTUAL_STEPS[solarPanelAdoptionBuilder.fromMonth-1];	

		else 
			return solarPanelAdoptionBuilder.ACTUAL_STEPS[crm];	

	}

	//GET remaining budget of seeding experiment
	public double getRemainBudget(){

		return remain_budget;

	}

	//GET deviance
	public double getDeviance(){

		return deviance;

	}

	//GET null deviance
	public double getNullDeviance(){

		return deviance_null;

	}

	//GET serial number of rate structure, i.e., row number in rates matrix
	//See more details in solarPanelAdoptionBuilder
	public double getSerNo(){
		return solarPanelAdoptionBuilder.incentiveSerNo;
	}

	//GET total megawatt installed in SD
	public double getTotalMegawatt(){
		return solarPanelAdoptionBuilder.totalMegawatt;
	}

	//GET current CSI rate in ABM
	public double getRate(){
		return solarPanelAdoptionBuilder.currentRate;
	}

	//GET current CSI step in ABM
	public double getStep(){
		return solarPanelAdoptionBuilder.currentStep;
	}
}
