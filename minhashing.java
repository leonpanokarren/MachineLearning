package miscellaneous;

import java.math.BigDecimal;

public class minhashing {
	
  public static void main(String[] args){
	printMatrix();
	getJaccardSimilarity(charMatrix);
    //permute(2);
	byte[][] minHashArray = null;
	try {
	  minHashArray = getMinHash();
	} catch (Exception e){
	  e.printStackTrace();
	}
	getMinHashSimilarity(minHashArray);
  }
	
  static byte[][] charMatrix = new byte[][]{
	  { 1, 0, 0, 1},
	  { 0, 0, 1, 0},
	  { 0, 1, 0, 1},
	  { 1, 0, 1, 1},
	  { 0, 0, 1, 0}
  }; 
  
  static void printMatrix(){
	String sep = "   ";
	System.out.println("Characteristic Matrix M is\n----------------------------------");
    for (byte x  = 0; x < charMatrix.length; x++){
      if (x == 0){
        for (byte y = 0; y < charMatrix[x].length; y++){
          System.out.print("S" + String.valueOf(y+1) + sep);
        }
        System.out.println();
      }
      for (byte y = 0; y < charMatrix[x].length; y++){
        System.out.print(" " + String.valueOf(charMatrix[x][y]) + sep);
      }
      System.out.println();
    }
    System.out.println();
  }

  static byte[][] getMinHash() throws Exception{
	byte[][] minHash = new byte[(int)factorial(charMatrix.length)][charMatrix[0].length];
	try {
      for (long permutation = 1; permutation <= factorial(charMatrix.length); permutation++){
        byte[] order = permute(permutation);
        for (byte col = 0; col < charMatrix[0].length; col++){
          for (byte x = 0; x < charMatrix.length; x++){
            if (charMatrix[order[x]-1][col] == 1){
              minHash[(int)(permutation - 1)][col] = (byte) (x+1);
              break;
            }
          }
        }
      }
	} catch (Exception e){
	  e.printStackTrace();
	}
	return minHash;
  }
  
  static void getJaccardSimilarity(byte[][] argCharMatrix){
	System.out.println("Jaccard Similarity of ");
    for (byte setIndex = 0; setIndex < argCharMatrix[0].length - 1; setIndex++){
      for (byte remIndex = (byte) (setIndex + 1); remIndex < argCharMatrix[0].length; remIndex++){
        float setUnion = 0;
        float setIntersection = 0;
        for (int row = 0; row < argCharMatrix.length; row++){
          switch(argCharMatrix[row][setIndex] + argCharMatrix[row][remIndex]){
            case 0: setUnion += 0; setIntersection += 0; break;
            case 1: setUnion += 1; setIntersection += 0; break;
            case 2: setUnion += 1; setIntersection += 1; break;
            default: break;
          }
        }
        System.out.println("Sets " + String.valueOf(setIndex) + 
          " and " + String.valueOf(remIndex) + " is : " + String.valueOf(round(setIntersection/setUnion,3)) 
        );
      }
    }
  }

