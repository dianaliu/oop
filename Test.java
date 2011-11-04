import testdependencies.DepFile1;
import testdependencies.DepFile2;
import testdependencies.starred.*;


public class Test{
	
	public static void main(String[] args) {
		DepFile1 dep1 = new DepFile1();
		DepFile2 dep2 = new DepFile2();
		System.out.println("Object");
		System.out.print("Oriented");
		System.out.print("Programming");
		
		for (int j = 0; j< 5; j++) {
			System.out.println(j);
		}
	}
	int testMethod(int num) {
		System.out.println("This is a method");
		
		return num;
	}
	
}

