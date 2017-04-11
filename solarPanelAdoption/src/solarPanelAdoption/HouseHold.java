/**
 * 
 */
package solarPanelAdoption;

import repast.simphony.space.gis.Geography;

/**
 * HouseHold represents a typical household
 * @author Haifeng Zhang, Computational Economics Lab, EECS, Vanderbilt University
 */
public class HouseHold {
	protected Geography  <Object > geospace; //GIS space	

	/**Current month [>=1]*/
	public double xmonth = 0;

	//Indicators
	/**Free solar (1) or not (0)?*/
	public boolean freeSolar;
	/**Adopt (1) or not (0)?*/
	public boolean iAdopt; 
	/**True adopt (1) or not (0)?*/
	public long adopt;	
	/**True ownership adopt (1) or not (0)?*/
	public long ownAdopt;
	/**True lease adopt (1) or not (0)?*/
	public long leaseAdopt;
	/**True adopt month [0 or m>0]*/
	public long monAdopt;

	//Home property features
	/**Owner type: own (1) or rent (0)*/
	public long ownerocc; 
	/**Has a pool (1) or not (0)?*/
	public long pool; 
	/**Livable sqr feet*/
	public long totalLvg; 
	/**Has pleasant view (1) or not (0)?*/
	public long parView; 
	/**Acreage greater than 0.25 (1) or not (0)?*/
	public long acreage; 
	/**CSI rating, Kilo Watt */
	public double csirating; 
	/**ZIP code*/
	public long zip; 
	/**total home value, US dollar/$*/
	public double totalVal;
	/**number of bathrooms*/
	public long baths; 
	/**number of bedrooms*/
	public long bedrooms; 
	/**number of car storage space*/
	public long numCarStorage; 
	/**year built*/
	public long asrYear; 
	/**home age in year 2004*/
	public long homeAge; 

	//Adoption likelihoods given by ABM
	/**Probability of adoption in general*/
	public double adoptProb=0; 
	/**Probability to buy solar*/
	public double ownProb=0;
	/**Probability to lease solar*/
	public double leaseProb=0;

	//[FACT]: ZIP CODE 92126
	//[WARN-ZIP]Initial installation status, USE the means for various peer measures
	/**Number of installation in 1/8 mile radius*/
	public int numEighthMile =1;
	/**Number of installation in 1/4 mile radius*/
	public int numFourthMile=1;
	/**Number of installation in 1/2 mile radius*/
	public int numHalfMile=3;
	/**Number of installation in 1 mile radius*/
	public int numMile =10;
	/**Number of installation in 2 mile radius*/
	public int num2Mile=48;
	/**Number of installation in 4 mile radius*/
	public int num4Mile =187;	
	/**Number of installation in 8 mile radius*/		
	public int num8Mile =3*num4Mile;//3X num4Mile Assumed, since >8 mile can not be simulated in a ZIP ABM

	/**ZIP code fraction of solar adoption*/
	public double fracAdopt=0;
	/**ZIP code fraction of solar installation*/
	public double fracInstall=0;
	/**ZIP code adoption*/
	public int numAdopt=0;
	/**ZIP code installation*/
	//i.e 92126(25)
	public int numInstall=0;

	/**system cost if buy*/
	public double totalcost =0;
	/**system cost if lease*/
	public double leasecost =0;

	/**ZIP code average electricity usage*/
	public double aveKwh=0; 
	/**system cost per KW for buyers*/
	public double costperkw =0;
	/**CSI incentive/rebate*/
	public double incentive=0;
	/**CSI incentive/rebate per KW*/
	public double incentiveperkw=0; 

	//Roof-top solar economic benefits, i.e. monthly savings on electricity bills
	/**total economic savings/benefits*/
	public double econBenefit=0;
	/**economic savings for tier 1 to 5*/
	public double [] eonBenefitByTier={0, 0, 0, 0, 0};

	//Net present value (NPV) of solar
	/**NPV for buyer*/
	public double NPV_own=0;
	/**NPV for lessee*/	
	public double NPV_lease=0;

	//Other Dummy variables
	/**lease available (1) or not (0)?*/
	public double Ls=0; 
	/**winter (1) or not (0)?*/
	public double wt=0;
	/**fall (1) or not (0)?*/
	public double fa=0;
	/**summer (1) or not (0)?*/
	public double sm=0; 
	/**spring (1) or not (0)?*/
	public double sp=0; 

	/**Baseline electricity consumption, Jan-Dec*/
	public double [] BASE_LINES;
	
	/**Number of neighbors*/
	public int numNBs=0;


	public HouseHold ( Geography  <Object > geospace ) {
		this.geospace = geospace ;
	}

	public HouseHold() {
	}

	/*Methods below are called by Repast S to load GIS shapefile and initialize agents*/

