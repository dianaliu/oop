package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Printer;
import xtc.tree.Visitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;

public class ClassLayoutParser extends Visitor {

    
    // TODO: Modify isa to be a pointer.  This way, only need findClass method


    public static final boolean DEBUG = true;

    // Returned to satisfy the compiler  
    // Hopefully, never actually returned
    public static final GNode impossible = GNode.create("wtf");

    GNode astTree; // Original JavaAST

    GNode classTree; // Hold basic class hierarchy

    GNode dataLayoutTree; // 
    GNode vTableTree;

    String className;
    ArrayList methods;


    // Creates a helper class hierarchy tree containing vtable structure
    // @param n node for a class declaration

    public ClassLayoutParser(Node ast) {

	astTree = (GNode)ast; // Original JavaAST, just in case
	initGrimmClassTree();

	// TODO: Getter methods for everything

    }


    // Returns node from classTree if you know it's name
    // @param sc name of Class you want
    public GNode getClass(String sc) {
	// return class node from classTree

	final String s = sc;
	
	return (GNode)( new Visitor() {
		
		public void  visit(GNode n) {
		    for( Object o : n) {
			if (o instanceof Node) dispatch((GNode)o);
		    }
		}
		
		public GNode visitClass(GNode n) {
		    // Found the class
		    if( getName(n).equals(s) ) {
			if(DEBUG) {
			    System.out.println("Retrieved class " 
					       + getName(n) );
			}
			return n;
		    }

		    // Keep Searching
		    for( Object o : n) {
			if (o instanceof Node) {
			    GNode returnValue = (GNode)dispatch((GNode)o);
			    if( returnValue != null ) return returnValue;
			}
		    }
		    return null;
		}
		
	    }.dispatch(classTree) );
    }

    public String getName(GNode n) {
	return n.getStringProperty("name");

    }

    // Adds classes in proper location in classTree
    // @param n ClassDeclaration node of AST Tree
    public void addClass(GNode n) {

	GNode parent = classTree;

	String className = n.get(1).toString();
	GNode newChildNode = GNode.create("Class");
	newChildNode.setProperty("name", className);
	
	if(DEBUG) System.out.println("Created new class " + 
				     getName(newChildNode));
	
	
	if(n.get(3) != null) { // Extends  something
	    
	    // adding vtable at index 0 messed this up
	    String extendedClass = (String) (n.getNode(3).getNode(0).getNode(0).get(0));

	    parent = getClass(extendedClass);
	    parent.addNode(newChildNode); // append new classes
	    
	    if(DEBUG) System.out.println( "+" + getName(newChildNode) + 
					  " extends " +  getName(parent));

	}
	else {
	    // Doesn't extend, add to root (Object)
	    classTree.addNode(newChildNode);
	    parent = classTree;
	    
	    if(DEBUG) System.out.println( "+" + getName(newChildNode) + 
					  " extends " +  getName(parent));

	}
	
      	// Part 2!
	// @param n AST classDeclaration node
	// 
	//       	addVTable( n, newChildNode, parent );
	
    }

