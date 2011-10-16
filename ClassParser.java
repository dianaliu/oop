package xtc.oop;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;

public class ClassParser extends Visitor {
    // Constructs a Class Hierarchy tree

    // Root of classTree is Object
    GNode classTree; 
    GNode currentNode;
    
    public ClassParser() {
	setupTree();
    }
    
    public void visitClassDeclaration(GNode n) {
	
	// Set currentNode to name of the Class
	currentNode = GNode.create((String)n.get(1));
	visit(n);

	// Index 3 of ClassDeclaration is Implements
	// If implements == null, it implements Object which is root node
	if( n.get(3) == null ) classTree.add(currentNode);
	else {
	    // Find the class it extends, so we can add as child to it
	    // FIX: Is this the only way to traverse tree?
	    searchForClass( (String)n.getNode(3).getNode(0).getNode(0).get(0) ).add(currentNode);
	}
    }    
    
    public void visitMethodDeclaration(GNode n) {
	// Associates each method with it's respective class
	// FIX: setProperty only lets us pass one string.  
	// Investigate String sets or other Annotations
      
	// Set the "Vtable" property to class name
	// FIX: Rename property to "Class" or other.
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

		    // Found the parent class
		    if( ((String)n.getName()).equals(className) ) 
			return n;

		    // Keep searching
		    for( Object o : n) {
			if (o instanceof Node) {
			    GNode returnValue = (GNode)dispatch((Node)o);
			    if( returnValue != null ) return returnValue;
			}
		    }
		    // Assuming correct input, should never return null
		    // i.e. parent class always exists
		    return null; 
		}
	    }.dispatch(classTree));
    }
    
    public void setupTree() {

	// Initialize with types Grimm created in java_lang.h 
	classTree = GNode.create("Object");
	GNode stringNode = GNode.create("String");
	GNode classNode = GNode.create("Class");
	GNode arrayNode = GNode.create("Array");
	classTree.add(stringNode);
	classTree.add(classNode);
	classTree.add(arrayNode);
	currentNode = null;
    }
    
    public GNode getClassTree() {
	return classTree;
    }
    
}