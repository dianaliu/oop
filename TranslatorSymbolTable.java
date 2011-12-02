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
import java.util.Iterator;

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
	String scopeName;//'a'
	String blockName;
	char prevName = 'a';

	//default constructor
   	public TranslatorSymbolTable(String rootName) {
   		symTable = new SymbolTable(rootName);
   		symTable.freshJavaId(rootName);
	}
	
	
	//returns the type of a variable or class
	public Object getType(String symbol) {//also needs the scope as well as name of symbol
		//Object type = symTable.current().lookup(symbol);
		//return type;
		System.out.println("Looking for symbol " + symbol);
		System.out.println("isRoot2? " +  symTable.current().isRoot());
		Iterator nestedScopes = symTable.root().nested();
		while(nestedScopes.hasNext()) {
			System.out.println("Nested scope : " + nestedScopes.next());
			
		}
		//symTable.setScope(symTable.root());
		System.out.println("before scope name? " +  symTable.current().getQualifiedName());
		System.out.println("scope? " + symTable.root().lookupScope(symbol));
		System.out.println("isDefined? " + symTable.isDefined(symbol));
		//symTable.setScope(symTable.lookupScope(symbol));

		System.out.println("after scope name? " +  symTable.current().getQualifiedName() + "\n");
		return symTable.current().lookup(symbol);
	}  
	
	
    public void addSymbols(GNode java) {
		System.out.println("Now printing all the variable names and their types:");
		symTable.enter(java);
		//System.out.println(symTable.current());
	
		new Visitor() {
	   	 	GNode thisBlock;
	    

			public void visitBlock(GNode n){
				//store scope information
				thisBlock = n;
				blockName = "" + n.hashCode();
				//scopeName = 
				System.out.println("VISITED A NEW BLOCK " + scopeName);
				symTable.enter("" + blockName);
				visit(n);
				symTable.exit();
				//scopeName--;
			}
			
			public void visitDeclarator(GNode n) {
				String name = "";
				String type = "";
				name = (String)(n.get(0));
				System.out.print("Added Var name: " + name + "     ");
				StringTokenizer st = new StringTokenizer((String)(n.getNode(2).toString()), "(");
				type = st.nextToken();
				if(type.compareTo("NewClassExpression") == 0){
					StringTokenizer stc = new StringTokenizer((String)(n.getNode(2).get(2).toString()), "\"");
					type = stc.nextToken();
					System.out.println( "b4 tokenizer " + type);
					type = stc.nextToken();
				}
				System.out.println("Added Var type: " + type);

				//add the name-type pairs to the symbol table(symTable) here
				symTable.current().addDefinition(name, type);
				System.out.println("Qualified name : " + symTable.current().getQualifiedName());
				
				try{
					PrintWriter fstream = new PrintWriter("Scope.txt");
					Printer babysFirstScope = new Printer(fstream);
				

					symTable.root().dump(babysFirstScope);
				
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
