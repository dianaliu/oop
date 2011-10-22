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

    
    // TODO: Modify isa to be a pointer to parent class using getparent class.
    // This way, only need findClass method

    public static final boolean DEBUG = true;

    // Returned to satisfy the compiler  
    // Hopefully, never actually returned
    public static final GNode impossible = GNode.create("wtf");

    GNode javaASTTree; // Original JavaAST

    GNode classTree; // Contains nodes of type :
    // "Class" 
    // "VTable" - at Class.getChild(0)



    // Creates a helper class hierarchy tree containing vtable structure
    // @param n node for a class declaration

    public ClassLayoutParser(Node ast) {

	javaASTTree = (GNode)ast; // Original JavaAST, just in case
	initGrimmClassTree();

	// TODO: Getter methods for everything
	// FIXME: Add if statements for safety everywhere!

    }


    // Returns node from classTree if you know it's name
    // @param sc name of Class you want
    public GNode getClass(String sc) {
	// return class node from classTree

	final String s = sc;
	
	return (GNode)( new Visitor() {
		
		public void visit(GNode n) {
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
	    
	    // String name of Parent Class
	    String extendedClass = (String) 
		(n.getNode(3).getNode(0).getNode(0).get(0));

	    // Append new class as child of Parent
	    parent = getClass(extendedClass);
	    parent.addNode(newChildNode);
	    
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
	addVTable( n, newChildNode, parent );
	
    }

    // Adds VTable substructure to nodes in classTree
    // @param n AST ClassDeclaration node
    // @param child newly added classTree node
    // @param parent node in classTree
    public void addVTable(GNode n, GNode child, GNode parent) {
	// FIXME: Trouble creating vtables - finding methods and add/overriding

	ArrayList childVTable = new ArrayList();



	// 1. Inherit all of parent's methods
	final ArrayList parentVTable = 
	    (ArrayList) (parent.getNode(0).getProperty("vtable"));
	

	for(int i = 0; i < parentVTable.size(); i++)
	    {
     		childVTable.add(parentVTable.get(i)); 
	    }

	// 2. Visit AST MethodDeclarations under ClassDeclaration
	// Is it already in the parent's vtable?
	// if yes, Override - replace with pointer to AST MethodDeclaration
	// if no, Create - append to vtable, pointer to AST MethodDeclaration

	/*	physically cycle through classDeclaration's children. and add as you're going along. fuck visitor. visitor is no good cause I need lots of information out of each method.*/

	/*

	for(int i = 0; i < n.size(); i++) {
	    
	    String methodName;
	    GNode methodDeclarationNode;

	    if( n.test(n.get(i)) && 
		"MethodDeclaration".equals(n.getName()) ) { 
	
		methodDeclarationNode = n;
		methodName = n.get(3).toString();

		System.out.println("Found a MethodDeclaration Node " + 
				   methodName);
		
	      	int index = overrides(methodName, parentVTable);
		
		if(index > 0) { // overrides
		    childVTable.add(index, methodDeclarationNode);
		}
		else { // extends
		    childVTable.add(methodDeclarationNode);
		}

	    } // end if methodDeclaration
	} // end for 

	*/


	// make vtable node and set property
	Node childVTableNode = GNode.create("VTable");
	childVTableNode.setProperty("vtable", childVTable);

	// Add VTable node to Child at index 0
	child.add(0, childVTableNode);


	if(DEBUG) {
	    System.out.println("==========Temporary VTable");
	    printVTable(child);
	  
	}

	
    }


    // Determines if a Class overrides it's parent method
    public int overrides(String methodName, ArrayList parentVTable) {
	
	// Search Parent VTable to see if needs to be overriden
	// and at what index
	for(int i = 1; i < parentVTable.size(); i++) {

	    // FIXME: Currently comparing strings 
	    // How to compare nodes?

	    if(methodName.equals(parentVTable.get(i))) {
		return i;
	    }
	}

	return -1;
    }

    // Init tree w/Grimm defined classes
    // Nodes have "name" property and vtable children
    public void initGrimmClassTree() {

	// FIXME: Don't hardcode Grimm objects
	// FIXME: Create Class/MethodDeclaration ASTs fro Grimm types

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

	// ------------------------------------------------
	
	GNode objectVT = GNode.create("VTable"); // lol, why?

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

	GNode stringVT = GNode.create("VTable");
	
	ArrayList stringVTArray = new ArrayList();
	// need to code automate get parent class
	// FIXME: Hardcoding Grimm's parent classes
	// use for isa and methods overriding
	stringVTArray.add(0, "Object");
	// over ridden
       	stringVTArray.add(1, "hashCode"); // FIXME: Point to method, not string
	stringVTArray.add(2, "equals");
	// inherited
	stringVTArray.add(3, ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(3) ); // getClass
	// over ridden
	stringVTArray.add(4, "toString");
	// new methods
	stringVTArray.add(5, "length");
	stringVTArray.add(6, "charAt");
	
	stringVT.setProperty("vtable", stringVTArray);

	getClass("String").add(0, stringVT);

	// ------------------------------------------------

	GNode classVT = GNode.create("VTable");
	
	ArrayList classVTArray = new ArrayList();

	classVTArray.add(0, "Object");
	// inherited
	classVTArray.add(1, ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(1) );
	classVTArray.add(2,  ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(2));
	classVTArray.add(3, ( (ArrayList) (classTree.getNode(0).getProperty("vtable") ) ).get(3) );
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

	GNode arrayVT = GNode.create("VTable");

	/**
	   Finish implementing
	 **/

	// ------------------------------------------------

	GNode integerVT = GNode.create("VTable");

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
	    }


	    public void visitClass(GNode n) {
		System.out.print("Class " + n.getStringProperty("name") + " ");
		visit(n);
	    }
	    
	    // fix?
	    public void visitVTable(GNode n) {
		System.out.println(" has vtable\n");
	    }
	    
	}.dispatch(classTree);

	System.out.println();
    }

    public GNode getParentClass(final String className) {
	// 10/22 THIS IS NEVER CALLED ANYWHERE. lol?
	
	// returns parent node for isa and class hierch tree

	// FIXME: Does nay work :-(
	// i think i fixed this?  do i even need this?
	// QUERY: Redundant to have class hierch tree and explicit isa?

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
