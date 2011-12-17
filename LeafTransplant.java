package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

import xtc.util.Runtime;

import java.util.Iterator;

/*
 * Builds a CPP AST tree using a Java AST tree
 * Uses new types of nodes and new tree structure
 *
 */


interface CPPUtil {
    // Custom GNode types:
    public static final String kHeadDec = "HeaderDeclaration";
    public static final String kImplDec = "ImplementationDeclaration";
    public static final String kStrmOut = "StreamOutputList";
	
    // xtc GNode types:
    public static final String kCmpStmt = "CompoundStatement";
    public static final String kDecl = "Declaration";
    public static final String kDeclSpef = "DeclarationSpecifiers";
    public static final String kFuncDecltor = "FunctionDeclarator";
    public static final String kFuncDef = "FunctionDefinition";
    public static final String kInitDecl = "InitializedDeclarator";
    public static final String kInitDeclList = "InitializedDeclaratorList";
    public static final String kInt = "Int";
    public static final String kPrimID = "PrimaryIdentifier";
    public static final String kPtr = "Pointer";
    public static final String kRoot = "TranslationUnit";
    public static final String kSimpDecl = "SimpleDeclarator";
    public static final String kStrLtrl = "StringLiteral";
    public static final String kStruct = "StructureTypeDefinition";
    public static final String kStructDecl = "StructureDeclaration";
    public static final String kStructDeclList = "StructureDeclarationList";
    public static final String kTypedef = "TypedefSpecifier";
	

}

public class LeafTransplant extends Visitor implements CPPUtil {
    

    public static boolean DEBUG = false;


    GNode javaTree;
    GNode cppTree;
    ClassLayoutParser clp;
    
    // Pros/Cons of global variables? Too bad!
    String className;
    GNode classImplementation;
    GNode expressionStatement;
    GNode newClassExpression;


    GNode thisClassDataLayoutStructDeclList; 
    GNode thisClassVTableStructDeclList;
    

    // ------------------------------------------
    // ------------- Constructor ----------------
    // ------------------------------------------

    // @param clp To gain access to vtables and data layouts
    // @param javaAST
    public LeafTransplant(ClassLayoutParser clp, GNode javaAST, boolean db) { 
		this.cppTree = GNode.create("TranslationUnit");
		this.javaTree = javaAST;
		this.clp = clp;
		DEBUG = db;
		
		// Create a CPP tree as we visit the Java AST
		createCPPTree();
    } 
    
    // ------------------------------------------
    // --------- Begin CPP Translation  ---------
    // ------------------------------------------


    public void createCPPTree() {
	// We add nodes to the CPP AST in 3 stages:
	// ImportDeclaration(s), ClassDeclaration - for header, 
	// and ClassDeclaration - for body nodes.

	// TODO: Vtable lookups at method invocation

	new Visitor() {

	    public void visitImportDeclaration(GNode n) {
       		// Add import node to cpp tree
		addImportNode(n);
	    }

	    public void visitClassDeclaration(GNode n) {
		// Build and add header node
		translateClassDeclaration(n);

		// Build and add implementation node
   		translateClassBody(n);
       
	    }
	    
	  
	    public void visit(GNode n) {
		// Need to override visit to work for GNodes
		for( Object o : n) {
		    if (o instanceof Node) dispatch((GNode)o);
		}
	    }
	    
	}.dispatch(javaTree);

	cppTree.add(className);
    }

    // ------------------------------------------
    // ----- Translate ImportDeclarations  ------
    // ------------------------------------------

    // Global variable required to ensure placement of ImportDeclarations 
    // at beginning of CPP AST
    GNode importDeclarations = GNode.create("ImportDeclarations");

    // Creates corresponding CPP import nodes
    // TODO: Printer will add appropriate syntax
    // @param n ImportDeclaration node from Java AST
    public void addImportNode (GNode n) {

	GNode importNode  = n;
	importDeclarations.add(importNode);

    }

    // ------------------------------------------
    // -------- Build HeaderDeclaration ---------
    // ------------------------------------------

