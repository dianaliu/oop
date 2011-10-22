package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

interface CPPUtil {
	//Node types:
	public static final String kRoot = "TranslationUnit";
	public static final String kHeadDec = "HeaderDeclaration";
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
}

public class LeafTransplant extends Visitor implements CPPUtil {
	
	GNode originalTree;
	GNode classHierarchy;
	GNode translatedTree;
	
	GNode thisClassVTableStructDeclList;
    
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
	
	GNode buildHeaderForClass(String className) {
		GNode objectTree = GNode.create(kHeadDec); //defines a header declaration node, which has been arbitrarly invented
		{
			GNode typedefDecl = GNode.create(kDecl); //typedef __Object* Object;
			{
				typedefDecl.add(0, null);
				GNode typeDefDeclSpef = GNode.create(kDeclSpef);
				{
					typeDefDeclSpef.add( 0, GNode.create(kTypedef));
					typeDefDeclSpef.add(1, createPrimaryIdentifier( "__" + className + "*" ) );
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
			GNode dataLayout = GNode.create(kDecl); //struct __Object {
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
			GNode vtableLayout = GNode.create(kDecl); //struct __Object_VT {
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
	
	public void translateJavaToCPP() { dispatch(originalTree); }
    
    public GNode getTranslatedTree() { return translatedTree; }

    //----- VISITOR METHODS -----
	
	public void visit(Node n) {
		for( Object o : n ) if( o instanceof Node ) dispatch((Node)o);
	}
	
	public void visitClassDeclaration(GNode n) {
		translatedTree.add(buildHeaderForClass( n.get(1).toString() ));
		visit(n);
	}
	
	public void visitMethodDeclaration(GNode n) {
		thisClassVTableStructDeclList.add(0, createPrimaryIdentifier( (String)n.get(3) ));
		visit(n);
	}
}