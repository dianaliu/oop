package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

/* Builds a CPP AST tree using a Java AST tree
 * Uses new types of nodes and new tree structure
 */


interface CPPUtil {
    //Custom GNode types:
    public static final String kHeadDec = "HeaderDeclaration";
    public static final String kImplDec = "ImplementationDeclaration";
    public static final String kStrmOut = "StreamOutputList";
	
    //xtc GNode types:
    // FIXME: Make alphabetical
	public static final String kRoot = "TranslationUnit";
	public static final String kStruct = "StructureTypeDefinition";
	public static final String kStructDeclList = "StructureDeclarationList";
	public static final String kStructDecl = "StructureDeclaration";
	public static final String kTypedef = "TypedefSpecifier";
	public static final String kDecl = "Declaration";
	public static final String kDeclSpef = "DeclarationSpecifiers";
	public static final String kInt = "Int";
	public static final String kInitDeclList = "InitializedDeclaratorList";
	public static final String kInitDecl = "InitializedDeclarator";
	public static final String kSimpDecl = "SimpleDeclarator";
	public static final String kPtr = "Pointer";
	public static final String kPrimID = "PrimaryIdentifier";
	public static final String kFuncDef = "FunctionDefinition";
	public static final String kFuncDecltor = "FunctionDeclarator";
	public static final String kCmpStmt = "CompoundStatement";
	public static final String kStrLtrl = "StringLiteral";
}

public class LeafTransplant extends Visitor implements CPPUtil {
	
	GNode originalTree;
	GNode translatedTree;
	
	String thisClassName;
	GNode thisClassVTableStructDeclList;
	GNode thisClassImplementation;
	GNode thisExpressionStatement;
    
    // FIXME: Must take in an array of Java ASTs
    public LeafTransplant(GNode classTree, GNode javaAST) { 
	this.translatedTree = GNode.create(kRoot);
	this.originalTree = javaAST;

	
	//translatedTree.add( buildHeaderForClass( "FooBar" ) );
	//translateJavaToCPP();
    } 
    
    GNode createPrimaryIdentifier( String contents ) {
	//creates and returns a simple primary identifier node, which is really just a string name of a variable.
	return (GNode)GNode.create( kPrimID ).add(contents);
    }
    
    GNode buildHeaderForClass() {
	String className = thisClassName;  

	// objectTree(root) = HeaderDeclaration node
	GNode objectTree = GNode.create(kHeadDec); //defines a header declaration node, which has been arbitrarly invented

	{   // 1. Set objectTree.getNode(0) = Typedef Declaration node
	    // Create CPP Typedef Declaration node using Java Declaration nodes
	    GNode typedefDecl = GNode.create(kDecl); //typedef __Object* Object;
	    {
		typedefDecl.add(0, null);

		GNode typeDefDeclSpef = GNode.create(kDeclSpef);
		{
		    typeDefDeclSpef.add( 0, GNode.create(kTypedef));
		    typeDefDeclSpef.add( 1, createPrimaryIdentifier( "__" + className + "*" ) );
		}
		typedefDecl.add(1, typeDefDeclSpef);

		GNode initDeclList = GNode.create(kInitDeclList);
		{
		    GNode initDecl = GNode.create(kInitDecl);
		    initDecl.add(0, null);
		    initDecl.add(1, GNode.create(kSimpDecl).add(className));
		    initDecl.add(2, null);
		    initDecl.add(3, null);
		    initDecl.add(4, null);
		    initDeclList.add( initDecl );
		}
		typedefDecl.add(2, initDeclList);
	    }
	    objectTree.add(0, typedefDecl);


	    // 2. Set objectTree.getNode(1) = dataLayout node
	    // Create CPP dataLayout node using Java Declaration nodes
	    GNode dataLayout = GNode.create(kDecl); //struct __Object { }
	    { 
		dataLayout.add(0, null);
		
		// Create dataLayout Declaration Specifiers using 
		// Java Declaration Specifiers. 
		GNode dlDeclSpef = GNode.create(kDeclSpef);
		{
		    GNode structDef = GNode.create(kStruct, 4);
		    {
			structDef.add(0, null);
			structDef.add(1, "__" + className);
			structDef.add(2, GNode.create(kStructDeclList));
			structDef.add(3, null);
		    }
		    dlDeclSpef.add(structDef);
		}
		dataLayout.add(1, dlDeclSpef);

		dataLayout.add(2, null);
	    }
	    objectTree.add(1, dataLayout);

	    // 3. Set objectTree.getNode(2) = vtableLayout
	    // Create vtableLayout using Java Declarations
	    GNode vtableLayout = GNode.create(kDecl); //struct __Object_VT { }
	    {
		vtableLayout.add(0, null);
		GNode vtDeclSpef = GNode.create(kDeclSpef);
		{
		    GNode vtStructDef = GNode.create(kStruct, 4);
		    {
			vtStructDef.add(0, null);
			vtStructDef.add(1, "__" + className + "_VT");
			thisClassVTableStructDeclList = GNode.create(kStructDeclList);
			{
			    thisClassVTableStructDeclList.add(0, null);
			}
			vtStructDef.add(2, thisClassVTableStructDeclList);
			vtStructDef.add(3, null);
		    }
		    vtDeclSpef.add(vtStructDef);
		}
		vtableLayout.add(1, vtDeclSpef);
		vtableLayout.add(2, null);
	    }
	    objectTree.add(2, vtableLayout);

	}
	
	return objectTree;
    }
    
