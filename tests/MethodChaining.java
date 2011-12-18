public class MethodChaining {
  
    public String retS(String s) {
	return s;

    }

    public int retI(int i) {
	return i + 2;
    }


    public static void main (String[] args) {

	MethodChaining d = new MethodChaining();

	String s = "soo";

	System.out.println("d.retS(s) = " + d.retS(s));
	System.out.println("d.retI(4) = " + d.retI(4));
	// FIXME: When method chaining, it doesn't pass the target
	System.out.println("d.retS(s).toString() = " + d.retS(s).toString());

	Object o = d;


	System.out.println("o class = " + o.getClass().toString());

    }

}
