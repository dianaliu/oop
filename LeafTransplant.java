package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

interface CPPUtil {
	//Group defined GNode types:
	public static final String kHeadDec = "HeaderDeclaration";
	public static final String kImplDec = "ImplementationDeclaration";
	public static final String kStrmOut = "StreamOutputList";
	
	//Preexisting GNode types:
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
	//GNode classHierarchy;
	GNode translatedTree;
	
	String thisClassName;
	GNode thisClassVTableStructDeclList;
	GNode thisClassImplementation;
	GNode thisExpressionStatement;
    
    public LeafTransplant(GNode classTree, GNode javaAST) { 
		this.translatedTree = GNode.create(kRoot);
		this.originalTree = javaAST;
		this.classHierarchy = classTree;
		
		//translatedTree.add( buildHeaderForClass( "FooBar" ) );
		//translateJavaToCPP();
	} 
	
	GNode createPrimaryIdentifier( String contents ) {
		//creates and returns a simple primary identifier node, which is really just a string name of a variable.
		return (GNode)GNode.create( kPrimID ).add(contents);
	}
	
	GNode buildHeaderForClass() {
		String className = thisClassName;
		GNode objectTree = GNode.create(kHeadDec); //defines a header declaration node, which has been arbitrarly invented
		{
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
			GNode dataLayout = GNode.create(kDecl); //struct __Object { }
			{
				dataLayout.add(0, null);
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
			strOut.add(1, n.getNode(3).get(0) ); //FIXME: only adds the first argument
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
			strOut.add(1, n.getNode(3).get(0) ); //FIXME: only adds the first argument
			thisExpressionStatement.set(0, strOut);
		}
	}
}