//This file tests: field setting and getting 

public class CustomObject {
	
	int field;
	
	int number() {
		return field;
	}
	
	void setNumber( int newNum ) {
		field = newNum;
	}
	
	
	
    public static void main( String[] args ) {
		CustomObject thing1 = new CustomObject();
		thing1.setNumber(3);
		CustomObject thing2 = new CustomObject();
		thing2.setNumber(1);
		CustomObject thing3 = new CustomObject();
		thing3.setNumber(3339);
		System.out.println( "It works!" );
		System.out.println( thing1.number() );
		System.out.println( thing2.number() );
		System.out.println( thing3.number() );
	}
}
