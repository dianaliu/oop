// run with java -cp . FILENAME because source changes 
// String arrays work wtih 10/13

public class Arguments {
  
    public String retS(String s, String t) {
	return s;

    }



    public static void main (String[] args) {

	Arguments d = new Arguments();

	String boo = "boo";

	System.out.println("d.retS(s) = " + d.retS("hi", boo));

    }

}