    public GNode buildImplementationForClass() {
	GNode mainTree = GNode.create(kImplDec);
	mainTree.setProperty("className", thisClassName);
	return mainTree;
    }
    
    public void addToVTable(GNode n) {
	thisClassVTableStructDeclList.add(0, createPrimaryIdentifier( "__" + thisClassName + "::" + (String)n.get(3) ));
    }
    
    public void addMethodImplementation(GNode n) {
	thisClassImplementation.add(functionDefForMethDecl(n));
	visit(n);
    }
    
    GNode functionDefForMethDecl(GNode n) {
	//Java:MethodDeclaration() -> CPP:FunctionDefinition()
	// Function name:
	// Return type:
	// Parameters: 
	GNode fncDef = GNode.create(kFuncDef);
	{
	    fncDef.add(0, null);
	    GNode declSpef = GNode.create(kDeclSpef);
	    {
		declSpef.add( n.get(2) ); //add return type (java type)
	    }
	    fncDef.add(1, declSpef);
	    GNode fncDeclarator = GNode.create(kFuncDecltor);
	    {
		GNode simpDecl = (GNode)GNode.create(kSimpDecl).add(n.get(3)); //method name
		fncDeclarator.add(0, simpDecl);
		fncDeclarator.add(1, null);
	    }
	    fncDef.add(2, fncDeclarator);
	    fncDef.add(3, null);
	    fncDef.add(4, n.get(7));  
	    // ^ NOTE: we are adding a java code block instead of a C compound statement
	}
	return fncDef;
    }
    
    public void translateJavaToCPP() { dispatch(originalTree); }
    
    public GNode getTranslatedTree() { return translatedTree; }
    
    //----- VISITOR METHODS -----
    
    public Object visit(Node n) {
	for( Object o : n ) if( o instanceof Node ) dispatch((Node)o);
	return null;
    }
    
    public void visitClassDeclaration(GNode n) {
	thisClassName = n.get(1).toString();
	translatedTree.add(buildHeaderForClass());
	thisClassImplementation = buildImplementationForClass();
	translatedTree.add(thisClassImplementation);
	visit(n);
    }
    
    public void visitMethodDeclaration(GNode n) {
	addToVTable(n);
	addMethodImplementation(n);
	visit(n);
    }
    
    public void visitExpressionStatement(GNode n) {
	thisExpressionStatement = n;
	visit(n);
    }
    
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
	    thisExpressionStatement.set(0, strOut);
	}
	else if( n.size() >= 3 && "print".equals((String)n.get(2)) ) {
	    GNode strOut = GNode.create(kStrmOut);
	    strOut.add(0, (GNode)GNode.create( kPrimID ).add(0, "std::cout") );
	    // Add all arguments to System.out.print
	    for(int i = 0; i < n.getNode(3).size(); i++) {
		strOut.add(1, n.getNode(3).get(i) ); 
	    }
	    

	    thisExpressionStatement.set(0, strOut);
	} // end else if 
	

    } // end visitCallExpression
}