  static void getMinHashSimilarity(byte[][] argCharMatrix){
	System.out.println("Min Hash Similarity of ");
    for (byte setIndex = 0; setIndex < argCharMatrix[0].length - 1; setIndex++){
      for (byte remIndex = (byte) (setIndex + 1); remIndex < argCharMatrix[0].length; remIndex++){
        float setTotal = 0;
        float setIntersection = 0;
        for (int row = 0; row < argCharMatrix.length; row++){
          setTotal++;
          setIntersection += argCharMatrix[row][setIndex] == argCharMatrix[row][remIndex] ? 1 : 0;  
        }
        System.out.println("Sets " + String.valueOf(setIndex) + " and " + 
          String.valueOf(remIndex) + " is : " + String.valueOf(round(setIntersection/setTotal,3)) 
        );
      }
    }
  }

  
  static void permutations(){
    byte[] arr = new byte[] {1, 2, 3, 4, 5};
    try {
      System.out.println(arr.length + " factorial is " + factorial(arr.length));
    } catch (Exception e){
      e.printStackTrace();
    }
    try {
      byte[] arrCopy = new byte[arr.length];
      for (long permut = 1; permut <= factorial(arr.length); permut++){
        System.arraycopy(arr, 0, arrCopy, 0, arr.length);
        Byte arrCopyLen = (byte) arrCopy.length;
        for (byte x = 0; x < arr.length; x++){
          byte i = 0;
          int index;
          
          // resolve the index for this (permutation, position)
          index = (int) Math.ceil(((float) permut)/factorial(arr.length - (x+1)));
          if (x == 0){
            i = (byte) (index - 1);
          } else {
            i = (byte) ((index - 1) % arrCopyLen);
          }
          
          System.out.print(arrCopy[i] + ", ");
          // reset the element at the resolved to make it unavailable 
          arrCopy[i] = 0;
          
          
          // Move all the non-zeros to the front and calculate the non-zero array length
          // Default to 'No shifting needed'
          char shift = 'N';
          char zeroDetected = 'N';
          // Start shifting
          for(byte y = 0; y < arrCopy.length; y++){
            if (arrCopy[y] == 0){
              shift = 'Y';
            }
            if (shift == 'Y' && y < arrCopy.length - 1){
              Byte temp = arrCopy[y]; 
              arrCopy[y] = arrCopy[y+1];
              arrCopy[y+1] = temp;
            }
            if (zeroDetected == 'N' && arrCopy[y] == 0){
              zeroDetected = 'Y';
              arrCopyLen = (byte) (y);
            }
          }          
        }
        System.out.println();
      }
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  static byte[] permute(long order){
	byte[] arr = new byte[] {1, 2, 3, 4, 5};
	byte[] permutedOrder = new byte[5];
	try {
	  byte[] arrCopy = new byte[arr.length];
	  System.arraycopy(arr, 0, arrCopy, 0, arr.length);
	  Byte arrCopyLen = (byte) arrCopy.length;
	  for (byte x = 0; x < arr.length; x++){
	    byte i = 0;
	    int index;
	          
	    // resolve the index for this (permutation, position)
	    index = (int) Math.ceil(((float) order)/factorial(arr.length - (x+1)));
	    if (x == 0){
	      i = (byte) (index - 1);
	    } else {
	      i = (byte) ((index - 1) % arrCopyLen);
	    }
	    permutedOrder[x] = arrCopy[i];
	    //System.out.print(arrCopy[i] + ", ");
	    // reset the element at the resolved to make it unavailable 
	    arrCopy[i] = 0;
	          
	    // Move all the non-zeros to the front and calculate the non-zero array length
	    // Default to 'No shifting needed'
	    char shift = 'N';
	    char zeroDetected = 'N';
	    // Start shifting
	    for(byte y = 0; y < arrCopy.length; y++){
	      if (arrCopy[y] == 0){
	        shift = 'Y';
	      }
	      if (shift == 'Y' && y < arrCopy.length - 1){
	        Byte temp = arrCopy[y]; 
	        arrCopy[y] = arrCopy[y+1];
	        arrCopy[y+1] = temp;
	      }
	      if (zeroDetected == 'N' && arrCopy[y] == 0){
	        zeroDetected = 'Y';
	        arrCopyLen = (byte) (y);
	      }
	    }          
	  }
	  //System.out.println();
	} catch (Exception e){
	  e.printStackTrace();
    }
	return permutedOrder;
  }
  
  static float round(float d, int decimalPlace) {
    BigDecimal bd = new BigDecimal(Float.toString(d));
    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
    return bd.floatValue();
  }
  
  static long factorial(int x) throws Exception{
    if (x > 15){
      throw new Exception (x + " is too large for computation");
    } else if (x==0){
      return 1 ;
    } else {
      long factorial = x;
      for (long f = x-1; f > 1; f--){
        factorial *= f;
      }
      return factorial;
    }
    	
  }
}
