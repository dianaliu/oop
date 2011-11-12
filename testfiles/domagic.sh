#!/bin/bash

 source setup.sh
 
 echo This is a Java to C++ translator
 echo This shell script is an automated tester that runs 
 echo the translator and outputs a .cpp sourcecode. The
 echo output of the .cpp program is compared to the saved
 echo output of the original Java program.
 echo
 echo Group: Robert, Diana, Hernel, Kirim
 
 #compile Translator
 make

 
 #initialize array of java test files
 javaFiles=(xxHello.java xxMath.java xxDependency.java ) #
 #initialize array of expected output files
 expectedOutput=(xxHello.txt xxMath.txt xxDependency.txt) #
 
 #keeps track of number of passed and number failed
 pass=0
 fail=0
 
 #loop over the test a <= number of test files in array javaFiles
 #a = number of test files - 1
 for ((a=0; a <= 2; a++)) 
	do
		#for submissions - jar support
		#java -jar midterm-translator.jar -translate -debug ${javaFiles[0]}
 		java xtc.oop.Translator -translate -debug ${javaFiles[a]}
 		#compile/run cpp code(change to Code.cpp) and save terminal output cOut
 		g++ Code.cpp  
 		./a.out > cOut.txt
 		#compare cpp terminal output to presaved Java terminal output
 		if diff ${expectedOutput[a]} cOut.txt > /dev/null ; then
 			pass=`expr $pass + 1` 
 		else 
 			fail=`expr $fail + 1`  
 			failed[fail]=${javaFiles[a]}
 			
 		fi
    done
    
 echo
 echo
 
 #print out test results
 echo The following testing cases failed:
 for ((b=0; b <= fail; b++))
 	do
 		echo ${failed[b]}
 	done 
 
 echo
 echo "Passes: ${pass} Fails: ${fail} "
 echo
 read -p "End of program - Press Enter to quit."
#todo:
#java >> .txt
