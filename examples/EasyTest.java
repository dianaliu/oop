//This file tests: field setting and getting 

public class EasyTest {
	
	int field;
	
	int number() {
		return field;
	}
	
	void setNumber( int newNum ) {
		field = newNum;
	}
	
	
	
    public static void main( String[] args ) {
		EasyTest thing1 = new EasyTest();
		thing1.setNumber(3);
		EasyTest thing2 = new EasyTest();
		thing2.setNumber(1);
		EasyTest thing3 = new EasyTest();
		thing3.setNumber(3339);
		System.out.println( "It works!" );
		System.out.println( thing1.number() );
		System.out.println( thing2.number() );
		System.out.println( thing3.number() );
	}
}