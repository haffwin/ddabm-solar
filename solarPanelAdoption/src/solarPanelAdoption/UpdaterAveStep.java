package solarPanelAdoption;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
/**
 * The class represents a particular type of Updater agent, which utilizes "average step" idea to obtain expected adoption.
 * @author Haifeng Zhang, Computatinal Economics Lab, EECS, Vanderbilt University
 *
 */
public class UpdaterAveStep extends Updater{

	/**number of runs to be averaged*/
	public int conSize;
	/**aggregate number of adopters for each run*/
	public double [] simAdopters;
	/**sum of likelihood*/
	public double sumProb;
	/**average/expected number of adopters in current step*/
	public double ave=0;
	/**tick at which the ABM will terminate*/
	public int FINAL_TICK;
	
	
	/**CSI expenditure*/
	//[WARN:ONLY for SEEDING experiment and the ZIP code]
	public double CSI_BUDGET;
		
	/**current ABM tick*/
	public int tk;
	/**current month*/
	public int crm;
	/**[ABM parameter]: month ABM starts*/
	public int simuFromMonth;
	/**Probability of the NULL model*/
	//i.e., 0.000231382
	public final double NULL_PROB=Math.pow(Math.E,-8.37121 )/(1+Math.pow(Math.E, -8.37121));
	/**budget left from last step*/
	public double bgt_left; 
	/**instance of Context object*/
	public Context <Object > context;

	public UpdaterAveStep (int intialNumOfAdopters ) {		
		super(intialNumOfAdopters);	
		sumProb=0;
		
		conSize=solarPanelAdoptionBuilder.AVERAGE_RUNS;
		simAdopters=new double [conSize];
		CSI_BUDGET=solarPanelAdoptionBuilder.CSI_BUDGET;
		remain_budget=CSI_BUDGET*solarPanelAdoptionBuilder.seedBudgetMultiplier;
		FINAL_TICK=solarPanelAdoptionBuilder.FINAL_TICK;
		bgt_left=0;
	}


