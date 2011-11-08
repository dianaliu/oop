package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

import java.util.ArrayList;

/*
 *  Builds a CPP AST tree using a Java AST tree
 * Uses new types of nodes and new tree structure
 */


interface CPPUtil {
    // Custom GNode types:
    public static final String kHeadDec = "HeaderDeclaration";
    public static final String kImplDec = "ImplementationDeclaration";
    public static final String kStrmOut = "StreamOutputList";
	
    // xtc GNode types:
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
    ClassLayoutParser classParser;
    
    String className;
    GNode thisClassVTableStructDeclList;
    GNode thisClassDataLayoutStructDeclList;
    GNode thisClassImplementation;
    GNode thisExpressionStatement;
    
    // Constructor
    // @param clp To gain access to vtables and data layouts
    // @param javaAST
    public LeafTransplant(ClassLayoutParser clp, GNode javaAST) { 
		this.translatedTree = GNode.create(kRoot);
		this.originalTree = javaAST;
		this.classParser = clp;
		
		// Translate Java tree to CPP tree using visitors
		dispatch(originalTree);
    } 
    
	
    // ------------------------------------------
    // ------------- Visitor Methods  -----------
    // ------------------------------------------
    
	
    public void visitClassDeclaration(GNode n) {
		className = n.get( 1 ).toString( );  
		GNode newHeaderNode = createHeader();
		translatedTree.add( newHeaderNode );
		
		// VTable
		GNode vTable = classParser.getVTable(className);
		for( Object o : vTable ) thisClassVTableStructDeclList.add( (GNode)o );
		
		// ----- Begin creating the data layout
		GNode dataLayoutList = classParser.getDataLayout(className);
		for( Object o : dataLayoutList ) thisClassDataLayoutStructDeclList.add( (GNode)o );
		//index 0 = __vptr
		// 1 - constructor - pulled out and inserted as nodes
		
		// ----- done creating the data layout
		
		thisClassImplementation = GNode.create(kImplDec);  //create a new implementation declaration
		translatedTree.add( thisClassImplementation );	//and add
		visit(n);
    }
    
    public void visitMethodDeclaration(GNode n) {
		//adding the actual method implementation 
		// (will go in the .cc file, includes the full method header and body):
		thisClassImplementation.add(functionDefForMethDecl(n));
		visit(n);
    }
    
    public void visitExpressionStatement(GNode n) {
		// set a global variable for tree traversal: 
		thisExpressionStatement = n;
		visit(n);
    }
    
    // Translates System.out.print/ln statements
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
		
    }
    
    public void visit(GNode n) {
		for( Object o : n ) if( o instanceof GNode ) dispatch((GNode)o);
    }
    
	
	
    // ------------------------------------------
    // ----------- Create Header Node  ----------
    // ------------------------------------------
	
    GNode createHeader() {
		GNode headerNode = GNode.create(kHeadDec); 
		
		createDataLayout(headerNode);
		createVTableLayout(headerNode);
		createTypedef(headerNode);
		
		return headerNode;
    }
	
    public void createDataLayout(GNode headerNode) {
		// 1. Set headerNode.getNode(1) = dataLayout node
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
					thisClassDataLayoutStructDeclList = GNode.create(kStructDeclList);
					structDef.add(2, thisClassDataLayoutStructDeclList);
					structDef.add(3, null);
				}
				dlDeclSpef.add(structDef);
			}
			dataLayout.add(1, dlDeclSpef);
			dataLayout.add(2, null);
		}
		headerNode.add(0, dataLayout);
    }
    
    public void createVTableLayout(GNode headerNode) {
		// 2. Set headerNode.getNode(2) = vtableLayout
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
		headerNode.add(1, vtableLayout);
		
    }
	
    public void createTypedef(GNode headerNode) {
		// 3. Set headerNode.getNode(0) = Typedef Declaration node
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
		headerNode.add(2, typedefDecl);
    }
	
    // ------------------------------------------
    // ------------- Getter Methods  ------------
    // ------------------------------------------
    
    public GNode getTranslatedTree() { return translatedTree; }
    
	
    // ------------------------------------------
    // ------------- Please explain  ------------
    // ------------------------------------------
	
    // Creates and returns a simple primary identifier node, 
    // which is really just a string name of a variable.
    GNode createPrimaryIdentifier( String contents ) {
		return (GNode)GNode.create( kPrimID ).add(contents);
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
				if( "main".equals(n.get(3)) ) declSpef.add( GNode.create(kInt) );
				else declSpef.add( n.get(2) ); //add return type (adding a java type)
		    }
		    fncDef.add(1, declSpef);
			GNode fncDeclarator = GNode.create(kFuncDecltor);
			{
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
    }
	
    
	
	
	
	
}