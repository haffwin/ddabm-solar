package solarPanelAdoption;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Random;
import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.gis.ShapefileLoader;
import repast.simphony.util.collections.IndexedIterable;

public class solarPanelAdoptionBuilder implements ContextBuilder<Object> {
	/** number of parallel ABM runs to be averaged*/
	public static final int AVERAGE_RUNS=1000;

	/*ABM Parameters*/
	/** [ABM Parameter]: ABM starts from month [1-69]*/
	public static int fromMonth;	
	/** [ABM Parameter]: multiple of CSI budget, used for testing sensitivity of CSI budget*/
	public static double budgetAdjFactor;
	/** [ABM Parameter]: seeding policy string, i.e., a sequence of factions of budget */
	public String seedingPlan;
	/** [ABM Parameter]: multiple of seeding budget/CSI equivalent, used for seeding optimization*/
	public static double seedBudgetMultiplier;
	/** Seeding policy in numeric representation */
	//[AAMAS] start from month 24
	public static double [] bgt_fracs=new double [70];
	/** [ABM Parameter]: incentive rates serial #, [0-18], used for 1 parameter incentive optimization*/
	public static double incentiveSerNo;
	/** [ABM Parameter]: multiple of CSI budget, used for 1 parameter incentive optimization*/
	public static double xBgtNewIncentive;
	/**tick at which the ABM will terminate*/
	public static int FINAL_TICK=70;

	/*Switches to enable policy experiment*/
	/** APPLY seeding policy?*/
	public static boolean APPLY_SeedingPlc=false;
	/** APPLY 1-parameter incentive optimization?*/
	public static boolean APPLY_OneParBic=false;	

	/** Batch run counter*/
	public static int batchNo;
	/** Random generator seed*/
	public static long pub_sd;
	/** ABM system wide random generator*/
	public static Random pub_getor;


	/*[WARN-ZIP]: ZIP code 92126 specific fields, REPLACE with corresponding values if run ABM in a different ZIP code*/
	/**Average electricity use in KWHs*/
	public static final double AVE_KWH_ZIP = 384.5;
	/**ZIP code population*/
	//[FACT]: 48214(92024) not 7094 (# of households);76373(92126),39425(92127),31360(92124)
	public static final double ZIPCODE_POPULATION=76373;
	/**San Diego population*/
	//3095313 v.s. 440955 [# of households]
	public static final double SD_POPULATION = 3056362;
	/** population ratio: SD/ZIP*/
	public static final double POPULATION_RATIO=(SD_POPULATION/ZIPCODE_POPULATION)*1;
	/** solar adoption level in ZIP code*/
	public static int numAdopt; //Initialize at run time
	/** solar installation level in ZIP code*/
	public static int numInstall; //Initialize at run time
	/** actual number of adopters in ZIP code*/
	public static final int [] ACTUAL_ADOPTERS=
		{0, 2, 3, 5,6,11,13,16,16,23,24,26,29,31,34,37,37,38,39,41,41,43,45,49,51,53,54,56,59,59,59,62,64,68,69,71,73,73,75,77,
		78,81,84,86,87,87,89,92,94,96,100,104,107,108,112,115,117,118,120,122,124,127,130,132,133,135,136,136,137,137};//Some Zip Code
	//{0,1,1,1,1,1,2,2,3,4,5,5,5,7,8,8,10,12,13,14,14,14,16,17,18,22,23,26,30,33,36,37,39,41,45,49,53,53,55,55,56,63,64,65,65,65,68,72,73,
	//78,82,85,86,87,91,95,97,99,102,105,107,110,113,117,118,124,131,136,139,139,139,139};//92126	
	//[FACT]: Baselines electricity consumption Jan-Dec
	//public static final double [] BASE_LINES={335, 302, 335, 324, 347, 336, 347, 347, 336, 347,324, 335};// ZIP code: 92024
	//public static final double [] BASE_LINES={336, 347, 347, 336, 347, 324, 335, 335, 302, 335,324, 347};// ZIP code: 92127
	/**Baseline electricity consumption, Jan-Dec*/
	public static final double [] BASE_LINES={313, 283, 313, 303, 298, 288, 298, 298, 288, 298,303, 313};// ZIP code: 92126
	/**CSI expenditure*/
	//[WARN-ZIP] ONLY for SEEDING experiment and the ZIP code
	//92126: (9-69)[323374], (25-70)[198020], (2-69)[335859][AAMAS DDABM], (46-70)[71151][AAMAS OPT] 
	public static double CSI_BUDGET=335859;	

