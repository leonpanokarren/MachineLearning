package FPTree;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class FPTree {

  // state
//  static String rootNode = "root";
  
	// Read in the transaction database file as argument-1
	// If available, read in the 1-item set file as argument-2 
	
	
	//  Build an FP Tree object by processing each transaction and each item 
	//  For every item, in each transaction, build the path id as follows - 
	//    For the first item,  from-node = root,             to-node = argument-item
	//    For all other items, from-node = previous path-id, to-node = argument-item
	//  Create unique path-id = MD5 hash of (path id + argument-item)
	//  Check if the unique path-id exists in the FP Tree
	//  If it does    , increment the count associated with it
	//  If it does not, create a new "path" object in the FPTree object with a 
	//    1) path-id, 
	//    2) from-path, 
	//    3) associated-node or "item"
	//    4) path-support
	//    5) emerging path array of "path-id"s
	
	// starting with the last item in the 1-item set header, 
	// for each 1-itemset, traverse the array of branches
	// for each array element, navigate the branches toward the root, until 
	// the parentBranch is null. Mark the support of each node down as that of the branch 
	// in the branch array 
	// then apply minsup and eliminate the nodes that dont meet minsup
	// traverse each node again, and generate a list of nodes excluding those 
  @SuppressWarnings({ "unchecked", "rawtypes" })
public static void main(String[] args){
	  
	int minsup = 30;
	  
    System.out.println(System.nanoTime());
//    System.out.println(FPBranch.getFPBranch("root", "y"));
//    System.out.println(FPBranch.getFPBranch("root", "x"));
    if (args.length < 2){
      throw new IllegalArgumentException("Incorrect number of arguments\n"
        + "Usage : java " + FPTree.class.getSimpleName() + " <transaction database file> <1-item set header>");
    }
    
    Map itemHeader = new LinkedHashMap<String, List<Object>>();
    
    BufferedReader itemHeaderFile = null;
    try {
      itemHeaderFile = new BufferedReader(new FileReader(args[1]));
      String line;
      String[] lineArray;
      while ((line = itemHeaderFile.readLine()) != null){
      	lineArray = line.split("\\|");
      	// Create the item header entry by adding the 1-item set, and its support
      	ArrayList<Object> itemDetail = new ArrayList<Object>();
      	itemDetail.add(lineArray[1]);
        itemHeader.put(lineArray[0], itemDetail);
      }
	} catch (FileNotFoundException e) {
	  e.printStackTrace();
    } catch (IOException e) {
	  e.printStackTrace();
	} finally {
	  if (itemHeaderFile != null){
	    try {
	      itemHeaderFile.close();
		} catch (IOException e) {
		  e.printStackTrace();
		}
	  }
	}
    
    
    // create the FP Tree and then add branches for every row read from the transaction file
    //FPTree fptree = new FPTree();
    BufferedReader transactionFile = null;
    try {
      int rec = 0;
	  transactionFile = new BufferedReader(new FileReader(args[0]));
      String line;
      String[] lineArray;
      while ((line = transactionFile.readLine()) != null){
    	lineArray = line.split("\\|");
        String[] branchArray = lineArray[2].split(",");
        FPBranch prevBranch = null;
        for (String s : branchArray){
          // build the tree
          prevBranch = prevBranch == null ? FPBranch.getRootFPBranch(s) : FPBranch.getNonRootFPBranch(prevBranch, s);
          // add the branch to the item header list
          if (itemHeader.containsKey(s)){
        	int i = 0;
        	boolean branchFound = false;
            for (Iterator item = ((ArrayList) itemHeader.get(s)).iterator(); item.hasNext(); i++){
              if (i>0 && prevBranch == item.next()){
            	branchFound = true;
                break;
              }
            }
            if(!branchFound){
              ((ArrayList) itemHeader.get(s)).add(prevBranch);
            }
          } else {
            // 1-item set not found in header and hence added to the item header 
            itemHeader.put(s, (new ArrayList()).add("Unknown"));
            ((ArrayList) itemHeader.get(s)).add(prevBranch);
          }
        }
        rec++;
      }
      
      // Initialize output file for capturing itemsets
      PrintWriter outputFile = new PrintWriter("C:\\Users\\lpanokarren\\Documents\\Work\\Java"
      		+ "\\miscellaneous\\bin\\repair_itemset.txt", "UTF-8");
      // Create Final HashMap for itemsets
      Map<String, Long> finalItemSet = new HashMap();
      String itemSets = null;
      
      // mine the FP Tree for item set frequency 
      Iterator itemsetIterator = itemHeader.entrySet().iterator();
      while (itemsetIterator.hasNext()){
        Entry itemSet = (Entry) itemsetIterator.next();
        String node = (String) itemSet.getKey();
        int i = 0;
        
        // Build the conditional database support for all itemsets
        Map<String, Long> cdb = new HashMap();

        // First-scan 
        // navigate the FP tree and build a Hash Map of String, Long to store itemset, and conditional database support
        for (Object branch : (List<Object>) itemSet.getValue()){
          if(i > 0){
      	    FPBranch treeBranch = ((FPBranch)branch); 
      	    boolean reachedRoot = treeBranch.isRoot();
      	    long twigSupport = treeBranch.getSupport();
      	    int j = 0;
            do{
          	  // skip the first non-root branch
      	      if (j != 0 || reachedRoot){
                String toNode = (j == 0 && reachedRoot) ? "çROOTç" : treeBranch.getBranchNode();
                cdb.put( toNode, twigSupport + (cdb.get(toNode) == null ? 0 : cdb.get(toNode)) ); // replaces cdb support for existing nodes
      	      }
      	      reachedRoot = treeBranch.isRoot();
              treeBranch = treeBranch.getPrevBranch();  
              j++;
            } while (!reachedRoot);
          }
          i++;
        }
        
        // Second-scan 
        // navigate the FP tree and add the nodes meeting the minsup threshold to a String array 
        // so the combination-generator can generate itemsets with the branch support
        i = 0;
        for (Object branch : (List<Object>) itemSet.getValue()){
          if(i > 0){
            List<String> candidateItemSet = new ArrayList();            
            FPBranch treeBranch = ((FPBranch)branch); 
            long twigSupport = treeBranch.getSupport();
            boolean reachedRoot = treeBranch.isRoot();
            int j = 0;
            do{
              // skip the first non-root branch
              if (j != 0 || reachedRoot){
                  String toNode = (j == 0 && reachedRoot) ? "çROOTç" : treeBranch.getBranchNode();
                  if(cdb.get(toNode) >= minsup){
                    candidateItemSet.add(toNode);
                  }
              }
        	  reachedRoot = treeBranch.isRoot();
              treeBranch = treeBranch.getPrevBranch();  
              j++;
            } while (!reachedRoot);
            // generate combinations of itemsets from the candidate itemset with support = treeBranch.getSupport()
            // and append 'node'
            
            Set<Set<String>> frequentItemSet = Sets.powerSet(new HashSet<String>(candidateItemSet));
            itemSets = null;
            for(Set<String> subSet : frequentItemSet){
              if (!subSet.toString().equals("[]")){
          		itemSets = node;            	  
            	if (subSet.toString().equals("[çROOTç]")){
            		//outputFile.println(node + "|" + support);
            	} else {
            	  //outputFile.print(node + "|");
            	  for (String item : subSet){
            		itemSets = itemSets + "," + item;
            	    //outputFile.print(item+ "|") ;
            	  }
            	  //outputFile.println(support);
            	}
              }
              if (finalItemSet.containsKey(itemSets)){
                finalItemSet.put(itemSets, finalItemSet.get(itemSets) + twigSupport);
              } else {
            	finalItemSet.put(itemSets, twigSupport);
              }
            }
            
          }
          i++;
        }       
        
        
      }

      for (String printItemSet : finalItemSet.keySet()){
        outputFile.println(printItemSet + "|" + finalItemSet.get(printItemSet));
      }
      outputFile.close();
      
      
      System.out.println("Processed " + rec + " rows");
	} catch (FileNotFoundException e) {
	  e.printStackTrace();
	} catch (IOException e){
	  e.printStackTrace();
	} finally {
	  if (transactionFile != null){
	    try {
		  transactionFile.close();
		} catch (IOException e) {
		  e.printStackTrace();
		}
	  }
	}
    System.out.println(System.nanoTime());

  }

  
}