    // At ClassDeclaration, we build hNode to hold info. for the header file
    // @param n ClassDeclaration node from Java AST
    public void translateClassDeclaration (GNode n) {

	// TODO: Use className to name output files and other hacks
	className = n.get(1).toString();

	// hNode contains all information for the .h file to be printed
	GNode hNode = buildHeader(n);
	cppTree.add(hNode);

	// templateNodes has the same information as CustomClass nodes.
	// It is built at the same time as the header, but must reside in a 
	// a different branch as it is in a different namespace __rt.
	GNode tNode = templateNodes;
	cppTree.add(tNode);
	
    }

    // Creates the header node for a Class
    // @param n ClassDeclaration node from Java AST
    GNode buildHeader(GNode n) {
	// Nodes are created "inside out" from the leaves up

	GNode hNode = GNode.create("HeaderDeclaration"); 

	GNode typedef = buildTypedef(n);
	hNode.addNode(typedef);

	GNode dataLayout = buildDataLayout(n);
	hNode.addNode(dataLayout);

	GNode vtable = buildVTable(n);
	hNode.addNode(vtable);

	// If there are any custom array types, declare them
	GNode arrayTemplates = findArrays(n);
	hNode.addNode(arrayTemplates);

	GNode constructors = findConstructors(n);
	hNode.addNode(constructors);
      
	return hNode;
    }

    // Build CPP Data Layout nodes to create struct __Class { }
    // @param n ClassDeclaration node from Java AST
    public GNode buildDataLayout(GNode n) {
	// Nodes are created "inside out" from the leaves up
       
	// Populate our Data Layout with information
    	GNode dataDeclarationList = GNode.create("StructureDeclarationList");
	GNode dl = clp.getDataLayout(className);

	// Copy data layout members to StructureDeclarationList
	for (Iterator<?> iter = dl.iterator(); iter.hasNext(); ) {
	    dataDeclarationList.add(iter.next());  
	}

	// -------------------------------------------------------------------

	// Build the skeleton of the Data Layout struct
	GNode structTypeDef = 
	    GNode.create("StructureTypeDefinition", "DataLayout", className, 
			 dataDeclarationList, null);
       	
	//	GNode declarationSpecifiers = GNode.create("DeclarationSpecifiers", 
	//						   structTypeDef);


	GNode dataLayout = GNode.create("Declaration", structTypeDef);


	return dataLayout;
    }
    
    // Build CPP VTable nodes to create struct __Class_VT { }
    // @param n ClassDeclaration node from Java AST
    public GNode buildVTable(GNode n) {
	// Nodes are created "inside out" from the leaves up

	GNode vtableDeclarationList = GNode.create("StructureDeclarationList");
	GNode vt = clp.getVTable(className);

	// Copy vtable members to StructureDeclarationList
	for (Iterator<?> iter = vt.iterator(); iter.hasNext(); ) {
	    vtableDeclarationList.add(iter.next());  
	}
	
	// --------------------------------------------------------------------

	// Build skeleton of VTable struct
	GNode vtableStructDefinition = 
	    GNode.create("StructureTypeDefinition", "VTable", className, 
			 vtableDeclarationList, null);

	//	GNode vtableDeclarationSpecifiers = 
	//	    GNode.create("DeclarationSpecifiers", vtableStructDefinition);

	GNode vtable = 
	    GNode.create("Declaration", vtableStructDefinition);
       
	return vtable;
    }

