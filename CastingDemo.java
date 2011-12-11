// run with java -cp . FILENAME because source changes 
// String arrays work wtih 10/13

public class Demo {
  
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

	boolean f = false;
	
	Object Ob = (Object)s;
	System.out.println("Ob is: " + Ob.toString());
	
	int intC = 5;
	double douC = (double)intC;
	System.out.println(douC);

    }
}
