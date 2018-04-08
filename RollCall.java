/* Author: Kristian Brasel
 * Project: Roll Call
 * Date: Summer 2017
 * */


package rollCall;

import java.io.IOException;
import java.util.List;

public class RollCall {

    
    /***********************************************************************************************************
     * studLogIn()
     * The student logs their attendance for the day by passing their student Id along with the given rollKey
     * Parameters:
     * 		args[0]:  student Id
     * 		args[1]:  the roll key that the professor gave to the students
     * return: 
     * 		a message informing the user that their attendance was logged, or an error message explaining why it wasn't
     */
    public static String studLogIn(String studId, String rollKey, double latitude, double longitude) throws IOException {

    	StudentRoll book = new StudentRoll(studId, latitude, longitude);
    	
    	book.setSheet("CSC 131-1"); // <---- DELETE IN FINAL VERSION
    	
    	/*THIS WILL BE USED IN THE FINAL VERSION. IT IS FULLY FUNCTIONAL BUT MAKES IT HARD TO TEST OTHER FUNCTIONS
    	if(book.findCourse() == -1){
    		return "Class is currently not in session. Please try again durring class.";
    	}
    	*/
    	String message = book.present(rollKey);

   		return message;
    } // end studLogIn()
    
    
    /***************************************************************************************************************
     * studCheckAttendance()
     * The student checks their attendance for the class
     * Parameter:
     * 		args[0]:  student Id
     * return: 
     * 		a String list containing the dates and attendance
     */
    public static List<String> studCheckAttendance(String studId, String course) throws IOException {
    	StudentRoll book = new StudentRoll(studId, course); // creates object and gives necessary data
    	List<String> attendence = book.checkStudentAtt();
    	
   		return attendence;
    } // end studCheckAttendance()
    
    
    /*****************************************************************************************************************
     * profSetRollKey()
     * The professor sets a rollKey for the daily attendance, students will use this to log into their class
     * Parameter:
     * 		rollKey:  roll key set by professor
     * return:
     * 		a string either indicating a successful log of roll key, or if a roll key has already been logged a message
     * 		is returned reminding the professor of the current roll key.
     */
    public static String profSetRollKey(String rollKey, int timeLimit, double latitude, double longitude) throws IOException {
    	ProfRoll book = new ProfRoll(timeLimit, latitude, longitude);

    	book.setSheet("CSC 131-1"); // <--- DELETE IN FINAL VERSION
    	book.placeLocation();
    	book.setLocation(latitude, longitude); // <--- DELETE IN FINAL VERSION
    	
    	
    	/*  THIS WILL BE USED IN THE FINAL VERSION. IT IS FULLY FUNCTIONAL BUT MAKES IT HARD TO TEST OTHER FUNCTIONS
    	if(book.findCourse() == -1){ // if no class is found is in session currently a message is returned, no rollkey is set
    		return "Class is currently not in session. Please try again during class.";
    	}
    	*/
    	String message = book.setKey(rollKey);
    	
   		return message; // indicates success, or specific error
    } // end profSetRollKey()


    /*********************************************************************************************************************
     * profLogStudentIn()
     * Allows the professor to log a student in if the student is experiencing difficulties logging themselves in. 
     * Bypasses all security checks.
     * Parameters:
     * 		course: student Id that is being logged in
     *		studId: user needs to select which class they are logging into
     *		txtDate: this is the date the student could not log in
     * 
     * */
    public static String profLogStudentIn(String course, String studId, String txtDate) throws IOException {
    	ProfRoll book = new ProfRoll(studId, course, txtDate);
		return book.profLogStudIn();
	}
    
    /*********************************************************************************************************************
     * saveSpreadsheetAddress()
     * Stores spreadsheet ID in an external file(scrambled) to allow professor to create as many spreadsheets as possible.
     * Parameters:
	 * 		ssid: The string of the google spreadsheet address 
     * */
    public static String saveSpreadsheetAddress(String ssid) throws IOException {
    	ProfRoll book = new ProfRoll();
		return book.saveSpreadsheetAddress(ssid);
	}
    
    /*********************************************************************************************************************
     * profClassReport()
     * The professor gets a report for the whole class.
     * Parameters:
	 * 		course: This is the class they are requesting a report from. 
     * */
    public static List<List<Object>> profClassReport(String course) throws IOException {
    	ProfRoll book = new ProfRoll();
		return book.getProfReport(course);
	}

    /*********************************************************************************************************************
     * profClearGeoLoc()
     * The professor gets a report for the whole class.
     * Parameters:
	 * 		course: This is the class they are requesting a report from. 
     * */
    public static String profClearGeoLoc(String course) throws IOException {
    	ProfRoll book = new ProfRoll();
		return book.clearGeoLoc(course);
	}
    /* STILL NEEDED:
     * 		Front end support: 
     * 				Professor override. Let professor log someone in for a certain date. prof enters class, date, and student id
     * 				Professor setTitle() sets title of spreadsheet. Good for spreadsheet initialization. Ask professor for name only. title = (name)'s RollCall
     * 				Professor sets new address for google spreadsheet. Just pass String to profClassReport()
     * 				Student also sends IP address 
     * 		Front and back:
     * 				Professor gives everyone credit for attendance for the day. ex: class is cancelled, feeling generous.
     * 				studCheckAttendance() needs to be finished
     * 				Professor sets up Spreadsheet for semester, Enters Professor name, name of classes, and student info
     * 				Professor Clear Geolocation
     * 
     * */
	// System.out.println(book.getTitle());
    
    
    /*Format
     * lat/lon shall never be blank
     * */
}