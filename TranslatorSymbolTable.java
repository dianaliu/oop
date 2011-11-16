package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Location;
import xtc.tree.Printer;

import xtc.util.SymbolTable;
import xtc.util.SymbolTable.Scope;

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;

import java.util.StringTokenizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class TranslatorSymbolTable {
	
	//Our symbol table
	SymbolTable symTable;
	//SymbolTable.Scope symTableScope;
	char scopeName = 'a';
	char prevName = 'a';

	//default constructor
   	public TranslatorSymbolTable(String rootName) {
   		symTable = new SymbolTable(rootName);
   		symTable.freshJavaId(rootName);
	}
	
	
	//returns the type of a variable or class
	public Object getType(String symbol) {
		Object type = symTable.lookup(symbol);
		return type;
	} 
	
	public void addSymbols(GNode java) {
		System.out.println("Now printing all the variable names and their types:");
		symTable.enter(java);
		System.out.println(symTable.current());

		
		new Visitor() {
				GNode thisBlock;

				
				public void visitBlock(GNode n){
					//store scope information
					thisBlock = n;
					scopeName++;
					System.out.println("VISITED A NEW BLOCK " + scopeName);
					visit(n);
					scopeName--;
				}
				
				//add a method that exits one scope heirarchy in the symbol tabel if a scope is exited
				//if(a scope was left) then 
				//	symTable.exit();
				//  decrement current level counter by 1
				
		    	public void visitDeclarator(GNode n) {
					//reference thisBlock as the scope of declarators
					//SymbolTable.Scope Kirim = SymbolTable.new Scope();
					
					String name = "";
					String type = "";
					name = (String)(n.get(0));
					System.out.print("Var name: " + name + "     ");
					StringTokenizer st = new StringTokenizer((String)(n.getNode(2).toString()), "(");
					type = st.nextToken();
					if(type.compareTo("NewClassExpression") == 0){
						StringTokenizer stc = new StringTokenizer((String)(n.getNode(2).get(2).toString()), "\"");
						type = stc.nextToken();
						type = stc.nextToken();
					}
					System.out.println("Var type: " + type);
					
					//add the name-type pairs to the symbol table(symTable) here
					System.out.println("Scopename : " + scopeName);
					symTable.enter("" + scopeName);
					symTable.current().addDefinition(name, type);
					symTable.exit();
					
					System.out.println("Scope : " + symTable.current().lookupScope("DepFile1"));
					
					/*
					System.out.println("Unqualified name : " + symTable.current().getName());
					System.out.println("Look up i : " + symTable.current().lookup("i"));
					System.out.println("Scope i : " + symTable.lookupScope("i").getName());
					System.out.println("Look up dep : " + symTable.current().lookup("dep"));
					System.out.println("Look up iN : " + symTable.current().lookup("iN"));
					System.out.println("Look up deep : " + symTable.current().lookup("deep"));
					//symTable.enter("TestString");
					//symTable.exit();
					System.out.println("Is root : " + symTable.current().isRoot());
					System.out.println("Has symbols : " + symTable.current().hasSymbols());
					System.out.println("Has nested : " + symTable.current().hasNested());
					System.out.println("Qualified i : " + symTable.current().qualify("i"));
					//symTable.setScope(symTable.lookupScope("deep"));
					System.out.println("Scope deeper : " + symTable.current().lookupScope("deeper").getName());
					*/
					
 /*
					//symTable.freshJavaId("Grimm");
					System.out.println("FreshJavaID : " + symTable.freshJavaId(name));
					System.out.println("FreshNameID : " + symTable.freshName(name));
					System.out.println("Tomethodname : " + symTable.toMethodName(name));
					System.out.println(symTable.isDefined("Grimm"));
					System.out.println("FreshNameCount : " + symTable.freshNameCount);
					symTable.enter("Hello");
					System.out.println("Scope : " + symTable.lookupScope("Hello"));

					//Kirim.addDefinition(name, type);
*/		
			
				try{
					PrintWriter fstream = new PrintWriter("Scope.txt");
					Printer babysFirstScope = new Printer(fstream);
					
					symTable.current().dump(babysFirstScope);

					babysFirstScope.flush();
				}
				catch(Exception e) {
					System.err.println(e.getMessage());
				}
					
					
					System.out.println(" ");
					visit(n);
				} 
		    
				public void visit(GNode n) {
					for (Object o : n) if (o instanceof GNode) dispatch((GNode)o);
				}
		}.dispatch(java);
	}

}