	@ScheduledMethod(start=1, interval=1, priority=ScheduleParameters.FIRST_PRIORITY )
	/**
	 * BE called at each step of the simulation, for more details please refer to Repast documentation.
	 */
	public void step() {
		simuFromMonth=solarPanelAdoptionBuilder.fromMonth;
		tk = (int)RepastEssentials.GetTickCount();
		crm= tk+simuFromMonth;

		context = ContextUtils.getContext (this);

		//TERMINATE ABM if specified final tick hit.
		if (tk == FINAL_TICK-simuFromMonth ) {
			RunEnvironment.getInstance().endRun();
		}

		/*PROCESS all adopters and CONFIRM solar installation and adoption*/

		IndexedIterable<Object> it= context.getObjects(Adopter.class);		
		numAdopters=it.size();	

		int numNewAdopters=0; //Adopters generated in last month		

		for (Object o: it){
			Adopter a=	(Adopter) o;		
			int am= a.adoptMonth; //adopt month
			int im= a.installMonth; //install month

			//UPDATE ZIP installation
			if (crm==im) {
				if (a.freeSolar){
					//System.out.println("Free Solar");					
				}

				//System.out.println(tk+":"+"Installed");				

				//[WARN: updating peer effects is costly]
				//[AAAI, AAMAS]				
				a.updatePeerEffect(0.25);
				a.updatePeerEffect(1);
				a.updatePeerEffect(2);		

				//[Seldom Use]
				//a.updatePeerEffect(0.125);
				//a.updatePeerEffect(0.5);
				//a.updatePeerEffect(4);
				//a.updatePeerEffect(8);			

				//UPDATE ZIP code installation					
				solarPanelAdoptionBuilder.numInstall++;	
			}


			//UPDATE ZIP adoption
			if (crm==(am+1)) {
				numNewAdopters++;

				//UPDATE ZIP code adoption				
				solarPanelAdoptionBuilder.numAdopt++;	
			}
		}

		//[WARN-ZIP] valid ONLY for the ZIP code
		//Adjustment factor used to match average adoption pace of SD county.
		double adjNumSD=1;
		//Initial Average CSI rating, adjust to match initial CSI step
		double cr0=4.5;
		
		/*[FACT]: Mapping Chart*/
		//YEAR    1  2     3     4     5     6     sum
		//AVERAGE 4  7.76  20.98 14.27 22.09 15.12 84.22 
		//92126   5  12    32    23    33    34    139

		if (crm==simuFromMonth+1){

			switch (simuFromMonth){
			case 1: adjNumSD=1;
			break;
			case 12: adjNumSD=4.0/5.0;
			break;
			case 24: adjNumSD=(4.0+7.76)/(5.0+12.0);
			break;
			case 36: adjNumSD=(4.0+7.76+20.98)/(5.0+12.0+32.0);
			break;
			case 48: adjNumSD=(4.0+7.76+20.98+14.27)/(5.0+12.0+32.0+23.0);
			break;
			default: adjNumSD=1; //[WARN: probably AAMAS influemax paper used the factor]
			break;
			}

			double ia=solarPanelAdoptionBuilder.ACTUAL_ADOPTERS[0];

			//[SCALE up through ZIP code]
			solarPanelAdoptionBuilder.totalMegawatt+=((expNumAdopt-ia)*cr0/1000.0)*100*adjNumSD;//100 ZIP codes
			solarPanelAdoptionBuilder.numAdoptersSD+=(expNumAdopt-ia)*100*adjNumSD;
			solarPanelAdoptionBuilder.updateIncentiveRate();//Update CSI program rate and step

		} else {
			ave=sumProb;
			if (solarPanelAdoptionBuilder.APPLY_SeedingPlc)
				ave=numNewAdopters;//[WARN: including seeded individuals]
			updateSdLevelVars(ave); 			
		}

		/*PROCESS all non-adopters*/
		IndexedIterable<Object> nit_1= context.getObjects(NonAdopter.class);   

		for (Object o: nit_1){
			NonAdopter na=	(NonAdopter) o;	
			na.xmonth=crm;
			try {
				na.updatePredictors();
			} catch (negativeVariableException e) {
				System.out.println("Error MSG: Negative "+e.getVarName());
				e.printStackTrace();

				System.exit(1); //STOP ABM
			}

		}

		//SEED with FREE solar panels
		if (solarPanelAdoptionBuilder.APPLY_SeedingPlc) seedPVs();

		/*MAKE multiple parallel predictions and instantiate the "average"*/
		IndexedIterable<Object> nit_2= context.getObjects(NonAdopter.class);   
		sumProb=0;//sum of adoption probabilities, i.e., expected number of adopters per stage

		//INITIALIZE counters of multiple simulated parallel steps
		for (int k=0;k<conSize;k++){
			simAdopters[k]=0;			
		}

		for (Object o: nit_2){
			NonAdopter na=	(NonAdopter) o;	
			if (!na.iAdopt){//[WARN: ONLY apply to non-adopter who not receive free panels]
				na.makeDecision();

				double da=0;
				double dna=0;
				double da_null=0;
				double dna_null=0;				


				//CALCUALTE deviance of full model and null model
				if (!Double.isNaN(na.adoptProb)){

					sumProb+=na.adoptProb;

					if(na.adopt==1&crm==na.monAdopt){
						da=-2*Math.log(na.adoptProb);
						da_null=-2*Math.log(this.NULL_PROB);
						deviance+=da;
						deviance_null+=da_null;
					}
					else 
					{
						dna=-2*Math.log(1-na.adoptProb);
						dna_null=-2*Math.log(1-this.NULL_PROB);
						deviance+=dna;
						deviance_null+=dna_null;
					}
				}

				//ADD to get aggregate adoption for each parallel run
				for (int c=0;c<conSize;c++){
					simAdopters[c]+=na.conDecisions[c];				
				}
			}

		}

		//AVERAGE number of adopters over all parallel steps
		double sum=0;
		double min=Integer.MAX_VALUE;
		double max=Integer.MIN_VALUE;			

		for(int c=0;c<conSize;c++){
			sum+=simAdopters[c];
			if (simAdopters[c]>=max) max=simAdopters[c];
			if (simAdopters[c]<=min) min=simAdopters[c];			
		}

		//[WARN: average adoption]
		ave=sum/conSize;
		//[WARN: depracated]
		//ave=max;	
		//ave=min;

		//LOCATE run that is most close to average
		double [] diffs=new double [conSize];
		int minIdx=-1;
		double minDiff=Integer.MAX_VALUE; 

		for(int c=0;c<conSize;c++){
			diffs[c]=Math.abs(ave-simAdopters[c]);
			if (diffs[c]<minDiff) {
				minIdx=c;
				minDiff=diffs[c];
			}else if (diffs[c]==minDiff&&solarPanelAdoptionBuilder.pub_getor.nextBoolean()){//Randomized when not unique
				minIdx=c;
			}
		}		

		//CONFIRM the average step
		//System.out.println("No.adopters BEFORE:"+numAdopters);
		int ii=0;
		for (Object o: nit_2){
			NonAdopter na=	(NonAdopter) o;
			if (na.conDecisions[minIdx]==1) {
				na.iAdopt=true;
				ii++;
			}
		}		

		//System.out.println("Instantiated NO. Adopters:"+ii);


		expNumAdopt+=sumProb; 

		//[WARN: deprecated]
		//expNumAdopt+=ave;

		//LOG Final Adoption
		if (tk == FINAL_TICK-simuFromMonth ) {
			System.out.println(solarPanelAdoptionBuilder.numAdopt);
		}
	}