    // Build CPP typedef node to create typedef __Class* Class;
    // Typedefs are declarations for Objects 
    // @param n ClassDeclaration node from Java AST
    public GNode buildTypedef(GNode n) {
	// Nodes are created "inside out" from the leaves up

	GNode simpleDeclarator = GNode.create("SimpleDeclarator", className);
	
	GNode initializedDeclarator =
	    GNode.create("InitializedDeclarator", null, simpleDeclarator, 
			 null,null, null);
	    
	GNode initializedDeclaratorList = 
	    GNode.create("InitializedDeclaratorList", initializedDeclarator);
	    
	// ----------------------------------------------------------------
	    
	GNode typedefSpecifier = GNode.create("TypedefSpecifier");

	GNode classIdentifier = 
	    createPrimaryIdentifier(className);

	GNode td = GNode.create("TypedefDeclaration", 
				typedefSpecifier, classIdentifier);
	
	// ----------------------------------------------------------------

	GNode forwardDeclaration = GNode.create("ForwardDeclaration", 
						initializedDeclaratorList, td);

	GNode typedef = GNode.create("Declaration", forwardDeclaration);

	/**
	GNode typedef = 
	    GNode.create("Declaration", null, typedefDeclarationSpecifiers, 
		     initializedDeclaratorList);
	**/

	return typedef;
    }

    // Find all arrays of "custom" type declared.  If so, generate
    // nodes so we may generate nodes to specialize the array template
    // by adding Class information and create __class() method
    // "Custom" types are anything not ints, Objects, and Strings which Grimm
    // made for us.  This includes multi-d arrays
    //
    // @param n Java ClassDeclaration Node

    // Global node to allow access from Visitor 
    GNode customClasses = GNode.create("CustomClasses");
    GNode templateNodes = GNode.create("ArrayTemplates");
    public GNode findArrays(GNode n) {
	
	// To generate the template specialization, we need to create a new 
	// Class object.  The class constructor is below.  He only passes the 
	// first 3 parameters
	/*
	  __Class(String name,
              Class parent,
              Class component = (Class)__rt::null(),
              bool primitive = false);
	*/

	// Hack: To be accessible from Visitor
	final GNode classDeclaration = n;

	new Visitor() {
	    
	    public void visitFieldDeclaration(GNode n) {
       
		String qID = "";
		int dim = 1;

		// 1. Determine if we're declaring an Array
		if(n.size() >= 2 && n.getNode(1).size() >= 2) {
		    
		    if( n.getNode(1).getNode(1) != null &&
			n.getNode(1).getNode(1).hasName("Dimensions") ) {

		

			// Found an array declaration
			GNode dims = (GNode) n.getNode(1).getNode(1);
			// qID holds the component Type of the array
			qID = n.getNode(1).getNode(0).getString(0);

			if(DEBUG) System.out.println("-- Found ArrayDeclaration of Type  "
					   + qID);

			if( dims.size() > 1 || 
			    isCustomType(classDeclaration, qID)) {

			    System.out.println("\t Array customization needed");

			    // Customization is needed if it's a 
			    // multi-dimensional array or it has custom 
			    // component types

			    // First, let's find the parent class
			    GNode parent = GNode.create("ParentType");
			    String pID = clp.getSuperclassName(qID);
			    parent.add(clp.createTypeNode(pID));

			    // Give component class the proper dimensions
			    for(int i = 0; i < dims.size(); i++) {
				qID = "[" + qID; 
			    }

			    // Then, add the component class
			    GNode component = GNode.create("ComponentType");
			    component.add(clp.createTypeNode(qID));

			    /**
			     * Now, add this info to two places in the tree
			     * The same information is needed, but in different
			     * namespaces
			     */


			    // Custom __class() is in java_lang namespace	
			    GNode customClass = GNode.create("CustomClass");
			    customClass.add(parent);
			    customClass.add(component);
			    customClasses.add(customClass);
			    
			    // Template specialization is in the __rt namespace
			    GNode templateNode = GNode.create("ArrayTemplate");
			    templateNode.add(parent);
			    templateNode.add(component);
			    templateNodes.add(templateNode);

			    System.out.println("--- Done adding array " + qID);
			}
			else {
			    // Nothing to do. Standard array
			}
			
		    }

		}
		
	    } // end visitFieldDeclaration

	    public void visit(GNode n) {
		// Need to override visit to work for GNodes
		for( Object o : n) {
		    if (o instanceof Node) dispatch((GNode)o);
		}
	    }
	    
	}.dispatch(n);
	
	if(customClasses.size() <= 0) customClasses = null;
	GNode customs = GNode.create("Declaration", customClasses);
	return customs;
    }


