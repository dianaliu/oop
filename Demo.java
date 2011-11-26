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

    public int retI(int i) {
	return i + 2;
    }

    //    public String toString() {
	// FIXME: Method overriding. Doesn't work
    //	return "HAHA I OVERRODE YOU";
    //    }

    public static void main (String[] args) {

	Demo d = new Demo();
	String s = "soo";

	System.out.println("d.retS(s) = " + d.retS(s));
	System.out.println("d.retI(4) = " + d.retI(4));
	System.out.println("d.retS(s).toString() = " + d.retS(s).toString());

	Object o = d;
	System.out.println("o class = " + o.getClass().toString());

	String[] ss = new String[2];
	ss[0] = "zero";
	ss[1] = s;
	
	for(int i=0; i < 2; i++) {
	    System.out.println("ss = " + ss[i]);
	}

	System.out.println("ss[1] = " + ss[1]);

	System.out.println("A string s = " + s);


	// FIXME: Generate templates for custom arrays if used, 
	// lookup via symbol table!
	//	Demo[] dd = new Demo[2];
	//	dd[0] = new Demo();
	//	dd[1] = new Demo();

	//	for(int i=0; i < 2; i++) {
	//	    System.out.println("dd[] = " + dd[i].getClass().toString());
	//	}
	

    }
}
