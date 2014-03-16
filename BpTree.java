
/*******************************************************************************
 * @file BpTree.java
 *
 * @author  John Miller
 */

import java.io.*;
import java.lang.reflect.Array;
import static java.lang.System.out;
import java.util.*;

/*******************************************************************************
 * This class provides B+Tree maps.  B+Trees are used as multi-level index structures
 * that provide efficient access for both point queries and range queries.
 */
public class BpTree <K extends Comparable <K>, V>
       extends AbstractMap <K, V>
       implements Serializable, Cloneable, SortedMap <K, V>
{
    /** The maximum fanout for a B+Tree node.
     */
    private static final int ORDER = 5;

    /** The class for type K.
     */
    private final Class <K> classK;

    /** The class for type V.
     */
    private final Class <V> classV;

    /***************************************************************************
     * This inner class defines nodes that are stored in the B+tree map.
     */
    private class Node
    {
        boolean   isLeaf;
        int       nKeys; // max = ORDER - 1
        K []      key;
        Object [] ref;
        Node parent;
        Node nextLeaf;
        
        Node (boolean _isLeaf)
        {
            isLeaf = _isLeaf;
            nKeys  = 0;
            key    = (K []) Array.newInstance (classK, ORDER - 1);
            
            if (isLeaf) {
                //ref = (V []) Array.newInstance (classV, ORDER);
                ref = new Object [ORDER];
            } else {
                ref = (Node []) Array.newInstance (Node.class, ORDER);
            } // if
        } // constructor
        
        
        @SuppressWarnings("unchecked")
        Node (boolean _isLeaf, Node par)
        {
            isLeaf = _isLeaf;
            nKeys  = 0;
            key    = (K []) Array.newInstance (classK, ORDER - 1);
            parent = par;
            if (isLeaf) {
                //ref = (V []) Array.newInstance (classV, ORDER);
                ref = new Object [ORDER];
                for (int j=0; j<nKeys; j++){
                	for (int i=0; i<parent.key.length; i++){
                		if (parent.key[i].compareTo(key[j]) == 0){
                			 nextLeaf = (Node) parent.ref[i];
                			 break;
                		}
                	}
                }
            } else {
                ref = (Node []) Array.newInstance (Node.class, ORDER);
            } // if
        } // constructor
    } // Node inner class

    /** The root of the B+Tree
     */
    private Node root;

    /** The counter for the number nodes accessed (for performance testing).
     */
    private int count = 0;

    /***************************************************************************
     * Construct an empty B+Tree map.
     * @param _classK  the class for keys (K)
     * @param _classV  the class for values (V)
     */
    public BpTree (Class <K> _classK, Class <V> _classV)
    {
        classK = _classK;
        classV = _classV;
        root   = new Node (true);
    } // BpTree

    /***************************************************************************
     * Return null to use the natural order based on the key type.  This requires
     * the key type to implement Comparable.
     */
    public Comparator <? super K> comparator () 
    {
        return null;
    } // comparator

    /***************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     */
    public Set <Map.Entry <K, V>> entrySet ()
    {
        Set <Map.Entry <K, V>> enSet = new HashSet <> ();

             //-----------------\\
            // TO BE IMPLEMENTED \\
           //---------------------\\
            
        return enSet;
    } // entrySet

    /***************************************************************************
     * Given the key, look up the value in the B+Tree map.
     * @param key  the key used for look up
     * @return  the value associated with the key
     */
    @SuppressWarnings("unchecked")
    public V get (Object key)
    {
        return find ((K) key, root);
    } // get

    /***************************************************************************
     * Put the key-value pair in the B+Tree map.
     * @param key    the key to insert
     * @param value  the value to insert
     * @return  null (not the previous value)
     */
    public V put (K key, V value)
    {
        insert (key, value, root, null);
        return null;
    } // put

    /***************************************************************************
     * Return the first (smallest) key in the B+Tree map.
     * @return  the first key in the B+Tree map.
     */
    public K firstKey () 
    {
             //-----------------\\
            // TO BE IMPLEMENTED \\
           //---------------------\\

        return null;
    } // firstKey

    /***************************************************************************
     * Return the last (largest) key in the B+Tree map.
     * @return  the last key in the B+Tree map.
     */
    public K lastKey () 
    {
             //-----------------\\
            // TO BE IMPLEMENTED \\
           //---------------------\\

        return null;
    } // lastKey

    /***************************************************************************
     * Return the portion of the B+Tree map where key < toKey.
     * @return  the submap with keys in the range [firstKey, toKey)
     */
    public SortedMap <K,V> headMap (K toKey)
    {
             //-----------------\\
            // TO BE IMPLEMENTED \\
           //---------------------\\

        return null;
    } // headMap

    /***************************************************************************
     * Return the portion of the B+Tree map where fromKey <= key.
     * @return  the submap with keys in the range [fromKey, lastKey]
     */
    public SortedMap <K,V> tailMap (K fromKey)
    {
             //-----------------\\
            // TO BE IMPLEMENTED \\
           //---------------------\\

        return null;
    } // tailMap

    /***************************************************************************
     * Return the portion of the B+Tree map whose keys are between fromKey and toKey,
     * i.e., fromKey <= key < toKey.
     * @return  the submap with keys in the range [fromKey, toKey)
     */
    public SortedMap <K,V> subMap (K fromKey, K toKey)
    {
             //-----------------\\
            // TO BE IMPLEMENTED \\
           //---------------------\\

        return null;
    } // subMap

    /***************************************************************************
     * Return the size (number of keys) in the B+Tree.
     * @return  the size of the B+Tree
     */
    public int size ()
    {
        int sum = 0;

             //-----------------\\
            // TO BE IMPLEMENTED \\
           //---------------------\\

        return  sum;
    } // size

    /***************************************************************************
     * Print the B+Tree using a pre-order traveral and indenting each level.
     * @param n      the current node to print
     * @param level  the current level of the B+Tree
     */
    @SuppressWarnings("unchecked")
    private void print (Node n, int level)
    {
        out.println ("BpTree");
        out.println ("-------------------------------------------");

        for (int j = 0; j < level; j++) out.print ("\t");
        out.print ("[ . ");
        for (int i = 0; i < n.nKeys; i++) out.print (n.key [i] + " . ");
        out.println ("]");
        if ( ! n.isLeaf) {
            for (int i = 0; i <= n.nKeys; i++) print ((Node) n.ref [i], level + 1);
        } // if

        out.println ("-------------------------------------------");
    } // print

    /***************************************************************************
     * Recursive helper function for finding a key in B+trees.
     * @param key  the key to find
     * @param ney  the current node
     */
    @SuppressWarnings("unchecked")
    private V find (K key, Node n)
    {
        count++;
        for (int i = 0; i < n.nKeys; i++) {
            K k_i = n.key [i];
            if (key.compareTo (k_i) <= 0) {
                if (n.isLeaf) {
                    return (key.equals (k_i)) ? (V) n.ref [i] : null;
                } else {
                    return find (key, (Node) n.ref [i]);
                } // if
            } // if
        } // for
        return (n.isLeaf) ? null : find (key, (Node) n.ref [n.nKeys]);
    } // find

    /***************************************************************************
     * Recursive helper function for inserting a key in B+trees.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @param p    the parent node
     */
    private void insert (K key, V ref, Node n, Node p)
    {
    	if(n.isLeaf){//This node is a leaf node
    	    if (n.nKeys < ORDER - 1) {//Node is not full
    	        for (int i = 0; i < n.nKeys; i++) {
    	        	K k_i = n.key [i];
    	        	if (key.compareTo (k_i) < 0) {
    	        		wedge (key, ref, n, i);
    	        	} else if (key.equals (k_i)) {
    	        		out.println ("BpTree:insert: attempt to insert duplicate key = " + key);
    	        	} // if

    	        } // for
    	        wedge (key, ref, n, n.nKeys);
    	        return;
    	    } else {//Node is full
    	        split (key, ref, n);
    	        return;
    	    } // else  
    	}   
    	else{//This node is an internal node
    	        for (int i = 0; i < n.nKeys; i++) {
    	            K k_i = n.key [i];
    	            if (key.compareTo (k_i) <= 0) {
    	                insert (key, ref, (Node) n.ref[i], n);
    	                return;
    	            }
    	        } // for
    	        insert (key, ref, (Node) n.ref[n.nKeys], n);
    	        return;
    	}
    	
//        if (n.nKeys < ORDER - 1) {
//            for (int i = 0; i < n.nKeys; i++) {
//                K k_i = n.key [i];
//                if (key.compareTo (k_i) < 0) {
//                    wedge (key, ref, n, i);
//                } else if (key.equals (k_i)) {
//                    out.println ("BpTree:insert: attempt to insert duplicate key = " + key);
//                } // if
//            } // for
//            wedge (key, ref, n, n.nKeys);
//        } else {
//            Node sib = split (key, ref, n);
//
//             //-----------------\\
//            // TO BE IMPLEMENTED \\
//           //---------------------\\
//
//        } // if
    } // insert

    /***************************************************************************
     * Wedge the key-ref pair into node n.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @param i    the insertion position within node n
     */
    private void wedge (K key, V ref, Node n, int i)
    {
        for (int j = n.nKeys; j > i; j--) {
            n.key [j] = n.key [j - 1];
            n.ref [j] = n.ref [j - 1];
        } // for
        n.key [i] = key;
        n.ref [i] = ref;
        n.nKeys++;
        
    } // wedge

    /***************************************************************************
     * Split node n and return the newly created node.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     */
    private Node split (K key, V ref, Node n)
    {
        out.println ("split not implemented yet");
        
        if(n == root){
        	K[] _key = (K []) Array.newInstance (classK, ORDER);
            V[] _ref = (V []) Array.newInstance (classV, ORDER+1);
             int y=0;
            for(int x=0;x<n.key.length+1;++x){
               	/**
               	 * Adds all Keys and their respective refs to array
               	 * Maintains order as to determine which value will be promoted to 
               	 * the new root node
               	 */
               	if(n.key[y-1].compareTo(key)<0 && n.key[y].compareTo(key)>0){
               		_key[x] = key;
               		_ref[x] = ref;
               	}
               	else{
               		_key[x] = n.key[y];
               		_ref[x] = (V) n.ref[y];
               		if(y==4){
               			_ref[x+1] = (V) n.ref[y+1];
               			++x;
               		}
               		++y;
               		
               	}
               	
            }
            
            Node new_root = new Node(false,null);
            Node new_lc = new Node(false, new_root);
            new_root.key[0] = _key[2];
            new_lc.key[0] = _key[0];
            new_lc.key[1] = _key[1];
            new_lc.ref[0] = _ref[0];
            new_lc.ref[1] = _ref[1];
            new_lc.ref[2] = _ref[2];
            new_root.ref[0] = new_lc;
            Node new_rc = new Node(false,null);
            new_rc.key[0] = _key[3];
            new_rc.key[1] = _key[4];
            new_rc.ref[0] = _ref[3];
            new_rc.ref[1] = _ref[4];
            new_rc.ref[2] = _ref[5];
            new_root.ref[1] = new_rc;
        }
        if(n.isLeaf){
            Node sibling = new Node(true, n.parent);
            for (int i = 0; i<n.parent.nKeys; i++){
            	if(n.key[n.nKeys-1].compareTo(n.parent.key[i]) < 0){
            		n.nextLeaf = (Node) n.parent.ref[i];
            	}
            }
            Node aux = n.nextLeaf;
            n.nextLeaf = sibling;
            sibling.nextLeaf = aux;
            Node lastLeaf = (Node) n.parent.ref[n.parent.nKeys-1];
            if(n == lastLeaf){
                lastLeaf = sibling;
            }
            K[] _key = (K []) Array.newInstance (classK, ORDER);
            V[] _ref = (V []) Array.newInstance (classV, ORDER);
            
        
             //-----------------\\
            // TO BE IMPLEMENTED \\
           //---------------------\\
        }
        else{//Splitting an internal node
            Node sibling = new Node(false,n.parent);
            
            K[] _key = (K []) Array.newInstance (classK, ORDER);
            Node[] _ref = (Node[]) Array.newInstance (Node.class, ORDER+1);
            
             //-----------------\\
            // TO BE IMPLEMENTED \\
           //---------------------\\

            
            //out.println("nKeys: " + n.nKeys + "\tcount: " + count);
            _ref[count] = (Node) n.ref[n.nKeys];//Copy the 'else' pointer
            //Restore the Node to empty, ready to be re-filled
            for(int i = 0; i < ORDER-1; i++){
                n.key[i] = null;
                n.ref[i] = null;
            }
            //size -= n.nKeys;
            n.nKeys = 0;
            for (int i = ORDER-1; i >= 0; i--){//Copy keys and references to the proper objects
                if(i > ORDER/2){
                    if(i == ORDER-1){
                        //out.println("Sibling - Rightmost Ref");
                        sibling.ref[0] = _ref[ORDER];
                        _ref[ORDER].parent = sibling;
                    }
                    //out.println("Sibling - Key/Ref Pair");
                    wedge(_key[i],(V) _ref[i],sibling,0);
                    _ref[i].parent = sibling;
                }
                else{
                    if(i == ORDER/2){
                        //out.println("N Node - Rightmost Ref");
                        n.ref[0] = _ref[i];
                    }
                    else{
                        //out.println("N Node - Key/Ref Pair");
                        wedge(_key[i], (V) _ref[i],n,0);
                    }
                }
                
            }
            int loc=0;
            for(int i = 0; i < ORDER; i++){//Move the pointer to the sibling
                if(n.parent.ref[i] == n){
                    n.parent.ref[i] = sibling;
                    loc = i;
                }
            }
            if(n.parent.nKeys == ORDER-1){//If parent is full
                //out.println("Throwing " + _key[ORDER/2]);
                return split(_key[ORDER/2], (V) n,n.parent);//Split the parent
            }
            else{//Parent not full
                //out.println("Throwing " + _key[ORDER/2]);
            	wedge(_key[ORDER/2], (V) n,n.parent,loc);//Insert to the parent 
                return n.parent;
            }
        }



        return null;
    } // split

    /***************************************************************************
     * The main method used for testing.
     * @param  the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main (String [] args)
    {
        BpTree <Integer, Integer> bpt = new BpTree <> (Integer.class, Integer.class);
        int totKeys = 10;
        if (args.length == 1) totKeys = Integer.valueOf (args [0]);
        for (int i = 1; i < totKeys; i += 2) bpt.put (i, i * i);
        bpt.print (bpt.root, 0);
        for (int i = 0; i < totKeys; i++) {
            out.println ("key = " + i + " value = " + bpt.get (i));
        } // for
        out.println ("-------------------------------------------");
        out.println ("Average number of nodes accessed = " + bpt.count / (double) totKeys);
    } // main

} // BpTree class

