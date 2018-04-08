/* Author: Kristian Brasel
 * Project: Roll Call
 * Date: Summer 2017
 * */

package rollCall;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.Sheets;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.text.SimpleDateFormat;
import java.lang.Object;
import java.util.Date;
import java.lang.System;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;




//import javax.swing.JOptionPane;


/*Superclass*/
public class RollBook {
    /** Application name. */
    private final String APPLICATION_NAME = "Roll Call";

    /** Directory to store user credentials for this application. */
    private final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials//sheets.googleapis.com-java-quickstart.json");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart.json
     */
    private final List<String> SCOPES =
        Arrays.asList( SheetsScopes.SPREADSHEETS );

    private String spreadsheetId; // spreadsheet identifier found in url of google spreadsheet
    private String studentId; // student Id
    private Sheets service; // an authorized API client service.
	private String txtDate; // a string of todays date MM/dd/yyyy
	private String dayOfWeek; // holds the day of the week
	private String currentTime; // H:mm
	private String expirationTime;
	private String sheet;
	private int sheetId;
	private double[] location = {0,0}; // [latitude, longitude]

    {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    
    
    /* **Constructor**  // Used for student present
     * Parameters:
     * 		ssid: identifier for google spreadsheet, found in url of spreadsheet
     * 		studId: Students Id
     * 		lat: latitude of current location
     * 		lon: longitude of current location
     * */
    protected RollBook(String studId, double lat, double lon) throws IOException {
    	setSpreadsheetId();
    	setStudentId(studId);
    	setService();// Build a new authorized API client service.
       	setTxtDate();
    	setDayOfWeek();
    	setCurrentTime();
    	setLocation(lat, lon);
    }
    /* **Constructor** Used for profLogStudIn()
     * Parameters:
     * 		ssid: identifier for google spreadsheet, found in url of spreadsheet
     * 		studId: Students Id
     * 		course: course professor is logging student into
     * 		textDate: day professor is logging student in
     * */
    protected RollBook(String studId, String course, String textDate) throws IOException{
    	//super();
    	setSpreadsheetId();
    	setStudentId(studId);
    	setService();// Build a new authorized API client service.
       	setTxtDate(textDate);
    	setDayOfWeek();
    	setCurrentTime();
    	setSheet(course);
   	}
    /* **Constructor**
     * Parameters:
     * 		ssid: identifier for google spreadsheet, found in url of spreadsheet
     * 		studId: Students Id
     * */
    protected RollBook(String studId, String course) throws IOException{
    	//super();
    	setSpreadsheetId();
    	setStudentId(studId);
    	setService();// Build a new authorized API client service.
       	setTxtDate();
    	setDayOfWeek();
    	setCurrentTime();
    	setSheet(course);
   	}
    
    /* **Constructor**
     * Parameters:
     * 		ssid: identifier for google spreadsheet, found in url of spreadsheet
     * 		studId: Students Id
     * */
    protected RollBook(String studId) throws IOException{
    	//super();
    	setSpreadsheetId();
    	setStudentId(studId);
    	setService();// Build a new authorized API client service.
       	setTxtDate();
    	setDayOfWeek();
    	setCurrentTime();
   	}
    
    /* **Constructor** Used for setRollKey
     * Alternate constructor used in ProfRoll Class
     * Parameter:
     * 		ssid: identifier for google spreadsheet, found in url of spreadsheet
     * */
    protected RollBook(int timeLimit, double latitude, double longitude) throws IOException {
    	//writeTo("1PTDXzFH1T4ULKOieMIQtGQPHhS4S2vlqNVJ-tC4OUt0", "C:\\CSC131\\spreadsheetId.txt");
    	setSpreadsheetId();
    	setService();// Build a new authorized API client service.
    	setTxtDate();
    	setCurrentTime();
    	setExpirationTime(timeLimit);
    	setDayOfWeek();
    	setLocation(latitude, longitude);
	}

    // Used when saving a new spreadsheetId
    protected RollBook() throws IOException{
    	setService();// Build a new authorized API client service.
    }
    
     
    public void setSpreadsheetId() throws IOException {
    	this.spreadsheetId = readFrom("C:\\CSC131\\spreadsheetId.txt");  	
    }
    public String getSpreadsheetId() throws IOException {
    	return this.spreadsheetId;    	
    }
    
    public void setStudentId(String studId) throws IOException {
    	this.studentId = studId;   	
    }
    public String getStudentId() throws IOException {
    	return this.studentId;    	
    }
    
    public void setService() throws IOException {
    	this.service = this.getSheetsService(); // Build a new authorized API client service.	
    }
    public Sheets getService() throws IOException {
    	return this.service;    	
    }
    
    /*stores the name of the sheet for the course, also sets the matching Id*/
    public void setSheet(String course) throws IOException {
    	this.sheet = course;  
    	setSheetId(); // sets sheet id to match new sheet
    }
    public String getSheet() throws IOException {
    	return this.sheet;    	
    }
    
    public void setLocation(double latitude, double longitude) throws IOException {
    	this.location[0] = latitude; 
    	this.location[1] = longitude; 
    }
    public double[] getLocation() throws IOException {
    	return this.location;    	
    }
    /* stores the current date in MM/dd/yyyy format */
    public void setTxtDate() throws IOException {
    	Date currentDate = new Date(); 
    	this.txtDate = new SimpleDateFormat("MM/dd/yyyy").format(currentDate); 
    }
    /* stores the a user input date, used for professor overwriting attendance for a student */
    public void setTxtDate(String textDate) throws IOException { 
    	this.txtDate = textDate; 
    }
    public String getTxtDate() throws IOException {
    	return this.txtDate;    	
    }
    
    public void setDayOfWeek() throws IOException {
    	Date now = new Date();
    	SimpleDateFormat day;
    	day = new SimpleDateFormat("EEEE"); // the day of the week spelled out completely
        this.dayOfWeek = day.format(now);
    }
    public String getDayOfWeek() throws IOException {
    	return this.dayOfWeek;    	
    }
    
    public void setCurrentTime() throws IOException {
    	Date now = new Date();
    	SimpleDateFormat day;
    	day = new SimpleDateFormat("H:mm"); // the day of the week spelled out completely
    	System.out.println("It is " + now);
    	this.currentTime = day.format(now);
    }
    public String getCurrentTime() throws IOException {
    	return this.currentTime;    	
    }
    
    // This is the time that the student has to log in
    public void setExpirationTime(int timeLimit) throws IOException {
 		String delims = "[:]"; // divides the hours and minutes
 		String[] hourMin = getCurrentTime().split(delims); // assigns hours and min to array

 		// clearly labels hours and min for comparisons
 		int hour = Integer.parseInt(hourMin[0]);
 		int min = Integer.parseInt(hourMin[1]);

 		min = min + timeLimit; 		// adds time to the current minutes
 		int tlHours = min/60; 	// extracts hours
 		min = min%60; 			// leaves minutes
 		hour = hour + tlHours; 
 		if(min < 10){
 			this.expirationTime = hour+":0"+min;// adds hours
 		}else{
 			this.expirationTime = hour+":"+min;  // sets expiration time to object as String to be placed in sheet
 		}  	
     }
     public String getExpirationTime() throws IOException {
     	return this.expirationTime;    	
     }
    

    
    /* ****************************************************************************************
     * Use sheet title to retrieve sheetId
     * return:
     * 		sheetId, which is needed for writing to spreadsheet
     * */
    public void setSheetId() throws IOException{
        // The spreadsheet to request.
        String spreadsheetId = getSpreadsheetId(); 

        // True if grid data should be returned.
        // This parameter is ignored if a field mask was set in the request.
        boolean includeGridData = false; 

        Sheets.Spreadsheets.Get request = getService().spreadsheets().get(spreadsheetId);
        request.setIncludeGridData(includeGridData);
        Spreadsheet response = request.execute();
        List<Sheet> values = response.getSheets(); // places sheets in a list
        
        if (values == null || values.size() == 0) { // checks to make sure there is data in list
            System.out.println("No data found.");
        }else {
            for (Sheet col : values) { // cycles through sheets
            	if(((GenericData)col.get("properties")).get("title").equals(getSheet())){ // checks the titles for a match
            		this.sheetId = (int)(((GenericData) col.get("properties")).get("sheetId")); // sets the sheetId on match
            	}
            } // end for
       } // end else
    } // end setSheetId()
    
    /* Returns the sheetId for writing to spreadsheet */
    public int getSheetId() throws IOException {
    	return this.sheetId;    	
    }
    
    
    /* *****************************************************************************************************************
     * return:
     * 		the first name of the student occupying the row given by the parameter, or an indication that no name was found
     */
    public String getFirstName(int nameRow) throws IOException{
    	String range = getSheet()+"!"+nameRow+":"+nameRow;
    	 ValueRange response = getService().spreadsheets().values()
                 .get(getSpreadsheetId(), range).execute();
         List<List<Object>> values = response.getValues(); // row to search 
         int col = findCol("First Name", getSheet());
  
         if (values == null || values.size() == 0) { // checks to make sure there is data in list
             return ("No data found.");
         }else {
             for (List<?> row : values) { 
            	 return ((String) (row.get(col)));
             }
         }
         return("No data found");
    }// end getFirstName()
    
    /*Used for printing messages to user letting them know what class and date the application is dealing with*/
    public String getClassAndDate() throws IOException {
    	return(getSheet() + " on " + getTxtDate());
    } // end getClassAndDate()
    
    /***************************************************************************************************
     *  writeTo()
     *  Light encryption on string, stores it at path
     *  Parameters:
     *  	text: This is the string you want to store
     *  	path: This is where you want to store it ex. "C:\\CSC131\\spreadsheetId.txt"
     * */
    public void writeTo(String text, String path) throws IOException{

    	BufferedWriter bw = null;
    	FileWriter fw = null;

    	try {
            int x = 0;
            StringBuilder sb = new StringBuilder(text.length());
            while(x < text.length()) {
            	sb.append((char)((int)text.charAt(x)+8)); // Changes the characters to add security
            	x++;
            }
            String scrambled = sb.toString();
    		    		
    		fw = new FileWriter(path);
    		bw = new BufferedWriter(fw);
    		bw.write(scrambled);

    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
    		try {
    			if (bw != null)
    				bw.close();
    			if (fw != null)
    				fw.close();
    		} catch (IOException ex) {
    			ex.printStackTrace();
    		}
    	}
    }
    
    /*******************************************************************************************
     * readFrom()
     * opens file and decrypts the String, returns the decrypted String
     * Parameter:
     * 		path: this is where the application finds the file containing the String
     * return:
     * 		the dectypted StringC:\\CSC131\\spreadsheetId.txt
     * */
    public String readFrom(String path) throws IOException{

    	
    	FileReader fr = new FileReader(path);
    	BufferedReader textReader = new BufferedReader(fr);
    	
    	int numberOfLines = 3;
    	String[] textData = new String[numberOfLines];
    	
    	int i;
    	
    	for(i = 0; i < numberOfLines; i++){
    		textData[i] = textReader.readLine();
    	}

    	textReader.close();
    	
    	String word = textData[0];
    	
        int x = 0;
        StringBuilder sb = new StringBuilder(word.length());
        while(x < word.length()) {
        	sb.append((char)((int)word.charAt(x)-8)); // changes the characters back to origianl form
        	x++;
        }
        return sb.toString();
        
    }
    
    
    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public  Credential authorize() throws IOException {
        // Load client secrets.
        // Todo: Change this text to the location where your client_secret.json resided
        InputStream in = new FileInputStream("C:\\CSC131\\client_secret.json");
            // SheetsQuickstart.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    } // end authorize()

    /**
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public Sheets getSheetsService() throws IOException {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    } // end getSheetsService
    
    /*updates the spreadsheet*/
    public void update(List<Request> requests) throws IOException{
        BatchUpdateSpreadsheetRequest batchUpdateRequestNew = new BatchUpdateSpreadsheetRequest()
    	        .setRequests(requests);
        getService().spreadsheets().batchUpdate(getSpreadsheetId(), batchUpdateRequestNew)
    	        .execute(); 
    } // end update()
    
    
    /*adds string to a list to be placed in a spreadsheet cell*/
    public void addCellData(List<CellData> valuesKey, String value) throws IOException{
    	valuesKey.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue((value))));
    } // end addCellData()
    
    
    /**************************************************************************************
     * adds data from cell data list to a request list
     * Parameters:
     * 		requests: The list used to store requests
     * 		sheetID:  This is the sheet id that the information is being sent to
     * 		row:  This is the row of the spreadsheet data is being added to
     * 		col:  This is the column of the spreadsheet data is being added to
     * 		valuesAdd: This is the data that is being added to the spreadsheet 		
     * */
    public void addToRequest(List<Request> requests, int sheetID, int row, int col, List<CellData> valuesAdd) throws IOException{
    	
    	requests.add(new Request()
                .setUpdateCells(new UpdateCellsRequest()
                        .setStart(new GridCoordinate()
                                .setSheetId(sheetID)
                                .setRowIndex(row)     // set the row to row 1 
                                .setColumnIndex(col)) // set the new column 6 to value "yes" at row 1
                        		.setRows(Arrays.asList(
                                new RowData().setValues(valuesAdd)))
                        		.setFields("userEnteredValue,userEnteredFormat.backgroundColor")));    
    } // end addToRequest()
    
    
    /* *******************************************************************************************
     * return:
     * 		a String list that will print the students attendance to the user
     * */
    public List<String> checkStudentAtt() throws IOException{
    	List<String> attendence = new ArrayList<String>();
    	String range = getSheet();//+"!A3:Z99"; // The range needs to be more dynamic 131-1 refers to the sheetID
    	//String studentID = JOptionPane.showInputDialog("What is your Student ID?");
    	int rowFound = findRow(getStudentId(), getSheet()+"!D:D");
    	if (rowFound == -1){
    		attendence.add("Student ID was not found!!");
    		return attendence;
    	}
    	
    	range = getSheet() +"!"+ rowFound + ":" + rowFound;
    	
        ValueRange response = getService().spreadsheets().values()
                .get(getSpreadsheetId(), range)
                .execute();
        List<List<Object>> values = response.getValues();
        
        
        String rangeDates = getSheet()+"!3:3"; // This is the date row
        ValueRange responseD = getService().spreadsheets().values()
                .get(getSpreadsheetId(), rangeDates)
                .execute();
        List<List<Object>> valuesD = responseD.getValues();
 
        
        for (List<?> dateRow : valuesD) {
            int x = dateRow.size(); // this is the number of filled rows
            for (List<?> studentRow : values) {
            	for(int i = 0; i < x; i++){
            		attendence.add("\t "+ dateRow.get(i)); // This prints the whole date row
            	} // end for
            	attendence.add("<br>");// newline
            	for(int i = 0; i < x; i++){ 
            		attendence.add("\t " + studentRow.get(i)); // This prints matching students row
            	} // end for
            	return attendence;
            } // end for
        } // end else   
        return attendence;
    } // end checkStudentAtt()
        
        
    /* **************************************************************************
     * This is used to confirm that the list is not empty*/
    public boolean listNotEmpty(List<List<Object>> test) throws IOException{
    	if(test == null || test.size() == 0){
    		return false;
        }
        	return true;
    } // end listNotEmpty()
    
    
    /* **********************************************************************************************************
     * Finds the Column that the passed argument is sitting in.
     * Parameters:
     * 		find: the string you wish to find
     * 		range: the row you are looking in. Should be formated "Sheet1!3:3" to contain the entire row. 
     * return:
     * 		 an int indicating which column the argument was found in (A=0, B=1, etc.) or -1 if no match was found 
     * */
    public int findCol(String find, String range) throws IOException{
    	int colNum = 0; // this will be used to store matching column
 
        ValueRange response = getService().spreadsheets().values()
                .get(getSpreadsheetId(), range).execute();
        List<List<Object>> values = response.getValues(); // row to search 
 
        if (values == null || values.size() == 0) { // checks to make sure there is data in list
            System.out.println("No data found.");
        }else {
            for (List<?> row : values) { 
            	for(colNum = 1; colNum < row.size(); colNum++){ // cycles through columns of row
            		if(row.get(colNum).equals(find)){ // checks the column for match
            			return colNum; // return column number where match is found
            		} // end if  
            	} // end for
            } // end for
          } // end else
        return -1; // no match was found
	} // end findCol()
    
    
    /* **********************************************************************************************************
     * Finds the Row that the passed argument is sitting in.
     * Parameters:
     * 		find: the string you wish to find
     * 		range: the col you are looking in. Should be formated "Sheet1!D:D" to contain the entire column. 
     * return:
     * 		an int indicating which row the argument was found in or -1 if no match was found 
     * */
    public int findRow(String find, String range) throws IOException{
    	int rowNum = 0; // this will be used to store the matching row
 
        ValueRange response = getService().spreadsheets().values()
                .get(getSpreadsheetId(), range).execute();
        List<List<Object>> values = response.getValues(); // list of rows  
 
        if (values == null || values.size() == 0) { // checks to make sure there is data in list
            System.out.println("No data found.");
        }else {
            for (List<?> row : values) { // iterates through rows
            	rowNum++;
            	if(row.size() != 0){	// skips empty cells
            		if(row.get(0).equals(find)){ // checks the row for a match
            			return rowNum; // return row number where match is found
            		} // end if  
            	} // end if   	
            } // end for
          } // end else
        return -1; // no match was found
	} // end findRow()
    

  
    /*  DOESNT WORK
    public String getTitle() throws IOException{

    	List<Request> requests = new ArrayList<>();
    	
    	String test = new SpreadsheetProperties().getTitle();
    	return test;


    }
    */
    
    
    /* THIS WILL LIKELY NOT BE USED: too much possibility of destroying data on spreadsheet */
    public void replaceText(String find, String replace) throws IOException{
    	List<Request> requests = new ArrayList<>();
    	// replaces find with replace
     	requests.add(new Request()
    	        .setFindReplace(new FindReplaceRequest()
    	                .setSheetId(131)));
    	// Update
        update(requests);
    	BatchUpdateSpreadsheetRequest body =
    	        new BatchUpdateSpreadsheetRequest().setRequests(requests);
     	BatchUpdateSpreadsheetResponse response =
     			getService().spreadsheets().batchUpdate(getSpreadsheetId(), body).execute();
    	FindReplaceResponse findReplaceResponse = response.getReplies().get(0).getFindReplace();
    	System.out.printf("%d replacements made.", findReplaceResponse.getOccurrencesChanged()); // displays the amount of replacements made
    } // end replaceText()
    

