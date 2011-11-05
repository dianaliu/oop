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

/* 
 * Creates a helper class hierarchy tree of vtables and data layouts
 */

public class ClassLayoutParser extends Visitor {


    public static boolean DEBUG = false;

    GNode classTree; // Contains nodes of type Class, VTable, and Data

    // Constructor
    // @param n node for a class declaration
    public ClassLayoutParser(GNode[] ast, boolean db) {
	DEBUG = db;
	if(DEBUG) System.out.println("--- Begin Class Layout Parser\n\n");
	initGrimmTypes();
	parseAllClasses(ast);
	if(DEBUG) System.out.println("--- End Class Layout Parser\n\n");
	
    }
    
    // Traverses all dependencies to find and add all classes
    // @param trees Array of all ASTs (main and dependencies)
    public void parseAllClasses(GNode[] trees) {

    	for(int i = 0; i < trees.length; i++) {
	    if(trees[i] != null) {
		new Visitor() {
		  
		    public void visitClassDeclaration(GNode n) {
			addClass(n);
			visit(n);
		    } 
		    
		    public void visit(GNode n) {
			for( Object o : n) {
			    if (o instanceof GNode) dispatch((GNode)o);
			}
		    }

		}.dispatch(trees[i]);
	    }
	}
    }
    
    // Adds class in proper hierarchal location to classTree
    // Calls methods to addVTable and addDataLayout
    // @param n ClassDeclaration node from a Java AST 
    public void addClass(GNode n) {
		
	String className = n.get(1).toString();
	GNode newChildNode = GNode.create("Class");
	newChildNode.setProperty("name", className);
	
	GNode parent = null; 
	
	if(n.get(3) != null) { // Extends something
	    
	    // String name of Parent Class
	    String extendedClass = (String)(n.getNode(3).getNode(0).getNode(0).get(0));
	    
	    // Append new class as child of Parent
	    parent = getClass(extendedClass);
	}
	else {
	    // Doesn't extend, add to root (Object)
	    parent = getClass("Object");	   
	}
	
	parent.addNode(newChildNode);
	
	if(DEBUG) System.out.println("\nAdded class " + getName(newChildNode) 
				      + " extends " +  getName(parent));
	    
	// VTable resides at index 0 of a Class node
	// DataLayout resides at index 1 of a Class node
	addVTable( n, newChildNode, parent );
	addDataLayout(n, newChildNode, parent);
	
    }
    
    // Adds VTable node to child 0 of a Class node.
    // Actual structure is kept in property "vtable" as an Arraylist
    // @param n AST ClassDeclaration node
    // @param child New child node in classTree
    // @param parent Parent node in classTree
    
    // Using global variable childVTableList, so as accessible from 
    // inner class of Visitor :-(
    ArrayList childVTableList = new ArrayList();
    
