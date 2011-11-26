// run with java -cp . FILENAME because source changes 
// String arrays work wtih 10/13

public class Demo {
    
    /*
     * Errors for multiple declarations, but getting closer to overloading!
    public static int retInt() {
	int i = 4+5;
	return i;
    }
    
    */

    public String retS(String s) {
	return s;

    }

    public static void main (String[] args) {

	Demo d = new Demo();
	String s = "soo";


	System.out.println("d.retS(s) = " + d.retS(s));

	String[] ss = new String[2];
	ss[0] = "zero";
	ss[1] = s;
	
	for(int i=0; i < 2; i++) {
	    System.out.println("ss = " + ss[i]);
	}

	System.out.println("ss[1] = " + ss[1]);

	System.out.println("A string s = " + s);


    }
}