    /**************************************************************************************************************************
     * findCourse()
     * Uses the current time to find what class is currently in session. This allows for automation of class selection for the Professor to set
     * the rollKey, as well as the Student to log their attendance.
     * return:
     * 		1 indicates the class was found
     * 		-1 indicates that there is no class currently in session
     * */
	public int findCourse() throws IOException {
    	int col = findCol(getDayOfWeek(), "2:2"); // finds the col that holds todays day
    	String range = "schedule!4:60"; // range beginning with row of first course
    	ValueRange response = getService().spreadsheets().values()
                .get(getSpreadsheetId(), range).execute();
       	List<List<Object>> values = response.getValues(); // rows of classes 
 
       	for (List<?> row : values) { // cycle through courses
       		if(hasData((String)row.get(col))){ // skips row if course is not in session this day
       			if(isAfter(getCurrentTime(),(String)row.get(col)) && isAfter((String)row.get(col+1),getCurrentTime())){ // if current time is in range of class
       				setSheet((String) row.get(0)); // found the class, store the sheet title
       				return 1; // 1 indicates class found
       			} // end if
       		} // end if
       	} // end for
       	return -1; // -1 indicates class was not found
    } // end findClass

	
	/********************************************************************************************************************
	 * hasData()
	 * Checks String to see if it is empty or only contains white space. This helps avoid errors when reading cells from 
	 * the spreadsheet.
	 * Parameter:
	 * 		the data from a cell in string form
	 * return:
	 * 		true if the cell contains data.
	 * */
	public boolean hasData(String cell) throws IOException {
		return(cell!= null && !cell.isEmpty());
	} // end hasData
	
	
	/***************************************************************************************************************
	 * isAfter()
	 * Compares two Strings in time format H:mm. 
	 * Parameters:
	 * 		time1/time2- two strings that hold a time-stamp in format H:mm
	 * return:
	 * 		true if time1 is after time2
	 * */
	public boolean isAfter(String time1, String time2) throws IOException {
		
		String delims = "[:]"; // divides the hours and minutes
		String[] hourMin1 = time1.split(delims); // assigns hours and min to array
		String[] hourMin2 = time2.split(delims);
		
		// clearly labels hours and min for comparisons
		int hour1 = Integer.parseInt(hourMin1[0]);
		int min1 = Integer.parseInt(hourMin1[1]);
		int hour2 = Integer.parseInt(hourMin2[0]);
		int min2 = Integer.parseInt(hourMin2[1]);
		
		if(hour1 > hour2){
			return true;
		} // end if
		if(hour1 == hour2){
			if(min1 >= min2){
				return true;
			} // end if
		} // end if
		return false;
	} // end isAfter()
	
	
    /* ***********************************************************************************************************************************
     * isThis()
     * compares a string to a specific location on a sheet
     * Parameters:
     * 		isThis: The String to be compared
     * 		sheet: sheet that cell resides in
     * 		row: row location
     * 		col: col location
     * return:
     * 		true if string matches
     * */
    public boolean isThis(String isThis, String sheet, int row, int col) throws IOException{
    	if(getThis(sheet, row, col).equals(isThis)){
    		return true;
    	}
    	return false;


    } // end isThis()
    
    /* ***********************************************************************************************************************************
     * getThis()
     * returns the string that resides in the row/col from the sheet given
     * Parameters:
     * 		sheet: sheet that cell resides in
     * 		row: row location
     * 		col: col location
     * return:
     * 		The string in row/col
     * */
    public String getThis(String sheet, int row, int col) throws IOException{
    	String range = getSheet()+"!"+row+":"+row;
    	ValueRange response = getService().spreadsheets().values()
             .get(getSpreadsheetId(), range).execute();
    	List<List<Object>> values = response.getValues(); // row to search 

    	for (List<?> findRow : values) { 
    		return (String)findRow.get(col);
    	} // end for
    	return null;

    } // end getThis()
    

    public String errorRed(String message) throws IOException{
    	return( "<strong ><p style=\"color:red;background-color: #FFFF00\"> ERROR: " + message +"</p></strong>");
    }
} // end Class RollCall