	//Actual adopter or not
	public long getAdopt(){
		return adopt;
	}	
	public void setAdopt(long a) {
		this.adopt = a;
	}

	//Actual ownership adopter or not
	public long getOwnadopt(){
		return ownAdopt;
	}	
	public void setOwnadopt(long oa) {
		this.ownAdopt = oa;
	}

	//Actually lease adopter or not
	public long getLeaseadopt(){
		return leaseAdopt;
	}	
	public void setLeaseadopt(long la) {
		this.leaseAdopt = la;		
	}

	//Actually adopt month
	public long getMondopt(){
		return leaseAdopt;
	}	
	public void setMonadopt(long ma) {
		this.monAdopt = ma;		
	}

	//Acreage
	public void setAcreage(long acreage) {
		this.acreage = acreage;
	}
	public long getAcreage(){
		return acreage;
	}

	//CSI rating
	public void setCsirating(double csirating) {
		this.csirating = csirating;
	}
	public double getCsirating(){
		return csirating;
	}

	//Owner or tenant?
	public void setOwnerocc(long ownerocc) {
		this.ownerocc = ownerocc;
	}
	public long getOwnerocc(){
		return ownerocc;
	}

	//Has pool?
	public void setPool(long pool) {
		this.pool = pool;
	}
	public long getPool(){
		return pool;
	}

	//Has pleasant view?
	public void setParview(long parview) {
		this.parView = parview;
	}
	public long getParview(){
		return parView;
	}

	//Car storage space
	public void setNumcarstor(long numCarStorage) {
		this.numCarStorage = numCarStorage;
	}
	public long getNumcarstor(){
		return numCarStorage;
	}

	//Number of bedrooms
	public void setBedrooms(long bedrooms) {
		this.bedrooms = bedrooms;
	}
	public long getBedrooms(){
		return bedrooms;
	}

	//Number of bathrooms
	public void setBaths(long baths) {
		this.baths = baths;
	}
	public long getBaths(){
		return baths;
	}

	//Total home value
	public void setTotalval(double totalVal) {
		this.totalVal = totalVal;
	}
	public double getTotalval(){
		return totalVal;
	}

	//Total livable sqr feet
	public void setTotallvg(long totalLvg) {
		this.totalLvg = totalLvg;
	}
	public long getTotallvg(){
		return totalLvg;
	}	

	//ZIP code
	public long getZip(){
		return zip;
	}	
	public void setZip(long zip) {
		this.zip = zip;
	}	

	//Built year
	public long getAsryear(){
		return asrYear;
	}	
	public void setAsryear(long asrYear) {
		this.asrYear = asrYear;
	}

	//Other methods
	/**
	 * INCREMENT number of installation by one for rds mile radius neighbors
	 * @param rds multiple of 1-mile radius
	 */
	public void incrementNumXMile(double rds){
		if (rds==0.125) this.numEighthMile++;
		if (rds==0.25) this.numFourthMile++;
		if (rds==0.5) this.numHalfMile++;
		if (rds==1) this.numMile++;
		if (rds==2) this.num2Mile++;
		if (rds==4) this.num4Mile++;	

		//EXTROPOLATE mum8Mile through num4Mile
		if (rds==8) this.num8Mile=3*this.num4Mile;		
	}


	public void setNumNBs(int num){
		this.numNBs=num;
	}


	/*Methods called by Repast routine to generate output*/

	//ABM sums over value returned by this method to get number of adopters
	public int getCount(){		
		return 1;		
	}

	//ABM sums over value returned by this method to get number of adopters that are correctly predicted.
	public int getCaptured(){
		if (this.iAdopt&&adopt==1)
			return 1;
		else return 0;
	}

	//ABM sums over value returned by this method to get number of adopters that are seeded.
	public int getSeeded(){
		if (this.freeSolar)
			return 1;
		else return 0;
	}

	//GET adoption probability
	public double getAdoptProb(){
		if (Double.isNaN(adoptProb)) return 0;
		else return adoptProb;
	}

	//GET adoption probability to buy
	public double getOwnProb(){
		if (Double.isNaN(ownProb)) return 0;
		else return ownProb;
	}

	//GET adoption probability to lease
	public double getLeaseProb(){
		if (Double.isNaN(leaseProb)) return 0;
		else return leaseProb;
	}

	//GET Adoption likelihood for ground false
	public double getNADprob(){
		if (adopt==0) return adoptProb;
		else return 0;		
	}

	//GET Adaption likelihood for ground truth
	public double getADprob(){
		if (adopt==1) return adoptProb;
		else return 0;		
	}

	//GET Adaption likelihood for predicted adopters	
	public double getPADprob(){
		if (iAdopt) return adoptProb;
		else return 0;		
	}


}