	/** solar adoption level in San Diego county*/
	public static double numAdoptersSD;
	/** total solar capacity in San Diego county*/
	// CCSE/SDG&E
	public static double totalMegawatt;

	/** ABM CSI program step, 2 by default*/
	public static int currentStep;
	/** ABM CSI program rate, 2.5$/w by default*/
	public static double currentRate;

	/**Original CSI rate*/
	public static  double[] EBPP_RATES = { 2.5, 2.2, 1.9, 1.55, 1.1, 0.65, 0.35, 0.25, 0.2 };// CSI GM EPBB Payment Amounts by Step
	/**Original CSI megawatt targets*/
	public static  double[] TARGETS = { 1.7,3.8,7.4,10.8,15.0,18.8,25.5,32.0,40.5};

	/*One parameter incentive optimization*/
	//Keep same MW in each step and vary initial incentive level, 1-parameter optimization
	//Rate Matrix   19 rows  one-para optimization, 1st row is CSI GM EPBB Rate
	public double[][] EBPP_RATES_TEST = new double [19][9];

	/**Actual CSI program step by month*/
	public static final int[] ACTUAL_STEPS = { 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3,
		3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6,
		6, 6, 6, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 9, 9,
		9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10
	};


	/**SDGE historical 5-tier electric rates 2007.5 thru 2014.1**/
	public static double [] [] trs=new double [81][5];


	/**Coefficient Table of the Linear Utilization Model*/
	//11 months & 11 features[AAMAS, 2015: lambda=29]
	public static double[][] ec_coef =new double[11][11];

