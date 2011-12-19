public class ShortArray {

    // Testing array of another primitive type - must be memset

    public static void main (String[] args) {

	short[] f = new short[2];
	
	f[0] = (short)32768;
	System.out.println("f[0]=" + f[0]);
	
	//	System.out.println("class = " + f.getClass().toString());
	//	System.out.println("super = " + f.getClass().getSuperclass().toString());
	//	System.out.println("length =  " + f.length);


	/**
	   class = class [S
	   super = class java.lang.Object
	   length =  2
	*/


	
    } // end main


} // end class