    public void addVTable(GNode n, GNode child, GNode parent) {
	
	// Need to clear out array before starting, 
	// since it's a global variable
	if(!childVTableList.isEmpty()) childVTableList.clear();
	
	// VTable[0] = isa
	childVTableList.add(0, getClass(getName(parent)));
	
	// 1. Inherit all of parent's methods
	final ArrayList parentVTable = 
	    (ArrayList) (parent.getNode(0).getProperty("vtable"));
	
	for(int i = 1; i < parentVTable.size(); i++)
	    {
     		childVTableList.add(parentVTable.get(i)); 
	    }
	
	// 2. Visit AST MethodDeclarations under ClassDeclaration.
	// Override and extend methods as needed.
	new Visitor() {
	    
	    public void visitMethodDeclaration(GNode n) {
		String methodName = n.get(3).toString();
		int overrideIndex = overrides(methodName, parentVTable);
		
		if(overrideIndex > 0) {
		    // overrides, must replace
		    childVTableList.add(overrideIndex, n);
		}
		else { //extended, append to list
		    childVTableList.add(n);
		}
	    }
	    
	    public void visit(GNode n) {
		// Need to override visit to work for GNodes
		for( Object o : n) {
		    if (o instanceof Node) dispatch((GNode)o);
		}
	    }
	    
	}.dispatch(n);
	
	// Make VTable node and attach list
	GNode childVTableNode = GNode.create("VTable");
	childVTableNode.setProperty("vtable", childVTableList);
	
	// Add VTable node to Child at index 0
	child.add(0, childVTableNode);
	
	if(DEBUG) printVTable(child);
	
    }
    
     
    // Adds DataLayout node to Class node at index 1
    // Actual structure is kept in property "data" as an ArrayList
    // @param n ClassDeclaration node of Java AST
    // @param child Child Class node
    // @param parent Parent Class node
    // Using global variable childDataStructure, so as to be accessible from 
    // inner class of Visitor :-(
    ArrayList childDataStructure = new ArrayList();
    public void addDataLayout(GNode n, GNode child, GNode parent) {
	
	// Need to clear out array before starting, 
	// since it's a global variable
	if(!childDataStructure.isEmpty()) childDataStructure.clear();
			
	// DataLayout[0] = __vptr
	childDataStructure.add(0, getVTable( child.getProperty("name").toString())); 
	
	// Only want to visit the Constructor
	GNode constructorDeclaration = (GNode) n.getNode(5).getNode(0);
	
	// Visit FieldDeclarations and pull out/save relevant information
	new Visitor() {
	    
	    // FIXME: There are more things we need from the constructor
      	    public void visitConstructorDeclaration(GNode n) {
		childDataStructure.add(1, n); // Constructor is always 1
		visit(n);
	    }
	    
	    public void visitFieldDeclaration(GNode n) {
		childDataStructure.add(n);
		visit(n);
	    }
	    
	    public void visitExpressionStatement(GNode n) {
		childDataStructure.add(n);
		visit(n);
	    }
	    
	    public void visit(GNode n) {
		// Need to override visit to work for GNodes
		for( Object o : n) {
		    if (o instanceof Node) dispatch((GNode)o);
		}
	    }
	    
	}.dispatch(constructorDeclaration);
	
	// Create DataLayout node and attach list
	GNode childData = GNode.create("DataLayout");
	childData.setProperty("data", childDataStructure);
	
	// Add DataLayout node to Child at index 1
	child.add(1, childData); 
	
	if(DEBUG) printDataLayout(child);
    }
    
    
    // ----------------------------------------------------
    // ------------- Internal Methods --------------
    // ----------------------------------------------------

  
    // Returns the name of a class
    // @param n A Class node 
   public String getName(GNode n) {
	return n.getStringProperty("name");
	
    }

