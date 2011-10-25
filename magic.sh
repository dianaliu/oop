#!/bin/bash  

 clear
 source setup.sh
 
 echo This is a Java to C++ translator
 echo This shell script is an automated tester that runs the translator and
 echo compares the .cpp files it output to the expected files provided 
 echo Group: Robert, Diana, Hernel, Kirim

 
 #initialize array of java test files
 javaFiles=(Test.java Test.java Test.java)
 #initialize array of expected output files
 expectedCppFiles=(Exp0.cpp Exp1.cpp Exp2.cpp)
 pass=0
 
 for ((a=0; a <= 2 ; a++)) 
	do
	 java -jar midterm-translator.jar -translate -debug ${javaFiles[0]}
 	 
    done
 
 if diff diff1.cpp diff2.cpp > /dev/null ; then
 	pass=${pass+1}
 else 
 	echo different
 fi
 
 
 echo "Number of translations that passed: ${pass}"
 echo "Out of: 3"
 echo "( A work in progress )"
 read -p "End of program - Press Enter to quit."

#todo:
#java >> .txt
