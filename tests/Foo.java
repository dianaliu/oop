public class Foo {

    public static void main(String args[]) {
	int test = 0;
	int success = 0;



	 // THESE TESTS WORK



	// ---------------------------------------------------------------------

	short[] a0 = new short[0];

	if (a0.length == 0) {
	    System.out.println("PASS short[0].length");
	    success++;
	} else {
	    System.out.println("FAIL short[0].length");
	}
	test++;


	// ---------------------------------------------------------------------

	short[] a1 = new short[1];

	if (a1.length == 1) {
	    System.out.println("PASS short[1].length");
	    success++;
	} else {
	    System.out.println("FAIL short[1].length");
	}
	test++;

	
	// ---------------------------------------------------------------------

	short a2[] = new short[2];

	if (a2.length == 2) {
	    System.out.println("PASS short[2].length");
	    success++;
	} else {
	    System.out.println("FAIL short[2].length");
	}
	test++;
	

	// ---------------------------------------------------------------------

	if (a1[0] == 0 && a2[0] == 0 && a2[1] == 0) {
	    System.out.println("PASS short[i] == 0");
	    success++;
	} else {
	    System.out.println("FAIL short[i] == 0");
	}
	test++;

	
	// -----------------------------------------------------------------------
	


	a1[0] = (short)32768;
	// WTF: This passes!
	// 	    System.out.println("a1[0] = " + a1[0]);
	if (a1[0] == -32768) {
	    System.out.println("PASS short[0] = (short)32768");
	    success++;
	} else {
	    System.out.println("FAIL short[0] = (short)32768");

	}
	test++;

	// -----------------------------------------------------------------------

	//       	**/
	

	    /**

	// I AM WORKING ON THIS SHIT


	if (a0.getClass().getName().equals("[S")) {
	    System.out.println("PASS short[0].getClass().getName()");
	    success++;
	} else {
	    System.out.println("FAIL short[0].getClass().getName()");
	}
	test++;


	/**

	 // ---------------------------------------------------------------------

	 String s1 = "Hello Kitty #1";
	 String s2 = "Hello Kitty #1";
	 if (s1.equals(s2)) {
	 System.out.println("PASS s1.equals(String)");

	 } else {
	 System.out.println("FAIL s1.equals(String)");
	 }

	 // ---------------------------------------------------------------------

	 s2 = "Hel" + "lo Kitty #1";
	 if (s1.equals(s2)) {
	 System.out.println("PASS s1.equals(String + String)");

	 } else {
	 System.out.println("FAIL s1.equals(String + String)");
	 }


	 // ---------------------------------------------------------------------

	 s2 = "He" + "ll" + "o Kitty #1";
	 if (s1.equals(s2)) {
	 System.out.println("PASS s1.equals(String + String + String)");

	 } else {
	 System.out.println("FAIL s1.equals(String + String + String)");
	 }


	 // ---------------------------------------------------------------------

	 s2 = "Hello Kitty #" + 1;
	 if (s1.equals(s2)) {
	 System.out.println("PASS s1.equals(String + int)");

	 } else {
	 System.out.println("FAIL s1.equals(String + int)");
	 }


	 // ---------------------------------------------------------------------

	 s2 = "Hello Kitty #" + '1';
	 if (s1.equals(s2)) {
	 System.out.println("PASS s1.equals(String + char)");

	 } else {
	 System.out.println("FAIL s1.equals(String + char)");
	 }


	 // ---------------------------------------------------------------------

	 s2 = (char)72 + "ello Kitty #1";
	 if (s1.equals(s2)) {
	 System.out.println("PASS s1.equals(char + String)");

	 } else {
	 System.out.println("FAIL s1.equals(char + String)");
	 }


	 // ---------------------------------------------------------------------

	 char c = 72;
	 s2 = c + "ello Kitty #1";
	 if (s1.equals(s2)) {
	 System.out.println("PASS s1.equals(char + String)");

	 } else {
	 System.out.println("FAIL s1.equals(char + String)");
	 }


	 // ---------------------------------------------------------------------

	 s2 = 'H' + "ello Kitty #1";
	 if (s1.equals(s2)) {
	 System.out.println("PASS s1.equals(char + String)");

	 } else {
	 System.out.println("FAIL s1.equals(char + String)");
	 }


	**/


	System.out.println(success + " out of " + test + " tests have passed.");



    }

}
