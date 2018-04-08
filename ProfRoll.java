/* Author: Kristian Brasel
 * Project: Roll Call
 * Date: Summer 2017
 * */


package rollCall;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateSpreadsheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

public class ProfRoll extends RollBook {


	/*Constructor*/
	protected ProfRoll(String studId, String course, String textDate) throws IOException {
		super(studId, course, textDate);
	}
	/*Constructor*/
	protected ProfRoll(String studId, String course) throws IOException {
		super(studId, course);
	}
	/*Constructor*/
	protected ProfRoll(String studId) throws IOException {
		super(studId);
	}
	/*Constructor*/
	protected ProfRoll(int timeLimit, double latitude, double longitude) throws IOException {
		super(timeLimit, latitude, longitude);
	}

	protected ProfRoll() throws IOException{
		super();
	}
	
	
	protected String saveSpreadsheetAddress(String ssid) throws IOException{
		writeTo(ssid, "C:\\CSC131\\spreadsheetId.txt");
		return "Spreadsheet ID has been saved for future use";
	}
	/* ***********************************************************************************************************************
	 * Used to set the title of the Spreadsheet. Professor enters his name, and the spreadsheet is titled "(name)'s RollCall"
	 * Ideally this would be used only when setting up the spreadsheet for the first time, by the professor 
	 * Parameter:
	 * 		title: this is the desired title for the spreadsheet
	 * */
    public void setTitle(String title) throws IOException{
    	List<Request> requests = new ArrayList<>();
    	// Change the spreadsheet's title.
    	requests.add(new Request()
    	        .setUpdateSpreadsheetProperties(new UpdateSpreadsheetPropertiesRequest()
    	                .setProperties(new SpreadsheetProperties()
    	                		.setTitle(title))
    	                		.setFields("title"+"'s RollCall")));

    	// Update
    	BatchUpdateSpreadsheetRequest body =
    	        new BatchUpdateSpreadsheetRequest().setRequests(requests);
    	BatchUpdateSpreadsheetResponse response =
    			getService().spreadsheets().batchUpdate(getSpreadsheetId(), body).execute();
    }
	 
    
    // Adds geolocation to sheet
    public void placeLocation() throws IOException{
    	double[] latLong = getLocation();
    	int row = findRow( getSheet(), "schedule!A:A");
    	int latCol = findCol( "latitude", "schedule!3:3");
    	int lonCol = findCol( "longitude", "schedule!3:3");

    	// Create requests object
        List<Request> requests = new ArrayList<>();
    	
        // add rollKey to expiration
     	List<CellData> valuesLat = new ArrayList<>();        
     	addCellData(valuesLat, String.valueOf(latLong[0])); // add rollKey to CellData arraylist
        addToRequest(requests, 0, row-1, latCol, valuesLat); // Prepare request with proper row and column and its value     
       // add expiration to request
     	List<CellData> valuesLon = new ArrayList<>();        
     	addCellData(valuesLon, String.valueOf(latLong[1])); // add rollKey to CellData arraylist
        addToRequest(requests, 0, row-1, lonCol, valuesLon); // Prepare request with proper row and column and its value   
        
        update(requests);

    }
    
