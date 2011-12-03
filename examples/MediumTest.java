//this file tests: method chaining and setting and getting of fields

public class MediumTest {
	
	int field;
	
	int number() {
		return field;
	}
	
	MediumTest setNumber( int newNum ) {
		field = newNum;
		return this;
	}
	
    public static void main( String[] args ) {
		MediumTest thing1 = new MediumTest();
		thing1.setNumber(30);
		
		System.out.println( "It works!" );
		System.out.println( thing1.number() );
		System.out.println( thing1.setNumber(30).setNumber(40).number() );
	}
}