    // Returns a Class node from the classTree
    // @param sc name of the Class desired
    public GNode getClass(String sc) {
	
	// Declared final to be accessible from inner Visitor classes
	final String s = sc;
	
	return (GNode)( new Visitor() {
			
		public GNode visitClass(GNode n) {

		    // Found the class
		    if( getName(n).equals(s) ) {
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
				
		public void visit(GNode n) { // override visit for GNodes
		    for( Object o : n) {
			if (o instanceof Node) dispatch((GNode)o);
		    }
		}
		
	    }.dispatch(classTree));
    }
    
    // Determines if a Class overrides it's Parent method
    // @param methodName
    // @param parentVTable
    public int overrides(String methodName, ArrayList parentVTable) {
	
	// Search Parent VTable to see if overriden and at what index
	for(int i = 1; i < parentVTable.size(); i++) {
	    
	    // FIXME: Currently comparing strings 
	    // How to compare nodes?
	    // ALTHOUGH - nodes  can be compared as strings...
	    if(methodName.equals(parentVTable.get(i))) {
		return i;
	    }
	}
	
	// If not overriden, return -1
	return -1;
    }


    // ----------------------------------------------------
    // ---------- Getter methods for Translator -----------
    // ----------------------------------------------------


    // Returns VTable list for a class
    // @param cN Class Name
    public ArrayList getVTable(String cN) {

	GNode className = getClass(cN);
	GNode classVT = (GNode) (className.getNode(0));
	if(classVT.hasProperty("vtable")) {
	    return (ArrayList) (classVT.getProperty("vtable"));
	}

	return null;
    }

    // Returns Data Layout list for a class
    // @param cN Class Name
    public ArrayList getDataLayout(String cN) {

	GNode className = getClass(cN);
	GNode classData = (GNode) (className.getNode(1));
	if(classData.hasProperty("data")) {
	    return (ArrayList) (classData.getProperty("data"));
	}

	return null;
    }


    // ----------------------------------------------------
    // -------------- Initialization Code -----------------
    // ----------------------------------------------------
    

    // Init tree w/Grimm defined classes
    // Nodes have "name" property 
    // Class nodes have children nodes DataLayout, VTable, and maybe Class
    public void initGrimmTypes() {
	
	// FIXME: Don't hardcode Grimm objects
	
	GNode objectNode = GNode.create("Class");
	objectNode.setProperty("name", "Object");
	
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
	
	
	initGrimmVTables();
	initGrimmDataLayout();
    }
    
    // ------------------ VTables -------------------------

    // VTable nodes always reside at index 0 of it's class
    public void initGrimmVTables() {

	GNode objectVT = GNode.create("VTable");
	
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
	
	ArrayList arrayVTArray = new ArrayList();
	
	arrayVTArray.add(0, "Object"); // isa
	arrayVTArray.add(1, "hashCode"); 
	arrayVTArray.add(2, "equals");
	arrayVTArray.add(3, "getClass");
	arrayVTArray.add(4, "toString");

	arrayVT.setProperty("vtable", arrayVTArray);
	
	getClass("Array").add(0, arrayVT);

	// ------------------------------------------------

	GNode integerVT = GNode.create("VTable");
	
	ArrayList integerVTArray = new ArrayList();

	integerVTArray.add(0, "Object");
	
	integerVT.setProperty("vtable", integerVTArray);
	
	getClass("Integer").add(0, integerVT);
	
    }

    // ---------------- Data Layouts ----------------

    // DataLayout nodes always reside at index 1 of it's class
    public void initGrimmDataLayout() {
	GNode objectData = GNode.create("DataLayout");
	
	ArrayList objectDataStructure = new ArrayList();
	objectDataStructure.add(0, getVTable("Object")); // pointer to vtable
	objectDataStructure.add(1,"__Object()" ); // constructor
	
	// TODO: Create integration with Translator
	// Method invocations without parameter(s)
	objectDataStructure.add(2, "static int32_t hashCode");
	objectDataStructure.add(3, "static bool equals");
	objectDataStructure.add(4, "static Class getClass");
	objectDataStructure.add(5, "static String toString");
	objectDataStructure.add(6, "static Class __class()");
	objectDataStructure.add(7, "static __Object_VT __vtable");

	objectData.setProperty("data", objectDataStructure);
	getClass("Object").add(1, objectData);

	// --------------------------------------------------

	GNode stringData = GNode.create("DataLayout");
	
	ArrayList stringDataStructure = new ArrayList();
	stringDataStructure.add(0, getVTable("String")); // pointer to vtable
	stringDataStructure.add(1,"__String(std::string data)" ); // constructor
	
	// Method invocations without parameter(s)
	stringDataStructure.add(2, "static int32_t hashCode");
	stringDataStructure.add(3, "static bool equals");
	stringDataStructure.add(4, "static String toString");
	stringDataStructure.add(5, "static int32_t length");
	stringDataStructure.add(6, "static char charAt");
	stringDataStructure.add(7, "static Class __class()");
	stringDataStructure.add(8, "static __String_VT __vtable");

	stringData.setProperty("data", stringDataStructure);
	getClass("String").add(1, stringData);

	// --------------------------------------------------

	GNode classData = GNode.create("DataLayout");
	
	ArrayList classDataStructure = new ArrayList();
	classDataStructure.add(0, getVTable("Class")); // pointer to vtable
	classDataStructure.add(1, "__Class(String name, Class parent, Class component = (Class)__rt::null(), bool primitive = false)" ); // constructor
	
	// Method invocations without parameter(s)
	classDataStructure.add(2, "static String toString"); 
	classDataStructure.add(3, "static String getName");
	classDataStructure.add(4, "static Class getSuperclass");
	classDataStructure.add(5, "static bool isPrimitive");
	classDataStructure.add(6, "static bool isArray");
	classDataStructure.add(7, "static Class getComponentType");
	classDataStructure.add(8, "static bool isInstance");
	classDataStructure.add(9, "static Class __class()");
	classDataStructure.add(10, "static __Class_VT __vtable");

	classData.setProperty("data", classDataStructure);
	getClass("Class").add(1, classData);

	// --------------------------------------------------

	GNode integerData = GNode.create("DataLayout");
	
	ArrayList integerDataStructure = new ArrayList();
	integerDataStructure.add(0, getVTable("Integer")); // __vptr
	// FIXME: this is all Grimm has online! Do we continue implementing?
	integerDataStructure.add(1, "static Class TYPE()");

	integerData.setProperty("data", integerDataStructure);
	getClass("Integer").add(1, integerData);

	// --------------------------------------------------

	GNode arrayData = GNode.create("DataLayout");
	
	ArrayList arrayDataStructure = new ArrayList();
	arrayDataStructure.add(0, getVTable("Array")); // __vptr
	arrayDataStructure.add(1,"Array(const int32_t length)" );
	arrayDataStructure.add(2, "static java::lang::Class __class()");
	arrayDataStructure.add(3, "static Array_VT<T> __vtable");

	arrayData.setProperty("data", arrayDataStructure);
	getClass("Array").add(1, arrayData);

    }



    // ----------------------------------------------------
    // -----------------  Printers ------------------------
    // ----------------------------------------------------
    // Note: Crude printers are implemented only for debugging.
    // Printing internal structures is not needed to translate.


    // FIXME: Should parameter be node or className?
    // @param n Class node
    public void printDataLayout (GNode n ) {
	
	if(n.getNode(1).hasName("DataLayout")) {
	   
	    GNode dataLayoutNode = (GNode) n.getNode(1);
	    ArrayList dataLayoutList = 
		(ArrayList) dataLayoutNode.getProperty("data");

	    System.out.println("--- Data Layout for " + n.getProperty("name"));

	    for(int i = 0; i < dataLayoutList.size(); i++) {
		
		// The dataLayoutList can contain: Strings, or nodes of type
		// ConstructorDeclarations, FieldDeclarations,
		// ExpressionStatements.

		// FIXME: More elegant way to print
		if(dataLayoutList.get(i) instanceof java.lang.String) {
		    System.out.println(i + "\t" + 
				       dataLayoutList.get(i).toString());
		}
		else if (dataLayoutList.get(i) instanceof GNode){
		    GNode g = (GNode) dataLayoutList.get(i);

		    if("ConstructorDeclaration".equals(g.getName())) {
		
			new Visitor() {
			    
			    public void visitConstructorDeclaration(GNode n) {
				// print constructor name
				System.out.print(n.get(2).toString() + " ");
				visit(n);
			    }
			    
			    public void visitModifier(GNode n) {
				// print modifiers
				System.out.print(n.get(0).toString() + " ");
				visit(n);
			    }

			    public void visitPrimitiveType(GNode n) {
				// FIXME: Extend for non primitive types
				System.out.print(n.get(0).toString() + " ");
				visit(n);
			    }

			    public void visitFormalParameter(GNode n) {
				// print parameter name
				System.out.print(n.get(3).toString() + " ");
				visit(n);
			    }

			    public void visit(GNode n) {
				// Need to override visit to work for GNodes
				for( Object o : n) {
				    if (o instanceof Node) dispatch((GNode)o);
				}
			    }
			}.dispatch(g);


		    }
		    else if("FieldDeclaration".equals(g.getName())) {
			
		    }
		    else if("ExpressionStatement".equals(g.getName())) {

		    } 
		    
		} // end else if
	    } // end for
	} // end if
	System.out.println();
    }


    // Prints out vtable given a class Node
    // NOTE: used only for debugging
    // @param n Class node
    public void printVTable(GNode n) {

	if(n.getNode(0).hasName("VTable")) {

	    GNode vtableNode = (GNode) n.getNode(0);
	    ArrayList vtableList = 
		(ArrayList) vtableNode.getProperty("vtable");

	    System.out.println("--- VTable for class " + n.getProperty("name"));

	    for(int i = 0; i < vtableList.size(); i++) {
		System.out.println(i + "\t" + vtableList.get(i).toString());
	    }

	} // end if
	System.out.println();
    }


    // Prints a list of all classes
    // FIXME: Make prettier
    // Note: Never actually called, for debugging
    public void printClassTree() {
	
	System.out.println("\n\n--- Class Tree");
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
	    
	    public void visitVTable(GNode n) {
		System.out.print(" has vtable");
	    }
	    
	    public void visitDataLayout(GNode n) {
		System.out.println(" has data layout");
	    }
	    
	}.dispatch(classTree);
	
	System.out.println();
    }

}