    // Moves the ClassDeclaration node(s) into the header.
    // @param n Java ClassDeclaration node
    // @return Possibly null ConstructorDeclarations node to add to Header
    GNode constructorDeclarations = GNode.create("ConstructorDeclarations");
    public GNode findConstructors(GNode n) {

	new Visitor() {

	    public void visitClassBody(GNode n) {
		for( Object o : n) {  if (o instanceof Node) { 
			GNode tmp = GNode.cast(o);
			
			if(tmp.hasName("ConstructorDeclaration")) {
			    // Add it in the header
			    constructorDeclarations.add(clp.deepCopy(tmp));
			    
			    // CPPPrinter ignores any 
			    // ConstructorDeclarations in the ClassBody
			    if(DEBUG) System.out.println("\t--- Moved a Constructor");
			}
		    }
		}
		
		visit(n);
	    }
	    
	    public void visit(GNode n) {
		// Need to override visit to work for GNodes
		for( Object o : n) {
		    if (o instanceof Node) dispatch((GNode)o);
		}
	    } // end visit
	    
	}.dispatch(n);
    
	    // Does this still allow for a blank default constructor?
	    // in CPPPrinter, if size()==0, print default constructor else,
	    // do the custom stuff
	return constructorDeclarations;
    }
    

    // ------------------------------------------
    // -------------- Build ccNode --------------
    // ------------------------------------------

    // At ClassBody, we build the node holding all .cc information
    // @param n ClassBody node from Java AST
    public void translateClassBody(GNode n) {

	// NOTE: ccNode is now nameed "ClassBody" and must use it's visitor
	GNode ccNode = n;
	buildImplementation(ccNode);
	cppTree.add(importDeclarations);
	cppTree.add(ccNode);

    }

