/*
 * xtc - The eXTensible Compiler
 * Copyright (C) 2011 Robert Grimm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 * USA.
 */
package xtc.oop;

import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.Attribute;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Location;
import xtc.tree.Printer;

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;

import xtc.lang.CParser;
import xtc.lang.CPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import java.util.ArrayList;



public class Translator extends xtc.util.Tool {

     /** Create a new translator. */
    public Translator() {
	    // Nothing to do.
	}
    
    public String getName() {
	    return "Java to C++ Translator";
	}
    
    public String getCopy() {
	    return "Diana, Hernel, Kirim, & Robert";
	}

    public String getVersion() {
	return "0.1";
    }
    
    public void init() {
	    super.init();
		runtime.
		    bool("printAST", "printAST", false, "Print Java AST.").
		    bool("backToC", "backToC", false, "Print C AST to C code.").
		    bool("vtable", "vtable", false, "Create data structures for vtable and data layout").
			bool("translateToCPP", "translateToCPP", false, "Testing out cppprinter.");
	}
    
    public Node parse(Reader in, File file) throws IOException, ParseException {
	    JavaFiveParser parser =
		    new JavaFiveParser(in, file.toString(), (int)file.length());
		Result result = parser.pCompilationUnit(0);
		return (Node)parser.value(result);
	}  
    
    public void process(Node node) {
	    if (runtime.test("printAST")) {
		    runtime.console().format(node).pln().flush();
		}
	
	    if( runtime.test("backToC") ) {
		new CPrinter( runtime.console() ).dispatch(node);
		runtime.console().flush();
	    }
	    
	    if(runtime.test("vtable")) {
			
			final ClassLayoutParser clp = new ClassLayoutParser(node);
			
			new Visitor() {
				// When we encounter a class in the AST, send it to 
				// ClassLayoutParser 
				public void visitClassDeclaration(GNode n) {
					clp.addClass(n);
					visit(n);
				} // end visitClassDeclaration
				
				public void visit(Node n) {
					for( Object o : n) {
						if (o instanceof Node) dispatch((Node)o);
					}
				}
			}.dispatch(node);
			runtime.console().format(clp.getClassTree()).pln().flush();
			runtime.console().pln("------------");
			//		     runtime.console().format(clp.getDataLayoutTree("Object")).pln().flush();
	    } // end transform	
		
		if( runtime.test("translateToCPP") ) {
			//This simple visitor is just used to place a dummy vtable declaration node into each classes class body
			//for testing purposes.  It also tests and implements mutability of the AST tree.
			new Visitor() {
				public void visit(Node n) {
					for( Object o : n ) if( o instanceof Node ) dispatch((Node)o);
				}
				
				public void visitClassDeclaration(GNode n) {
					n.set(5, GNode.ensureVariable(GNode.cast(n.getNode(5)))); //make sure the class body is mutable (num of nodes can be changed).  If not, it is made mutable.
					n.getNode(5).add(0, GNode.create("VirtualTableDeclaration")); //insert a vtabledecl node, currently has no functionality, just for testing
					visit(n);
				}
			}.dispatch(node);
			
			//The real CPPPrinter, initalized and dispatched...
			new CPPPrinter( runtime.console() ).dispatch(node); 
			
			runtime.console().flush();
		}
    } // end process
    
    
			
    /**
     * Run the translator with the specified command line arguments.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
	new Translator().run(args);
    }
    
}
