package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

public class AnalyzeVisitor extends Visitor {
	GNode vtableTree;
	GNode currentNode;
	
    public AnalyzeVisitor() {
		setupTree();
	}
	
    public void visitClassDeclaration(GNode n) {
		currentNode = GNode.create((String)n.get(1));
		visit(n);
		if( n.get(3) == null ) vtableTree.add(currentNode); //add to the object node if the class extends nothing
		else {
			searchForClass( (String)n.getNode(3).getNode(0).getNode(0).get(0) ).add(currentNode);
		}
    }    
    
    public void visitMethodDeclaration(GNode n) {
		currentNode.setProperty( "VTable", (String)n.get(3) );
		System.out.println((String)n.get(3));
		visit(n);
    }
	
    //public void visitBlock(GNode n) {
    //visit(n);
    //}
	
    public void visit(Node n) {
		for( Object o : n) {
			if (o instanceof Node) dispatch((Node)o);
		}
    }
	
	public GNode searchForClass( final String className ) {
		return (GNode)(new Visitor() {
			public Node visit(Node n) {
				if( ((String)n.getName()).equals(className) ) 
					return n;
				for( Object o : n) {
					if (o instanceof Node) {
						GNode returnValue = (GNode)dispatch((Node)o);
						if( returnValue != null ) return returnValue;
					}
				}
				return null;
			}
		}.dispatch(vtableTree));
	}
	
	public void setupTree() {
		vtableTree = GNode.create("Object");
		GNode stringNode = GNode.create("String");
		GNode classNode = GNode.create("Class");
		vtableTree.add(stringNode);
		vtableTree.add(classNode);
		currentNode = null;
	}
	
	public GNode getVtableTree() {
		return vtableTree;
	}
	
}