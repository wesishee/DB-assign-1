
/*******************************************************************************
 * @file LinHash.java
 *
 * @author  John Miller
 */

import java.io.*;
import java.lang.reflect.Array;
import static java.lang.System.out;
import java.util.*;

/*******************************************************************************
 * This class provides hash maps that use the Linear Hashing algorithm.
 * A hash table is created that is an array of buckets.
 */
public class LinHash <K, V>
    extends AbstractMap <K, V>
    implements Serializable, Cloneable, Map <K, V>
{
    /** The number of slots (for key-value pairs) per bucket.
     */
    private static final int SLOTS = 4;
    
    /** The class for type K.
     */
    private final Class <K> classK;
    
    /** The class for type V.
     */
    private final Class <V> classV;
    
    /***************************************************************************
     * This inner class defines buckets that are stored in the hash table.
     */
    private class Bucket
    {
        int    nKeys;
        K []   key;
        V []   value;
        Bucket next;
        @SuppressWarnings("unchecked")
	    Bucket (Bucket n)
        {
            nKeys = 0;
            key   = (K []) Array.newInstance (classK, SLOTS);
            value = (V []) Array.newInstance (classV, SLOTS);
            next  = n;
        } // constructor
    } // Bucket inner class
    
    /** The list of buckets making up the hash table.
     */
    private final List <Bucket> hTable;
    
    /** The modulus for low resolution hashing
     */
    private int mod1;
    
    /** The modulus for high resolution hashing
     */
    private int mod2;
    
    /** Counter for the number buckets accessed (for performance testing).
     */
    private int count = 0;
    
    /** The index of the next bucket to split.
     */
    private int split = 0;
    
    /***************************************************************************
     * Construct a hash table that uses Linear Hashing.
     * @param classK    the class for keys (K)
     * @param classV    the class for keys (V)
     * @param initSize  the initial number of home buckets (a power of 2, e.g., 4)
     */
    public LinHash (Class <K> _classK, Class <V> _classV, int initSize)
	{
	    classK = _classK;
	    classV = _classV;
	    hTable = new ArrayList <> ();
	    mod1   = initSize;
	    mod2   = 2 * mod1;
	} // LinHash
    
    /***************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     * @author Kim Bradley
     */
    public Set <Map.Entry <K, V>> entrySet ()
	{
	    Set <Map.Entry <K, V>> enSet = new HashSet <> ();
	    
	    // iterate through buckets in table
	    for (Bucket b : hTable) {
		// iterate through key-value pairs in current bucket
        	for (int i=0; i < b.nKeys; i++) {
		    // add pair to entry set
		    enSet.add(new AbstractMap.SimpleEntry<K,V> (b.key[i], b.value[i]));
        	}
        	
        	// if current bucket has overflow
        	if (b.next != null) {
		    // iterate through key-value pairs in overflow
		    for (int i=0; i < b.next.nKeys; i++) {
			// add pair to entry set
			enSet.add(new AbstractMap.SimpleEntry<K,V> (b.next.key[i], b.next.value[i]));
		    }
        	}
	    }
            
	    return enSet;
	} // entrySet
    
    /***************************************************************************
     * Given the key, look up the value in the hash table.
     * @param key  the key used for look up
     * @return  the value associated with the key
     * @author Kim Bradley
     */
    public V get (Object key)
    {
        int i = h (key);
        
        // if key is in the higher resolution hash buckets
        if (i < split)
	    i = h2(key);
        
        Bucket b = hTable.get(i);
        count++;
        // loop through bucket keys
        for (int j=0; j<b.nKeys; j++) {
	    if (b.key[j].equals(key))
		return b.value[j];
        }
        // if key not found, check bucket overflow
        if (b.next != null) {
	    b = b.next;
	    count++;
	    for (int j=0; j<b.nKeys; j++) {
	    	if (b.key[j].equals(key))
		    return b.value[j];
	    }
        }
	
        return null;
    } // get
    
    /***************************************************************************
     * Put the key-value pair in the hash table.
     * @param key    the key to insert
     * @param value  the value to insert
     * @return  null (not the previous value)
     * @author Kim Bradley
     */
    public V put (K key, V value)
    {
    	//out.println("add " +key+ " : " +value);
        
        int i = h (key);
        // if hashed key is smaller than split, use higher resolution hash
        if (i < split)
	    i = h2(key);
    	
        // if table is empty, add the initial number of home buckets
        if (hTable.size() == 0) {
	    for (int j=0; j < mod1; j++) {
		hTable.add(new Bucket(null));
	    }
        }
        
        Bucket b = hTable.get(i);
        // if bucket is not full
        if (b.nKeys != SLOTS) {
	    // find open slot
	    for (int j=0; j < SLOTS; j++) {
		if (b.key[j] == null) {
		    // add key-value pair and increment number of keys
		    b.key[j] = key;
		    b.value[j] = value;
		    b.nKeys++;
		    break;
		}
	    }
	    
	// if bucket is full
	} else {

	    // if bucket has overflow
	    if (b.next != null) {
		// find open slot
		for (int j=0; j < SLOTS; j++) {
		    if (b.next.key[j] == null) {
			// add key-value pair and increment number of keys
			b.next.key[j] = key;
			b.next.value[j] = value;
			b.next.nKeys++;
			break;
		    }
		}
		
	    // if bucket doesn't have overflow
	    } else {
		// create overflow bucket
		b.next = new Bucket(null);
		// add key-value pair and increment number of keys
		b.next.key[0] = key;
		b.next.value[0] = value;
		b.next.nKeys++;
	    }
	    
	    // overflow causes split; split bucket of current split index
	    b = hTable.get(split);
	    Bucket newb = new Bucket(null);	
	    
	    // iterate through old bucket key-value pairs
	    int num = b.nKeys;
	    for (int j=0; j < num; j++) {
		// if current key needs to be moved
		if (h(b.key[j]) != h2(b.key[j])) {
		    // add to new bucket
		    newb.key[newb.nKeys] = b.key[j];
		    newb.value[newb.nKeys] = b.value[j];
		    newb.nKeys++;
		    // remove from old bucket
		    b.key[j] = null;
		    b.value[j] = null;
		    b.nKeys--;   
		}   
	    }
	    
	    // iterate through old bucket's overflow key-value pairs
	    if (b.next != null) {
		num = b.next.nKeys;
		for (int j=0; j < num; j++) {
		    // if current key needs to be moved
		    if (h(b.next.key[j]) != h2(b.next.key[j])) {
			// add to new bucket
			newb.key[newb.nKeys] = b.next.key[j];
			newb.value[newb.nKeys] = b.next.value[j];
			newb.nKeys++;
			// remove from old bucket
			b.next.key[j] = null;
			b.next.value[j] = null;
			b.next.nKeys--;
		    }  
		}
	    }
	    
	    // remove intermediate null slots from old bucket and overflow
	    List<K> listK = new ArrayList<K>(Arrays.asList(b.key));
	    List<V> listV = new ArrayList<V>(Arrays.asList(b.value));
	    // iterate through slots and rotate nulls to the end of the bucket
	    for (int j=0; j < listK.size(); j++) {
	    	if (listK.get(j) == null) {
		    listK.remove(null);
		    listK.add(null);
		    listV.remove(null);
		    listV.add(null);
	    	}
	    }
	    
	    // rotate overflow values into open slots of old bucket (if needed)
	    if (b.next != null) {
		List<K> listKO = new ArrayList<K>(Arrays.asList(b.next.key));
		List<V> listVO = new ArrayList<V>(Arrays.asList(b.next.value));
		for (int j=b.nKeys; j < SLOTS; j++) {
		    if ((b.next != null) && (b.next.nKeys != 0)) {
	    		for (int k=0; k < SLOTS; k++) {
			    if (listKO.get(k) != null) {
				listK.set(j, listKO.get(k));
				listKO.set(k, null);
				listV.set(j, listVO.get(k));
				listVO.set(k, null);
				b.nKeys++;
				b.next.nKeys--;
			    }
			}
		    }
		}
		
		// iterate through slots and rotate nulls to the end of the bucket
		for (int j=0; j < listKO.size(); j++) {
		    if (listKO.get(j) == null) {
			listKO.remove(null);
			listKO.add(null);
			listVO.remove(null);
			listVO.add(null);
		    }
		}
		// iterate through slots and place list values back into arrays
		for (int j=0; j < SLOTS; j++) {
		    b.next.key[j] = listKO.get(j);
		    b.next.value[j] = listVO.get(j);
	    	} 
		// remove overflow bucket if empty
		if (b.next.nKeys == 0)
		    b.next = null;
	
	    }//if
	    
	    // iterate through slots and place list values back into arrays
	    for (int j=0; j < SLOTS; j++) {
		b.key[j] = listK.get(j);
	    	b.value[j] = listV.get(j);
	    }
	    
	    hTable.add(newb);
	    split++;
	    // check if split index needs to be reset and mods incremented
	    if (split == mod1) {
		split = 0;
		mod1 = mod2;
		mod2 = 2 * mod1;
	    }
	    
        }//else
	
        return null;
    } // put
    
    /***************************************************************************
     * Return the size (SLOTS * number of home buckets) of the hash table. 
     * @return  the size of the hash table
     */
    public int size ()
    {
        return SLOTS * (mod1 + split);
    } // size
    
    /***************************************************************************
     * Print the hash table.
     * @author Kim Bradley
     */
    private void print ()
    {
        out.println ("Hash Table (Linear Hashing)");
        out.println ("-------------------------------------------");
	
    	for (int i=0; i < hTable.size(); i++) {
	    out.print(i + ": \t");
	    for (int j=0; j < SLOTS; j++) {
		if (hTable.get(i).key[j] == null)
		    out.print("[ null ] ");
		else
		    out.print("[ " + hTable.get(i).key[j] + " ] ");
	    }
	    // if current bucket has overflow
	    if (hTable.get(i).next != null) {
		out.print("--> ");
		for (int j=0; j < SLOTS; j++) {
		    if (hTable.get(i).next.key[j] == null)
			out.print("[ null ] ");
		    else
			out.print("[ " + hTable.get(i).next.key[j] + " ] ");
    		}
	    }
	    out.println();
	}
	
	out.println ("-------------------------------------------");
    } // print
    
    /***************************************************************************
     * Hash the key using the low resolution hash function.
     * @param key  the key to hash
     * @return  the location of the bucket chain containing the key-value pair
     */
    private int h (Object key)
    {
	return key.hashCode () % mod1;
    } // h
    
    /***************************************************************************
     * Hash the key using the high resolution hash function.
     * @param key  the key to hash
     * @return  the location of the bucket chain containing the key-value pair
     */
    private int h2 (Object key)
    {
	return key.hashCode () % mod2;
    } // h2
    
    /***************************************************************************
     * The main method used for testing.
     * @param  the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main (String [] args)
    {
        LinHash <Integer, Integer> ht = new LinHash <> (Integer.class, Integer.class, 2);
        int nKeys = 20;
        if (args.length == 1) nKeys = Integer.valueOf (args [0]);
        for (int i = 1; i < nKeys; i++) ht.put (i, i+1);
        ht.print ();
        for (int i = 0; i < nKeys; i++) {
            out.println ("key = " + i + " value = " + ht.get (i));
        } // for
        out.println ("-------------------------------------------");
        out.println ("Average number of buckets accessed = " + ht.count / (double) nKeys);
    } // main
    
} // LinHash class

