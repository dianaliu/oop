// run with java -cp . FILENAME because source changes 

public class Casting {

    public static void main (String[] args) {

	String s = "soo";

	Object Ob = (Object)s;
	System.out.println("Ob is: " + Ob.toString());
	
	int intC = 5;
	double douC = (double)intC;
	System.out.println(douC);

    }
}