	@SuppressWarnings("deprecation")
	@Override
	public Context build(Context<Object> context) {
		//System.out.println();
		//System.out.println("ABM Initializing...");

		System.out.println("Batch No:"+batchNo++);

		//SET UP random generator
		pub_sd=System.nanoTime();
		pub_getor = new Random(pub_sd);

		//[TEST: JAVA path]
		//System.out.println(System.getProperty("java.library.path"));

		//INITIALIZE SD variables
		totalMegawatt = 0.510;
		numAdoptersSD = 106;
		currentStep = 2;

		//INITIALIZE ZIP code (92126) variables
		numAdopt=0;
		numInstall=25;

		context.setId("solarADPT");

		GeographyParameters<Object> pars = new GeographyParameters<Object>();
		GeographyFactory fac = GeographyFactoryFinder.createGeographyFactory(null);
		Geography<Object> geography = fac.createGeography("geography", context,	pars);

		// CREATE household agents from a shapefile
		File shapefile = null; // [WARN: import java.io, not the sun package!]

		//ZIP CODE: 92126
		//[WARN-ZIP]: REPLACE the path with the one for your actual shape file
		shapefile = new File("/home/local/VANDERBILT/zhangh24/code-release/solarPanelAdoption/data/agents/agentsInSomeZip_syn.shp");

		ShapefileLoader<NonAdopter> loader;
		try {
			loader = new ShapefileLoader<NonAdopter>(NonAdopter.class,	shapefile.toURI().toURL(), geography, context);
			loader.load();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		//READ historical electricity tiered rates
		String fn_rates="/home/local/VANDERBILT/zhangh24/code-release/solarPanelAdoption/data/models/SDGE_tiered_rates.csv";
		(new CsvFileReader(fn_rates, 2)).readCSV(this.trs); //ignore 1st two columns

		//READ linear electricity utilization model
		String fn_util="/home/local/VANDERBILT/zhangh24/code-release/solarPanelAdoption/data/models/linear_util_model.csv";
		(new CsvFileReader(fn_util, 1)).readCSV(this.ec_coef);; //ignore 1st column

		//READ one-parameter incentive structures 
		String fn_one_par="/home/local/VANDERBILT/zhangh24/code-release/solarPanelAdoption/data/models/one_par_ebpp_rates.csv";
		(new CsvFileReader(fn_one_par, 0)).readCSV(this.EBPP_RATES_TEST);; //ignore 1st zero column

		//SET UP initial month
		Parameters params = RunEnvironment.getInstance().getParameters();
		fromMonth = (Integer) params.getValue("fromMonth");		
		int intialNumOfAdopters = ACTUAL_ADOPTERS[fromMonth-1];	

		//SET initial adopters as in actual data
		IndexedIterable<Object> nit = context.getObjects(NonAdopter.class);		

		//COPY to ArrayList, otherwise will cause JAVA concurrent modification exception
		ArrayList<NonAdopter> cna = new ArrayList<NonAdopter>();
		for (int index=0;index<nit.size();index++) {
			cna.add((NonAdopter) (nit.get(index)));
		}

		for (NonAdopter na : cna) {
			if(na.adopt==1&&fromMonth>=na.monAdopt){
				na.iAdopt=true;//WARN
				na.becomeAdopter(context);		
			}
		}

		//SET UP budge multiple of seeding policy, MUST do this before create updater agent
		seedBudgetMultiplier = (Double) params .getValue("seedBudgetMultiplier");

		//ADD Updater Agent
		Updater newUpdater;

		newUpdater = new UpdaterAveStep(intialNumOfAdopters); // UPDATER AVE
		context.add(newUpdater);

		//SET UP budge multiple for all steps, sensitive test of CSI budget
		budgetAdjFactor = (Double) params .getValue("budgeAdjAllSteps");

		//SET UP CSI incentive rates, varying initial rate with fixed MW. [0:18]
		//One-parameter incentive optimization
		incentiveSerNo = (Double) params .getValue("incentiveSerNo");
		xBgtNewIncentive=(Double) params .getValue("xBgtNewIncentive");

		double[] TARGETS_new = { 
				0.5+(1.7-0.5)*xBgtNewIncentive,
				0.5+(3.8-0.5)*xBgtNewIncentive,
				0.5+(7.4-0.5)*xBgtNewIncentive,
				0.5+(10.8-0.5)*xBgtNewIncentive,
				0.5+(15.0-0.5)*xBgtNewIncentive,
				0.5+(18.8-0.5)*xBgtNewIncentive,
				0.5+(25.5-0.5)*xBgtNewIncentive,
				0.5+(32.0-0.5)*xBgtNewIncentive,
				0.5+(40.5-0.5)*xBgtNewIncentive};

		if (APPLY_OneParBic) {
			TARGETS=TARGETS_new;  //HZ skipped ONE PARAMETER INCENTIVE
			EBPP_RATES =EBPP_RATES_TEST[(int) incentiveSerNo]; //Use only for incentive optimization
		}

		currentRate = EBPP_RATES[currentStep-2]*budgetAdjFactor;//Zero if zero incentive, other wise 2.5


		//INITIALIZE budget fractions [MUST DO THIS!!!]
		for (int i=0; i<bgt_fracs.length; i++){
			bgt_fracs[i]=0;
		}

		seedingPlan = (String) params .getValue("seedingPlan");
		System.out.print(seedingPlan+"\t");		

		String[] temp;
		temp = seedingPlan.split(";");
		for(int i =0; i < temp.length ; i++){
			//System.out.println(temp[i]);
			String mp=temp[i];
			String [] mp_parts=mp.split(",");
			int pos = Integer.parseInt(mp_parts[0]); 
			//seeding_plan[pos]=Integer.parseInt(mp_parts[1]);
			bgt_fracs[pos]=Double.parseDouble(mp_parts[1]);				

		}		

		return context;
	}

	public static void updateIncentiveRate() {
		if (currentStep!=(EBPP_RATES.length+2)){
			while (totalMegawatt > TARGETS[currentStep - 2]) {
				currentStep++;
				if (currentStep<=EBPP_RATES.length+1) currentRate = EBPP_RATES[currentStep - 2]*budgetAdjFactor;
				else {
					currentRate =0;
					break;
				}
			}	
		}
	}	
}
