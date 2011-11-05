package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

import xtc.util.Runtime;

import java.util.ArrayList;

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
    

    // FIXME: ya know?
    public static boolean DEBUG = true;


    GNode javaTree;
    GNode cppTree;
    ClassLayoutParser clp;
    
    // Pros/Cons of global variables?
    String className;
    GNode classImplementation;
    GNode expressionStatement;


    GNode thisClassDataLayoutStructDeclList; 
    GNode thisClassVTableStructDeclList;
    

    // ------------------------------------------
    // ------------- Constructor ----------------
    // ------------------------------------------

    // @param clp To gain access to vtables and data layouts
    // @param javaAST
    public LeafTransplant(ClassLayoutParser clp, GNode javaAST) { 
		this.cppTree = GNode.create("TranslationUnit");
		this.javaTree = javaAST;
		this.clp = clp;
		
		// Create a CPP tree as we visit the Java AST
		createCPPTree();
    } 
    
    // ------------------------------------------
    // --------- Begin CPP Translation  ---------
    // ------------------------------------------

    public void createCPPTree() {
	// We separate the Java AST into 3 stages for translation:
	// ImportDeclaration(s), ClassDeclaration, and ClassBody nodes.

	// FIXME: Do not hardcode java_lang
	importJavaLang();

	new Visitor() {

	    public void visitImportDeclaration(GNode n) {
		// There may be 1+ ImportDeclarations  
		// Call method to add import nodes
		
		// FIXME: What to name these methods?
		// Build and add import node to cpp tree
		translateImportNode(n);

	    }

	    public void visitClassDeclaration(GNode n) {
		// Call method to create header file. Anything else?

		// Build and add header node
		translateClassDeclaration(n);

		visit(n); // Necessary b/c ClassBody is a child
	    }
	    
	    public void visitClassBody(GNode n) {
		// Call method to translate class implementation.  
		// Anything else?

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
    }

    // ------------------------------------------
    // ----- Translate ImportDeclarations  ------
    // ------------------------------------------

    // Initializes the tree with java_lang.h and java_lang.cc
    // FIXME: Output java_lang.h and java_lang.cc to same pkg
    // FIXME: Do not hardcode java_lang
    public void importJavaLang() {
	// Nodes are created "inside out" - from the leaves up

	GNode qualifiedIdentifiers = GNode.create("QualifiedIdentifier");
	qualifiedIdentifiers.add("java");
	qualifiedIdentifiers.add("lang");
	
	GNode javaLang = GNode.create("ImportDeclaration", null, 
				      qualifiedIdentifiers, null);

	cppTree.add(0, javaLang);

    }

    // Creates corresponding CPP import nodes
    // TODO: Printer will add appropriate syntax
    // @param n ImportDeclaration node from Java AST
    public void translateImportNode (GNode n) {

	GNode importNode  = n;
	cppTree.add(importNode);

    }


    // ------------------------------------------
    // ------------- Build hNode ----------------
    // ------------------------------------------


    // At ClassDeclaration, we build hNode to hold info. for the header file
    // @param n ClassDeclaration node from Java AST
    public void translateClassDeclaration (GNode n) {

	// TODO: Use className to name output files
	className = n.get(1).toString();

	// hNode contains all information for the .h file to be printed
	GNode hNode = buildHeader(n);

	cppTree.add(hNode);
	
    }

    // Creates the header node for a Class
    // @param n ClassDeclaration node from Java AST
    GNode buildHeader(GNode n) {
	// Nodes are created "inside out" from the leaves up

	GNode hNode = GNode.create("HeaderDeclaration"); 

	GNode dataLayout = buildDataLayout(n);
	hNode.addNode(dataLayout);

	GNode vtable = buildVTable(n);
	hNode.addNode(vtable);

	GNode typedef = buildTypedef(n);
	hNode.addNode(typedef);

	return hNode;
    }

    // Build CPP Data Layout nodes to create struct __Class { }
    // @param n ClassDeclaration node from Java AST
    public GNode buildDataLayout(GNode n) {
	// Nodes are created "inside out" from the leaves up
       
	// Populate our Data Layout with information
       	// NOTE: The dataList is not quite accurate at the moment and needs to
	// modified in ClassLayoutParser.  However, the logic below is correct.
	GNode dataDeclarationList = GNode.create("StructureDeclarationList");
	ArrayList dataList = clp.getDataLayout(className);
	if(dataList != null) {
	    dataDeclarationList.addAll(dataList);
	}

	// ------------------------------------

	// Build the skeleton of the Data Layout struct
	// FIXME: Printer should print __className
	GNode structTypeDef = GNode.create("StructureTypeDefinition", 
					      null, className, 
					      dataDeclarationList, null);
       	
	
	GNode declarationSpecifiers = GNode.create("DeclarationSpecifiers", 
						   structTypeDef);


	GNode dataLayout = GNode.create("Declaration", null, 
					declarationSpecifiers, null);


	return dataLayout;
    }
    
    // Build CPP VTable nodes to create struct __Class_VT { }
    // @param n ClassDeclaration node from Java AST
    public GNode buildVTable(GNode n) {
	// Nodes are created "inside out" from the leaves up

	// Populate vtable with information
	// NOTE: The vtableList is not quite accurate at the moment and needs to
	// be modified in ClassLayoutParser. But the logic below is correct.
	GNode vtableDeclarationList = GNode.create("StructureDeclarationList");
	ArrayList vtableList = clp.getVTable(className);
	if(vtableList != null) {
	    vtableDeclarationList.addAll(vtableList);
	}

	// ------------------------------------

	// Build skeleton of VTable struct
	// FIXME: Printer should print __className_VT
	GNode vtableStructDefinition = 
	    GNode.create("StructureTypeDefinition", null, className, 
			 vtableDeclarationList, null);

	GNode vtableDeclarationSpecifiers = 
	    GNode.create("DeclarationSpecifiers", vtableStructDefinition);

	GNode vtable = 
	    GNode.create("Declaration", null, 
			 vtableDeclarationSpecifiers, null);
       
	return vtable;
    }

    // Build CPP typedef node to create typedef __Class* Class;
    // HELP: I'm confused. typedefs are also vtables?  
    // Do vtables go inside typedefs? Anyone?
    // @param n ClassDeclaration node from Java AST
    public GNode buildTypedef(GNode n) {
	// Nodes are created "inside out" from the leaves up

	// FIXME: What is the typedef for? What should it be populated with?

	GNode simpleDeclarator = GNode.create("SimpleDeclarator", className);
	
	GNode initializedDeclarator =
	    GNode.create("InitializedDeclarator", null, simpleDeclarator, 
			 null,null, null);
	    
	GNode initializedDeclaratorList = 
	    GNode.create("InitializedDeclaratorList", initializedDeclarator);
	    
	// ------------------------------------
	    
	GNode typedefSpecifier = GNode.create("TypedefSpecifier");
	
	// FIXME: Printer must print __className*
	GNode classIdentifier = 
	    createPrimaryIdentifier( "__" + className + "*" );
	
	GNode typedefDeclarationSpecifiers = 
	    GNode.create("DeclarationSpecifiers", typedefSpecifier, 
			 classIdentifier);
	
	// ------------------------------------

	GNode typedef = 
	    GNode.create("Declaration", null, typedefDeclarationSpecifiers, 
		     initializedDeclaratorList);

	return typedef;
    }



    // ------------------------------------------
    // -------------- Build ccNode --------------
    // ------------------------------------------

    // At ClassBody, we build the node holding all .cc information
    // @param n ClassBody node from Java AST
    public void translateClassBody(GNode n) {

	GNode ccNode = buildImplementation(n);
	cppTree.add(ccNode);

    }

    // Build ccNode to hold info for the class.cc file
    // @param n ClassBody node from Java AST
    public GNode buildImplementation(GNode n) {

	// What can we copy directly?
	// What needs to be 'translated' in the printer?
	// What needs to be translated here?
	// This needs to be done with visit methods?
	// Does order matter?

	GNode ccNode = GNode.create("ImplementationDeclaration");
	
	new Visitor () {
	    // FIXME: add to ccNode, where?

	    public void visitExpressionStatement(GNode n) {
		// set a global variable for tree traversal: 
		expressionStatement = n;
		visit(n);
	    }
	    
	    // Translates System.out.print/ln statements
	    // FIXME: Should this be done in printer?
	    public void visitCallExpression(GNode n) {
		
		if( n.size() >= 3 && "println".equals((String)n.get(2)) ) {
		    GNode strOut = GNode.create(kStrmOut);
		    strOut.add(0, (GNode)GNode.create( kPrimID ).add(0, "std::cout") );
		    // Add all arguments to System.out.println
		    for(int i = 0; i < n.getNode(3).size(); i++) {
			strOut.add(1, n.getNode(3).get(i) ); 
		    }
		    
		    strOut.add(2, (GNode)GNode.create( kPrimID ).add(0, "std::endl") );
		    
		    /*
		      n.set(2, "cout");
		      GNode strLiteral = (GNode)GNode.create( kPrimID ).add(0, "std");
		      n.set(0, strLiteral);
		    */
		    expressionStatement.set(0, strOut);
		}
		else if( n.size() >= 3 && "print".equals((String)n.get(2)) ) {
		    GNode strOut = GNode.create(kStrmOut);
		    strOut.add(0, (GNode)GNode.create( kPrimID ).add(0, "std::cout") );
		    // Add all arguments to System.out.print
		    for(int i = 0; i < n.getNode(3).size(); i++) {
			strOut.add(1, n.getNode(3).get(i) ); 
		    }
		    expressionStatement.set(0, strOut);
		} // end else if 
		
	    }
	    

	    public void visitConstructorDeclaration(GNode n) {
		visit(n);
	    }
	    
	    public void visitMethodDeclaration(GNode n) {
		//adding the actual method implementation 
		// (will go in the .cc file, includes the full method header and body):
		//	GNode functionDeclaration = translateMethodDeclaration(n);
		//	classImplementation.add(functionDeclaration);
		visit(n);
	    }
	    
	    
	    public void visit(GNode n) {
		// Need to override visit to work for GNodes
		for( Object o : n) {
		    if (o instanceof Node) dispatch((GNode)o);
		}
	    }
	    
	}.dispatch(n);



	return ccNode;
    }


    // ------------------------------------------
    // ------------- Getter Methods  ------------
    // ------------------------------------------
    
    public GNode getCPPTree() { return cppTree; }
    

    // ------------------------------------------
    // ----------- Internal Methods  ------------
    // ------------------------------------------

    // Creates and returns a simple primary identifier node, 
    // which is really just a string name of a variable.
    GNode createPrimaryIdentifier( String contents ) {

	return (GNode)GNode.create( kPrimID ).add(contents);
    }

    GNode translateMethodDeclaration (GNode n) {
	//Java:MethodDeclaration() -> CPP:FunctionDefinition()
	// Function name:
	// Return type:
	// Parameters:
	GNode fncDef = GNode.create(kFuncDef);
	{
	    fncDef.add(0, null);
		    GNode declSpef = GNode.create(kDeclSpef);
		    {
			if( "main".equals(n.get(3)) ) declSpef.add( GNode.create(kInt) );
			else declSpef.add( n.get(2) ); //add return type (adding a java type)
		    }
		    fncDef.add(1, declSpef);
			GNode fncDeclarator = GNode.create(kFuncDecltor);
			{
			    // FIXME: Must be done in Printer
			    GNode simpDecl = (GNode)GNode.create(kSimpDecl).add( "__" + className + "::" +  n.get(3));  //method name
			    fncDeclarator.add(0, simpDecl);
			    fncDeclarator.add(1, n.get(4));
			}
			fncDef.add(2, fncDeclarator);
			fncDef.add(3, null);
			fncDef.add(4, n.get(7));  
			// ^ NOTE: we are adding a java code block instead of a C compound statement
		}


		return fncDef;
    } // end translateMethodDeclaration

    




}