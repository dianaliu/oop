
public class Arrays {

    // Testing various types of arrays
    // multidimensional array of arrays, etc.

    public static void main (String[] args) {

	int x = 2;
	int y = 3;

	int[] ia = new int[x];
	
	for(int i = 0; i < x; i++) {
		ia[i] = i;
	}

	/**
	int[][]ii = new int[x][y];
	
	for(int i = 0; i < x; i++) {
	    for(int j = 0; j < y; j++) {
		ii[i][j] = i + j;
		System.out.println("ii[" + i + "," + j + 
				   "] = " + ii[i][j]);
	    }
	}
	**/ 
	
    } // end main


} // end class