    // Build ccNode to hold info for the class.cc file
    // @param n ccNode, a copy of the Java ClassDeclaration node.  
    // @return Transformed ClassDeclaration node, ready for the CPPPrinter
    // NOTE: Global variable thisClass to make "this" explicit
    String thisClass = "";
    public GNode buildImplementation(GNode n) {

	addTargets(n);

	// FIXME: n is fixed, can'tn.addNode(importDeclarations);
	// I wanted to put all import declarations under ClassDeclaration node.
	// but no biggie
	//	if(DEBUG) System.out.println("--- node " + n.getName() + " hasVariable() " + n.hasVariable());

	// TODO: translate method invocations using vtable

	// What can we copy directly?
	// What needs to be 'translated' in the printer?
	// What needs to be translated here?
	// This needs to be done with visit methods?
	// Does order matter? yes

	// to be accessible
	final GNode classD = n;

	new Visitor () {

	    public void visitClassDeclaration(GNode n) {
		thisClass = n.getString(1);
		visit(n);
	    }


	    // QUERY: At FieldDeclaration, can we copy Type to subsequent 
	    // PrimaryIdentifiers?
	    public void visitConstructorDeclaration(GNode n) {
		// Get .this' Class for explicit method invocation
		//		thisClass = n.getString(2);
		//		System.out.println("\t--- Entered class " + thisClass);
		visit(n);
	    }

	    public void visitExpressionStatement(GNode n) {
		// Set a global variable for tree traversal: 
		expressionStatement = n;
		visit(n);
	    }
	    
	    // Make the Class calling a method explicit
	    // Also, translate System methods
	    public void visitCallExpression(GNode n) {
	        
		// 1. Identify the PrimaryIdentifier - calling Class
		String primaryIdentifier = null;

		if(n.getNode(0) == null || 
		   n.getNode(0).hasName("ThisExpression")) {
		    
		    primaryIdentifier = thisClass;
		    if(DEBUG) System.out.println("\t--- primaryIdentifier = " + 
				       primaryIdentifier);
		}
		else if(n.getNode(0).hasName("SelectionExpression")) {
		  
		    primaryIdentifier = n.getNode(0).getNode(0).getString(0);
		    if(DEBUG) System.out.println("\t--- primaryIdentifier = " 
						 + primaryIdentifier);


		    // Are only System.outs wrapped in SelectionExpression 
		    // nodes? Is this if needed?
		    if("System".equals(primaryIdentifier)) {
			
			// Change any + to >>
			new Visitor () {
			    
			    public void visitAdditiveExpression(GNode n) {
				if("+".equals(n.getString(1))) {
				    n.set(1, "<<");
				}
			    }

			    public void visit(GNode n) {
				// Need to override visit to work for GNodes
				for( Object o : n) {
				    if (o instanceof Node) dispatch((GNode)o);
				}
			    }

			}.dispatch(n); // end Visitor

			if( "println".equals(n.getString(2)) ) {
			    GNode strOut = GNode.create(kStrmOut);
			    strOut.add(0, GNode.create( kPrimID ).add(0, "std::cout") );
			    // Add all arguments to System.out.println
			    for(int i = 0; i < n.getNode(3).size(); i++) {
				// HACK : check if primaryidentifer.get(0) == null
				if(GNode.test(n.getNode(3).get(i)) &&
				   null == n.getNode(3).getNode(i).get(0)) {

				} else {
				    // standard behavior
				    // removed addindex 1
				    strOut.add(n.getNode(3).get(i) ); 

				    //				    System.out.println("added print arguments "
				    //						       + n.getNode(3).get(i));
				}


			    }
			    
			    // removed add index 2
			    strOut.add(GNode.create( kPrimID ).add(0, "std::endl") );
			    
			    expressionStatement.set(0, strOut);

			}
			
			else if("print".equals(n.getString(2))) {
			    GNode strOut = GNode.create(kStrmOut);
			    strOut.add(0, GNode.create( kPrimID ).add(0, "std::cout") );
			    // Add all arguments to System.out.print
			    for(int i = 0; i < n.getNode(3).size(); i++) {
				strOut.add(1, n.getNode(3).get(i) ); 
			    }
			    expressionStatement.set(0, strOut);

			    // Must examine the arguments of this new std
			    //			    visit(expressionStatement);
			} 
			

		    }// end if "System"
		} // end SelectionExpression
		// Reminder, n is a CallExpression node
		else if(n.getNode(0).hasName("PrimaryIdentifier")) {

		    primaryIdentifier = n.getNode(0).getString(0);
		    
		    if(DEBUG) System.out.println("--- Found PrimaryIdentifier " 
						 + primaryIdentifier);

		    // Need to lookup the Type of primaryIdentifier
		    // If it's a custom class, need to pass it as the last param
		    // Eventually, can use SymbolTable, but hacking it for now
		    
		    // ClassDeclaration node, String PrimaryIdentifier
		    boolean yes = isCustomType(classD, primaryIdentifier);
		    
		    /**
		    if(yes) {
			// Add primaryIdentifier to Arguments
			if(DEBUG) System.out.println("--- Must pass Argument " +
					   primaryIdentifier + " to method " +
					   n.getString(2));

			GNode arguments = (GNode) n.getNode(3);
			arguments = clp.deepCopy(arguments);
	        
			// Arguments should only have node children 
			GNode p = GNode.create("PrimaryIdentifier");
			p.add(primaryIdentifier);
			arguments.add(p);
			if(DEBUG) System.out.println("\n\t--- Added argument " +
					   primaryIdentifier + " to node " + 
					   n + "\n");

			n.set(3, arguments);
		    }
		    **/ 

		}
		else if(n.getNode(0).hasName("SuperExpression")) {

		    // Replace Java keyword super with actual class
		    GNode pI = GNode.create("PrimaryIdentifier");
		   
		    GNode vtList = clp.getVTable(thisClass);
		    GNode superNode = clp.getSuperclass(className);
		    String superName = clp.getName(superNode);
		  
			pI.add(0, superName);
			n.set(0, pI);
		 
		}
		else if(n.getNode(0).hasName("CallExpression")) {
		    // keep on going!
		    visit(GNode.cast(n.getNode(0)));
		}
       		else { // catch all
		    visit(n);
		    //		    System.out.println("\t--- Didn't translate node " + 
		    //				       n.getNode(0).toString());
		}



		// Works, but not with method chaining?
		if(n.size() >= 4 && n.getNode(3).hasName("Arguments")) {
		    // Time to append arguments
		    GNode vt = clp.getVTable(thisClass);
		    String mName = n.getString(2);
		    GNode vtm = clp.getVTMethod(vt, mName);

		    if(null != vtm) {
			if(n.getNode(3).size() != vtm.getNode(2).size()) {
			    if(DEBUG) { 
				System.out.println("--- Adding arguments for " 
						   + vtm.getString(1)); 
			    }
			    // Being extra safe
			    if(n.getNode(3).hasName("Arguments")) {
				// Finally appending target
				GNode arg = clp.deepCopy((GNode)n.getNode(3));

				// Child should be PrimaryIdentifier node
				GNode tmp = clp.deepCopy((GNode)n.getNode(0));
				arg.add(tmp);

				n.set(3, arg);
			    }
			    
			}

		    }
		    // Keep on visiting arguments?

		} // end check for Arguments
		

	   
		// Uncommenting this gives an error.
		//		visit(n);
	    }// End visitCall Expression
  

	    public void visitArguments(GNode n) {

		for (Object o : n) {
		    if(o instanceof GNode) {
			if(GNode.cast(o).hasName("CallExpression")) {
			    visit(GNode.cast(o));
			}
		    }
		}
		// duplicate?
		visit(n);
	    }



	    public void visitMethodDeclaration(GNode n) {
		// If Parameters.size() of MethodDeclaration and clp's lookup
		//  append CLASSNAME __this to parameters

		// FIXME: Rob has duplicate and more robust? implementation

		GNode vt = clp.getVTable(thisClass);
		String mName = n.getString(3);
		GNode vtm = clp.getVTMethod(vt, mName);

		if(null != vtm) {
		    if( n.getNode(4).size() != vtm.getNode(2).size() ) {
			// Append __this parameter
			System.out.println("--- Need to add nodes for " + 
					   vtm.getString(1));
			// Add a new Formal Parameter
			if(n.getNode(4).hasName("FormalParameters")) {
			    GNode fps = clp.deepCopy((GNode)n.getNode(4));
			    fps.addNode( createFormalParameter(thisClass, "__this") );
			    n.set(4, fps);
			    // Need to deep copy to ensure variable
			    // then, replace the FormalParameters node
			   
			}
		    }
		}
		visit(n);
		
	    }

	    // Translate exceptions to the few Grimm Defined ones
	    public void visitThrowStatement(GNode n) {

		if(n.getNode(0).hasName("NewClassExpression")) {
		    
		    String throwType = n.getNode(0).getNode(2).getString(0);
		    
		    if(!isGrimmException(throwType)) {
			// If it doesn't match, just throw a general exception
			throwType = "Exception";
		    }
		    
		    // Collapse node structure.  
		    // NOTE: This also removes any arguments to the Exception
		    // This is ok bc Grimm doesn't support arguments.
		    // Remeber to change Printer.
		    GNode tmp = GNode.create("QualifiedIdentifier");
		    tmp.add(throwType);
		    
		    n.set(0, tmp);
		}
		

		visit(n);
		
	    }

	    public void visit(GNode n) {
		// Need to override visit to work for GNodes
		for( Object o : n) {
		    if (o instanceof Node) dispatch((GNode)o);
		}
	    }
		
	    
	}.dispatch(n);//end Visitor
	
	return n;
    }

