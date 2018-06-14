import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 *  Title: DatabaseCompiler
 *  SubClass of: mini1.java
 *  Class: CS522
 *  Date: 2/21/18
 *  @author Aaron B
 *  
 */
public class DatabaseCompiler {

    /**
     * Function: create the table found in the first line of the file submitted by the user
     * @param fileName - name of file
     * @param userName - Username input provided by the person currently running the program
     * @param conn - Oracle connection
     */
   public void createTable(String fileName, String userName, Connection conn){
        //Read in the file
       
        Scanner word;
        try {
            word = new Scanner(new File(fileName));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatabaseCompiler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.print("FileNotFoundException");
            return;
        }
        
        String tableType = word.next();
        String tableName = word.next();
        
        try{
            Statement stmt = conn.createStatement();
            //Check if the table exists already
            ResultSet checkTableExists=stmt.executeQuery("SELECT * from Relation WHERE RName ='"+tableName+"'");
            if(!checkTableExists.next()){
            
            	//EXECUTE ME
            	String query = "Insert INTO Relation(RName,Type,Username) VALUES('" + tableName + "', " + "'" + tableType + "', '" + userName + "')" ; //Add entry to Relation table
            	stmt.executeQuery (query);
            	System.out.println("Table "+tableName+" has been created.");
            	
            	String fileEnd = word.next();
            	while(!fileEnd.equalsIgnoreCase("END")){ // This loop adds entries into the Attributes table for the table entered above
            		String aName = fileEnd;
            		String aType = word.next();
            		int aSize = Integer.parseInt(word.next());
            		
            		query = "Insert INTO Attributes VALUES('" + tableName +"'," + "'" + aName + "'," + "'" + aType + "'," + aSize + ")";
            		// Execute the Query
            		stmt.executeQuery(query);
            		
            		fileEnd = word.next();
            	}
            	stmt.executeQuery ("commit");
            word.close();
            }else {
            	System.out.println("Table already exists!");
            }

            }
        catch(SQLException e){
            System.out.println("Caught SQL Exception: \n" + e );
        }
    }
}
