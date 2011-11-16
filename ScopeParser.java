package xtc.oop;

import xtc.Constants;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;

import xtc.util.SymbolTable;
import xtc.util.SymbolTable.Scope;

/* 
 * Creates a Symbol Table of instance variables and their scopes.
 * Used as lookup table in method overloading and _____.
 */ 

public class ScopeParser extends Visitor{

    public static boolean DEBUG = false;
    Node javaAST; 
    Node cppAST;
    SymbolTable jst = new SymbolTable("java");
    SymbolTable global = new SymbolTable("global");
    SymbolTable clss = new SymbolTable("class");


    // Constructor
    public ScopeParser(Node javaAST, Node cppAST,  boolean db) {
	DEBUG = db;
	this.javaAST = javaAST;
	this.cppAST = cppAST;
	//	System.out.println("\t--- Java Scope");
	//	initJScope();

	// Scope should be created from CPP tree
	System.out.println("\t--- CPP Scope");
	initScope();
    }

    // How to determine scope based on AST structure?
    // Scope property is meant to be set while traversing tree in order?

    // See Symbol.rats for the symbol he is referring to

     public void initScope() {
	System.out.println("\tRoot Scope = " + global.root().getName());
	global.freshName();
	global.freshCId();

	new Visitor() {
	    
	    public void visitClassDeclaration(GNode n) {
	
		clss.mark((Node)n); // SymbolTable deals only with Nodes

		if(n.hasProperty(Constants.SCOPE)) {
		    System.out.println("\t--- Node " + n.getName() + 
				       " hasScope = "  +  
				       n.getProperty(Constants.SCOPE));
		}
		else {
		    System.out.println("\t--- Node " + n.getName() + 
				       " has no scope"  );
		}



		visit(n);
	    }
	    
	    public void visit(Node n) {
		if(n.hasProperty(Constants.SCOPE)) {
		    System.out.println("\t--- Node " + n.getName() + 
				       " hasScope = "  +  
				       n.getProperty(Constants.SCOPE));
		}
		else {
		    global.mark(n);
		    System.out.println("\t--- Node " + n.getName() + 
				       " set Scope to " 
				       + n.getProperty(Constants.SCOPE) );
		}
		
		for (Object o : n) if (o instanceof Node) dispatch((Node)o);
	    }
	    
	}.dispatch(cppAST); // only root of cppAST was cast to Node




	// Initialize everything to global scope.
	// If a node n's children don't have scope, then n's scope will be 
	// passed down. 
	//	global.enter(javaAST);
	//	global.exit(javaAST);


    }


  public void initJScope() {
	System.out.println("\tRoot Scope = " + jst.root().getName());
	jst.freshName();
	jst.freshJavaId();


	// Initialize everything to global scope.
	// If a node n's children don't have scope, then n's scope will be 
	// passed down. 
	jst.enter(javaAST);  

	new Visitor () {
	    
	    public void visit(Node n) {

		System.out.println("\tNode " + n.getName() + " hasScope = " 
				   +  jst.current().getName());
		
		for( Object o : n ) {
		    if (o instanceof Node) dispatch((Node)o);
		}
	    }
	    
	    
	}.dispatch(javaAST);
    }


    // ----------------------------------------------------
    // ----------------- Getter methods -------------------
    // ----------------------------------------------------

    
    public SymbolTable getJSymbolTable() {
	return jst;
    }

}