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
    public Node getClass(String sc) {
	// return class node from classTree

	final String s = sc;

	Node foundClass = (Node)(new Visitor() {

		public Node visit(Node n) {
	
		    if(n.hasProperty("name")) {

			if( n.getStringProperty("name").equals(s) )
			    return n;
			
			for( Object o : n) {
			    if (o instanceof Node) {
				Node returnValue = (Node)dispatch((Node)o);
				if( returnValue != null ) return returnValue;
			    }
			}
			return null;
			
		    } 

		    else if(n.hasProperty("vtable")) {
			// ignore vtable nodes
			return null;
		    }
		    
		    return classTree; //NEVER RETURN THIS PLEASE!

		} // end visit()
	    }.dispatch(classTree));

	return foundClass;

    }

    public String getName(Node n) {
	return n.getStringProperty("name");

    }


    // Adds classes in proper location in classTree
    // @param n ClassDeclaration node of AST Tree
    public void addClass(GNode n) {

	String className = n.get(1).toString();
	GNode newChildNode = GNode.create("ClassDeclaration");
	newChildNode.setProperty("name", className);
	Node parent;
	
	if(n.get(3) != null) {
	    // Extends  something
	    
	    // adding vtable at index 0 messed this up
	    parent = 
		getClass( n.getNode(3).getNode(0).getNode(0).get(0).toString() );
	    
	    parent.addNode(newChildNode); // append new classes
	    
	    System.out.println("+Sub of " + getName( parent ) + ": "
			       + getName(newChildNode));
	}
	else {
	    // Doesn't extend, add to root (Object)
	    classTree.addNode(newChildNode);
	    parent = classTree;
	    
	    if(DEBUG) System.out.println("+Sub of Object: " + getName(newChildNode));
	}
	
	
	
	// Part 2!
	createVTable( n, newChildNode, parent );
	
    }

    // Adds VTable substructure to nodes in classTree
    // @param n Java AST ClassDeclaration node
    // @param childNode classTree node
    public void createVTable(Node n, Node child, Node parent) {

	// when adding methods, check parent first
	// if parent has by same index, point to it

	ArrayList vTable = new ArrayList();
	// isa returns parent node
	// All classes have Object's getClass - returns "name" property
	vTable.add(0, parent); // isa

	addParentMethods(parent, child); // copy parents vtable entries over
	// hopping to grimm objects to make sure they have vtables now!

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

    public void addParentMethods(Node parent, Node child) {
	// go to parent in classTree
	// set pointers to it's vtable from index 1 - n in child's vtable
    }
   

    // Init tree w/Grimm defined classes
    // Nodes have "name" property and vtable children
    public void initGrimmClassTree() {

	// FIXME: Don't hardcode Grimm objects
	// FIXME: has shitty name "MethodDeclaration".
	// How do we create our own node types/visitors

	GNode objectNode = GNode.create("ClassDeclaration");
	objectNode.setProperty("name", "Object");
	
	GNode stringNode = GNode.create("ClassDeclaration");
	stringNode.setProperty("name", "String");

	GNode classNode = GNode.create("ClassDeclaration");
	classNode.setProperty("name", "Class");

	GNode arrayNode = GNode.create("ClassDeclaration");
	arrayNode.setProperty("name", "Array");
 
	GNode integerNode = GNode.create("ClassDeclaration");
	integerNode.setProperty("name", "Integer");


	classTree = objectNode;
	classTree.add(0, stringNode);
	classTree.add(1, classNode);
	classTree.add(2, arrayNode);
	classTree.add(3, integerNode);

	if(DEBUG) System.out.println("Init Grimm Class Tree");

	// Part 2!
	
	initGrimmVTables();
    }

    public void initGrimmVTables() {
	// FIXME: Making them type "MethodDeclaration" doesn't even give us 
	// the corresponding visitor method! Cheated!  
	// FIXME: vtables are type "MethodDeclaration" nodes
	// shitty name, how do we create new types/visitors?

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


	GNode stringVT = GNode.create("MethodDeclaration");
	
	ArrayList stringVTArray = new ArrayList();
	// need to code automate get parent class
	// FIXME: Hardcoding Grimm's parent classes
	// use for isa and methods overriding
	stringVTArray.add(0, "Object");
	stringVTArray.add(1, "hashCode"); // FIXME: Point to method, not string
	stringVTArray.add(2, "equals");
	stringVTArray.add(3, "Parent's " + 
			  (  (ArrayList) (classTree.getNode(0).getProperty("vtable") )).get(3) );
	stringVTArray.add(4, "toString");
	stringVTArray.add(5, "length");
	stringVTArray.add(6, "charAt");
	
	stringVT.setProperty("vtable", stringVTArray);

	getClass("String").add(0, stringVT);

	if(DEBUG) {
	    printVTable(getClass("Object"));
	    printVTable(getClass("String"));
	    
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
	    public void visit(Node n) {
		for( Object o : n) {
		    if (o instanceof Node) dispatch((Node)o);
		}

		if(n.hasProperty("name")) {
		    System.out.println( "\tNode is " + n.getProperty("name") );
		    
		    // FIXME: Never called
		    if( !n.isEmpty() &&  n.getNode(0).hasProperty("vtable") )
			System.out.print(" and has a vtable");
		}

		if(n.hasProperty("vtable")) {
		    
		}

	    }

	    public void visitClassDeclaration(Node n) {
		// er, why is this never visited?
		System.out.println("AT A NODE OF TYPE CLASSDECLARATION");
		visit(n);
	    }


	    public void visitMethodDeclaration(Node n) {
		// never called??????
		visit(n);
	    }
	 
 
	}.dispatch(classTree);
    }

    public GNode getParentClass(final String className) {
	// returns parent node for isa and class hierch tree

	// FIXME: Does nay work :-(
	// QUERY: Redundant to have class hierch tree and explicit isa?

	    GNode parentClass =  (GNode)(new Visitor() {
		    public Node visit(Node n) {
			
			// Found the parent class
			if( ((String)n.getName()).equals(className) ) 
			    return n;
			
			// Keep searching
			for( Object o : n) {
			    if (o instanceof Node) {
				GNode returnValue = (GNode)dispatch((Node)o);
				if( returnValue != null ) return returnValue;
			    }
			}
			// Assuming correct input, should never return null
			// i.e. parent class always exists
			return null; 
		    }
		}.dispatch(classTree));

	    if(DEBUG) System.out.println("The parent class of " + className + 
					 " is " + parentClass.getName());

	    return parentClass;
    }

    public GNode getClassTree() {
	return classTree;
    }



}
