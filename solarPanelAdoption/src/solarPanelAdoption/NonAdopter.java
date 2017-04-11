package solarPanelAdoption;

import java.util.Random;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

/**
 * The class represents solar adopter
 * @author Haifeng Zhang, Computatinal Economics Lab, EECS, Vanderbilt University
 *
 */

public class NonAdopter extends HouseHold{
	/**State in the Markov chain model*/
	public int state;	

	/**	 Concurrent/parallel decisions	 */
	public int [] conDecisions;

	/** Number of concurrent/parallel decisions	 */
	public int conSize;

	/** Random seed */
	public long sd;

	/** Random generator */
	public Random getor;

	/**Coefficient Table of the Linear Utilization Model*/
	//11 months & 11 features[AAMAS, 2015: lambda=29]
	public double[][] ec_coef;
	
	public NonAdopter ( Geography <Object > geospace) {
		super(geospace);
	}

	public NonAdopter() {		
		super();
		state=0;
		conSize=solarPanelAdoptionBuilder.AVERAGE_RUNS;
		ec_coef=solarPanelAdoptionBuilder.ec_coef;
		BASE_LINES=solarPanelAdoptionBuilder.BASE_LINES;
		
		conDecisions=new int [conSize];
		freeSolar=false;

		sd=System.nanoTime();
		getor = new Random(sd);
	}


	/*
	 * Methods called by Repast routine to generate ABM output
	 */

	/**GET system cost of buyer*/
	public double getCost() {
		return (int) totalcost;	//Convert to integer otherwise cause a display problem.		
	}

	/**GET CSI incentive*/
	public double getIncentive() {
		return incentive;		
	}

	/**GET net system cost of buyer, i.e., cost after CSI rebates*/
	public double getNetCost() {
		return totalcost-incentive;		
	}	

	/**GET ZIP code accumulative adoption*/
	public double getNumAdopt(){		
		return numAdopt;
	}

	/**GET net present value if to buy*/
	public double getNPVown(){
		return NPV_own;		
	}

	/**GET net present value if to lease*/
	public double getNPVlease(){
		return NPV_lease;		
	}

	/**
	 * BE called by Repast routine at each step of simulation
	 */
	@ScheduledMethod(start=1, interval=1)
	public void step() {
		//[WARN]: Variable Update, likelihood computation etc are managed by a special agent: Updater
		//See class of Updater for more details
		//The NonAdopter agent is only responsible to transform to Adopter. 

		Context <Object > context = ContextUtils.getContext (this);

		if (iAdopt) {
			becomeAdopter(context);
		}	
	}

	/**
	 * CALL to update variables/predictors before making prediction on solar adoption.
	 * @throws negativeVariableException
	 */
	public void updatePredictors() throws negativeVariableException{		
		//AT very beginning...
		if (RepastEssentials.GetTickCount()==1) {
			aveKwh=solarPanelAdoptionBuilder.AVE_KWH_ZIP;		//ASSIGN ZIP code average electricity usage
			this.csirating=estimateCSIrating();		//ESTIMATE CSI rating
		}

		/*CALCULATE economic benefits*/
		this.econBenefit=calEconBenifit_v1();//USE estimated electric consumption

		/*QUERY total adoptions*/
		this.numAdopt=countAdopters();

		/*ESTIMATE costs*/
		this.totalcost=estimateOwnCost();
		this.leasecost=estimateLeaseCost();

		/*ESTIMATE incentive*/	
		this.incentive=estimateIncentive();

		/*TRACK net cost*/
		if (totalcost-incentive<0) {
			System.out.println("Extra Bucks: "+(incentive-totalcost));
			//throw new negativeVariableException("system cost-incentive");
		}

		/*Other variable updates*/
		prepLogit();

		/*ESTIMATE NPVs*/
		this.NPV_own=estimateOwnNPV();
		this.NPV_lease=estimateLeaseNPV();

		/*CALCULATE ZIP installation & adoption*/
		this.fracAdopt=calFracAdopt();	
		this.numInstall=countInstall();
		this.fracInstall=calFracInstall();

	}

