// Testing two-dimensional arrays

public class Arrays2 {

    public static void main (String[] args) {

	int x = 2;
	int y = 3;

	int[][]ii = new int[x][y];
	
	for(int i = 0; i < x; i++) {
	    for(int j = 0; j < y; j++) {
		ii[i][j] = i + j;
		System.out.println("ii[" + i + "," + j + 
				   "] = " + ii[i][j]);
	    }
	}
	
    } // end main


} // end class