    /***********************************************************************************************
     * clearGeoLoc()
     * Sets the values for lat and lon to 0, this will allow students to log in from any location.
     * This can be used for online classes, or in cases where geolocation is malfunctioning. 
     * Parameter:
     * 		course: This function only clears geoloc data for the course specified
     * return:
     * 		Notifies the user that students can now log in from anywhere.      * 
     * */
    public String clearGeoLoc(String course) throws IOException{
    	double[] latLong = getLocation();
    	int row = findRow( course, "schedule!A:A");
    	int latCol = findCol( "latitude", "schedule!3:3");
    	int lonCol = findCol( "longitude", "schedule!3:3");

    	// Create requests object
        List<Request> requests = new ArrayList<>();
    	
        // add rollKey to expiration
     	List<CellData> valuesLat = new ArrayList<>();        
     	addCellData(valuesLat, "0"); // add rollKey to CellData arraylist
        addToRequest(requests, 0, row-1, latCol, valuesLat); // Prepare request with proper row and column and its value     
       // add expiration to request
     	List<CellData> valuesLon = new ArrayList<>();        
     	addCellData(valuesLon, "0"); // add rollKey to CellData arraylist
        addToRequest(requests, 0, row-1, lonCol, valuesLon); // Prepare request with proper row and column and its value   
        
        update(requests);
    	
    	return("Geolocation values were cleared for " + course + ", students can now log in from any location.");
    } // end ClearGeoLoc()
    
    
    /******************************************************************************************************************
     *  Set Professor defined key for date and class
     *  Parameters:
     * 		sheet: String of the sheet used for the class
     *  return:
     * 		a string either indicating a successful log of roll key, or if a roll key has already been logged a message
     * 		is returned reminding the professor of the current roll key.
     */
    public String setKey(String rollKey) throws IOException{
    	
    	//String rollKey = JOptionPane.showInputDialog("Enter roll key");
    	int emptyCol = 0; // this will be used to store the first empty col
    	String dateRange = getSheet() + "!3:3"; // a string made up of the sheetId and range. Date row is row 3
    	
        ValueRange response = getService().spreadsheets().values()
                .get(getSpreadsheetId(), dateRange).execute();
        List<List<Object>> values = response.getValues(); // date row 
 
        if (values == null || values.size() == 0) { // checks to make sure there is data in list
            System.out.println("No data found in row 3.");
        }else {
            for (List<?> col : values) { 	// finds number of entries in date row.
            	emptyCol = col.size(); 		// gives length of date row
            	if(col.get(emptyCol-1).equals(getTxtDate())){ 	// checks to see if date was already entered
            	  String rollRow = getSheet() + "!1:1"; 		// Roll row
                  ValueRange responseR = getService().spreadsheets().values()
                          .get(getSpreadsheetId(), rollRow).execute();
                  List<List<Object>> valuesR = responseR.getValues(); 	// list with rollkey row
                  
                  for (List<?> colR : valuesR) { 	// tells professor the date was already written, reminds him or rollKey
                	  return(errorRed("A roll key has already been recorded for " + getClassAndDate() + ". Your roll key is " + colR.get(emptyCol-1))); // exits method. Entering new date is no longer necessary.
                  } // end for                 
            	} // end if
            } // end for
          } // end else

    	// Create requests object
        List<Request> requests = new ArrayList<>();
 
        // add rollKey to expiration
     	List<CellData> valuesRollKey = new ArrayList<>();        
     	addCellData(valuesRollKey, rollKey); // add rollKey to CellData arraylist
        addToRequest(requests, getSheetId(), 0, emptyCol, valuesRollKey); // Prepare request with proper row and column and its value     
       // add expiration to request
     	List<CellData> valuesExpKey = new ArrayList<>();        
     	addCellData(valuesExpKey, getExpirationTime()); // add rollKey to CellData arraylist
        addToRequest(requests, getSheetId(), 1, emptyCol, valuesExpKey); // Prepare request with proper row and column and its value    
        // add date to request
        List<CellData> valuesDate = new ArrayList<>();
        addCellData(valuesDate, getTxtDate()); // Add string of current date to value
        addToRequest(requests, getSheetId(), 2, emptyCol, valuesDate); // Prepare request with proper row and column and its value

        
        // Fill col with "NO"
    	String range = getSheet() ; // The range needs to be more dynamic
    	    	
        ValueRange responseNO = getService().spreadsheets().values()
                .get(getSpreadsheetId(), range)
                .execute();
        List<List<Object>> valuesNO = responseNO.getValues();
    	
        int count = 1;
        if (valuesNO == null || valuesNO.size() == 0) {
            System.out.println("No data found.");
        }else {
            for (List<?> row : valuesNO) {
            	count++; // this tells me how many rows there are, and used to print "NO"
            } // end for
        } // end else
        
        
        List<CellData> valuesNOKey = new ArrayList<>();
        valuesNOKey.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue("NO")));
        int noRow = 3;

        // initialize attendance for all students "No" 
        for(noRow = 3; noRow <= count-2; noRow++){
            addToRequest(requests, getSheetId(), noRow, emptyCol, valuesNOKey); // add request with proper row and column and its value
        } // end for
        
        update(requests);
        return "Congratulations, your roll key for " + getClassAndDate()  + " is " + rollKey;
    } // end setKey()
    
    
    /* **********************************************************************************************************************************
     * Professor marks student present and returns appropriate string to inform user if roll was successfully recorded, or what errors were found.
     * This is used by the professor under special circumstances, such as a student's battery dying, excused absence, or error with the application.
     * Parameters:
     * 		rollKey: This is the roll key the student gets from the professor at login
     * 		studId: Students Id
     * return:
     * 		a message informing the user that their attendance was logged, or an error message explaining why it wasn't
     * */
    protected String profLogStudIn() throws IOException{
    	if(findRow(getSheet(), "schedule") == -1){ 
    		return("I'm sorry, this course is not in this roll book."); // validate course selection
    	} // end if
    	
    	// find row and col
    	int col = findCol(getTxtDate(), getSheet()+"!3:3"); // find todays date
    	int row = findRow(getStudentId(), getSheet()+"!D:D");
    	
    	// check to make sure row and col were found
    	if (col == -1){ // date was not found
    		return "I'm sorry, I could not find a record for " + getClassAndDate() + "."; 
    	} 
    	if(row == -1){ // student Id was not found
    		return errorRed("You entered an invalid Student ID for " + getSheet() + ".");
    	} 
    	
    	if( isThis("NO", getSheet(), row, col)){ // log attendance
    		List<Request> requests = new ArrayList<>(); // Create requests object
    		List<CellData> valuesAdd = new ArrayList<>(); // Create values object
    		addCellData(valuesAdd, "YES"); // Add string of current date to value
    		addToRequest(requests, getSheetId(), row-1, col, valuesAdd); // Prepare request with proper row and column and its value, place Date
    		update(requests);
    		return  ("Thank you " + getFirstName(row) + "'s attendence has been logged for " + getClassAndDate() + ".");
    	} else{ // already logged in
    		return (getFirstName(row)+"'s attendence has ALREADY been logged for " + getClassAndDate() + ".");
    	} // end else
    } // end profLogStudIn()
    

    public List<List<Object>> getProfReport(String course) throws IOException{
    	List<List<Object>> attendence = new ArrayList<List<Object>>();
    	String range = course;//+"!A3:Z99"; // The range needs to be more dynamic 131-1 refers to the sheetID
    	//String studentID = JOptionPane.showInputDialog("What is your Student ID?");
    	range = getSheet();
    	
        ValueRange response = getService().spreadsheets().values()
                .get(getSpreadsheetId(), range)
                .execute();
        List<List<Object>> values = response.getValues();
        return values;
    } // end checkStudentAtt()
    

} // end profRoll()