    // Exhaustively add targets to all cal expressions
    // Should work for method chaining and nested expressions inside
    // cout statements
    // @param n ClassDeclaration node
    String target = null;
    public void addTargets(GNode n) {

	new Visitor() {

	    public void visitCallExpression(GNode n) {
		

		// No problem here!
		if( n.get(0) != null && GNode.test(n.get(0)) ) {
		    //		    System.out.println("--- Yes, a GNode");

		    if(n.getNode(0).hasName("PrimaryIdentifier")) {
			target = n.getNode(0).getString(0);
			//			System.out.println("target = " + target);
		    }
		}
		
        
		// Using the wrong n?
		GNode tmp = clp.deepCopy(GNode.cast(n.getNode(3)));
		tmp.addNode(createPrimaryIdentifier(target));
		n.set(3, tmp);
						
	    }
	    
	    public void visit(GNode n) {
		// Need to override visit to work for GNodes
		for( Object o : n) {
		    if (o instanceof Node) dispatch((GNode)o);
		}
	    }
	    	    
	}.dispatch(n);//end Visitor

    } // end addTargets

 
    // ------------------------------------------
    // ----------- Internal Methods  ------------
    // ------------------------------------------

    // Creates and returns a simple primary identifier node, 
    // which is really just a string name of a variable.
    GNode createPrimaryIdentifier( String contents ) {

	return (GNode)GNode.create( kPrimID ).add(contents);
    }


