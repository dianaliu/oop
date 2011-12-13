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
	
    // Constructor - initializes the CLP instance and begins processing
    // @param n node for a class declaration
    public ClassLayoutParser(GNode[] ast, boolean db) {
		DEBUG = db;
		if(DEBUG) System.out.println("--- Begin Class Layout Parser\n");
		
		
		
		
		//preinitialize the hardcoded Grimm types
		initGrimmTypes();
		
		//parse all classes to be translated
		for(int i = 0; i < ast.length; i++) {
			if(ast[i] != null) {
				this.dispatch(ast[i]);
			}
		}
		
		
		if(DEBUG) sequenceGenome();
		if(DEBUG) printClassTree();
		if(DEBUG) System.out.println("--- End Class Layout Parser\n");
		//
		String damar[] = new String[2];
		damar[0] = "muslum";
		//degisken adi, metod adi, giren cesitleri
		kirimbaba("arabesk", "orhan", damar);
    }
    
	// ----------------------------------
	//         Tree Processing
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
	GNode currentHeaderNode; //global storage variable to point to the current working header node (vtable and data layout)
	String className; //global current class name
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
		currentHeaderNode = inheritHeader( (GNode)parent.getNode(0) );
		visit(n); // <-- VISITING ALL CHILDREN OF THIS CLASS (DATA FIELDS, METHODS, CONSTRUCTORS, ...)
		newChildNode.add(currentHeaderNode);
		newChildNode.setProperty( "parentClassNode", parent);
		parent.addNode(newChildNode);
    }
    
	// ----------------------------------
	//    Virtual Method Processing
	// ----------------------------------
	
	// Visits the method declarations in a class, adding them as virtual methods to the vtable
	// @param n MethodDeclaration node from a Java AST
	public void visitMethodDeclaration(GNode n) {
		
	    // new VirtualMethodDeclaration: (0) return type, (1) method name, (2) parameters
	    
	    GNode methodSignature = GNode.create("VirtualMethodDeclaration");
	    methodSignature.add(n.get(2)); //return type
	    methodSignature.add(n.get(3)); //method name
	    GNode formalParameters = deepCopy((GNode)n.getNode(4));
	    for( int i = 0; i < formalParameters.size(); i++ ) {
			formalParameters.set(i, formalParameters.getNode(i).getNode(1) ); // this kills the parameter name
	    }
		
	    int over = overridesMethod(n, (GNode)currentHeaderNode.getNode(0) );
	    // Don't want to pass thisClass if a method is overridden
	    if(over > 0)
			formalParameters.add(0, createTypeNode( className ) );
		
	    methodSignature.add(formalParameters); //parameter types
	    
	    //Override check here -- if overrides, use vtdecl.set(overrideIndex, methodSig) else just add it
	    String methodName = n.get(3).toString();
	    
	    if ("main".equals(methodName)) return; // Don't want main method
	    
	    int overrideIndex = overridesMethod(n, (GNode)currentHeaderNode.getNode(0) );
	    if( overrideIndex >= 0 ) {
			if(DEBUG) System.out.println( "overriding method: " + methodName );
			currentHeaderNode.getNode(0).set(overrideIndex, methodSignature); // overrides, must replace
			
			//changing the constructor too
			int index = currentHeaderNode.getNode(0).size()-1;
			GNode vtConstructorPtrList = (GNode)currentHeaderNode.getNode(0).getNode(index).getNode(4);
			GNode newPtr = GNode.create( "vtMethodPointer" );
			newPtr.add( n.get(3) ); //method name
			newPtr.add( createTypeNode( "__"+className ) ); //target
			newPtr.add( GNode.create( "FormalParameters" ) );
			// FIXME: Add PointerCast to this Class, see below for ex.
			vtConstructorPtrList.set( overrideIndex, newPtr );
	    }
	    else {
			// FIXME: If  new method, don't pass self Class
			// as parameter, use for calling class
			if(DEBUG) System.out.println( "adding method: " + methodName );
			int index = currentHeaderNode.getNode(0).size()-1; //add it before the constructor, which is last(index+1)
			currentHeaderNode.getNode(0).add(index, methodSignature); //extended, add the method signature to the vtable declaration
			
			//adding it to the constructor too
			GNode vtConstructorPtrList = (GNode)currentHeaderNode.getNode(0).getNode(index+1).getNode(4);
			// FIXME: Need to Flesh out for overriden as well?
			GNode newPtr = GNode.create( "vtMethodPointer" );
			newPtr.add( n.get(3) ); //method name
			newPtr.add( createTypeNode( "__"+className ) ); //Calling Class
			newPtr.add( formalParameters );
			//	newPtr.add( GNode.create( "FormalParameters" ) );
			// add Pointer cast node
			GNode pCast = GNode.create("PointerCast");
			pCast.add(n.getNode(2)); // return Type
			// if return type is String, int, etc. then do TYPE(*)(TYPE)
			// When it's a new method, you don't need to cast from this.Class
			// Instead, for @ least one case, String(*)(String)
			// I'm not sure what the general case is
			pCast.add(n.getNode(2)); // return Type
			//		pCast.add(createTypeNode(className)); // this Class
			newPtr.add(pCast);
			vtConstructorPtrList.add( newPtr );
			
			//adding it to the data layout
			GNode dataLayoutMethList = (GNode)currentHeaderNode.getNode(1).getNode(3);
			GNode hdr = GNode.create( "StaticMethodHeader" );
			hdr.add( n.get(2) ); //return type
			hdr.add( n.get(3) ); //method name
			hdr.add( formalParameters ); //params
			dataLayoutMethList.add( hdr );
	    }
		//FIXME: FINAL: name mangling
	}
	
	// [INTERNAL]
    // Determines if a Class overrides it's Parent method
    // @param MethodDeclaration node
    // @param parentVTable
	// @return the index of the method if overrided, or -1 if not overrided
    int overridesMethod(GNode  n, GNode currentVTable) {
		String methodName = n.get(3).toString();
		// Due to method overloading, must check method name and parameters!
		
		// Search current VTable to see if overriden and at what index
		for(int i = 1; i < currentVTable.size()-1; i++) { //start at one to ignore __isa, end at size-1 to ignore constructor
			if(methodName.equals(currentVTable.getNode(i).get(1).toString())) {
				if (n.getNode(4) == currentVTable.getNode(i).getNode(2)) // Compare Formal Parameters - equals()?
					return i; //string comparison, return indexof
			}
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
		int overrideIndex = overridesField(n, (GNode)currentHeaderNode.getNode(1).getNode(1) );
		if( overrideIndex >= 0 ) {
			if(DEBUG) System.out.println( "overriding field: " + n.getNode(2).getNode(0).get(0).toString() );
			currentHeaderNode.getNode(1).getNode(1).set(overrideIndex, n); //add the data field to the classes' data layout declaration
		}
		else {
			if(DEBUG) System.out.println( "adding field: " + n.getNode(2).getNode(0).get(0).toString() );
			currentHeaderNode.getNode(1).getNode(1).add(n); //add the data field to the classes' data layout declaration
		}
	}
	
	// [INTERNAL]
	// Determines if a new data field declaration has already been declared (by name)
	// @param newField the new field to check
	// @param currentDL a node containing the current data layout field list
	// @return the index of the field if overrided, or -1 if not overrided
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
	
	// -----------------------------------
	//       Constructor Processing
	// -----------------------------------
	
	public void visitConstructorDeclaration(GNode n) {
	    if(DEBUG) System.out.println( "***constructor found in class " + className );
	    GNode constructorSignature = GNode.create("ConstructorHeader");
	    //	    constructorSignature.add(className);
	    constructorSignature.add( n.get(2) ); //constructor name (just the class name)
	    constructorSignature.add( n.get(3) ); //parameters
	    currentHeaderNode.getNode(1).getNode(2).add(constructorSignature); //add the signature to the constructor list
	}
    
    
    // ----------------------------------------------------
    // ------------- Internal Methods --------------
    // ----------------------------------------------------
	
	// Returns a deep copy of a tree through a simple recursive use of the visitor model with return values 
	// @param n the root node of the tree to copy
	// @return A complete deep copy of node n
	GNode deepCopy(GNode n) {
	    GNode retVal = (GNode) new Visitor() {
		    public Object visit(GNode n) {
				GNode retVal = GNode.ensureVariable(GNode.create(n));
				while( retVal.size() > 0 ) retVal.remove(0);
				for( Object o : n ) {
					if( o instanceof GNode ) retVal.add( visit((GNode)o) );
					else retVal.add( o ); //arbitrary objects don't need to be copied because they would just be replaced
				}
				return retVal;
		    }
		}.dispatch(n);
	    return retVal;
	}
    
    // Using the currently set global className, copies the parents header and makes necessary changes
    // @param parentHeader A node representing the parent classes header structure
    // @return The new header node for the child class with appropriate modifications
    GNode inheritHeader( GNode parentHeader ) {
		GNode copy = deepCopy( parentHeader );
		GNode copyVT = (GNode)copy.getNode(0);
		int size = copyVT.size();
		copyVT.getNode(size-1).getNode(4).getNode(0).set(0, "__"+className ); //changing the Class __isa pointer in the constructor;
		for( int i = 1; i < size-1; i++ ) { //start at one to ignore Class __isa, end at size-1 to ignore constructor
			GNode thisVirtualMethod = (GNode)copyVT.getNode(i);
			thisVirtualMethod.getNode(2).getNode(0).getNode(0).set(0, className);
		}
		// Change constructor here: updating the casts for non overrided methods
		GNode vtConstructorPointerList = (GNode)copyVT.getNode(size-1).getNode(4);
		for( int i = 1; i < vtConstructorPointerList.size(); i++ ) { // start at one to ignore __isa
			GNode thisPointer = (GNode)vtConstructorPointerList.getNode(i);
			GNode caster = GNode.create("PointerCast");
			caster.add( copyVT.getNode(i).getNode(0) ); //return value
			caster.add( copyVT.getNode(i).getNode(2) ); //parameters
			if( thisPointer.size() >= 4 ) thisPointer.set(3, caster);
			else thisPointer.add( caster );
		}
		
	    GNode copyDL = (GNode)copy.getNode(1);
	    copyDL.set(0, createSkeletonDataField( "__"+className+"_VT*", "__vptr" )); //setting the right vtable pointer name
	    GNode constructorList = GNode.create("ConstructorHeaderList");
	    constructorList.add(0, className);
	    copyDL.set(2, constructorList);//clear out the constructor list
	    GNode statMethList = (GNode)copyDL.getNode(3);
	    for( Object o : statMethList ) { //changing the 'this' parameter types in the static data layout methods
			((GNode)o).getNode(2).getNode(0).getNode(0).set(0, className); //ugh is that ugly or what?
	    }
	    copyDL.set(4, createSkeletonStaticDataField( "__"+className+"_VT", "__vtable" ));
		return copy;
	}
	
	// Creates a new type node, either primitive or a 'qualified' class name
	// @param type The type that the new node should specify
	// @return the type node
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
	
	// ---------------------------------------------------- //
    // --------------- Overloading Coding ----------------- //
    // ---------------------------------------------------- //
	
	// Tests the inheritance hierarchy for subclass relationships
	// @param parentClassName The name of the parent class
	// @param childClassName The name of the child class
	// @return -1 if no relationship, 0 if parentClass == childClass, levels of inherited distance otherwise
	public int paternityTest( String parentClassName, String childClassName ) {
		GNode parentClassNode = getClass(parentClassName);
		GNode childClassNode = getClass(childClassName);
		int levels = 0; 
		while( childClassNode != null ) {
			System.out.println( "ccn: " + childClassNode.getProperty("name") + " : ");
			if( parentClassNode.equals(childClassNode) ) return levels;
			childClassNode = getSuperclass( childClassNode.getStringProperty("name") );
			levels++;
		}
		return -1; // you are NOT the father.
	}
	
	
	// degisken adi, metod adi, giren cesitleri
	// thing1.getNumber(int bok)
	// duydum = object, ki = getNumber, unutmussun = bok
	
	// once dudum un classini bul, ardindan vtable i al, ardindan o table dan soz konusu metodun datasini al
	// sonra dagiren cikanla karsilastir datasini alirken bir kac kali bir arraye almak isteyebilirsin
	// ondan sonra da ki absolute superiority gosteriyorsa onu sec

	public String kirimbaba(String duydum, String ki, String unutmussun[]) {
		duydum = "Testere";
		String result = "";
		
		int mCount = 0;
		int a = 6;
		Node ana = getVTable(duydum).getNode(a);

		while(getVTable(duydum).getNode(a).getString(1) != null) {
			ana = getVTable(duydum).getNode(a);
			if(ana.getNode(2).size() == unutmussun.length && ki.equals(ana.getString(1))) {
				mCount++;
			}
			a++;
		}
		
		System.out.println(" Number of methods with same name " + mCount);
		String damar[][] = new String[mCount][unutmussun.length];
		int count = 0;
		a = 6;
		
		while(getVTable(duydum).getNode(a).getString(1) != null) {
			ana = getVTable(duydum).getNode(a);
			if(ana.getNode(2).size() == unutmussun.length && ki.equals(ana.getString(1))) {
				for(int k = 0; k < ana.getNode(2).size(); k++) {
					damar[count][k] = ana.getNode(2).getNode(k).getNode(0).getString(0);
				}
				count++;
			}
			a++;
		}
		
		
		
		// unutmussun ve nuhtelif dmarlri karsilastir ondan sonra en kiyak olani sec
		// kod ise yarar bir pust oldugu icin mutlaka birini secmek meccburiyetinndesin
		
		// butun degerlerde herife ulasana dek getSuperClass yap sonra degerleri tut ve en krali al
		String kral[] = new String[mCount];
		int netice = 100;
		for(int j = 0; j < unutmussun.length; j++) {
			int g = 0;
			for(int p = 0; p < mCount; p++) {
				
				String dene = "";/*
				while (dene != unutmussun[j]) {
					dene = getSuperclassName(damar[p][j]);
					g++;
				}*/
				if(g < netice) {
					netice = g;
					kral[j] = "" + p;
				}
				
			}
		}
		for(int j = 0; j < kral.length; j++) {
			System.out.println(kral[j]);
		}
		
		return result;
			
		}
		
		
	
	
	//[DEBUG - INTERNAL]
	public void sequenceGenome() {
		new Visitor () {
			public void visit(GNode n) {
				for( Object o : n) {
					if (o instanceof Node) dispatch((GNode)o);
				}
			}
			
			public void visitClass(GNode n) {
				final String parentName = n.getStringProperty("name");
				new Visitor () {
					public void visit(GNode n) {
						for( Object o : n) {
							if (o instanceof Node) dispatch((GNode)o);
						}
					}
					
					public void visitClass(GNode n) {
						String childName = n.getStringProperty("name");
						
						System.out.println( "Testing " + parentName + " and " + childName + " : " + paternityTest(parentName, childName) );
						
						visit(n);
					}
				}.dispatch(classTree);
				visit(n);
			}
		}.dispatch(classTree);
	}
	
	// 
	// @param methodNode
	// @return 
	public String mangleMethod( GNode methodNode ) {
		return null;
	}
	
	// ---------------------------------------------------- //
    // -------------- Initialization Code ----------------- //
    // ---------------------------------------------------- //
    
	
    // Init tree w/Grimm defined classes
    // Nodes have "name" property 
    // Class nodes have a single predefined child node representing the header information, 
	// plus additional children to represent subclasses.
    public void initGrimmTypes() {
		GNode objectNode = GNode.create("Class");
		objectNode.setProperty("name", "Object");
		//We create a new node that represents the class header information (vtable and data layout) 
		//NOTE: this is the ONLY time it should be manually created, because Object inherits from no one
		GNode classHeaderDeclaration = GNode.create("ClassHeaderDeclaration");
		classHeaderDeclaration.add( objectClassVirtualTable() );
		classHeaderDeclaration.add( objectClassDataLayout() );
		objectNode.add(classHeaderDeclaration);
		
		// FIXME: Properly hardcode all these other Grimm objects
		
		GNode stringNode = GNode.create("Class");
		stringNode.setProperty("name", "String");
		stringNode.setProperty("parentClassNode", objectNode);
		
		GNode classNode = GNode.create("Class");
		classNode.setProperty("name", "Class");
		classNode.setProperty("parentClassNode", objectNode);
		
		GNode arrayNode = GNode.create("Class");
		arrayNode.setProperty("name", "Array");
		arrayNode.setProperty("parentClassNode", objectNode);
		
		GNode integerNode = GNode.create("Class");
		integerNode.setProperty("name", "Integer");
		integerNode.setProperty("parentClassNode", objectNode);
		
		classTree = objectNode;
		classTree.add(stringNode);
		classTree.add(classNode);
		classTree.add(arrayNode);
		classTree.add(integerNode);
    }
	
	// Creates a hard-coded virtual table for the object class.
	// @return the complete virtual table for Object
	GNode objectClassVirtualTable() {
		
	    GNode retVal = GNode.create("VTableDeclaration");
	    retVal.add( createSkeletonDataField( "Class", "__isa" ) ); //Class __isa;
	    // For delete
	    retVal.add( createSkeletonVirtualMethodDeclaration("void", "__delete", new String[]{"__Object*"}) );
	    retVal.add( createSkeletonVirtualMethodDeclaration( "int32_t", "hashCode", new String[]{"Object"} ));      // int32_t x (*hashCode)(Object);
	    retVal.add( createSkeletonVirtualMethodDeclaration( "bool", "equals", new String[]{"Object","Object"} )); // bool (*equals)(Object , Object);
	    retVal.add( createSkeletonVirtualMethodDeclaration( "Class", "getClass", new String[]{"Object"} ));      // Class (*getClass)(Object);
	    retVal.add( createSkeletonVirtualMethodDeclaration( "String", "toString", new String[]{"Object"} ));    // String (*toString)(Object);
	    retVal.add( initializeVTConstructor( retVal ) ); //adding the constructor
	    return retVal;
	}
	
	// Initializes a brand new virtual table constructor for the specified VTable
	// This should only produce valid results for the base Object class, there is no inheritance analysis
	// @param vTable The vTable to create a constructor for (should only be OBJECT)
	// @return the virtual table constructor for the vtable (SHOULD ONLY BE OBJECT)
	GNode initializeVTConstructor( GNode vTable ) {
	    GNode retVal = GNode.create("VTConstructorDeclaration");
	    retVal.add( GNode.create( "Modifiers" ) ); //empty modifiers
	    retVal.add( null ); //index 1 null
	    
	    // Proper className in vtable constructor
	    //	    retVal.add("__" + className +  "_VT");
	    retVal.add( "__Object_VT" ); //name of constructor
	    retVal.add( GNode.create( "FormalParameters" ).add(null) ); //no parameters
	    final GNode methodPtrList = GNode.create( "vtMethodPointersList" );
	    methodPtrList.add( GNode.create( "ClassISAPointer" ).add( "__Object" ) ); //hard coded __isa(__Object...) pointer
	    new Visitor() { //visit all the virtual method declarations in the vtable and make an appropriate pointer in the constructor
			public void visit( Node n ) {
				for( Object o : n ) if (o instanceof GNode ) dispatch((GNode)o);
			}
			public void visitVirtualMethodDeclaration( GNode n ) {
				GNode newPtr = GNode.create( "vtMethodPointer" );
				// 0 - method name
				// 1 - target Object
				// 2 - params if any
				// 3 - pointer cast (if needed)
				newPtr.add( n.get(1) ); //0 - method name
				newPtr.add( createTypeNode( "__Object" ) ); //1 - target object
				newPtr.add( GNode.create( "FormalParameters" ) ); //parameters, but there are none so unneeded?
				methodPtrList.add( newPtr );
			}
	    }.dispatch(vTable);
	    retVal.add( methodPtrList );
	    retVal.add( GNode.create( "Block" ) ); //empty code block is needed to complete the constructor, all real initialization is using the default constructor
	    return retVal;
	}
    
    // Creates a hard-coded data layout for the object class.
	// @return the data layout for Object	
    GNode objectClassDataLayout() {
		GNode retVal = GNode.create("DataLayoutDeclaration");
		retVal.add( createSkeletonDataField( "__Object_VT*", "__vptr" ) );
		retVal.add( GNode.create( "DataFieldList" ) ); //simple node to contain the data fields
		GNode constructorList =  GNode.create("ConstructorHeaderList");
		constructorList.add(0, className);
		retVal.add( constructorList); //simple node to contain constructors
		GNode objMethHeaders = GNode.create( "MethodHeaderList" ); //simple node to contain method headers
		//adding static methods:
		objMethHeaders.add( createSkeletonStaticMethodHeader( "int32_t", "hashCode", new String[]{"Object"} ) ); //int32_t (*hashCode)(Object);
		objMethHeaders.add( createSkeletonStaticMethodHeader( "bool", "equals", new String[]{"Object","Object"} ) ); //bool (*equals)(Object, Object);
		objMethHeaders.add( createSkeletonStaticMethodHeader( "Class", "getClass", new String[]{"Object"} ) ); //Class (*getClass)(Object);
		objMethHeaders.add( createSkeletonStaticMethodHeader( "String", "toString", new String[]{"Object"} ) ); //String (*toString)(Object);
		
		retVal.add( objMethHeaders );
		retVal.add( createSkeletonStaticDataField( "__Object_VT", "__vtable" ) );
		return retVal;
	}
	
	// Creates a new 'skeleton' virtual method declaration with the specified information
	// Used to force display of the primitive Grimm types, which are prewritten and #included instead of traversed in an AST tree
	// @param returnType Return type for method
	// @param methodName The name of the new method
	// @param parametersList The list of parameters for the method
	// @return the virtual method declaration node
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
    
	// Creates a new STATIC 'skeleton' virtual method header with the specified information
	// Used to force display of the primitive Grimm types, which are prewritten and #included instead of traversed in an AST tree
	// @param returnType Return type for method
	// @param methodName The name of the new method
	// @param parametersList The list of parameters for the method
	// @return the static method header node
    GNode createSkeletonStaticMethodHeader( String returnType, String methodName, String[] parameterTypes ) {
		GNode retVal = GNode.create("StaticMethodHeader");
		retVal.add( createTypeNode( returnType ) );
		retVal.add( methodName ); //method name is just a string still
		GNode params = GNode.create("FormalParameters"); //node for a parameter list
		for( String s : parameterTypes ) {
			params.add( createTypeNode(s) );
		}
		retVal.add( params );
		return retVal;
    }
    
    // Creates a new 'skeleton' data field with the specified information
    // Used to force display of the primitive Grimm types, which are prewritten and #included instead of traversed in an AST tree
    // @param type The type of the data field 
    // @param name the name of the data field
	// @return the field declaration node
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
    
    // Creates a new STATIC 'skeleton' data field with the specified information
    // Used to force display of the primitive Grimm types, which are prewritten and #included instead of traversed in an AST tree
    // @param type The type of the data field 
    // @param name the name of the data field
	// @return the field declaration with a 'static' modifier
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
	// @return its name
    public String getName(GNode n) {
		return n.getStringProperty("name");
		
    }
    
    // Returns a Class node from the classTree
    // @param sc name of the Class desired
    // @return the appropriate class node
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
    
    //Returns a Class node representing the parent class of the specified class name
	//@param sc name of the Class for which the parent is needed.
	//@return the node of the superclass
	public GNode getSuperclass(String sc) {
		GNode child = getClass(sc);
		if (child.getProperty("name").equals("Object")) return null;
		return (GNode)child.getProperty("parentClassNode");
	}
	
	//Returns a Class node representing the parent class of the specified class node
	//@param n class node
	//@return the node of the superclass
	public GNode getSuperclass(GNode n) {
		return getSuperclass( n.getStringProperty("name") );
	}
	
    // ----------------------------------------------------
    // ---------- Getter methods for Translator -----------
    // ----------------------------------------------------
	
	
    // Returns VTable list for a class
    // @param cN Class Name
	// @return the classes vtable node
    public GNode getVTable(String cN) {
		GNode className = getClass(cN);
		GNode classVT = (GNode)(className.getNode(0).getNode(0));
		return classVT;
    }
	
    // Returns Data Layout list for a class
    // @param cN Class Name
	// @return the classes data layout node
    public GNode getDataLayout(String cN) {
		GNode className = getClass(cN);
		GNode classData = (GNode) (className.getNode(0).getNode(1));
		return classData;
    }
	
	//Returns the name of the super class of the specified class, or null if class name is Object (the root class)
	//@param cN the Class name
	//@return the string name of the superclass
	public String getSuperclassName(String cN) {
		GNode sooper = getSuperclass(cN);
		if( sooper == null) return "Object";
		return (String)sooper.getProperty("name");
	}
	
    // Prints a list of all classes for debugging purposes
    public void printClassTree() {
		
		System.out.println("\n\n--- Class Tree");
		new Visitor () {
			public void visit(GNode n) {
				for( Object o : n) {
					if (o instanceof Node) dispatch((GNode)o);
				}
			}
			
			public void visitClass(GNode n) {
				System.out.print("Class " + n.getStringProperty("name"));
				String superClass = getSuperclassName(n.getStringProperty("name"));
				/*if( parent == null ) System.out.println( " is the root of the Class hierarchy." );
				 else*/System.out.println( " extends " + superClass/*(superClass==null?superClass:"")*/);
				//GNode temp = parent;
				//parent = n;
				visit(n);
				//parent = temp;
			}
		}.dispatch(classTree);
		System.out.println();
    }
	
}
