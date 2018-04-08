/* Author: Kristian Brasel
 * Project: Roll Call
 * Date: Summer 2017
 * */

package rollCall;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.ValueRange;

public class StudentRoll extends RollBook{

	/*Constructor*/
	protected StudentRoll(String studId, String course) throws IOException {
		super(studId, course);
	}
	/*Constructor Used for present*/ 
	protected StudentRoll(String studId, double latitude, double longitude) throws IOException {
		super(studId, latitude, longitude);
	}
	
	/*Returns the expiration time to notify the student how much time they have left to log in*/
    public String getExpirationTime() throws IOException {
    	int col = findCol(getTxtDate(), getSheet()+"!3:3"); // find todays date
    	return getThis(getSheet(), 2, col);
    	
     }
    
    /***********************************************************************************************
     * Checks to see if the roll key given by the student matches the roll key set by the professor
     * Parameters:
     * 		rollKey: This is the roll key given by the student
     * 		col: This is the col that the current date was found in
     * */
    private boolean isRollKey(String rollKey, int col) throws IOException{
        ValueRange response = getService().spreadsheets().values()
                .get(getSpreadsheetId(), getSheet() + "!1:1").execute();
        List<List<Object>> values = response.getValues(); 	// row to search 
 
        if (values == null || values.size() == 0) {		 	// checks to make sure there is data in list
            System.out.println("No Roll Key found in spread sheet.");
        }else {
            for (List<?> row : values) { 
            	if(row.get(col).equals(rollKey)){
            		return true;
            	} // end if
            } // end for
          } // end else
        return false; // not a match
    }
    
    /***********************************************************************************************
     * Checks to see if the roll key has expired
     * Parameters:
     * 		col: This is the col that expiration time is found
     * reuturn:
     * 		true if the student logged in before the expiration time
     * 		false if the roll key is expired
     * */
    private boolean notExpired(int col) throws IOException{
        ValueRange response = getService().spreadsheets().values()
                .get(getSpreadsheetId(), getSheet() + "!2:2").execute();
        List<List<Object>> values = response.getValues(); 	// row to search 
 
        if (values == null || values.size() == 0) {		 	// checks to make sure there is data in list
            System.out.println("No expiration time found in spread sheet.");
        }else {
            for (List<?> row : values) { 
            	if(isAfter((String)row.get(col), getExpirationTime())){
            		return true;
            	} // end if
            } // end for
          } // end else
        return false; // not a match
    }
    
    /****************************************************************************************************************************************
     * isCloseEnough()
     * Checks to make sure the user is within range of the classroom they are trying to log attendance for
     * return:
     * 		true if they are close enough
     * 		false if they are too far from class
     * */
    private boolean isCloseEnough() throws IOException{
    	double[] latlong = getLocation();
    	double radius = 0.001; // distance student is allowed to be from class location
    	int row = findRow( getSheet(), "schedule!A:A");
    	int latCol = findCol( "latitude", "schedule!3:3");
    	int lonCol = findCol( "longitude", "schedule!3:3");
    	String range = "schedule!" + row + ":" + row; // only row of current course
    	ValueRange response = getService().spreadsheets().values()
                .get(getSpreadsheetId(), range).execute();
       	List<List<Object>> values = response.getValues(); // rows of classes 
    	
       	for (List<?> course : values) { // cycle through courses

       		if(hasData((String)course.get(latCol))){ //verifies that location has been set.
       			if(((Math.abs(Double.parseDouble((String)course.get(latCol))-latlong[0]) < radius) // makes sure the latitude and longitude are both within the desired distance from class 
       					&& (Math.abs(Double.parseDouble((String)course.get(lonCol))-latlong[1]) < radius) )
       					|| ((String)course.get(lonCol)).equals("0")){ 
       				return true; // 1 indicates class found
       			}// end if
       		} else{
       			return true; // if no location is set, allow student to log attendance
       		} // end else
       	} // end for
    	return false;
    }
    
    
    /* **********************************************************************************************************************************
     * present()
     * Marks student present and returns appropriate string to inform user if roll was successfully recorded, or what errors were found.
     * Parameters:
     * 		rollKey: This is the roll key the student gets from the professor at login
     * return:
     * 		a message informing the user that their attendance was logged, or an error message explaining why it wasn't
     * */
	protected String present(String rollKey) throws IOException{
    	int col = findCol(getTxtDate(), getSheet()+"!3:3"); // find todays date
    	int row = findRow(getStudentId(), getSheet()+"!D:D");
    	if (col == -1){ // date was not found
    		return "Professor has not begun Roll Call yet for " + getClassAndDate() + "."; 
    	}
    	if(row == -1){ // student Id was not found
    		return "You entered an invalid Student ID for " + getSheet() + ".";
    	}
    	
    	if(isCloseEnough()){
    		if(isRollKey(rollKey, col)){
    			if(notExpired(col)){
    				if( isThis("NO", getSheet(), row, col)){ 		// log attendance
    					List<Request> requests = new ArrayList<>(); 	// Create requests object
    					List<CellData> valuesAdd = new ArrayList<>(); 	// Create values object
    					addCellData(valuesAdd, "YES"); 					// Add string of current date to value
    					addToRequest(requests, getSheetId(), row-1, col, valuesAdd); // Prepare request with proper row and column and its value, place Date
    					update(requests);
    					return  ("Thank you " + getFirstName(row) + ", your attendence has been logged for " + getClassAndDate() + ".");
    				} else{ // already logged in
    					return (getFirstName(row)+", your attendence has ALREADY been logged for " + getClassAndDate() + ".");
    				} // end already logged in
    			}else{
    				return("I'm sorry "+getFirstName(row)+", but the expiration time for Roll Call was "+ getExpirationTime()+ " for " + getClassAndDate() + ".");
    			} //end expired 
    		} else{ // wrong roll key
    		return ("I'm sorry " + getFirstName(row)+ ", this is not the correct roll key for " + getClassAndDate() + ".");	
    		} // end wrong roll key
    	} else{
    		return "I'm sorry " +  getFirstName(row) + ", but you need to be in the classroom for " + getSheet() + " to log attendance. You have until " + getExpirationTime() + " to get to class.";
    	}
    } // end present()

} // end StudentRoll()