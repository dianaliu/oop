public class CustomArray {


    public static void main (String[] args) {


	CustomArray[] c = new CustomArray[2];

	for(int i = 0; i < c.length; i++) {
	    c[i] = new CustomArray();
	}

	System.out.println("Class of c = " + c.getClass());

    }
}