	/**CALCULATE solar economic savings
	 * @return estimated solar economic savings*/
	//[AAMAS]
	public double calEconBenifit_v1() {

		int month=(int) ((xmonth+4)%12);
		int m=(int) xmonth;
		double bfSolarKwh=0;


		double eb_bef, eb1_bef, eb2_bef, eb3_bef, eb4_bef, eb5_bef;
		double eb_aft, eb1_aft, eb2_aft, eb3_aft, eb4_aft, eb5_aft;


		//ESTIMATE Before solar electric consumption
		homeAge=2014-asrYear;

		//[AAMAS] Penalized Linear Model
		if (month!=0){
			bfSolarKwh=ec_coef[month-1][0]+
					ec_coef[month-1][1]*totalVal+
					ec_coef[month-1][2]*ownerocc+
					ec_coef[month-1][3]*bedrooms+
					ec_coef[month-1][4]*baths+
					ec_coef[month-1][5]*pool+
					ec_coef[month-1][6]*numCarStorage+
					ec_coef[month-1][7]*totalLvg+
					ec_coef[month-1][8]*parView+
					ec_coef[month-1][9]*acreage+
					ec_coef[month-1][10]*homeAge;
		} else {
			bfSolarKwh=0.5*(ec_coef[0][0]+
					ec_coef[0][1]*totalVal+
					ec_coef[0][2]*ownerocc+
					ec_coef[0][3]*bedrooms+
					ec_coef[0][4]*baths+
					ec_coef[0][5]*pool+
					ec_coef[0][6]*numCarStorage+
					ec_coef[0][7]*totalLvg+
					ec_coef[0][8]*parView+
					ec_coef[0][9]*acreage+
					ec_coef[0][10]*homeAge)+
					0.5*(ec_coef[10][0]+
							ec_coef[10][1]*totalVal+
							ec_coef[10][2]*ownerocc+
							ec_coef[10][3]*bedrooms+
							ec_coef[10][4]*baths+
							ec_coef[10][5]*pool+
							ec_coef[10][6]*numCarStorage+
							ec_coef[10][7]*totalLvg+
							ec_coef[10][8]*parView+
							ec_coef[10][9]*acreage+
							ec_coef[10][10]*homeAge);			
		}			


		if (bfSolarKwh<0) bfSolarKwh=0; //CAP to ZERO

		//CALCULATE before-solar electric bill
		double bslinekwh=BASE_LINES[month];
		double kwhs=bfSolarKwh;
		double [] brks_1={0, 0, 0, 0, 0};	

		if (kwhs>bslinekwh){
			brks_1[0]=bslinekwh;
			kwhs=kwhs-bslinekwh;
			if (kwhs>bslinekwh*0.3){
				brks_1[1]=bslinekwh*0.3;
				kwhs=kwhs-bslinekwh*0.3;
				if (kwhs>bslinekwh*0.7){
					brks_1[2]=bslinekwh*0.7;
					kwhs=kwhs-bslinekwh*0.7;
					if (kwhs>bslinekwh) {
						brks_1[3]=bslinekwh;
						kwhs=kwhs-bslinekwh;
						brks_1[4]=kwhs;
					}else {brks_1[3]=kwhs;}
				}else { brks_1[2]=kwhs; }
			}else { brks_1[1]=kwhs; }
		}else { brks_1[0]=kwhs; }

		eb1_bef=brks_1[0]*solarPanelAdoptionBuilder.trs[m][0];
		eb2_bef=brks_1[1]*solarPanelAdoptionBuilder.trs[m][1];
		eb3_bef=brks_1[2]*solarPanelAdoptionBuilder.trs[m][2];
		eb4_bef=brks_1[3]*solarPanelAdoptionBuilder.trs[m][3];
		eb5_bef=brks_1[4]*solarPanelAdoptionBuilder.trs[m][4];

		eb_bef=eb1_bef+eb2_bef+eb3_bef+eb4_bef+eb5_bef;


		//CALCULATE after-solar electric bill
		double solarkwh=csirating*5*30.5;
		kwhs=bfSolarKwh-solarkwh;//KWHs not offset by solar

		brks_1[0]=0;
		brks_1[1]=0;
		brks_1[2]=0;
		brks_1[3]=0;

		if (kwhs>bslinekwh){
			brks_1[0]=bslinekwh;
			kwhs=kwhs-bslinekwh;
			if (kwhs>bslinekwh*0.3){
				brks_1[1]=bslinekwh*0.3;
				kwhs=kwhs-bslinekwh*0.3;
				if (kwhs>bslinekwh*0.7){
					brks_1[2]=bslinekwh*0.7;
					kwhs=kwhs-bslinekwh*0.7;
					if (kwhs>bslinekwh) {
						brks_1[3]=bslinekwh;
						kwhs=kwhs-bslinekwh;
						brks_1[4]=kwhs;
					}else {brks_1[3]=kwhs;}
				}else { brks_1[2]=kwhs; }
			}else { brks_1[1]=kwhs; }
		}else { brks_1[0]=kwhs; }


		eb1_aft=brks_1[0]*solarPanelAdoptionBuilder.trs[m][0];
		eb2_aft=brks_1[1]*solarPanelAdoptionBuilder.trs[m][1];
		eb3_aft=brks_1[2]*solarPanelAdoptionBuilder.trs[m][2];
		eb4_aft=brks_1[3]*solarPanelAdoptionBuilder.trs[m][3];
		eb5_aft=brks_1[4]*solarPanelAdoptionBuilder.trs[m][4];

		if (eb1_aft<0) eb1_aft=(bfSolarKwh-solarkwh)*0.04;
		eb_aft=eb1_aft+eb2_aft+eb3_aft+eb4_aft+eb5_aft;


		//SUBSTRACT to get solar economic savings
		eonBenefitByTier[0]=eb1_bef-eb1_aft; //tier 1
		eonBenefitByTier[1]=eb2_bef-eb2_aft; //tier 2
		eonBenefitByTier[2]=eb3_bef-eb3_aft; //tier 3
		eonBenefitByTier[3]=eb4_bef-eb4_aft; //tier 4
		eonBenefitByTier[4]=eb5_bef-eb5_aft; //tier 5

		return eb_bef-eb_aft;
	}