    // Adds VTable substructure to nodes in classTree
    // @param n AST ClassDeclaration node
    // @param child newly added classTree node
    // @param parent 
    public void addVTable(GNode n, GNode child, GNode parent) {
	// FIXME: Is super buggy

	if(DEBUG) System.out.println("[ entered addVTable ] ");

	// 0. How to get Methods?
	// Initially fill index 1 - parent_vtable.size() w/parent's methods
	// assume inheritance
	// Must search vtable each time we "add" a new method
	// If method exists, replace it w/child's implementation
	// If method DNE, append new method

	ArrayList vTable = new ArrayList();

	vTable.add(0, parent); // isa
	// FIXME: Can we assume parent's vtable exists when adding children?
	// if not, could run addClass on it.  
	// FIXME: Add if statements for safety everywhere!
	
	// Initially, inherit all of parent's methods
	// FIXME: Need to cast all getNodes to GNode?
	final ArrayList parentVTable = 
	    (ArrayList) (parent.getNode(0).getProperty("vtable"));
	
	for(int i = 0; i < parentVTable.size(); i++)
	    {
		// will follow all the way up
		vTable.add(parentVTable.get(i)); 
	    }

	// visit method names under AST ClassDeclaration
	// Does it exist in index 1 - parentVTable.size()?
	// if yes, replace
	// if no, append new entry to vTable

	new Visitor() {

	    public void visitMethodDeclaration(GNode n) {
		
		String className = n.get(3).toString();
		if(DEBUG) System.out.println("Encountered method " + className);

		if(overrides(className, parentVTable) > 0) {
		    // FIXME: Can't access vTable from inner method
		    //		    vTable.add(overrides(className, parentVTable), className);
		}
		else {
		    //FIXME: Change everything to pointers to nodes
		    // Could do vTable.add(n) right now, but GrimmVTables 
		    // are strings.
		    // Come back and fix after I've implemented GrimmObjects
		    // in tree form
		    //		    vTable.add(className); 
		}

      	    }
	    
	    public void visit(GNode n) {
		for( Object o : n) {
		    if (o instanceof Node) dispatch((GNode)o);
		}
	    }


	}.dispatch(n);



	// default add pointers to all of parent methods
       	// get methods from AST Tree,
	// if overridden, replace ptr to parent with self's implementation

	// when parent name == self.name that's when we reached top level class
	
	/** ADD CODE **/



	// make vtable node and set property
	// has shitty name "MethodDeclaration"
	// FIXME: How do we create our own node types/visitors
	Node vTableNode = GNode.create("MethodDeclaration");
	vTableNode.setProperty("vtable", vTable);


    }

    // Determines if a method overrides it's parent method
    public int overrides(String className, ArrayList parentVTable) {
	
	for(int i = 1; i < parentVTable.size(); i++) {

	    // FIXME: Currently comparing strings 
	    // When pointers to nodes are implemented, 
	    // change equals implementation (by location?)

	    if(className.equals(parentVTable.get(i))) {
		return i;
	    }
	}

	return -1;
    }

    // Init tree w/Grimm defined classes
    // Nodes have "name" property and vtable children
    public void initGrimmClassTree() {

	// FIXME: Don't hardcode Grimm objects

	GNode objectNode = GNode.create("Class");
	objectNode.setProperty("name", "Object");
	
	// FIXME: Subsequent properties set to null!!!

	GNode stringNode = GNode.create("Class");
	stringNode.setProperty("name", "String");

	GNode classNode = GNode.create("Class");
	classNode.setProperty("name", "Class");

	GNode arrayNode = GNode.create("Class");
	arrayNode.setProperty("name", "Array");
 
	GNode integerNode = GNode.create("Class");
	integerNode.setProperty("name", "Integer");


	classTree = objectNode;
	classTree.add(0, stringNode);
	classTree.add(1, classNode);
	classTree.add(2, arrayNode);
	classTree.add(3, integerNode);

	// Part 2!
	
	initGrimmVTables();
    }

