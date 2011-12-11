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
			
			public void visitFormalParameters(GNode n) {

				String name = n.getNode(0).getString(3);

//				System.out.println("name of this " + name);
				
				String type = n.getNode(0).getNode(1).getNode(0).getString(0);
				
//				System.out.println("type of this " + type);

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
				System.out.println(" ");
				visit(n);
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
			public void visitPrimaryIdentifier(GNode n) {
				
				String name = (String)(n.get(0));
//				System.out.println("This is name " + name);
				String type = "";
				if(!name.equals("System"))	
					type = symTable.lookup(name).toString();
				
				n.setProperty(type,name);
				
//				System.out.println("This is type " + type + ",This is name " + name);
				visit(n);
				
			}
			
			
			
			
			public void visit(GNode n) {
				for (Object o : n) if (o instanceof GNode) dispatch((GNode)o);
			}
			//System.out.println(symTable.current());
			
		}.dispatch(java);
		
	}
}
