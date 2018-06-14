/*
 * To grant privileges the file must be in this format
 * 		First line: GRANT <TABLENAME> <USERNAME>
 * Following lines: <Privilege>
 * 		 Last line: END 
 * Example:
GRANT EMPLOYEES MARTY
SELECT
INSERT
UPDATE
END
 */

import java.sql.*;
import java.util.*;
import java.io.*;

/*   
 *  Project Mini1
 *  Date: 2/21/18
 *  Group Members:
 *  	Aaron Barr
 * 		Kevin Leehan
 * 		Suyog Mankar
 * 	
 *  Title: mini1.java
 *  This program is the entry point of the program program
 *  SubClasses: 
 *  		DatabaseCompiler
 *  		verifier
 *  		SecurityChecker
 *    
 */

class mini1
{
    static BufferedReader keyboard;
    static Connection conn; 
    static Statement stmt;  
    
    public static void main (String args [])throws IOException{
    	String sqlUsername="krl102", password = "cs522";
    	keyboard = new BufferedReader(new InputStreamReader (System.in));
    	            
            Scanner reader = new Scanner(System.in);  // Reading from System.in
            String username = "";
            do {	//Makes sure user is not empty or only spaces
            	System.out.print("Enter your username: ");
            	username = reader.nextLine();
            }while((username.trim().length() < 1) || username.isEmpty() );
            username = username.toUpperCase();
            if(username.equals("EXIT")) 
            	System.exit(0);
        		        	
    	try { 
    		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
    		System.out.println("Connecting to database...");
    		conn = DriverManager.getConnection (
	    		"jdbc:oracle:thin:@oracle1.wiu.edu:1521/toolman.wiu.edu", sqlUsername, password);
    		conn.setAutoCommit(false);
            System.out.println("Connected");
            
            DatabaseCompiler compiler = new DatabaseCompiler();
            verifier verifier = new verifier();
            SecurityChecker checker = new SecurityChecker();
            
            stmt = conn.createStatement ();
            ResultSet checkUsername=stmt.executeQuery("SELECT * from UserID WHERE username ='"+username+"'");
            if(!checkUsername.next()) {
            	String insertUserName = "INSERT INTO USERID Values ('"+username+"')";
            	stmt = conn.createStatement ();
            	stmt.execute(insertUserName);
            	System.out.println("Username added to table username");
            	stmt.execute("commit");
            }else {
            	System.out.println("Welcome back, "+username);
            }
            String fileName="";
            do {
            	System.out.print("Enter a filename: ");
            	fileName = reader.nextLine(); // Records user input to "input"
            }while((fileName.trim().length() < 1) || (fileName.isEmpty())); 
			while(!fileName.equalsIgnoreCase("exit")){
    			
    			File file = new File(fileName);
    			if(!file.exists()) {
    				System.out.println("The file specified does not exist.");
    				System.out.print("Enter a filename: ");
    				fileName = reader.nextLine(); // Records user input to "input"
    			}else {
    				
    				String type;
    				Scanner sc = new Scanner(new File(fileName));
    		        type = sc.next();
    		        sc.close();
    				if(type.equals("TABLE")) {
    					//COMMAND = TABLE
    					compiler.createTable(fileName, username, conn);
    				}else if(type.equals("GRANT")) {
    					//COMMAND = GRANT
    					if(verifier.checkStatement(fileName, conn))
    						checker.privGrant(fileName, conn);
    				}else {
    					//COMMAND = SELECT, INSERT, DELETE, OR UPDATE
    					if(checker.privCheck(fileName, username, conn)) {
    						if(verifier.checkStatement(fileName, conn)) {
    						
    						   System.out.println("Query verified as correct!");
    						   stmt.executeQuery ("commit");
    						}else
    							System.out.println("Query invalid!");
    					}else
    						System.out.println("Query invalid!");	
    				}
  			
    				do {
    					System.out.print("Enter a filename: ");
    					fileName = reader.nextLine(); // Records user input to "input"
    				}while((fileName.trim().length() < 1) || (fileName.isEmpty()));
    			}
    		}
    		reader.close(); //Closes the scanner
    	
    		stmt.executeQuery ("commit");
    		conn.close();
    		System.out.println("Database connection closed.");
    	}catch(SQLException e){
    		System.out.println("Caught SQL Exception: \n     " + e);
    	}
    }//end Main
}