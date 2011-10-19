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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;


import xtc.parser.ParseException;
import xtc.parser.Result;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Location;
import xtc.tree.Printer;

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaPrinter;

import xtc.lang.CParser;
import xtc.lang.CPrinter;

public class Translator extends xtc.util.Tool {
	
	/** Create a new translator. */
	public Translator() {
	}
	
	public String getName() {
		return "Java to C++ Translator";
	}
	
	public String getCopy() {
		return "Group";
	}
	
	public void init() {
		super.init();
		runtime.
		bool("getNode", "getNode", false, "Get the tree root node.").
		bool("printAST", "printAST", false, "Print Java AST.").
		bool("backToJava", "backToJava", false, "Convert Java AST to Java code.").
		bool("backToC", "backToC", false, "Convert C AST to C code.").
		bool("test", "test", false, "Test.").
		bool("dependencytest", "dependencytest", false, "Dep Test.");
	}
	
	public Node parse(Reader in, File file) throws IOException, ParseException {
		JavaFiveParser parser =
			new JavaFiveParser(in, file.toString(), (int)file.length());
		Result result = parser.pCompilationUnit(0);
		return (Node)parser.value(result);
	}  
	
	//Parse C++ file
	/*    public Node parse(Reader in, File file) throws IOException, ParseException {
	 CParser parser =
	 new CParser(in, file.toString(), (int)file.length());
	 Result result = parser.pTranslationUnit(0);
	 return (Node)parser.value(result);
	 }   */
	
	public void process(Node node) {
		if (runtime.test("printAST")) {
			runtime.console().format(node).pln().flush();
		}
		
		if( runtime.test("backToJava") ) {
			new JavaPrinter( runtime.console() ).dispatch(node);
			runtime.console().flush();
		}  
		
		if( runtime.test("backToC") ) {
			new CPrinter( runtime.console() ).dispatch(node);
			runtime.console().flush();
		}
		
		//Java to C++ translator prototype
		//make >> output.txt writes terminal output to file
		if( runtime.test("dependencytest") ) {
			Node depTree;
		
			//
			//Print out the current working directory - helps with file I/O
			//
			System.out.println("\n------------------------------");
			System.out.println("The Current Working Directory");
			System.out.println("------------------------------");
			String curDir = System.getProperty("user.dir");
			System.out.println(curDir);
			
			//
			//Begin analyzing the AST to determine all dependencies
			//
			System.out.println("\n-------------------------------");
			System.out.println("Initial Dependency Analysis Stage");
			System.out.println("-------------------------------");
			DependencyVisitor depVisitor = new DependencyVisitor();
			
			//call DependencyVisitor on node and store the addresses inside 
			//DependencyVisitor.addressArray
			depVisitor.dispatch(node);
			depVisitor.printAddressArray();
			//parse the dependencies listed in addressArray and append to node
			try {
				depTree = depVisitor.parseArray(node);
			} 
			catch (IOException e) {
				depTree = null;
				System.out.println("IOException");
			}
			catch (ParseException e) {
				depTree = null;
				System.out.println("ParseException");
			} 
			
			runtime.console().pln().format(depTree).pln().flush();
				
			
			//
			//Begin analyzing the AST to create the VTable
			//
/* 			System.out.println("\n-----------------------------");
			System.out.println("Analyzing Virtual Table Stage");
			System.out.println("-----------------------------");
			
			AnalyzeVisitor myVisitor = new AnalyzeVisitor();
			myVisitor.dispatch(node);
			new Visitor() {
				public void visit(Node n){
					System.out.println( n.getName() + " " + ((GNode)n).getProperty("VTable") );
					for( Object o : n) {
						if (o instanceof Node) dispatch((Node)o);
					}
				}
			}.dispatch(myVisitor.getVtableTree());
			runtime.console().format(myVisitor.getVtableTree()).pln().flush();  
*/
			
			//Translate to C++ - to do
			//Print out C++ code - to do
		}
		
		
	}
	
	
	/**
	 * Run the translator with the specified command line arguments.
	 *
	 * @param args The command line arguments
	 */
	public static void main(String[] args) throws IOException , ParseException {
		new Translator().run(args);
	}
	
}