    public void initGrimmVTables() {



	// FIXME: Complete implementation w/ array and integer pls!

	// FIXME: Making them type "MethodDeclaration" doesn't even give us 
	// the corresponding visitor method! Cheated!  
	// FIXME: vtables are type "MethodDeclaration" nodes
	// shitty name, how do we create new types/visitors?


	// ------------------------------------------------
	
	GNode objectVT = GNode.create("MethodDeclaration"); // lol, why?

	ArrayList objectVTArray = new ArrayList();
	objectVTArray.add(0, "Object"); // Object's parent is Object or null?
	// FIXME: Needs to point to actual method
	// Perhaps create separate GrimmClassTree?
	// FIXME: For new classes,point to method declaration node in AST
	objectVTArray.add(1, "hashCode");
	objectVTArray.add(2, "equals");
	objectVTArray.add(3, "getClass"); // returns "name" property
	objectVTArray.add(4, "toString");

	objectVT.setProperty("vtable", objectVTArray);

	// ALWAYS add vtable at index 0 because we append new classes
	classTree.add(0, objectVT);

	// ------------------------------------------------

	GNode stringVT = GNode.create("MethodDeclaration");
	
	ArrayList stringVTArray = new ArrayList();
	// need to code automate get parent class
	// FIXME: Hardcoding Grimm's parent classes
	// use for isa and methods overriding
	stringVTArray.add(0, "Object");
	// over ridden
       	stringVTArray.add(1, "hashCode"); // FIXME: Point to method, not string
	stringVTArray.add(2, "equals");
	// inherited
	stringVTArray.add(3, ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(3) );
	// over ridden
	stringVTArray.add(4, "toString");
	// new methods
	stringVTArray.add(5, "length");
	stringVTArray.add(6, "charAt");
	
	stringVT.setProperty("vtable", stringVTArray);

	getClass("String").add(0, stringVT);

	// ------------------------------------------------

	GNode classVT = GNode.create("MethodDeclaration");
	
	ArrayList classVTArray = new ArrayList();

	classVTArray.add(0, "Object");
	// inherited
	classVTArray.add(1, ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(1) );
	classVTArray.add(2,  ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(2));
	classVTArray.add(3, ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(3) );


	/**  If, for testing, you want to append parent in front of method name

	classVTArray.add(1, classVTArray.get(0) + "'s " + 
			 ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(1) );
	classVTArray.add(2,  classVTArray.get(0) + "'s " + 
			  ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(2));
	classVTArray.add(3, classVTArray.get(0) + "'s " + 
			  ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(3) );
	**/
	// overridden
	classVTArray.add(4, "toString"); 
	// new methods
	classVTArray.add(5, "getName");
	classVTArray.add(6, "getSuperClass");
	classVTArray.add(7, "isPrimitive");
	classVTArray.add(8, "isArray");	
	classVTArray.add(9, "getComponentType");
	classVTArray.add(10, "isInstance");

	// Attach vTable to vt node
	classVT.setProperty("vtable", classVTArray);

	// Attach vt node to class node
	getClass("Class").add(0, classVT);

	// ------------------------------------------------

	GNode arrayVT = GNode.create("MethodDeclaration");

	/**
	   Finish implementing
	 **/

	// ------------------------------------------------

	GNode integerVT = GNode.create("MethodDeclaration");

	/**
	   Finish implementing
	 **/

	// ------------------------------------------------


	if(DEBUG) {
	    printVTable(getClass("Object"));
	    printVTable(getClass("String"));
	    printVTable(getClass("Class"));
	    
	}
	

    }

    // Prints out vtable given a class Node
    //@param n class node
    // FIXME: How do we get the vtable node?  ALWAYS INSERT AT ZERO.
    public void printVTable(Node n) {
	ArrayList al = (ArrayList) n.getNode(0).getProperty("vtable");

	System.out.println("========= VTable " + " for class " + 
			   n.getProperty("name") + " ==========");

	for(int i = 0; i < al.size(); i++)
	    System.out.println(i + "\t" + al.get(i).toString());

    }


    public void pClassTree() {

	System.out.println("========= Class Tree ============");
	new Visitor () {
	    public void visit(GNode n) {
		for( Object o : n) {
		    if (o instanceof Node) dispatch((GNode)o);
		}

		/*
		if(n.hasProperty("name")) {
		    System.out.print( "\tNode is " + n.getProperty("name") );
		    
       		    if( !n.isEmpty() &&  n.getNode(0).hasProperty("vtable") )
			System.out.println(" and has a vtable");
		    else
			System.out.println();
		*/
	    }


	    public void visitClass(GNode n) {
		System.out.print("Class " + n.getStringProperty("name"));
		visit(n);
	    }
	    
	    // fix?
	    public void visitMethodDeclaration(GNode n) {
		System.out.println(" has vtable\n");
	    }
	    
	}.dispatch(classTree);

    }

    public GNode getParentClass(final String className) {
	// returns parent node for isa and class hierch tree

	// FIXME: Does nay work :-(
	// QUERY: Redundant to have class hierch tree and explicit isa?

	// FIXME: Use GNodes and "Class" and "VT" type nodes/visitors

	return (GNode) ( new Visitor() {
		     public GNode visit(GNode n) {
			
			// Found the parent class
			if( ((String)n.getName()).equals(className) ) 
			    return n;
			
			// Keep searching
			for( Object o : n) {
			    if (o instanceof Node) {
				GNode returnValue = (GNode)dispatch((GNode)o);
				if( returnValue != null ) return returnValue;
			    }
			}
			// Assuming correct input, should never return null
			// i.e. parent class always exists
			return null; 
		    }

	    }.dispatch(classTree) );

    }

    public GNode getClassTree() {
	return classTree;
    }



}
