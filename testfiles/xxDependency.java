
//package xtc;

import testdependencies.DepFile1;
//import testdependencies.cave.DepFile3;
//import testdependencies.starred.*;


public class xxDependency{

  public static void main(String[] args) {
	
	DepFile1 dep1 = new DepFile1();
	//DepFile3 dep3 = new DepFile3();
	int j = dep1.methOneDepFile1(2);
	System.out.println(" j = " + j);
  }
}

