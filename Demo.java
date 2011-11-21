// run with java -cp . FILENAME because source changes 
// String arrays work wtih 10/13

public class Demo {

    public static void main (String[] args) {

	Demo d = new Demo();
	//	System.out.println("d.toString() = " + d.toString());

	// Can only print type Strings lol bc don't know Type in Expressions
	
	String s = "soo";

	String[] ss = new String[2];
	ss[0] = "zero";
	ss[1] = s;
	
	for(int i=0; i < 2; i++) {
	    System.out.println("ss = " + ss[i]);
	}

	//	System.out.println(ss.getClass());
	System.out.println("ss[1] = " + ss[1]);




    }
}