	/**ESTIMATE CSI rating
	 * @return estimated CSI rating*/
	public double estimateCSIrating() {
		double estimatedCSIrating=0; 

		//USE average [AAMAS experiment]
		//estimatedCSIrating=4.32510;

		//Step Wise Regression Model (AAMAS)
		estimatedCSIrating=1.592e+00+
				-2.547e-01*ownerocc+
				6.315e-01*pool+
				7.582e-04*totalLvg+
				1.319e+00*acreage+
				8.249e-04*aveKwh;	

		return estimatedCSIrating;
	}

	/**COUNT number of adopters
	 * @return number of adopters in the ZIP code*/
	public int countAdopters() {
		return solarPanelAdoptionBuilder.numAdopt; //single zip code
	}

	/**CALCULATE fraction of adoption for the ZIP code
	 * @return fraction of adoption in the ZIP code*/
	public double calFracAdopt() {//Fraction of adoption actually.
		double caldFracAdopt=0;

		/*CALCULATGE population fraction of adoption prior to current month*/
		caldFracAdopt=((double)numAdopt)/solarPanelAdoptionBuilder.ZIPCODE_POPULATION;
		return caldFracAdopt;
	}

	/**GET number of installation
	 * @return number of installation in the ZIP code*/
	public int countInstall() {
		return solarPanelAdoptionBuilder.numInstall;		
	}

	/**CALCULATE fraction of installation for the ZIP code
	 * @return fraction of installation in the ZIP code*/
	public double calFracInstall(){
		return ((double)numInstall)/solarPanelAdoptionBuilder.ZIPCODE_POPULATION;
	}

