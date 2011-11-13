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
		printClassTree();
		if(DEBUG) System.out.println("--- End Class Layout Parser\n\n");
    }
    
    // Traverses all dependencies to find and add all classes
    // @param trees Array of all ASTs (main and dependencies)
    public void parseAllClasses(GNode[] trees) {
    	for(int i = 0; i < trees.length; i++) {
			if(trees[i] != null) {
				this.dispatch(trees[i]);
			}
		}
    }
	
	
	// ----------------------------------
	//    Tree Processing
	// ----------------------------------
	
	//Visitor method to dispatch this visitor on a tree
	//@param n The tree to dispatch on.
	public void visit(Node n) {
		for( Object o : n ) {
			if (o instanceof Node) dispatch((Node)o);
		}
	}
    
    // Adds class in proper hierarchal location to classTree
    // Calls methods to addVTable and addDataLayout
    // @param n ClassDeclaration node from a Java AST 
	GNode currentHeaderNode; //global storage variable to enable proper visitor traversal
	String className; 
    public void visitClassDeclaration(GNode n) {
		
		//Setting up the new node for the class tree
		className = n.get(1).toString();
		GNode newChildNode = GNode.create("Class");
		newChildNode.setProperty("name", className);
		
		//Now we need a super class -- every node but object has one, either explicitly or Object by default
		GNode parent = null; 
		if(n.get(3) != null) { // Extends something
			String extendedClass = (String)(n.getNode(3).getNode(0).getNode(0).get(0)); // String name of Parent Class
			parent = getClass(extendedClass); // Append new class as child of Parent
		}
		else { 
			parent = getClass("Object"); // Doesn't extend, add to root (Object)
		}
		
		if(DEBUG) System.out.println( "Visiting new class: " + className + " extends " + parent.getProperty("name") );
		 
		//Building the vtables and data layout
		
		// --- Place vt/dl setup here: --- 
		
		//currentHeaderNode = GNode.create((GNode)parent.getNode(0)); //copy the header node from the parent
		currentHeaderNode = inheritHeader( (GNode)parent.getNode(0) );
		
		//STUFF WE NEED TO DO EVENTUALLY:
		//add the virtual methods and data fields from the parents here -- DONE with the above parent header copy
		//add (or rather, modify) the isa statement at vtable[0] -- will be done in Object forced init - no modification necessary
		
		// --- end setup -----------------
		
		visit(n);
		
		// --- Place vt/dl finalization here: ---
		// FIXME: change the types of explicit this's in the vmethod decls
		newChildNode.add(currentHeaderNode);
		parent.addNode(newChildNode);
    }
	
	// ----------------------------------
	//    Virtual Method Processing
	// ----------------------------------
	
	//Visits the method declarations in a class, adding them as virtual methods to the vtable
	//@param n MethodDeclaration node from a Java AST
	public void visitMethodDeclaration(GNode n) {
		//new VirtualMethodDeclaration: (0) return type, (1) method name, (2) parameters
		
		GNode methodSignature = GNode.create("VirtualMethodDeclaration");
		methodSignature.add(n.get(2)); //return type
		methodSignature.add(n.get(3)); //method name
		GNode formalParameters = deepCopy((GNode)n.getNode(4));
		for( int i = 0; i < formalParameters.size(); i++ ) {
			formalParameters.set(i, formalParameters.getNode(i).getNode(1) ); //this kills the parameter name
		}
		formalParameters.add(0, createTypeNode( className ) );
		//FIXME: redefine the explicit THIS for the correct type?
		methodSignature.add(formalParameters); //parameter types
		
		//Override check here -- if overrides, use vtdecl.set(overrideIndex, methodSig) else just add it
		String methodName = n.get(3).toString();
		int overrideIndex = overridesMethod(methodName, (GNode)currentHeaderNode.getNode(0) );
		if(overrideIndex >= 0) {
			if(DEBUG) System.out.println( "overriding method: " + methodName );
			currentHeaderNode.getNode(0).set(overrideIndex, methodSignature); // overrides, must replace
		}
		else {
			if(DEBUG) System.out.println( "adding method: " + methodName );
			currentHeaderNode.getNode(0).add(methodSignature); //extended, add the method signature to the vtable declaration
		}
		
		//FIXME: also add to the data layout as static methods
		//FIXME: VTABLE CONSTRUCTOR
		//FIXME: FINAL: name mangling
	}
	
	// [INTERNAL]
    // Determines if a Class overrides it's Parent method
    // @param methodName
    // @param parentVTable
    int overridesMethod(String methodName, GNode currentVTable) {
		// Search current VTable to see if overriden and at what index
		for(int i = 1; i < currentVTable.size(); i++) { //start at one to ignore __isa
			if(methodName.equals(currentVTable.getNode(i).get(1).toString())) return i; //string comparison, return indexof
		}
		
		// If not overriden, return -1
		return -1;
    }
	
	// ----------------------------------
	//    Data Layout Processing
	// ----------------------------------
	
	//Visits the field declarations in a class, and adds them to the vtable.
	//@param n FieldDeclaration node from a Java AST.
	public void visitFieldDeclaration(GNode n) {
		int overrideIndex = overridesField(n, (GNode)currentHeaderNode.getNode(1) );
		if( overrideIndex >= 0 ) {
			System.out.println( "overriding field: " + n.getNode(2).getNode(0).get(0).toString() );
			currentHeaderNode.getNode(1).set(overrideIndex, n); //add the data field to the classes' data layout declaration
			//FIXME: is additional processing needed for field overrides? like type checking? 
		}
		else {
			System.out.println( "adding field: " + n.getNode(2).getNode(0).get(0).toString() );
			currentHeaderNode.getNode(1).add(n); //add the data field to the classes' data layout declaration
		}
		//FIXME: add overriding of data fields 
	}
	
	// [INTERNAL]
	// Determines if a new data field declaration has already been declared (by name)
	// @param newField the new field to check
	// @param currentDL a node containing the current data layout
	int overridesField(GNode newField, GNode currentDL) {
		// Search current data layout to see if overriden and at what index
		String newFieldName = newField.getNode(2).getNode(0).get(0).toString();
		String currentComparisonField;
		for(int i = 0; i < currentDL.size(); i++) {
			currentComparisonField = currentDL.getNode(i).getNode(2).getNode(0).get(0).toString(); //this is the comparison field name
			if( newFieldName.equals(currentComparisonField) ) return i;
		}
		
		// If not overriden, return -1
		return -1;
    }
	

    // ----------------------------------------------------
    // ------------- Internal Methods --------------
    // ----------------------------------------------------
	
	// Returns a deep copy of a tree through a simple recursive use of the visitor model with return values 
	// @param n the root node of the tree to copy
	GNode deepCopy(GNode n) {
		GNode retVal = (GNode) new Visitor() {
			public Object visit(GNode n) {
				GNode retVal = GNode.ensureVariable(GNode.create(n));
				while( retVal.size() > 0 ) retVal.remove(0);
				for( Object o : n ) {
					if( o instanceof GNode ) retVal.add( visit((GNode)o) );
					else retVal.add( o ); //FIXME: arbitrary objects added to the copy are still pointers...problem?
				}
				return retVal;
			}
		}.dispatch(n);
		return retVal;
	}
	
	// Using the currently set global className, copies the parents header and makes necessary changes
	// @param parentHeader A node representing the parent classes header structure
	GNode inheritHeader( GNode parentHeader ) {
		//FIXME: make those necessary changes
		GNode copy = deepCopy( parentHeader );
		GNode copyVT = (GNode)copy.getNode(0);
		for( int i = 1; i < copyVT.size(); i++ ) { //start at one to ignore Class __isa;
			copyVT.getNode(i).getNode(2).getNode(0).getNode(0).set(0, className);
		}
		GNode copyDL = (GNode)copy.getNode(1);
		return copy;
	}
			
	//Creates a new type node, either primitive or a 'qualified' class name
	//@param type The type that the new node should specify
	GNode createTypeNode(String type) {
		GNode retVal = GNode.create("Type");
		GNode typeSpecifier;
		if( type.equals("int") || type.equals("float") || type.equals("boolean") ) { //testing for primitive, obvs needs to be expanded
			typeSpecifier = GNode.create( "PrimitiveType" );
		} else {
			typeSpecifier = GNode.create( "QualifiedIdentifier" ); //separating 'qualified' class names from primitives just seems safer for now...
		}
		typeSpecifier.add(type);
		retVal.add(typeSpecifier);
		retVal.add(null);
		return retVal;
	}
	
	// ----------------------------------------------------
    // -------------- Initialization Code -----------------
    // ----------------------------------------------------
    
	
    // Init tree w/Grimm defined classes
    // Nodes have "name" property 
    // Class nodes have a single predefined child node representing the header information, 
	// plus additional children to represent subclasses.
    public void initGrimmTypes() {
		
		// FIXME: Properly hardcode Grimm objects
		
		GNode objectNode = GNode.create("Class");
		objectNode.setProperty("name", "Object");
		//We create a new node that represents the class header information (vtable and data layout) 
		//NOTE: this is the ONLY time it should be manually created, because Object inherits from no one
		GNode classHeaderDeclaration = GNode.create("ClassHeaderDeclaration");
		classHeaderDeclaration.add( objectClassVirtualTable() );
		classHeaderDeclaration.add( objectClassDataLayout() );
		objectNode.add(classHeaderDeclaration );
		
		// FIXME: all following statements need to be replaced with the nodal hardcoded implementation
		// ...which is going to be SO much fun...
		// NOTE: this includes the other two initGrimm...() methods
		GNode stringNode = GNode.create("Class");
		stringNode.setProperty("name", "String");
		
		GNode classNode = GNode.create("Class");
		classNode.setProperty("name", "Class");
		
		GNode arrayNode = GNode.create("Class");
		arrayNode.setProperty("name", "Array");
		
		GNode integerNode = GNode.create("Class");
		integerNode.setProperty("name", "Integer");
		
		classTree = objectNode;
		classTree.add(stringNode);
		classTree.add(classNode);
		classTree.add(arrayNode);
		classTree.add(integerNode);
    }
	
	// Creates a hard-coded virtual table for the object class.
	GNode objectClassVirtualTable() {
		/*
		 TODO: CONSTRUCTOR
		 __Object_VT()
		 : __isa(__Object::__class()),
		 hashCode(&__Object::hashCode),
		 equals(&__Object::equals),
		 getClass(&__Object::getClass),
		 toString(&__Object::toString) {
		 }
		 */
		GNode retVal = GNode.create("VTableDeclaration");
		retVal.add( createSkeletonDataField( "Class", "__isa" ) ); //Class __isa;
		retVal.add( createSkeletonVirtualMethodDeclaration( "int32_t", "hashCode", new String[]{"Object"} )); //int32_t (*hashCode)(Object);
		retVal.add( createSkeletonVirtualMethodDeclaration( "bool", "equals", new String[]{"Object","Object"} )); //bool (*equals)(Object, Object);
		retVal.add( createSkeletonVirtualMethodDeclaration( "Class", "getClass", new String[]{"Object"} )); //Class (*getClass)(Object);
		retVal.add( createSkeletonVirtualMethodDeclaration( "String", "toString", new String[]{"Object"} )); //String (*toString)(Object);
		//retVal.add( initializeVTConstructor( retVal ) );
		return retVal;
	}
	
	// Initializes a brand new virtual table constructor for the specified VTable
	// This should only produce valid results for the base Object class, there is no inheritance analysis
	// @param vTable The vTable to create a constructor for 
	GNode initializeVTConstructor( GNode vTable ) {
		GNode retVal = GNode.create("VTableConstructor");		

		return retVal;
	}
	
	//Creates a hard-coded data layout for the object class.
	GNode objectClassDataLayout() {
		GNode retVal = GNode.create("DataLayoutDeclaration");
		retVal.add( createSkeletonDataField( "__Object_VT*", "__vptr" ) );
		//retVal.add( GNode.create( "DataFieldList" ) );
		//retVal.add( GNode.create( "StaticMethodList" ) );
		retVal.add( createSkeletonStaticDataField( "__Object_VT", "__vtable" ) );
		return retVal;
	}
	
	// Creates a new virtual method header with the specified information
	// Used to force display of the primitive Grimm types, which are prewritten and #included 
	// @param returnType Return type for method
	// @param methodName The name of the new method
	// @param parametersList The list of parameters for the method
	GNode createSkeletonVirtualMethodDeclaration( String returnType, String methodName, String[] parameterTypes ) {
		GNode retVal = GNode.create("VirtualMethodDeclaration");
		retVal.add( createTypeNode( returnType ) );
		retVal.add( methodName ); //method name is just a string still
		GNode params = GNode.create("FormalParameters"); //node for a parameter list
		for( String s : parameterTypes ) {
			params.add( createTypeNode(s) );
		}
		retVal.add( params );
		return retVal;
	}
	
	// Creates a new data field with the specified information
	// Used to force display of the primitive Grimm types, which are prewritten and #included
	// @param type The type of the data field 
	// @param name the name of the data field
	GNode createSkeletonDataField( String type, String name ) {
		GNode retVal = GNode.create( "FieldDeclaration" );
		retVal.add(GNode.create("Modifiers")); //empty value for Modifiers()
		retVal.add( createTypeNode( type ) );
		GNode declrs = GNode.create("Declarators");
		GNode declr = GNode.create("Declarator");
		declr.add( name );
		declr.add( null ); 
		declr.add( null ); //need to fill in these nulls for printer compatibility
		declrs.add( declr );
		retVal.add( declrs );
		return retVal;
	}
	
	// Creates a new STATIC data field with the specified information
	// Used to force display of the primitive Grimm types, which are prewritten and #included
	// @param type The type of the data field 
	// @param name the name of the data field
	GNode createSkeletonStaticDataField( String type, String name ) {
		GNode retVal = createSkeletonDataField( type, name ); //just create a standard data field
		GNode modifiers = GNode.create("Modifiers");
		GNode staticMod = GNode.create("Modifier"); //then add the static keyword
		staticMod.add("static");
		modifiers.add(staticMod);
		retVal.set(0,modifiers);
		return retVal;
	}
	
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
    
	
    // ----------------------------------------------------
    // ---------- Getter methods for Translator -----------
    // ----------------------------------------------------
	
	
    // Returns VTable list for a class
    // @param cN Class Name
    public GNode getVTable(String cN) {
		GNode className = getClass(cN);
		GNode classVT = (GNode)(className.getNode(0).getNode(0));
		return classVT;
    }
	
    // Returns Data Layout list for a class
    // @param cN Class Name
    public GNode getDataLayout(String cN) {
		GNode className = getClass(cN);
		GNode classData = (GNode) (className.getNode(0).getNode(1));
		return classData;
    }
	
    // Prints a list of all classes
    public void printClassTree() {
		
		System.out.println("\n\n--- Class Tree");
		new Visitor () {
			public void visit(GNode n) {
				for( Object o : n) {
					if (o instanceof Node) dispatch((GNode)o);
				}
			}
			
			GNode parent = null;
			public void visitClass(GNode n) {
				System.out.print("Class " + n.getStringProperty("name"));
				if( parent == null ) System.out.println( " is the root of the Class hierarchy." );
				else System.out.println( " extends " + parent.getStringProperty("name"));
				GNode temp = parent;
				parent = n;
				visit(n);
				parent = temp;
			}
			
			public void visitVTableDeclaration(GNode n) {
				System.out.println(" has vtable");
			}
			
			public void visitDataLayoutDeclaration(GNode n) {
				System.out.println(" has data layout");
			}
			
		}.dispatch(classTree);
		
		System.out.println();
    }
	
}
