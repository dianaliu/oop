// run with java -cp . FILENAME because source changes 

public class Constructor {

    int field;

    public Constructor() {
    	field = 0;
    }
    
    //    public Constructor (int f) {
    //	field = f;
    //    }


    public static void main (String[] args) {

	Constructor c = new Constructor();
	System.out.println(c.field);

    }

}