	/**ESTIMATE net present value for buyer
	 * @return estimated net present value for buyer*/
	public double estimateOwnNPV() {
		double estimatedOwnNPV=0;

		//ASSUME infinite solar use		
		if (xmonth<=20) {	//Before YEAR 2009
			//estimatedOwnNPV=(incentive-totalcost)+
			estimatedOwnNPV=(incentive-totalcost-2000)+ //post_jaamas: ITC
					(this.eonBenefitByTier[0]+this.eonBenefitByTier[1])*12*0.95/(1-0.95)+
					(this.eonBenefitByTier[2]+this.eonBenefitByTier[3]+this.eonBenefitByTier[4])*12*0.95*1.035/(1-0.95*1.035);

		}  else {			//After YEAR 2009
			//estimatedOwnNPV=(incentive-totalcost)+
			estimatedOwnNPV=(incentive-totalcost)*0.7+  //post_jaamas: ITC
					(this.eonBenefitByTier[0]+this.eonBenefitByTier[1])*12*0.95*1.01/(1-0.95*1.01)+
					(this.eonBenefitByTier[2]+this.eonBenefitByTier[3]+this.eonBenefitByTier[4])*12*0.95*1.035/(1-0.95*1.035);			
		}

		//COMMIT
		return estimatedOwnNPV;

	}

	/**ESTIMATE net present value for leasee
	 * @return estimated net present value for leasee*/
	public double estimateLeaseNPV() {
		double estimatedLeaseNPV=0;

		//ASSUME infinite solar use
		if (xmonth<=20) {	//Before YEAR 2009		
			estimatedLeaseNPV=(-leasecost)+
					(this.eonBenefitByTier[0]+this.eonBenefitByTier[1])*12*0.95/(1-0.95)+
					(this.eonBenefitByTier[2]+this.eonBenefitByTier[3]+this.eonBenefitByTier[4])*12*0.95*(1.035)/(1-0.95*1.035);

		}  else {			//After YEAR 2009
			estimatedLeaseNPV=(-leasecost)+
					(this.eonBenefitByTier[0]+this.eonBenefitByTier[1])*12*0.95*1.01/(1-0.95*1.01)+
					(this.eonBenefitByTier[2]+this.eonBenefitByTier[3]+this.eonBenefitByTier[4])*12*0.95*(1.035)/(1-0.95*1.035);

		}

		//COMMIT
		if (xmonth<9) return 0; //[WARN: significant change seen in month: 9 ]
		else 
			return estimatedLeaseNPV;

	}	


	/**ESTIMATE CSI incentive
	 * @return estimated CSI incentive*/
	public double estimateIncentive() {		
		double estimatedIncentive=0;

		/*ESTIMATE by CSI schedule*/
		estimatedIncentive=this.csirating*solarPanelAdoptionBuilder.currentRate*1000;

		//ZERO CSI incentive if seeding policy applied
		if (solarPanelAdoptionBuilder.APPLY_SeedingPlc) estimatedIncentive=0;

		return estimatedIncentive;
	}

	/**ESTIMATE system cost for buyer
	 * @return estimated system cost for buyer*/
	public double estimateOwnCost() throws negativeVariableException {		
		double estimatedOwnCost=0;

		//USE expected number of adopters tracked by Updater agent
		int numAdoptersSD=(int) (solarPanelAdoptionBuilder.numAdoptersSD);

		//Lasso Regression with CV lambda  [AAMAS]
		estimatedOwnCost=1.138391e+04+
				7.377731e-04*totalVal+
				1.518842e-01*totalLvg+
				6.213036e+03*csirating+
				-1.062339e+00*numAdoptersSD;


		/*AAMAS influemax paper related models*/
		//Time-involved cost model [AAMAS]
		//		estimatedOwnCost=Math.exp(9.600540493+  
		//				0.725915640*Math.log(csirating)+
		//				-0.005478337*xmonth);

		//Exponential Cost, no CSI rating [AAMAS]
		// double decay_rate=1; //Control Cost Decay Rate, 1 (default, trained on data)
		//		estimatedOwnCost=Math.exp(10.55012105+
		//				decay_rate*-0.00591074*xmonth);

		//Polynomial Cost, no CSI rating [AAMAS]
		//	estimatedOwnCost=Math.exp(10.69636395+
		//				-0.09834124*Math.log(xmonth));

		//Linear Cost, no CSI rating [AAMAS]
		//		estimatedOwnCost=42053.08+
		//				-201.46*xmonth;

		//Linear Cost, totalAdoptSD, no CSI rating [AAMAS]
		//		estimatedOwnCost=40356.068029+
		//				-1.736474*numAdoptersSD;

		//THROW exception and stop ABM if negative cost is present
		if (estimatedOwnCost<0) {
			throw new negativeVariableException("Ownership Cost");
		}

		return estimatedOwnCost;
	}

