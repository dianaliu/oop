package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Location;
import xtc.tree.Printer;

import xtc.util.Runtime;
import xtc.util.SymbolTable;
import xtc.util.SymbolTable.Scope;

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;
import xtc.lang.JavaAnalyzer;
import xtc.lang.JavaAstSimplifier;

import java.util.StringTokenizer;
import java.util.Iterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class MavisBeacon {
	
	SymbolTable symTable;
	JavaAstSimplifier mySimplifier;
	JavaAnalyzer myAnalyzer; 
	GNode root;

	//constructor
   	public MavisBeacon(Runtime runtime) {
   		symTable = new SymbolTable();
	    mySimplifier = new JavaAstSimplifier();
	   	myAnalyzer = new JavaAnalyzer(runtime, symTable);
		System.out.println("BACON CREATED");
	}
	
	//populate symbol table
	public void createTable(GNode[] trees){
		for(int i = 0; i < trees.length; i++) {
			//check if the node array position is not empty
			if(trees[i] != null) {
				mySimplifier.visit(trees[i]);
				myAnalyzer.dispatch(trees[i]);
	   		}
	   	}
	   	//dump the symbol table to text file
	   	try{
			PrintWriter fstream = new PrintWriter("Scope.txt");
			Printer babysFirstScope = new Printer(fstream);
				
			symTable.root().dump(babysFirstScope);

			babysFirstScope.flush();
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
		System.out.println("SYMBOL TABLE CREATED");
	}
	
	//finds the entry for intMain in symbol table and return its type - for testing purposes
	public void finder(GNode java) {		
		System.out.println("Root has scope?: " + symTable.hasScope(java));
		new Visitor() {

				public void visitDeclarator(GNode n) {
					System.out.println("PROBING THE BACON");
					String name = "";
					name = (String)(n.get(0));
					if(name.equals("intMain")){
						System.out.println("Found Var name: " + name);
						System.out.println("Of type: " + (String)getTyper((GNode)(n)));
					}
					visit(n);
				}
			
				public void visit(GNode n) {
				for (Object o : n) if (o instanceof GNode) dispatch((GNode)o);
				}
		}.dispatch(java);
	}
	
	
	//returns the type of a variable or class given its node
	public Object getTyper(GNode n) {
		System.out.println("Node has scope?: " + symTable.hasScope(n));
		System.out.println("Node has scope?: " + symTable.hasScope((GNode)n.get(2)));
		
		System.out.println("Node has property?: " + n.hasProperty("SCOPE"));
		//System.out.println("Node has property?: " + ((GNode)(n.get(0))).hasProperty("SCOPE"));
		//System.out.println("Node has property?: " + ((GNode)(n.get(1))).hasProperty("SCOPE"));
		System.out.println("Node has property?: " + ((GNode)(n.get(2))).hasProperty("SCOPE"));

		symTable.enter((String)n.getProperty("SCOPE"));
		Object type = symTable.current().lookup(n.get(0).toString());
		return type;
		
	}  
	
    
}