    // Make a new FormalParameter node
    GNode createFormalParameter(String type, String name) {

	GNode fp = GNode.ensureVariable(GNode.create("FormalParameter"));
	fp.add(GNode.create("Modifiers"));
	fp.add(clp.createTypeNode(type));
	fp.add(null);
	fp.add(name);
	fp.add(null);

	return fp;
    }


    // Looks up the type of a primaryIdentifier.  Tells you if it is custom
    // Custom != String, Object, Class, etc.
    // @param n Java AST ClassDeclaration
    // @param s String name of the PrimaryIdentifier
    boolean isCustomType(GNode n, String s) {
	// FIXME: Source of lots of bugs due to variable nature of Declarator 
	// node.  Make more robust.
	
	final String p = s;

	GNode isCT = (GNode) (new Visitor() {

		public GNode visitDeclarator(GNode n) {
		    
		    if( p.equals(n.getString(0)) ) {
			// We found where it is declared to get Type
			String type;
			if(n.getNode(2).hasName("Type")) {
			    type = n.getNode(2).getNode(2).getString(0);
			}
			else if(n.getNode(2).hasName("NewClassExpression")) {
			    type = n.getNode(2).getNode(2).getString(0);
			}
			else {
			    // Going down one more level should be Type node
			    type = 
				n.getNode(2).getNode(0).getNode(0).getString(0);
			}
			if(isCustom(type))
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


		public GNode visit(GNode n) { // override visit for GNodes
		    
		    // Keep Searching
		    for( Object o : n) {
			if (o instanceof Node) {
			    GNode returnValue = (GNode)dispatch((GNode)o);
			    if( returnValue != null ) return returnValue;
			}
		    }
		    
		    return null;
		    
		}
		
	    }.dispatch(n));

	if(isCT != null) return true;
	return false;
	

    }



    public boolean isCustom(String s) {

	if("String".equals(s) ||
	   "Object".equals(s) ||
	   "Class".equals(s)) {
	    // What about arrays? 
	    return false;

	}

	return true;
    }


    // Checks to see if Exception is defined in java_lang.h
    // @param s String Exception name
    public boolean isGrimmException(String s) {

	if( "Exception".equals(s) ||
	    "RuntimeException".equals(s) ||
	    "NullPointerException".equals(s) ||
	    "NegativeArraySizeException".equals(s) ||
	    "ArrayStoreException".equals(s) ||
	    "ClassCastException".equals(s) ||
	    "IndexOutOfBoundsException".equals(s) ||
	    "ArrayIndexOutOfBoundsException".equals(s) ) {
	    return true;
	}
	

	return false;
    }

   // ------------------------------------------
    // ------------- Getter Methods  ------------
    // ------------------------------------------
    
    public GNode getCPPTree() { return cppTree; }
    

}
