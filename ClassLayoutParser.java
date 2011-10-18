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

    
    public static final boolean DEBUG = false;

    Node astTree;
    GNode classTree;
    GNode dataLayoutTree;
    GNode vTableTree;

    String className;
    ArrayList methods;


    // Creates a class tree.  
    // Each class node has a data property pointing to it's data layout tree
    // Each data layout node has a pointer to it's vtable tree
    // @param n node for a class declaration
    public ClassLayoutParser(Node ast) {
	astTree = ast;
	initGrimmClassTree();
	initGrimmDataLayout();
    }


    public void addClass(GNode n) {

	String className = n.get(1).toString();


	if(!(n.get(1).toString()).equals("Object") ||
	   !(n.get(1).toString()).equals("String") ||
	   !(n.get(1).toString()).equals("Array") ||
	   !(n.get(1).toString()).equals("Integer") ) {
	    
	    // If not Grimm defined class, create it.
	    GNode newClass = GNode.create(className);


	    // n.get(3) = implements
	    // Doesn't implement anything, direct child of Object
	    if(n.get(3) == null) {
		classTree.addNode(newClass);
		// create Data Layout for newly appended Class Node
		generateDataLayout(classTree.getNode(classTree.size() - 1));

		if(DEBUG) System.out.println("+Sub of Object: " + className);
	    }
	    else {
		
		// FIXME: null ptr exception. getParentClass is defective.
		//		System.out.println("Parent of " + className + 
		// " is " + getParentClass(className).getName() );


	    } // end else

	    
	}

    }

    public void generateDataLayout (Node n) {
	// Creates DataLayout and isa relationships for newly added classes
	// @param n new class node
	
	GNode dl = GNode.create(n.getName());
	dl.setProperty("isa", getParentClass(n.getName()).getName());

	// add method nodes w/corresponding 4 children
	// use astTree

	new Visitor() {

	    public void visit(Node n) {
		for( Object o : n) {
		    if (o instanceof Node) dispatch((Node)o);
		}
	    }
	    
	    public void visitMethodDeclaration(Node n) {

		// FIXME: Never accessed
		// QUERY: for modifier(s) and parameter(s) can we visitModifiers
		// and visitFormalParameters?
		// add to dl
		System.out.println("method name = " + n.get(3));
		System.out.println("modifier(s) = ");
		System.out.println("return type = " + n.getNode(2).getNode(0).getNode(0));
		System.out.println("parameter(s) = ");
		visit(n);
	    }
	    
	}.dispatch(astTree);


	// get ast node of the class and find each MethodDeclaration Node
	// index 0.0.x = string modifier(s)
	// index 2.0.0 = return type
	// index 3 = name of method
	// index 4.0-x.0.0.0 = parameter type aka qualIden

	n.setProperty("dataLayout", dl);

	if(DEBUG) System.out.println("Generated data layout for " + n.getName());

    }


    // Init tree w/Grimm defined classes
    // Class Tree nodes have "dataLayout" property
    public void initGrimmClassTree() {

	// FIXME: Have only hardcoded object

	classTree = GNode.create("Object"); // shallow copy
	classTree.addNode(GNode.create("String")); // 0 
	classTree.addNode(GNode.create("Class")); // 1
	classTree.addNode(GNode.create("Array")); // 2

	if(DEBUG) System.out.println("Init Grimm Class Tree");
    }

    // Init trees  w/Grimm defined data layouts
    // Data Layout root node has "isa" property
    public void initGrimmDataLayout() {

	// FIXME: Have only hardcoded object

	// Structure of Data Layout :
	// Class Name w/isa property >
	// Methods 0 - X >
	// 0.method name 1. modifier 2. return type 3. parameter(s)

	GNode objectData = GNode.create("Object");

	// VTable contains same information as Data Layout, except:
	// 1. Formatted differently
	// 2. has isa field
       	objectData.setProperty( "isa", getParentClass("Object").getName() );

	// static int32_t hashCode(Object);
	objectData.addNode(GNode.create("Method0"));
	objectData.getNode(0).addNode(GNode.create("hashCode")); // method name
	objectData.getNode(0).addNode(GNode.create("static"));  // modifier
	objectData.getNode(0).addNode(GNode.create("int32_t")); // return type
	objectData.getNode(0).addNode(GNode.create("Object"));  // param

	// static bool equals(Object, Object);
	objectData.addNode(GNode.create("Method1"));
	objectData.getNode(1).addNode(GNode.create("equals")); // method name
	objectData.getNode(1).addNode(GNode.create("static"));  // modifier
	objectData.getNode(1).addNode(GNode.create("bool")); // return type
	objectData.getNode(1).addNode(GNode.create("Object, Object"));  // param

	// static Class getClass(Object)
	objectData.addNode(GNode.create("Method2"));
	objectData.getNode(2).addNode(GNode.create("getClass")); // method name
	objectData.getNode(2).addNode(GNode.create("static"));  // modifier
	objectData.getNode(2).addNode(GNode.create("Class")); // return type
	objectData.getNode(2).addNode(GNode.create("Object"));  // param

	// static String toString(Object)
	objectData.addNode(GNode.create("Method3"));
	objectData.getNode(3).addNode(GNode.create("toString")); // method name
	objectData.getNode(3).addNode(GNode.create("static"));  // modifier
	objectData.getNode(3).addNode(GNode.create("String")); // return type
	objectData.getNode(3).addNode(GNode.create("Object"));  // param


	// Set Object's data layout
       	classTree.setProperty("dataLayout", objectData );


	if(DEBUG) System.out.println("Init Grimm data layouts");

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
    
    // returns the data layout tree for a specific class
    // @param className class you want data layout for
    // note data layout contains isa property
    public GNode getDataLayoutTree(final String className) {
	
	// traverse class tree
	// if node.hasName(className)
	// return the dataLayout property value

	return (GNode) ( new Visitor() {
	    
		public Node visit(Node n) {
		    if( ((String)n.getName()).equals(className) )
			return (GNode)(n.getProperty("dataLayout"));
		    

		    // Continue searching
		    for( Object o : n) {
			if (o instanceof Node) {
			    GNode returnValue = (GNode)dispatch((Node)o);
			    if( returnValue != null ) return returnValue;
			}
		    }
		    return null; 
		    
		} // end visit()
		
	    }.dispatch(classTree) );

    }

    public void visit(Node n) {
	for( Object o : n) {
	    if (o instanceof Node) dispatch((Node)o);
	}
    }
    

    public void findMethods(GNode n) {
	
    }


    public void formDataLayout() {
	// Data Layout contains the method signatures implemented by a class

	// Each method signature must include : 
	// 

    }


    public void formVTableLayout() {
	// VTable Layout contains the "resolved" method signatures
	// formatted as pointers and typed correctly

    }

}
