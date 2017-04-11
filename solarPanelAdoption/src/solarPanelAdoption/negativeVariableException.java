package solarPanelAdoption;
/**
 * 
 * @author Haifeng Zhang
 *
 */
/**
 * 
 * This class represents exception when a value, i.e. system cost, is below zero.
 *
 */
public class negativeVariableException extends Exception {

	private String varName;
	   public negativeVariableException(String amount)
	   {
	      this.varName = amount;
	   } 
	   
	   public String getVarName()
	   {
	      return varName;
	   }

	
}
