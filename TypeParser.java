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

public class TypeParser {
	
    public static boolean DEBUG = false;
	
    //Our symbol table
    SymbolTable symTable;
    String scopeName;//'a'
    String blockName;
    char prevName = 'a';
	
    //default constructor
    public TypeParser(String rootName, boolean db) {
		symTable = new SymbolTable(rootName);
		symTable.freshJavaId(rootName);
		DEBUG = db;
    }
	
	
    //returns the type of a variable or class
    public Object getType(String symbol) {//also needs the scope as well as name of symbol
		//Object type = symTable.current().lookup(symbol);
		//return type;//
		//		System.out.println("Looking for symbol " + symbol);
		//		System.out.println("isRoot2? " +  symTable.current().isRoot());
		Iterator nestedScopes = symTable.root().nested();
		while(nestedScopes.hasNext()) {
			nestedScopes.next();
			//			System.out.println("Nested scope : " + nestedScopes.next());
			
		}
		//symTable.setScope(symTable.root());//
		//		System.out.println("before scope name? " +  symTable.current().getQualifiedName());
		//		System.out.println("scope? " + symTable.root().lookupScope(symbol));
		//		System.out.println("isDefined? " + symTable.isDefined(symbol));
		//symTable.setScope(symTable.lookupScope(symbol));
		
		//		System.out.println("after scope name? " +  symTable.current().getQualifiedName() + "\n");
		return symTable.current().lookup(symbol);
    }  
	
	
    public void addSymbols(GNode java) {
		//		System.out.println("Now printing all the variable names and their types:");
		symTable.enter(java);
		//System.out.println(symTable.current());
		
		new Visitor() {
			GNode thisBlock;
			
			
			public void visitBlock(GNode n){
				//store scope information
				thisBlock = n;
				blockName = "" + (n.hashCode() + n.getLocation().line + n.getLocation().column);
				//scopeName = 
				//				System.out.println("VISITED A NEW BLOCK " + scopeName);
				symTable.enter("" + blockName);
				visit(n);
				symTable.exit();
				
				
			}
			public void visitMethodDeclaration(GNode n){
				
				
				blockName = "" + (n.hashCode() + n.getLocation().line + n.getLocation().column);
				
				//				System.out.println("VISITED A NEW METHOD " + scopeName);
				symTable.enter("" + blockName);
				visit(n);
				symTable.exit();
				
				
			}
		    
			public void visitFormalParameter(GNode n) {
				
				String name = n.getString(3);
				String type = 
				n.getNode(1).getNode(0).getString(0);
				
				symTable.current().addDefinition(name, type);
				
				visit(n);
			} 
		    
		    
		    
			public void visitFieldDeclaration(GNode n) {
				
				String name = n.getNode(2).getNode(0).getString(0);				
				String type = n.getNode(1).getNode(0).getString(0);
				
				symTable.current().addDefinition(name, type);
				//				System.out.println("Qualified name : " + symTable.current().getQualifiedName());
				try{
					PrintWriter fstream = new PrintWriter("Scope.txt");
					Printer babysFirstScope = new Printer(fstream);
					symTable.root().dump(babysFirstScope);
					babysFirstScope.flush();
				}
				catch(Exception e) {
					System.err.println(e.getMessage());
				}
				//			System.out.println(" ");
				visit(n);
			} 
			
			
			public void visitBasicForControl(GNode n) {
				
				// FIXME: Make name and type global variables for this 
				// method
				String name = null;
				String type = null;
				
				// Looking for variable type
				if(n.getNode(1).hasName("Type")) {
					type = n.getNode(1).getNode(0).getString(0);
				}
				else {
		    if(DEBUG) System.out.println("Node structure is " +
									   n.toString());
				}
				
				// Looking for variable name
				if(n.getNode(2).hasName("Declarators")) {
					if(n.getNode(2).getNode(0).hasName("Declarator")) {
						
						name = n.getNode(2).getNode(0).getString(0);
						
					}
				}
				else {
					System.out.println("Node structure is " 
									   + n.toString());
				}
				
				
				// Time to add it to the symbol table
				if(null != name && null != type) {
					
					if(DEBUG) 
						System.out.println("Adding to SymbolTable: ("  
										   + name + ", " + type + ")");
					
					symTable.current().addDefinition(name, type);
					
					try{
						// FIXME: Need to use the same PrintWriter
						// for all scope writing
						PrintWriter fstream = new PrintWriter("Scope.txt");
						Printer babysFirstScope = new Printer(fstream);
						symTable.root().dump(babysFirstScope);
						babysFirstScope.flush();
					}
					catch(Exception e) {
						System.err.println(e.getMessage());
					}
					
				}
				else {
					System.out.println("ERR: Couldn't find name/type");
				}
				
				// Entering a new scope due to For Loop 
				blockName = "" + (n.hashCode() + n.getLocation().line + n.getLocation().column);
				
				symTable.enter("" + blockName);
				visit(n);
				symTable.exit();
			}
		    
			public void visit(GNode n) {
				for (Object o : n) if (o instanceof GNode) dispatch((GNode)o);
			}
		}.dispatch(java);
    }
	
	
    public void addProperty(GNode java) {
		//		symTable.enter(java);
		
		new Visitor() {
			public void visitBlock(GNode n){
				//store scope information
				blockName = "" + (n.hashCode() + n.getLocation().line + n.getLocation().column);
			    
				if(DEBUG) System.out.println("About to enter " + blockName);
				symTable.enter("" + blockName);
				visit(n);
				symTable.exit();
			}
			public void visitMethodDeclaration(GNode n){
				
				blockName = "" + 
				(n.hashCode() + n.getLocation().line + n.getLocation().column);
				symTable.enter("" + blockName);
				visit(n);
				symTable.exit();
				
			}
			public void visitPrimaryIdentifier(GNode n) {
				
				String name = n.getString(0);
				String type = null;
			    
				if(!"System".equals(name)) {
					if(DEBUG) {
						System.out.println("--- Looking up type for "  + name); 
					}
					if( symTable.lookup(name) != null )
						type = symTable.lookup(name).toString();
					else {
						System.out.println("*** Did not find " + name );
					}
				}
			    
				if(null != type) {
					n.setProperty("type",type);
					if(DEBUG) {
						System.out.println("Set Type = " + type 
										   + " for var " + name);
					}
				}
				else {
					if(DEBUG) 
						System.out.println("Couldn't find Type for " + name);
				}
				visit(n);
			}
			
			
			
			
			public void visit(GNode n) {
				for (Object o : n) if (o instanceof GNode) dispatch((GNode)o);
			}
			//System.out.println(symTable.current());
			
		}.dispatch(java);
		
    }
}