	/**ESTIMATE cost for leasee
	 * @return estimated system cost for leasee*/
	public double estimateLeaseCost() throws negativeVariableException {
		double estimatedLeaseCost=0;

		//Lasso regression with CV lambda[AAMAS]
		estimatedLeaseCost=10446.832+
				1658.389*csirating;		

		//THROW exception and stop ABM if negative cost is present
		if (estimatedLeaseCost<0) {
			//System.out.println("Negative Leasing Cost");
			throw new negativeVariableException("Leasing Cost");
		}

		return estimatedLeaseCost;
	}	

	/**CALCULATE other variables*/
	public void prepLogit() {	
		costperkw=totalcost/csirating;
		incentiveperkw=incentive/csirating;

		//Seasonal Variables
		double ss=Math.floor(((xmonth+4)%12)/3);	
		if (ss==0) wt=1; else wt=0;
		if (ss==1) sp=1; else sp=0;
		if (ss==2) sm=1; else sm=0;
		if (ss==3) fa=1; else fa=0;

		//Lease dummy variable
		if (xmonth>8) 	{ 
			Ls=1;		
		}	else {
			Ls=0;			
		}


	}

	/**
	 * Transform a NonAdopter to an Adopter
	 * @param context instance of Context object
	 */
	public void becomeAdopter(Context<Object> context) {
		//REMOVE agent
		Geography<Object> geo=  (Geography<Object>) context.getProjection("geography");		
		Geometry geom=geo.getGeometry(this);
		context.remove (this);	

		//ADD a new Adopter agent
		//System.out.println(geo.getCRS());
		double tick= RepastEssentials.GetTickCount();
		int crtMonth;
		if (tick==-1) crtMonth=solarPanelAdoptionBuilder.fromMonth;//0,1
		else crtMonth=(int) xmonth;

		Adopter adopter = new Adopter (crtMonth,adoptProb);

		//ASSIGN properties
		adopter.zip=this.zip;//92126+92121+92131
		adopter.csirating=this.csirating;
		adopter.iAdopt=this.iAdopt;
		adopter.adopt=this.adopt;
		adopter.freeSolar=this.freeSolar;		
		adopter.setInstallMonth();		

		context.add (adopter);
		geo.move(adopter, geom);
	}

