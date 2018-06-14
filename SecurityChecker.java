import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;

/*
 * Title: SecurityChecker.java
 * SubClass of: mini1.java
 * Class: CS522
 * Date: 2/21/18
 * @author Aaron B
 * TODO CHECK PRIVELEGES OF USER DOING QUERIES
 * Returns true if checker passes
 */
public class SecurityChecker {
    
	/**
	* Function: checks if the current user has authorization to execute the command within the submitted file
	* @param fileName - name of the file submitted by the user
	* @param username - username of person attempting to run query/DML
	* @param conn - Oracle connection
	* @return boolean 
	*/
    public boolean privCheck(String fileName, String username, Connection conn){ // Should only be called after table has been verified
    //Check if the user has proper permission
    //Select privilege from HasPrivs Where RName = 'table' AND Username = 'username'
    //if result is "Y" then return true else false
       
        String priv = "";
        boolean answer = false;
         Scanner word;
        try {
            word = new Scanner(new File(fileName));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatabaseCompiler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.print("FileNotFoundException");
            return answer;
        }
        String[] firstLine = word.nextLine().split(" ");
        
        switch(firstLine[0].toLowerCase()){
                case "select":
                    priv = " S_elect";
                    break;
                case "insert":
                    priv = " I_nsert";
                    break;
                case "delete":
                    priv = " D_elete";
                    break;
                case "update":    
                    priv = " U_pdate";
                    break;    
                    
                default:
                    //System.out.println(" There was an error reading the DML command");
                    return answer;
            }
        
        try{
        Statement stmnt = conn.createStatement();
        //System.out.println("CheckPrivCheck");
        for(int i=1;i<firstLine.length; i++){
            String query = "SELECT " + priv + " FROM HasPrivs WHERE RName = '" + firstLine[i] + "' AND USERNAME = '" + username + "'";
            ResultSet rset = stmnt.executeQuery(query);
            if(rset.next())
            	firstLine[i] = rset.getString(1);
            else
            	return false; //returns false if username is not in HasPrivs table for the table in question
                 
        }
     
        for(int i= 1; i<firstLine.length;i++){
            if(firstLine[i].equalsIgnoreCase("y")) 
                answer = true;
            else
                answer = false;        	
        }
        //System.out.println(answer);
        return answer;
        
        }
        catch(SQLException e){
            System.out.println("Caught SQL Exception: \n" + e );
        }
         return answer;
        
}
    /**
     //function: Execute the GRANT privileges command within the submitted file
     * @param fileName - Name of file submitted by user
     * @param conn - Oracle Connection
     */
    public void privGrant(String fileName, Connection conn){
        Scanner word;  //Scanner for scanning the file with try/ catch
        try {
            word = new Scanner(new File(fileName));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatabaseCompiler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.print("FileNotFoundException");
            return;
        }
         String[] firstLine = word.nextLine().split(" "); // Acquire table name and username
         
         try{                                               //Look in the HasPrivs table to see if an entry exists,  if not add an entry
            Statement stmt = conn.createStatement();
            
           ResultSet rset = stmt.executeQuery("SELECT Rname, Username FROM HasPrivs WHERE Rname = '" + firstLine[1] + "' AND UserName = '" + firstLine[2] +"'"  );
           //System.out.println("DEBUG #1");
            if(!rset.next()){
                stmt.executeQuery("INSERT INTO HasPrivs VALUES( '" + firstLine[1] + "', '" + firstLine[2] +"', 'N', 'N', 'N', 'N')"  );
                System.out.println("Entry added to the Priveleges table");
            }
        
        
        //If there is already an entry in the HasPrivs table then update it with the appropriate values
       
        String fileEnd = word.next();
        String query1 = "UPDATE HasPrivs SET "; //First half of query
        String query2 = "WHERE RNAME = '" + firstLine[1] + "'AND UserName = '" + firstLine[2] + "'"; // Second half of query
        
        String priv = "";
        while(!fileEnd.equalsIgnoreCase("END")){  //Filter through file to find which privileges to grant
              
              
            switch(fileEnd.toLowerCase()){
                case "select":
                    priv += "S_elect = 'Y' ";
                    break;
                case "insert":
                    priv += "I_nsert = 'Y' ";
                    break;
                case "delete":
                    priv += "D_elete = 'Y' ";
                    break;
                case "update":    
                    priv += "U_pdate = 'Y' ";
                    break;    
                    
                default:
                    break;
            }
            
            query1 += priv;  //append proper column names to first half of query
            fileEnd = word.next();
            if(!fileEnd.equalsIgnoreCase("END"))
                priv = ",";
        }
        query1 = query1  + query2; //combine 2 query halves to make a whole
    
        //Execute the query
       
                stmt.executeQuery(query1);
                stmt.executeQuery("commit");
                System.out.println("Privileges granted to " + firstLine[2]);
           }
        catch(SQLException e){
            System.out.println("Caught SQL Exception: \n" + e );
        }
    }
}