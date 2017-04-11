package solarPanelAdoption;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
/**
 * This class contains method that read external csv file into a 2-D array.
 * @author Haifeng Zhang, Computational Economics Lab, EECS, Vanderbilt University
 *
 */
public class CsvFileReader {
	//Delimiter used in CSV file
	final String COMMA_DELIMITER = ",";	
	String fileName; 	//file name
    int ignore1stNcols;
	
	
	public CsvFileReader(String fn, int ncols){
		fileName=fn;	
		ignore1stNcols=ncols;
	}	

	public double[][] readCSV(double [][] dest) {
		BufferedReader fileReader = null;

		try {
			String line = "";

			//Create the file reader
			fileReader = new BufferedReader(new FileReader(fileName));

			//Read the CSV file header to skip it
			fileReader.readLine();

			int r=0; //row index

			//Read the file line by line starting from the second line
			while ((line = fileReader.readLine()) != null) {
				//Get all tokens available in line
				String[] tokens = line.split(COMMA_DELIMITER);
				if (tokens.length > ignore1stNcols) {
					for (int j=ignore1stNcols; j<tokens.length; j++){
						dest[r][j-ignore1stNcols]=Double.valueOf(tokens[j]);
					}
				}
				r++;
			}

		}
		catch (Exception e) {
			System.out.println("Error while reading CSV file !!!");
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
			} catch (IOException e) {
				System.out.println("Error while closing CSV file !!!");
				e.printStackTrace();
			}
		}
		return null;

	}






}