	/**COMPUTE adoption probability using logistic regression models and GENERATE multiple realizations of decision. 
	 * @return 1 if adopt, otherwise, zero.
	 */
	public void makeDecision() {	

		/*PREDICT adoption probability*/
		this.adoptProb=predict();

		long seed=System.nanoTime();
		Random generator = new Random(seed);
		//double baseProb = generator.nextDouble();

		//GENERATE X paralell decsions		
		for (int i=0;i<conSize;i++){			
			double bp=getor.nextDouble();

			if (bp<adoptProb) {				
				this.conDecisions[i]=1;	
			}
			else this.conDecisions[i]=0;			
		}			
	}
	/**
	 * PREDICT solar adoption
	 * @return probability of adoption
	 */
	public double predict() {
		double linearRslt=0;
		double linearRslt_own=0;
		double linearRslt_lease=0;
		double prob=0;

		/*Ownership Models [AAMAS: lambda_ec=29, stepwise regression CSI rating model]*/
		//[FINAL][BASELINE 3fs][1234][non Lasso]
		//		linearRslt_own=-9.988e+00+   
		//						ownerocc*1.095e+00+
		//						NPV_own*7.686e-06+
		//						fracInstall*1.286e+02;  

		//[FINAL] [Lasso_own_ownerocc][min lambda][selected x-miles by step-wisely]
		//[dev:1230][AAMAS]
		linearRslt_own=-1.019225e+01+
				ownerocc*9.365795e-01+
				num2Mile*-3.048504e-04+
				numMile*2.599633e-03+
				numFourthMile*6.781019e-03+
				Ls*6.854269e-01+
				wt*-5.943627e-01+
				sp*-1.862011e-01+
				sm*-2.797820e-01+
				fracInstall*1.001120e+02+
				NPV_own*7.580613e-06;	

		//Lease Models [AAMAS: lambda_ec=29, stepwise regression CSI rating model]
		//[FINAL] [BASELINE 3fs] [non Lasso]
		//				linearRslt_lease=-1.188e+01+ 
		//						ownerocc*9.476e-01+
		//						NPV_lease*7.499e-06+
		//						fracInstall*1.469e+02;

		//[FINAL][Lasso_lease_ownerocc][min lambda][selected x-miles by step-wisely]
		//[AAMAS]
		linearRslt_lease=-1.322059e+01+
				ownerocc*7.298925e-01+
				num2Mile*2.206199e-03+
				numMile*0.000000e+00+
				numFourthMile*7.873662e-03+
				Ls*1.648431e+00+
				wt*-3.890142e-01+
				sp*2.938187e-01+
				sm*-1.963066e-01+
				fracInstall*8.568565e+01+
				NPV_lease*7.065149e-06;	

		//Separte Models[AAMAS]
		ownProb=Math.pow(Math.E, linearRslt_own)/(1+Math.pow(Math.E, linearRslt_own));
		leaseProb=Math.pow(Math.E, linearRslt_lease)/(1+Math.pow(Math.E, linearRslt_lease));
		prob=ownProb+leaseProb-ownProb*leaseProb; //P(A)=P(A_O)+P(A_L)-P(A_O)*P(A_L)

		//[AAMAS simple logit model derived from time-involved cost model]
		//		linearRslt=-9.832e+00+
		//				1.084e+00*ownerocc+
		//				7.268e-06*NPV_own+				
		//				1.333e+02*fracInstall;

		//[AAMAS simple logit model derived from time-involved cost model w/o CSI rating, stepwise regressed CSI rating]
		//linearRslt=-8.881e+00+
		//6.278e-06*NPV_own+				
		//1.320e+02*fracInstall;

		//[AAMAS simple logit model derived from c=e^{a+b*t} OR logc=a+b*t w/o CSI rating, uniform CSI rating]
		//int xPeer=2;
		//linearRslt=-9.161e+00+
		//1.491e-05*NPV_own+				
		//1.081e+02*fracInstall*xPeer;  //Peer Effect nx, Cost decay_rate
		//		
		//[AAMAS simple logit model derived from c=k/t^delta cost model w/o CSI rating, uniform CSI rating]
		//		linearRslt=-9.163e+00+
		//				1.489e-05*NPV_own+				
		//				1.093e+02*fracInstall*2;		

		//[AAMAS simple logit model derived from c=a+b*t cost model w/o CSI rating, uniform CSI rating]
		//		linearRslt=-9.178e+00+
		//				1.677e-05*NPV_own+				
		//				9.912e+01*fracInstall;	
		//[AAMAS simple logit model derived from c=a+b*adopt_SD cost model w/o CSI rating, uniform CSI rating]
		//		linearRslt=-9.179e+00+
		//				1.680e-05*NPV_own+				
		//				9.875e+01*fracInstall;
		//
		//		adoptProb=Math.pow(Math.E, linearRslt)/(1+Math.pow(Math.E, linearRslt));

		//[EMAP models]
		//RR2 m1-24 unmatch, >24 match, [EMAP2014: dv1240.138, NPV_own stronger]		
		//				linearRslt=-8.814706e+00+
		//						2.503317e-01*ownerocc+
		//						1.627659e+01*fracInstall+
		//						2.994175e-06*NPV_own+
		//						7.020925e-06*NPV_lease;

		//UPDATED BASELINE 2 (EMAP2014: step wise regression CSI rating model)
		//		linearRslt= -8.858e+00+//(nohup, bl_new1, 2 vars) [dv1249,117]
		//						1.313e+02*fracInstall+
		//						1.142e-05*NPV_own;

		//adoptProb=Math.pow(Math.E, linearRslt)/(1+Math.pow(Math.E, linearRslt));		return 0;
		return prob;
	}
}
