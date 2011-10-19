package xtc.oop;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Location;
import xtc.tree.Printer;

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;

public class DependencyVisitor extends Visitor {

	String currentAddress = "";
	String[] addressArray = new String[100];
	int addressIndex = 0;
	Node depTree;
	
    public DependencyVisitor() {
	}
	
	//put addresses of .java files imported into the addressArray[] array
	//addressArray[99] contains the number of addresses stored in the array
	public void visitImportDeclaration(GNode n) {
		for (int i = 0; i < n.getNode(1).size(); i++){
			currentAddress += "/";
			currentAddress+= ((String)(n.getNode(1).get(i)));
		} 
		if(((String)(n.get(2))) != null) {
			currentAddress += ("." + ((String)(n.get(2))));
		}
		else {
			currentAddress += ".java";
		}
		addressArray[addressIndex] = currentAddress;
		currentAddress = "";
		addressIndex++;
	}
	
	public void visitPackageDeclaration(GNode n) {
		//System.out.println ((String)n.getName());
	}
	
	
    public void visit(Node n) {
		for( Object o : n) {
			if (o instanceof Node) dispatch((Node)o);
		}
    }
	
	public String[] getAddressArray() {
		addressArray[99] = Integer.toString(addressIndex);
		return addressArray;
	}
	
	public void printAddressArray() {
		for(int i = 0; i < addressIndex; i++) {
				System.out.println(addressArray[i]);
		}
	}
	
	public Node parseArray(Node fullAST) throws IOException, ParseException {
		Node depTree;
		try {
			DependencyParser depParse = new DependencyParser();
			depTree = depParse.parseDependencies(addressArray, addressIndex);
			//append dependencies to the main AST(fullAST)
			//reset addressIndex to 0
		} 
		catch (IOException e) {
			depTree = null;
			System.out.println("IOException");
		}
		catch (ParseException e) {
			depTree = null;
			System.out.println("ParseException");
		} 
		
		return depTree;
	}
	
	
}