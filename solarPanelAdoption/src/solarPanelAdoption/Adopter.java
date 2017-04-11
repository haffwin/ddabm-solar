package solarPanelAdoption;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.query.space.gis.GeographyWithin;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

/**
 * Adopter represents solar adopter
 * @author Haifeng Zhang, Computational Economics Lab, EECS, Vanderbilt University
 */

public class Adopter extends HouseHold {
	/**	 Month adoption happens, default: -1	 */
	public int adoptMonth = -1; 
	/** Month installation completes, default: -1 */
	public int installMonth = -1;
	/** Solar installation usually takes 1-6 months s.t. uniform distribution*/
	final int MAX_INSTALL_MONTH = 6;

	/**
	 * @param adoptMonth
	 *            month when solar adoption happens
	 * @param adoptProb
	 *            adoption probability given by the model
	 */
	public Adopter(int adoptMonth, double adoptProb) {
		super();
		// System.out.println("Created a new Adopter");
		this.adoptMonth = adoptMonth;
		this.adoptProb = adoptProb;
	}

	/**
	 * SET the month when the installation will be completed
	 */
	public void setInstallMonth() {
		// USE ABM system-wide random generator
		double r = 0;
		r = solarPanelAdoptionBuilder.pub_getor.nextDouble();

		// Time Delay (month) between adoption and installation completion
		int monthsAfter = 0;

		// ASSUME the initial adopters only took 1 month to complete solar
		// project, otherwise, randomly drawn from [1, 6]
		if (RepastEssentials.GetTickCount() == -1)
			monthsAfter = 1;
		else
			monthsAfter = (int) (1 + Math.round((MAX_INSTALL_MONTH - 1) * r));

		// ASSUME 1 month delay for seeding policy experiments [AAMAS]
		if (this.freeSolar)
			monthsAfter = 1;

		/* [FACT:] How many days to complete a solar project? */
		// e.g. ZIP CODE 92126
		// Min. 1st Qu. Median Mean 3rd Qu. Max.
		// 21.0 87.0 120.0 147.3 185.0 494.0
		installMonth = adoptMonth + monthsAfter;
	}

	/**
	 * BE called at each step of the simulation, for more details please refer to Repast documentation.
	 */
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// [WARN:] The method is intentionally unimplemented.
		// Developer can extend it per their own needs.
	}

	/**
	 * UPDATE mile-based peer effect measures, i.e., x-mile radius
	 * @param radius
	 *            multiple of 1-mile radius, peer effects, i.e., number/fraction
	 *            of solar installation, of all neighbors within the circular
	 *            region will be updated
	 */
	public void updatePeerEffect(double radius) {
		Context<Object> context = ContextUtils.getContext(this);
		Geography<Object> geo = (Geography<Object>) context
				.getProjection("geography");

		// EXTRACT [1*radius] mile neighbors and increment peer measures
		double rds = radius * 1609.344;
		GeographyWithin neighbors = new GeographyWithin(geo, rds, this);
		Iterable nbs = (Iterable) neighbors.query();

		for (Object nb : nbs) {
			((HouseHold) nb).incrementNumXMile(radius);
		}
	}
}
