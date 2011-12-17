public class Exceptions
{

    static void checkSpelling()
    {
	try {
	    if(true) {
		throw new Exception("hey");
	    }

	} catch (Exception e) {

	    System.out.println("exception was: " + e.getMessage());
	    
	}

	
    }
    
    
    public static void main (String args[])
    {
		
	try {
		checkSpelling(); 

	} catch (Exception e) {

		System.out.println("exception was: " + e.getMessage());

	}
    } // end main
    
    
} 




