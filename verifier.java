import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Title: verifier.java
 * SubClass of: mini1.java
 * Class: CS522
 * Date: 2/21/18
 * @Author: Kevin Leehan
 *
 *   The verifier figures out if the submitted command is a GRANT or Query/DML
 *   If GRANT
 *    	Verifier checks 
 *    		IF the table given is stored in the RELATION table
 *	 IF Query/DML
 *		Verifier checks 
 *			IF user attempting to run query has privileges over tables in query
 *			IF given table exists in the RELATION table    
 *			IF given column exists in the ATTRIBUTES table
 *			IF type of given information matches information of stored column
*/

public class verifier {
	
	static Connection conn;
	static Statement stmt;
	/**
	 * Function: Determines if the passed in file contains a GRANT or DML/Query then calls the tableExists and if needed columnExists functions 
	 * @param fileName - name of file submitted by user
	 * @param conn - connection passed in from mini1
	 * @return boolean
	 * @throws SQLException
	 */
	public boolean checkStatement(String fileName, Connection conn) throws SQLException {
		verifier.conn = conn; 
		Scanner sc;
		try {
	          sc = new Scanner(new File(fileName));
	        } catch (FileNotFoundException ex) {
	            Logger.getLogger(verifier.class.getName()).log(Level.SEVERE, null, ex);
	            System.out.print("FileNotFoundException");
	            return false;
	        }
	        
	        String[] temp = sc.nextLine().split(" ", 2);
	        String command = temp[0];
	        if(command.equals("GRANT")){
	        	String[] temp2= temp[1].split(" ");
		        if(!tableExists(temp2[0])) {
		        	System.out.println("Query is incorrect");
		        	return false;
		        }
	           //	String username = temp2[1];
	        		        			
	        }else if(command.equals("SELECT")||command.equals("INPUT")||
	        			command.equals("UPDATE")||command.equals("DELETE")){
	        	//System.out.println("check Query/DML");
	        	String[] tables = temp[1].split(" ");
	        	for(int i=0; i<tables.length; i++) {
	        		if(!tableExists(tables[i])) {
	        			System.out.println("Query is incorrect");
	        			return false;
	        		}
	        	}
	        }
	        String[] columns = sc.nextLine().split(" ");
	        
	        while(!columns[0].equals("END")) {
	        	if(!columnExists(columns)) 
	        		return false;
	        	
	        	columns = sc.nextLine().split(" ");
        	}

	    return true;
	}
	/**
	 * Function: determines if a referenced table is located in the Relation table
	 * @param tableName - The name of table to check exists
	 * @return boolean
	 * @throws SQLException
	 */
	public boolean tableExists(String tableName) throws SQLException {
		stmt = conn.createStatement ();
		ResultSet rset=stmt.executeQuery("SELECT RNAME from Relation WHERE RName ='"+tableName+"'");
		if(!rset.next()) 
			return false;
		return true;
	}
	
	/**
	 * Function: Checks if the referenced column name exists within ATTRIBUTES 
	 *			(and if provided is a valid type and size) 
	 * @param fileRow - a split line from the file submitted by the user
	 * @return boolean
	 * @throws SQLException
	 */
	public boolean columnExists(String[] fileRow) throws SQLException {
		String columnName="";
		String table="";
		String query="";
		String aType="";
		int aSize=0;
		int end = fileRow.length-1;
		stmt = conn.createStatement ();
			
			if(fileRow.length==1) {
				columnName=fileRow[0];
						return true;
			}else if(fileRow[end].equals("BLANK")) {
				aType=fileRow[end];
				columnName=fileRow[end-1];
				if(fileRow.length==3) {  //EMPLOYEE NAME BLANK
					table=fileRow[0];
					query="SELECT RName FROM ATTRIBUTES WHERE RName='"+table+"' and AName='"+columnName+"'";
					
				}else if (fileRow.length==2)   //ID BLANK
					query="SELECT RName FROM ATTRIBUTES WHERE AName='"+columnName+"'";
			}else {
				aSize=Integer.parseInt(fileRow[end]);
				aType=fileRow[end-1];
				columnName=fileRow[end-2];
								
				if(fileRow.length==4) { //DEPARTMENTS NAME CHAR 15
					table=fileRow[0];
					query="SELECT RName FROM ATTRIBUTES WHERE RName='"+table+"' and AName='"+columnName+"' and AType='"+aType+"' and ASize>='"+aSize+"'";
					
				}else if (fileRow.length==3)  //NAME CHAR 15
					query="SELECT RName FROM ATTRIBUTES WHERE AName='"+columnName+"' and AType='"+aType+"' and ASize>='"+aSize+"'";
			}
			
			//run query
			stmt = conn.createStatement ();
			ResultSet rset=stmt.executeQuery(query);
			if(!rset.next()) 
				return false;
		return true;
	}
}