/*******************************************************************************
 * @file  Table.java
 *
 * @author   John Miller
 */

import java.io.Serializable;
import java.nio.ByteBuffer;

import static java.lang.Boolean.*;
import static java.lang.System.out;

import java.util.*;

//import org.apache.commons.lang.ArrayUtils;

/*******************************************************************************
 * This class implements relational database tables (including attribute names,
 * domains and a list of tuples.  Five basic relational algebra operators are
 * provided: project, select, union, minus and join.  The insert data manipulation
 * operator is also provided.  Missing are update and delete data manipulation
 * operators.
 */
public class Table
       implements Serializable, Cloneable
{
    /** Debug flag, turn off once implemented
     */
    private static final boolean DEBUG = true;

    /** Counter for naming temporary tables.
     */
    private static int count = 0;

    /** Table name.
     */
    private final String name;

    /** Array of attribute names.
     */
    private final String [] attribute;

    /** Array of attribute domains: a domain may be
     *  integer types: Long, Integer, Short, Byte
     *  real types: Double, Float
     *  string types: Character, String
     */
    private final Class [] domain;

    /** Collection of tuples (data storage).
     */
    private final List <Comparable []> tuples;

    /** Primary key. 
     */
    private final String [] key;

    /** Index into tuples (maps key to tuple).
     */
    private final Map <KeyType, Comparable []> index;

    /***************************************************************************
     * Construct an empty table from the meta-data specifications.
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types)
     * @param _key        the primary key
     */  
    public Table (String _name, String [] _attribute, Class [] _domain, String [] _key)
    {
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = new ArrayList <> ();                // also try FileList, see below
//      tuples    = new FileList (this, tupleSize ());
        index     = new TreeMap <> ();                  // also try BPTreeMap, LinHash or ExtHash
    } // Table

    /***************************************************************************
     * Construct an empty table from the raw string specifications.
     * @param name        the name of the relation
     * @param attributes  the string containing attributes names
     * @param domains     the string containing attribute domains (data types)
     */
    public Table (String name, String attributes, String domains, String _key)
    {
        this (name, attributes.split (" "), findClass (domains.split (" ")), _key.split(" "));

        out.println ("DDL> create table " + name + " (" + attributes + ")");
    } // Table

    /***************************************************************************
     * Construct an empty table using the meta-data of an existing table.
     * @param tab     the table supplying the meta-data
     * @param suffix  the suffix appended to create new table name
     */
    public Table (Table tab, String suffix)
    {
        this (tab.name + suffix, tab.attribute, tab.domain, tab.key);
    } // Table

    /***************************************************************************
     * Project the tuples onto a lower dimension by keeping only the given attributes.
     * Check whether the original key is included in the projection.
     * #usage movie.project ("title year studioNo")
     * @param attributeList  the attributes to project onto
     * @return  the table consisting of projected tuples
     */
    public Table project (String attributeList)
    {
        out.println ("RA> " + name + ".project (" + attributeList + ")");
        
        String [] pAttribute = attributeList.split (" ");
        int []    colPos     = match (pAttribute);
        Class []  colDomain  = extractDom (domain, colPos);
        String [] newKey     = null;// FIX: original key if included, otherwise all attributes(FIXED by James and Eddie); wes: after note, a primary key must not be null if primary key is being used in this table
        int count = 0;
        for(int i = 0; i < key.length; i++){
        	for(int k = 0; k< pAttribute.length; k++){
        		if(key[i].equalsIgnoreCase(pAttribute[k])){
        			count++;
        		}
        	}
        }
        
        if(count == key.length){
        	newKey = key;
        }
        else{
        	newKey = pAttribute;
        }
        
        Table     result     = new Table (name + count++, pAttribute, colDomain, newKey);
         
        for (Comparable [] tup : tuples) { // for each tuple in tuples[], do:
            result.tuples.add (extractTup (tup, colPos));
        } // for

        return result;
    } // project

    /***************************************************************************
     * Select the tuples satisfying the given condition.
     * A condition is written as infix expression consists of 
     *   6 comparison operators: "==", "!=", "<", "<=", ">", ">="
     *   2 Boolean operators:    "&", "|"  (from high to low precedence)
     * #usage movie.select ("1979 < year & year < 1990")
     * @param condition  the check condition for tuples
     * @return  the table consisting of tuples satisfying the condition
     */
    public Table select (String condition)
    {
        out.println ("RA> " + name + ".select (" + condition + ")");

        //String [] postfix = { "title", "Star_Wars", "==" };      // FIX: delete after impl
        String [] postfix = infix2postfix (condition);           // FIX: uncomment after impl
        Table     result  = new Table (name + count++, attribute, domain, key);

        for (Comparable[] tup : tuples) { // for each tuple in tuples[], do:
            if (result.evalTup (postfix, tup)){
            	result.tuples.add (tup);
            }
        } // for

        return result;
    } // select

    /***************************************************************************
     * Union this table and table2.  Check that the two tables are compatible.
     * #usage movie.union (show)
     * @param table2  the rhs table in the union operation
     * @return  the table representing the union (this U table2)
     * @author Edward Killmeier
     */
    public Table union (Table table2)
    {
        out.println ("RA> " + name + ".union (" + table2.name + ")");

        Table result = new Table (name + count++, attribute, domain, key);
          //Check if tables are the same	
          if(this.compatible(table2)){
        	  //Add all the tuples from this table to result table
        	  for(Comparable [] tup : tuples){
        		  result.insert(tup);
        	  }
        	  //Check to see if there are matching tuples, if there are
        	  //then don't add, if there is no match then add the tuple.
        	  for(int i = 0; i < table2.tuples.size(); i++){
        		  boolean check = false;
        		  for(int k = 0; k < tuples.size(); k++){
        			  if(table2.tuples.get(i).equals(tuples.get(k))){
        				  check = true;
        			  }
        		  }
        		  if(!check){
        			  result.insert(table2.tuples.get(i));
        		  }
        	  }
          }
          //If tables are not compatible.
          else{
        	  out.println("Tables are not compatible");
          }

        return result;
    } // union

    /***************************************************************************
     * Take the difference of this table and table2.  Check that the two tables
     * are compatible.
     * #usage movie.minus (show)
     * @param table2  the rhs table in the minus operation
     * @return  the table representing the difference (this - table2)
     * @author  Kim Bradley
     */
    public Table minus (Table table2)
    {
      out.println ("RA> " + name + ".minus (" + table2.name + ")");
      
      Table result = new Table (name + count++, attribute, domain, key);
      
      boolean match = false;
      // iterate through this table
      for (int i=0; i < tuples.size(); i++) {
        // iterate through table2
        for (int j=0; j < table2.tuples.size(); j++) {
          // if tuple in this table matches one in table2
          if (tuples.get(i) == table2.tuples.get(j)) {
            match = true;
            break;
          }
        }
        // if tuple in this table has no match in table2
        if (!match) {
          // add tuple to result table
          result.insert(tuples.get(i));
        }
        else match = false;
      }
      
      return result;
    } // minus

    /***************************************************************************
     * Join this table and table2.  If an attribute name appears in both tables,
     * assume it is from the first table unless it is qualified with the first
     * letter of the second table's name (e.g., "s.").
     * In the result, disambiguate the attribute names in a similar way
     * (e.g., prefix the second occurrence with "s_").
     * Caveat: the key parameter assumes joining the table with the foreign key
     * (this) to the table containing the primary key (table2).
     * #usage movie.join ("studioNo == name", studio);
     * #usage movieStar.join ("name == s.name", starsIn);
     * @param condition  the join condition for tuples
     * @param table2     the rhs table in the join operation
     * @return  the table representing the join (this |><| table2)
     * @author Wes Ishee
     */
    public Table join (String condition, Table table2)
    {
        out.println ("RA> " + this.name + ".join (" + condition + ", " + table2.name + ")");
        
        int t1_colNo=0; // column to join on in this.table
        int t2_colNo=0; // column to join on in table2
        String[] comparison = condition.split(" "); // splits the condition into an array
        
        for(int j=0; j<attribute.length; j++){ // finds the column # in this.table to join on
        	if(comparison[0].equals(attribute[j])){
        		t1_colNo = j;
        		break;
        	}
        }
        for(int j=0; j<table2.attribute.length; j++){ // finds the column # in table2 to join on
        	if(comparison[2].equals(table2.attribute[j])){
        		t2_colNo = j;
        		break;
        	}
        }
        
        String[] attr = new String[attribute.length + table2.attribute.length];
        int a=0;
        while(a<attr.length){ // creates an attribute array with all attributes from this.table and table2
        	if(a<attribute.length){
        		attr[a] = attribute[a]; // add in attribute from this.table
        	}else{
        		for(int i=0; i<a; i++){
        			if(attr[a-1].equals(attr[i])){ // checks for duplicate column names between tables and changes the second table to "<table_name>.<column_name>"
        				String prefix = table2.getName();//.substring(0, 1);
        				prefix += "_";
        				attr[a] = prefix + table2.attribute[a-attribute.length]; // add in attribute from table2.attribute
        				break;
        			}
        		}
        	}
        	a++;
        }
        a=0;
        Class[] dom = new Class[domain.length + table2.domain.length];
        while(a<dom.length){ // creates a domain array with all domains from this.table and table2
        	if(a<domain.length){
        		dom[a] = domain[a];
        	}else{
        		dom[a] = table2.domain[a-domain.length];
        	}
        	a++;
        }
        
        for(int i=0; i<attribute.length; i++){
        	if(comparison[2].equals(attribute[i])){ // updating conditional statement with new column name
        		String prefix = table2.getName();//.substring(0, 1);
				prefix += "_";
        		comparison[2] = prefix + attribute[i];
        		break;
			}
        }
        
        
        Table crossProd = new Table (name + count++, attr, dom, key);  // uses created attribute and domain arrays to create a new table with the proper column names and types
        

        
        switch(comparison[1]){ // switch case for condition of join
        	case "==":
        		// populating crossProd.  filling with only tuples where keys are equal
                for(int i=0; i<tuples.size(); i++){
                	for(int j=0; j<table2.tuples.size(); j++){//prev: j<attribute.length
                		if(tuples.get(i)[t1_colNo].equals(table2.tuples.get(j)[t2_colNo])){
                			// creates temp array with length of cross product and copies this.table tuple into it
                        	Comparable[] temp = Arrays.copyOf(tuples.get(i), crossProd.attribute.length); 
                        	for(int k=0; k<table2.attribute.length; k++){ // adds in table2.tuple into temp // prev: k<tuples.size()
                        		temp[k+this.attribute.length] = table2.tuples.get(j)[k];
                        	}
                        	crossProd.insert(temp);
                		}
                	}
                }
        		break;
        	case "!=":
        		// populating crossProd.  filling with only tuples where keys are equal
                for(int i=0; i<tuples.size(); i++){
                	for(int j=0; j<table2.attribute.length; j++){
                		if(!(tuples.get(i)[t1_colNo].equals(table2.tuples.get(j)[t2_colNo]))){
                			// creates temp array with length of cross product and copies this.table tuple into it
                        	Comparable[] temp = Arrays.copyOf(tuples.get(i), crossProd.attribute.length); 
                        	for(int k=0; k<table2.tuples.size(); k++){ // adds in table2.tuple into temp
                        		temp[k+this.attribute.length] = table2.tuples.get(j)[k];
                        	}
                        	crossProd.insert(temp);
                		}
                	}
                }
        		break;
        		
        		
        	case ">":
        		for(int i=0; i<tuples.size(); i++){
                	for(int j=0; j<table2.tuples.size(); j++){
                		if(tuples.get(i)[t1_colNo].compareTo(table2.tuples.get(j)[t2_colNo]) < 0){
                			// creates temp array with length of cross product and copies this.table tuple into it
                        	Comparable[] temp = Arrays.copyOf(tuples.get(i), crossProd.attribute.length); 
                        	for(int k=0; k<table2.attribute.length; k++){ // adds in table2.tuple into temp
                        		temp[k+this.attribute.length] = table2.tuples.get(j)[k];
                        	}
                        	crossProd.insert(temp);
                		}
                	}
                }
        		break;
        	case "<":
        		for(int i=0; i<tuples.size(); i++){
                	for(int j=0; j<table2.tuples.size(); j++){
                		if(tuples.get(i)[t1_colNo].compareTo(table2.tuples.get(j)[t2_colNo]) < 0){
                			// creates temp array with length of cross product and copies this.table tuple into it
                        	Comparable[] temp = Arrays.copyOf(tuples.get(i), crossProd.attribute.length); 
                        	for(int k=0; k<table2.attribute.length; k++){ // adds in table2.tuple into temp
                        		temp[k+this.attribute.length] = table2.tuples.get(j)[k];
                        	}
                        	crossProd.insert(temp);
                		}
                	}
                }
        		break;
        	case ">=":
        		for(int i=0; i<tuples.size(); i++){
                	for(int j=0; j<table2.tuples.size(); j++){
                		if(tuples.get(i)[t1_colNo].compareTo(table2.tuples.get(j)[t2_colNo]) >= 0){
                			// creates temp array with length of cross product and copies this.table tuple into it
                        	Comparable[] temp = Arrays.copyOf(tuples.get(i), crossProd.attribute.length); 
                        	for(int k=0; k<table2.attribute.length; k++){ // adds in table2.tuple into temp
                        		temp[k+this.attribute.length] = table2.tuples.get(j)[k];
                        	}
                        	crossProd.insert(temp);
                		}
                	}
                }
        		break;
        	case "<=":
        		for(int i=0; i<tuples.size(); i++){
                	for(int j=0; j<table2.tuples.size(); j++){
                		if(tuples.get(i)[t1_colNo].compareTo(table2.tuples.get(j)[t2_colNo]) <= 0){
                			// creates temp array with length of cross product and copies this.table tuple into it
                        	Comparable[] temp = Arrays.copyOf(tuples.get(i), crossProd.attribute.length); 
                        	for(int k=0; k<table2.attribute.length; k++){ // adds in table2.tuple into temp
                        		temp[k+this.attribute.length] = table2.tuples.get(j)[k];
                        	}
                        	crossProd.insert(temp);
                		}
                	}
                }
        		break;
        }
        return crossProd;
    } // join

    /***************************************************************************
     * Insert a tuple to the table.
     * #usage movie.insert ("'Star_Wars'", 1977, 124, "T", "Fox", 12345)
     * @param tup  the array of attribute values forming the tuple
     * @return  whether insertion was successful
     */
    public boolean insert (Comparable [] tup)
    {
        out.println ("DML> insert into " + name + " values ( " + Arrays.toString (tup) + " )");
        
        //if float is in domain
        int floatInd = 0;
        boolean floatin = false;
        for(Class iClass : domain){
        	try {
				if(iClass.equals(Class.forName ("java.lang.Float"))){
					domain[floatInd] = Class.forName ("java.lang.Double");
					break;
				}
				else{
					floatInd++;
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        if (typeCheck (tup, domain)) {
        	Comparable [] keyVal = new Comparable [key.length];
            
            int [] cols = match (key);
            
            for (int j = 0; j < keyVal.length; j++){ 
            	keyVal [j] = tup [cols [j]];
            }    
            KeyType type = new KeyType(keyVal);
            if(!(this.index.containsKey(type))){ // if there is not a duplicate, add tuple into index and tuple list
            	index.put(type,tup);
            	tuples.add(tup);
            }else{
            	tuples.add(tup);
            }
//          index.put (new KeyType (keyVal), tup); // this is original code
        	
//          tuples.add (tup); // this is original code
            
            
            return true;
        } else {
        	System.out.println("False");
            return false;
        } // if
    } // insert

    /***************************************************************************
     * Get the name of the table.
     * @return  the table's name
     */
    public String getName ()
    {
        return name;
    } // getName

    /***************************************************************************
     * Print the table.
     */
    public void print ()
    {
        out.println ("\n Table " + name);

        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
        out.print ("| ");
        for (String a : attribute) out.printf ("%15s", a);
        out.println (" |");

        if (DEBUG) {
            out.print ("|-");
            for (int i = 0; i < domain.length; i++) out.print ("---------------");
            out.println ("-|");
            out.print ("| ");
            for (Class d : domain) out.printf ("%15s", d.getSimpleName ());
            out.println (" |");
        } // if

        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
        for (Comparable [] tup : tuples) {
            out.print ("| ");
            for (Comparable attr : tup) out.printf ("%15s", attr);
            out.println (" |");
        } // for
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
    } // print

    /***************************************************************************
     * Determine whether the two tables (this and table2) are compatible, i.e.,
     * have the same number of attributes each with the same corresponding domain.
     * @param table2  the rhs table
     * @return  whether the two tables are compatible
     * @author  Kim Bradley
     */
    private boolean compatible (Table table2)
    {
      int i = 0;
      // if this table and table2 have same number of attributes
      if (attribute.length == table2.attribute.length) {
        // iterate through domains
        for(int j=0; j < domain.length; j++) {
          // if this table and table2 dont have corresponding domains
          if (!(domain[j] == table2.domain[j])) return false;
        }
        return true;
      }
      
      return false;
    } // compatible

    /***************************************************************************
     * Return the column position for the given column/attribute name.
     * @param column  the given column/attribute name
     * @return  the column index position
     */
    private int columnPos (String column)
    {
        for (int j = 0; j < attribute.length; j++) {
           if (column.equals (attribute [j])) return j;
        } // for

        out.println ("columnPos: error - " + column + " not found");
        return -1;  // column name not found in this table
    } // columnPos

    /***************************************************************************
     * Return all the column positions for the given column/attribute names.
     * @param columns  the array of column/attribute names
     * @return  the array of column index positions
     */
    private int [] match (String [] columns)
    {
        int [] colPos = new int [columns.length];

        for (int i = 0; i < columns.length; i++) {
            colPos [i] = columnPos (columns [i]);
        } // for

        return colPos;
    } // match

    /***************************************************************************
     * Check whether the tuple satisfies the condition.  Use a stack-based postfix
     * expression evaluation algorithm.
     * @param postfix  the postfix expression for the condition
     * @param tup      the tuple to check
     * @return  whether to keep the tuple
     */
    @SuppressWarnings("unchecked")
    
    public boolean evalTup (String [] postfix, Comparable [] tup)
    {
    	
    	if (postfix == null) return true;
        Stack <Comparable <?>> s = new Stack <> ();
        
        for (String token : postfix) {
            //is & or | - evaluate top 2 on the stack 
        	if(token.equalsIgnoreCase("&") |token.equals("|")){
                    if(token.equalsIgnoreCase("&")){
                            
                    	Comparable clause1 = s.pop();
                        Comparable clause2 = s.pop();

                        Boolean truthVal = (clause1.compareTo(true) == 0 && clause2.compareTo(true) == 0);
                        s.push(truthVal);
           
                    
                    }
                    else{
                    	Comparable clause1 = s.pop();
                    	System.out.println("Push 444 -> " + clause1);
                        Comparable clause2 = s.pop();
                        System.out.println("Push 444 -> " + clause2);
                        
                        Boolean truthVal = (clause1.compareTo(true) == 0 | clause2.compareTo(true) == 0);
                        s.push(truthVal);
                    }
                }
            //is comparison - evaluate then push boolean
            else if(isComparison(token)){
                    Comparable right = s.pop();
                    Comparable left = s.pop();
                   
                    Boolean truthVal = compare(left, token, right);
                    s.push(truthVal);
        
            }
            //is operand - push if attribute, push val in tuple, else push
            else{
                if(Arrays.asList(this.attribute).contains(token)){
                     s.push(tup[this.columnPos(token)].toString());
                }
                else{
                	 s.push(token);
                }           
            }
        } // for
        System.out.println();
        return (Boolean) s.pop ();         
    } // evalTup

    /***************************************************************************
     * Pack tuple tup into a record/byte-buffer (array of bytes).
     * @param tup  the array of attribute values forming the tuple
     * @return  a tuple packed into a record/byte-buffer
     * @author Chris Klappich
     *
     */
    byte [] pack (Comparable [] tup)
    {
        byte [] record = new byte [tupleSize ()];
        byte [] b      = null;
        int     i      = 0;
        byte [] temp = new byte[0];
        //System.out.println(tupleSize());
        for (int j = 0; j < domain.length; j++) {
        	//ByteBuffer buffer = ByteBuffer.allocate(this.tupleSize());
        	//System.out.println(tup[j]);
        	//System.out.println("tuple: "+tup[j]);
            switch (domain [j].getName ()) {
            	case "java.lang.Integer":
            		ByteBuffer buffer = ByteBuffer.allocate(4);
            		buffer.putInt((Integer)tup[j]);
            		b=buffer.array();
            		//System.out.println(b.length);
            		break;
            	case "java.lang.String":
            		byte [] tempArray = new byte [64];
            		b = ((String) tup [j]).getBytes();
            		ByteBuffer buffer1 = ByteBuffer.allocate(4);
            		buffer1.putInt(b.length);
            		//System.out.println("here: "+buffer1.getInt(0));
            		byte [] t = buffer1.array();
//            		for(int x=0;x<t.length;++x){
//            			System.out.println(t[x]);
//            		}
            		//System.out.println("String size: " +b.length);
            		int f=0;
            		for(int x=4; x>0;--x){
            			tempArray[64-x] = t[f];
            			++f;
            			//System.out.println("in loop: "+t[f]);
            		}
            		for(int y=0;y<b.length;++y){
            			tempArray[y]=b[y];
            		}
            		b=tempArray;
            		//System.out.println("Final length: "+b.length);
            		break;
            	case "java.lang.Short":
            		ByteBuffer buffer2 = ByteBuffer.allocate(2);
            		int te = (Integer)tup[j];
            		Short shortVal = (short)te;
            		
            		buffer2.putShort(shortVal);
            		b=buffer2.array();
            		break;
            	case "java.lang.Float":
//            		ByteBuffer buffer3 = ByteBuffer.allocate(8);
//            		buffer3.putDouble((Double)tup[j]);
//            		b=buffer3.array();
//            		break;
            		ByteBuffer buffer3 = ByteBuffer.allocate(4);
            		Double doub = (Double)tup[j];
            		float fl = doub.floatValue();
            		System.out.println("float value: "+fl);
            		buffer3.putFloat(fl);
            		b=buffer3.array();
            		break;
            	case "java.lang.Double":
            		ByteBuffer buffer4 = ByteBuffer.allocate(8);
            		buffer4.putDouble((Double)tup[j]);
            		b=buffer4.array();
            		break;
            	case "java.lang.Long":
            		Integer temp_int = (Integer)tup[j];
            		long temp_long   = temp_int.longValue();
            		ByteBuffer buffer5 = ByteBuffer.allocate(8);
            		buffer5.putLong(temp_long);
            		b=buffer5.array();
            		break;
            } // switch
            if (b == null) {
                out.println ("Table.pack: byte array b is null");
                return null;
            } // if
            for (int k = 0; k < b.length; k++){
            	//System.out.println(i+ " "+ k);
            	record [i++] = b [k];
            }
        } // for
       // System.out.println(record);
        return record;
    } // pack
    /***************************************************************************
     * Unpack the record/byte-buffer (array of bytes) to reconstruct a tuple.
     * @param record  the byte-buffer in which the tuple is packed
     * @return  an unpacked tuple
     * @author Chris Klappich
     */ 
    Comparable [] unpack (byte [] record)
    {
    	//System.out.println(record);
    	Comparable [] tuple = new Comparable [domain.length];
    	ByteBuffer buf = ByteBuffer.wrap(record);
    	int offset =0;
    	//byte [] tempArray = buf.array();
    	//System.out.println(tempArray.length);
    	for (int j = 0; j < domain.length; j++) {
            switch (domain [j].getName ()) {
            	case "java.lang.Integer":
            		byte [] intBytes = new byte[4];
            		for(int x=0; x<4;++x){
            			intBytes[x]= record[offset+x];
            		}
            		//System.out.println("integer: "+offset);
            		ByteBuffer intBuf = ByteBuffer.wrap(intBytes);
            		tuple[j]= intBuf.getInt();
            		//System.out.println(tuple[j]);
            		offset += 4;
            		break;
            	case "java.lang.String":
            		//System.out.println("String: "+offset);
            		ByteBuffer temp = ByteBuffer.wrap(record);
            		int size = temp.getInt(offset+60);
            		//System.out.println(size);
            		byte [] string = new byte[size];
            		for(int x=0;x<size;++x){
            			//System.out.println(record[offset+x]);
            			string[x]=record[offset+x];
            		}
            		String newString = new String(string);
            		//System.out.println("new String: "+newString);
            		tuple[j]=newString;
            		offset+=64;
            		break;
            	case "java.lang.Short":
            		//System.out.println("short: "+offset);
            		byte [] shortBytes = new byte[2];
            		for(int x=0;x<2;++x){
            			shortBytes[x]=record[offset+x];
            		}
            		ByteBuffer shortBuf = ByteBuffer.wrap(shortBytes);
            		tuple[j]=shortBuf.getShort();
            		offset+=2;
            		break;
            	case "java.lang.Float":
            		byte [] floatBytes = new byte[4];
            		for(int x=0;x<4;++x){
            			floatBytes[x]=record[offset+x];
            		}
            		ByteBuffer floatBuf = ByteBuffer.wrap(floatBytes);
            		tuple[j]=floatBuf.getFloat();
            		offset +=4;
            		break;

            	case "java.lang.Double":
            		//System.out.println("double: "+offset);
            		byte [] doubleBytes = new byte[8];
            		for(int x=0;x<8;++x){
            			doubleBytes[x]=record[offset+x];
            		}
            		ByteBuffer doubleBuf = ByteBuffer.wrap(doubleBytes);
            		tuple[j]=doubleBuf.getDouble();
            		offset +=8;
            		break;
            	case "java.lang.Long":
            		//System.out.println("long: "+offset);
            		byte [] longBytes = new byte[8];
            		for(int x=0;x<8;++x){
            			longBytes[x]=record[offset+x];
            		}
            		ByteBuffer longBuf = ByteBuffer.wrap(longBytes);
            		tuple[j]=longBuf.getLong();
            		offset += 8;
            		break;
            } // switch
            
            
           
    	}
    	
        return tuple;
    } // unpack
    /***************************************************************************
     * Determine the size of tuples in this table in terms of the number of bytes
     * required to store it in a record/byte-buffer.
     * @return  the size of packed-tuples in bytes
     * @author Chris Klappich
     */ 
    public int tupleSize ()
    {
        int s = 0;
        for (int j = 0; j < domain.length; j++) {
            switch (domain [j].getName ()) {
            	case "java.lang.Integer": s += 4;  break;
            	case "java.lang.String":  s += 64; break;
            	case "java.lang.Long": s+= 8; break;
            	case "java.lang.Short": s+= 2; break;
            	case "java.lang.Double": s+= 8; break;
            	case "java.lang.Float": s+=8;break;

            } // if
        } // for
        return s;
    } // tupleSize
     

    //------------------------ Static Utility Methods --------------------------

    /***************************************************************************
     * Check the size of the tuple (number of elements in list) as well as the
     * type of each value to ensure it is from the right domain. 
     * @param tup  the tuple as a list of attribute values
     * @param dom  the domains (attribute types)
     * @return  whether the tuple has the right size and values that comply
     *          with the given domains
     * @author  Kim Bradley
     */
    private static boolean typeCheck (Comparable [] tup, Class [] dom)
    {
      int i = 0;
      
      // if tuple doesnt have same size as domain
      if (tup.length != dom.length){
    	  System.out.println("length false");
    	  return false;
      }
      
      // iterate through tuple
      for (Comparable t : tup) {
        // if tuple value type matches dom type
        if (t.getClass().equals(dom[i])){
        	
        	i++;
        }
        else{
        	System.out.println("match false");
        	return false;
        }
      }
      
      return true;
    } // typeCheck

    /***************************************************************************
     * Determine if the token/op is a comparison operator.
     * @param op  the token/op to check
     * @return  whether it a comparison operator
     */
    private static boolean isComparison (String op)
    {
        return op.equals ("==") | op.equals ("!=") |
               op.equals ("<")  | op.equals ("<=") |
               op.equals (">")  | op.equals (">=");
    } // isComparison

    /***************************************************************************
     * Compare values x and y according to the comparison operator.
     * @param   x   the first operand
     * @param   op  the comparison operator
     * @param   y   the second operand
     * @return  whether the comparison evaluates to true or false
     */
    @SuppressWarnings("unchecked")
    private static boolean compare (Comparable x, String op , Comparable y)
    {
        switch (op) {
        case "==": return x.compareTo (y) == 0;
        case "!=": return x.compareTo (y) != 0;
        case "<":  return x.compareTo (y) <  0;
        case "<=": return x.compareTo (y) <= 0;
        case ">":  return x.compareTo (y) >  0;
        case ">=": return x.compareTo (y) >= 0;
        default: { out.println ("compare: error - unexpected op"); return false; }
        } // switch
    } // compare

    /***************************************************************************
     * Convert an untokenized infix expression to a tokenized postfix expression.
     * This implementation does not handle parentheses ( ).
     * Ex: "1979 < year & year < 1990" --> { "1979", "year", "<", "year", "1990", "<", "&" } 
     * @param condition  the untokenized infix condition
     * @return  resultant tokenized postfix expression
     * @author Chris Klappich
     */
    private static String [] infix2postfix (String condition)
    {
    	if (condition == null | condition.trim () == "") return null;
        
    	String [] infix   = condition.split (" ");  // tokenize the infix
        for(int x = 0; x<infix.length;++x){
        	String temp = infix[x];
        	if(temp.charAt(0) == '\'' && temp.charAt(temp.length()-1) == '\'' ){
        		temp = temp.substring(1, temp.length()-1);
        		out.println(temp);
        		infix[x]=temp;
        	}
        }
        
        String [] postfix = new String [infix.length];    // same size, since no ( ) 
        String [] expWaitList = new String[infix.length];
        boolean prevExp = false;
        int insertPoint =0;
        int wait =0;
        for(int x=0;x<postfix.length;++x){
        	//System.out.println("looking at "+infix[x]);
        	if(!isComparison(infix[x])&&(!infix[x].equals("&")&&!infix[x].equals("|")&&!infix[x].equals("-"))){
        		postfix[insertPoint]=infix[x];
        		++insertPoint;
        		if(prevExp==false){
        			//System.out.println("switch");
        			prevExp=true;
        		}
        		else if(prevExp==true && wait>0){
        			//System.out.println("here");
        			prevExp=false;
        			//System.out.println(expWaitList[wait-1]);
        			while(wait>0){
        				postfix[insertPoint] = expWaitList[wait-1]; 
        				++insertPoint;
        				--wait;
        			}
        		}
        			
        		else if(prevExp==true && wait!=0){
        			postfix[insertPoint]=infix[x];
        			++insertPoint;
        			postfix[insertPoint]=expWaitList[wait];
        			--wait;
        			//System.out.println("Wait is: "+wait);
        		}
        		else{
        			//System.out.println("Invalid infix expression");
        		}
        	}
        	else if(isComparison(infix[x])){
        		System.out.println("is comparison");
        		if(prevExp == false){
        			//System.out.println("Invalid infix expression");
        		}
        	
        		else{
        			expWaitList[wait]=infix[x];
        			++wait;
        		}
        	}
        	else{
        		//System.out.println("not string or comparison");
        		if(prevExp==true){
        		//	System.out.println("Invalid infix expression");
        		}
        		else{
        			expWaitList[wait]=infix[x];
        			++wait;
        		}
        	}
        }
        //quick fix failed
       // while(insertPoint<postfix.length&wait>0){
        //	postfix[insertPoint]=expWaitList[wait-1];
        	//++insertPoint;
        	//--wait;
       // }
        return postfix;
    } // infix2postfix

    /***************************************************************************
     * Find the classes in the "java.lang" package with given names.
     * @param className  the array of class name (e.g., {"Integer", "String"})
     * @return  the array of Java classes for the corresponding names
     */
    private static Class [] findClass (String [] className)
    {
        Class [] classArray = new Class [className.length];

        for (int i = 0; i < className.length; i++) {
            try {
                classArray [i] = Class.forName ("java.lang." + className [i]);
            } catch (ClassNotFoundException ex) {
                out.println ("findClass: " + ex);
            } // try
        } // for

        return classArray;
    } // findClass

    /***************************************************************************
     * Extract the corresponding domains from the group.
     * @param group   where to extract from
     * @param colPos  the column positions to extract
     * @return  the extracted domains
     */
    private static Class [] extractDom (Class [] group, int [] colPos)
    {
        Class [] dom = new Class [colPos.length];

        for (int j = 0; j < colPos.length; j++) {
            dom [j] = group [colPos [j]];
        } // for

        return dom;
    } // extractDom

    /***************************************************************************
     * Extract the corresponding attribute values from the group.
     * @param group   where to extract from
     * @param colPos  the column positions to extract
     * @return  the extracted attribute values
     */
    private static Comparable [] extractTup (Comparable [] group, int [] colPos)
    {
            
            Comparable [] tup = new Comparable [colPos.length];
        
            int i = 0;
            for(int col : colPos){
                    tup[i] = group[col];
                    i++;
            }
   
        return tup;
    } // extractTup

} // Table class