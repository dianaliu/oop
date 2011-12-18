public class Arrays1 {

    // Testing various types of arrays
    // multidimensional array of arrays, etc.

    public static void main (String[] args) {

	int x = 2;
	int y = 3;

	int[] ia = new int[x];
	
	for(int i = 0; i < x; i++) {
		ia[i] = i;
		System.out.println("ia[i] = " + ia[i]);
	}

	
    } // end main


} // end class