	/**
	 * UPDATE variables at San Diego county level, i.e., total adoption and total megawatt 
	 * @param aveAds expected number of adopters per step
	 */
	public void updateSdLevelVars(double aveAds) {
		//[WARN-ZIP] valid ONLY for the ZIP code
		//Adjustment factor used to match average adoption pace of SD county.
		double adjNumSD=1;
		double cr1=4.5;//Average CSI rating (92126), adjust to match CSI steps			

		/*Mapping Chart*/
		//YEAR    1  2     3     4     5     6     sum
		//AVERAGE 4  7.76  20.98 14.27 22.09 15.12 84.22 
		//92126   5  12    32    23    33    34    139

		int simuFromMonth=solarPanelAdoptionBuilder.fromMonth;

		//FIGURE OUT ratio from ZIP to SD
		switch (simuFromMonth){
		case 1: adjNumSD=84.22/139.0;
		break;
		case 12: adjNumSD=(84.22-4.0)/(139.0-5.0);
		break;
		case 24: adjNumSD=(84.22-(4.0+7.76))/(139.0-(5.0+12.0));
		break;
		case 36: adjNumSD=(84.22-(4.0+7.76+20.98))/(139.0-(5.0+12.0+32.0));
		break;
		case 48: adjNumSD=(84.22-(4.0+7.76+20.98+14.27))/(139.0-(5.0+12.0+32.0+23.0));
		break;
		default: adjNumSD=1;
		break;

		}

		//[SCALE up through ZIP code]
		solarPanelAdoptionBuilder.totalMegawatt+=(aveAds*cr1/1000.0)*adjNumSD*100;
		solarPanelAdoptionBuilder.numAdoptersSD+=aveAds*adjNumSD*100;
		solarPanelAdoptionBuilder.updateIncentiveRate();//Update CSI program rate and step
	}

	/**
	 * SEED solar panels, i.e., giving away free solar panels to low-capacity individuals. 
	 * The seeding policies are given in form of a sequence of fractions of budget.
	 */
	private void seedPVs() {
		if (solarPanelAdoptionBuilder.bgt_fracs[tk-1]>0){

			//EXTRACT all non-adopters
			IndexedIterable<Object> nits= context.getObjects(NonAdopter.class);   

			//MAKE a copy of non-adopters
			List<Object> copiedList = new ArrayList<>();
			for (Object i : nits) {
				copiedList.add(i);
			}

			double bgt=solarPanelAdoptionBuilder.bgt_fracs[tk-1]*CSI_BUDGET*solarPanelAdoptionBuilder.seedBudgetMultiplier;
			bgt+=bgt_left;//[WARN: available budget=allocated fraction+left over in last step]

			int seeds=0;

			while (bgt>0) { //EXHAUST budget of given fraction

				//CHOOSE next least cost non-adopter
				NonAdopter nas = (NonAdopter) copiedList.get(getLeastCost(copiedList));			

				if (bgt>nas.totalcost){				
					nas.xmonth=crm;
					seeds++;

					nas.iAdopt=true;
					nas.freeSolar=true;

					bgt-=nas.totalcost; //[WARN: NEGATIVE cost can cause a failure, which is captured by exception.]
					remain_budget-=nas.totalcost;//UPDATE remain budget

					copiedList.remove(nas);

					//ZERO parallel decisions
					for (int i=0;i<conSize;i++){
						nas.conDecisions[i]=0;			
					}		

				} else {
					bgt_left=bgt;
					break;
				}

			}	

			//System.out.println("Seeds:"+seeds);
		}
	}

	/**
	 * 
	 * @param copiedList list of NonAdopter agents
	 * @return index of agent with the least solar system cost
	 */
	private int getLeastCost(List<Object> copiedList) {

		double minCost=99999999;
		int idx=0;
		int i=0;

		for (Object o:copiedList){
			if (((NonAdopter)o).totalcost<=minCost){
				idx=i;
				minCost=((NonAdopter)o).totalcost;
			}
			i++;
		}		

		return idx;
	} 
}
