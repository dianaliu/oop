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
    // @param n ClassDeclaration node of Java AST 
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
	addDataLayout(n, newChildNode, parent);
	
    }

    // Adds VTable substructure to nodes in classTree
    // @param n AST ClassDeclaration node
    // @param child newly added classTree node
    // @param parent node in classTree
 
    // FIXME: Temporarily using global variables, which are accessible from 
    // inner class of Visitor
    ArrayList childVTableList = new ArrayList();

    public void addVTable(GNode n, GNode child, GNode parent) {
	
	// Need to clear out array before starting, 
	// since we're using global variable
	if(!childVTableList.isEmpty()) childVTableList.clear();


	// 1. Inherit all of parent's methods
	final ArrayList parentVTable = 
	    (ArrayList) (parent.getNode(0).getProperty("vtable"));
	

	for(int i = 0; i < parentVTable.size(); i++)
	    {
     		childVTableList.add(parentVTable.get(i)); 
	    }

	// 2. Visit AST MethodDeclarations under ClassDeclaration
	// Is it already in the parent's vtable?
	// if yes, Override - replace with pointer to AST MethodDeclaration
	// if no, Create - append to vtable, pointer to AST MethodDeclaration

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

	// make vtable node and set property
	GNode childVTableNode = GNode.create("VTable");
	childVTableNode.setProperty("vtable", childVTableList);

	// Add VTable node to Child at index 0
	child.add(0, childVTableNode);

	if(DEBUG) printVTable(child);

    }



    // @param n ClassDeclaration node of Java AST
    // @param child Child class Node
    // @param parent Parent class Node

    // FIXME: Temporarily using global variables, which are accessible from 
    // inner class of Visitor
    ArrayList childDataStructure = new ArrayList();

    public void addDataLayout(GNode n, GNode child, GNode parent) {
	
	// Need to clear out array before starting, 
	// since we're using global variable
	if(!childDataStructure.isEmpty()) childDataStructure.clear();

	// Only want to visit constructor
	GNode constructorDeclaration = (GNode) n.getNode(5).getNode(0);
	
	GNode childData = GNode.create("DataLayout");

	// __vptr at index 0
	childDataStructure.add(0, getVTable( child.getProperty("name").toString())); 

	// Visit FieldDeclarations and pull out/save relevant information
	new Visitor() {
	    
      	    public void visitConstructorDeclaration(GNode n) {
		childDataStructure.add(1, n); // Constructor is always 1
		visit(n);
	    }

	    public void visitFieldDeclaration(GNode n) {
		System.out.println("Added a field declaration");
		childDataStructure.add(n);
		visit(n);
	    }
	    
	    public void visitExpressionStatement(GNode n) {
		System.out.println("Added an expression statemetn");
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

	childData.setProperty("data", childDataStructure);

	child.add(1, childData); 

	if(DEBUG) printDataLayout(child);
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



    // ----------------------------------------------------
    // -------------- Initialization Code -----------------
    // ----------------------------------------------------

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
	initGrimmDataLayout();
    }

    // ------------------ VTables -------------------------

    // VTable nodes always reside at index 0 of it's class
    public void initGrimmVTables() {

	// FIXME: Complete implementation w/ array and integer pls!

	// ------------------------------------------------
	
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

	// ------------------------------------------------


	if(DEBUG) {
	  
	    printVTable(getClass("Object"));
	    printVTable(getClass("String"));
	    printVTable(getClass("Class"));
	    printVTable(getClass("Array"));
	    printVTable(getClass("Integer"));
	 	    
	}
	
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
	
	// TODO: Create integration with Translator
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
	
	// TODO: Create integration with Translator
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
	integerDataStructure.add(0, getVTable("Integer")); // pointer to vtable
	// UH, this is all Grimm has online?
	integerDataStructure.add(1, "static Class TYPE()");

	integerData.setProperty("data", integerDataStructure);
	getClass("Integer").add(1, integerData);

	// --------------------------------------------------

	GNode arrayData = GNode.create("DataLayout");
	
	ArrayList arrayDataStructure = new ArrayList();
	arrayDataStructure.add(0, getVTable("Array")); // pointer to vtable
	arrayDataStructure.add(1,"Array(const int32_t length)" );
	
	// TODO: Create integration with Translator
	arrayDataStructure.add(2, "static java::lang::Class __class()");
	arrayDataStructure.add(3, "static Array_VT<T> __vtable");

	arrayData.setProperty("data", arrayDataStructure);
	getClass("Array").add(1, arrayData);


	// --------------------------------------------------
	if(DEBUG) {
	 
	    printDataLayout(getClass("Object"));
	    printDataLayout(getClass("String"));
	    printDataLayout(getClass("Class"));
	    printDataLayout(getClass("Array"));
	    printDataLayout(getClass("Integer"));
	 	    
	}
       

    }


    // ----------------------------------------------------
    // -----------------  Printers ------------------------
    // ----------------------------------------------------
    

    // Should parameter be node or className?
    // @param n Class node
    public void printDataLayout (GNode n ) {
	
	if(n.getNode(1).hasName("DataLayout")) {
	   
	    GNode dataLayoutNode = (GNode) n.getNode(1);
	    ArrayList dataLayoutList = 
		(ArrayList) dataLayoutNode.getProperty("data");

	    System.out.println("========= Data Layout for " + 
			       n.getProperty("name") + " ==========");

	    for(int i = 0; i < dataLayoutList.size(); i++) {
		
		// The dataLayoutList can contain: Strings, 
		// ConstructorDeclarations, FieldDeclarations,
		// ExpressionStatements.

		// FIXME: More elegant way to print
		if(dataLayoutList.get(i) instanceof java.lang.String) {
		    System.out.println(i + "\t" + 
				       dataLayoutList.get(i).toString());
		}
		else if (dataLayoutList.get(i) instanceof GNode){
		    GNode g = (GNode) dataLayoutList.get(i);

		    // FIXME: Implement pretty printing for Nodes
		    
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
    }


    // Prints out vtable given a class Node
    //@param n Class node
    // FIXME: How do we get the vtable node?  ALWAYS INSERT AT ZERO.
    public void printVTable(GNode n) {

	if(n.getNode(0).hasName("VTable")) {

	    GNode vtableNode = (GNode) n.getNode(0);
	    ArrayList vtableList = 
		(ArrayList) vtableNode.getProperty("vtable");

	    System.out.println("========= VTable for class " + 
			       n.getProperty("name") + " ==========");

	    for(int i = 0; i < vtableList.size(); i++) {
		System.out.println(i + "\t" + vtableList.get(i).toString());
	    }

	} // end if
    }

    // FIXME: consistent naming of method
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
		System.out.print(" has vtable");
	    }


	    public void visitDataLayout(GNode n) {
		System.out.println(" has data layout");
	    }
	    
	}.dispatch(classTree);

	System.out.println();
    }

    // ----------------------------------------------------
    // ---------- Getter methods for Translator -----------
    // ----------------------------------------------------

    // returns VTable list for a class
    // @param class Name of class whose VTable you want
    public ArrayList getVTable(String cN) {

	GNode className = getClass(cN);
	GNode classVT = (GNode) (className.getNode(0));
	if(classVT.hasProperty("vtable")) {
	    return (ArrayList) (classVT.getProperty("vtable"));
	}

	return null;
    }


    public ArrayList getDataLayout(String cN) {

	GNode className = getClass(cN);
	GNode classData = (GNode) (className.getNode(1));
	if(classData.hasProperty("data")) {
	    return (ArrayList) (classData.getProperty("data"));
	}

	return null;
    }

    // returns full class tree.  
    // Shouldn't need this if I make appropriate getter methods
    public GNode getClassTree() {
	return classTree;
    }


}
