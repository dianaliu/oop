public class FloatArray {

    // Testing array of another primitive type - must be memset

    public static void main (String[] args) {


	float[] f = new float[2];
	
	for(int i = 0; i < f.length; i++) {
		f[i] = i;
	}

	System.out.println("array f has " + f.length + " element(s)");


	
    } // end main


} // end class
