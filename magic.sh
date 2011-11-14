#!/bin/bash  

 clear
 source setup.sh
 make
 
 echo  Test Script: Compares Java output to translated CPP output.

  # 1. Get Java file(s) from command line and add to array javaInput
 javaInput=()
 # FIXME: Assumes .java files, what if directory? 
 # Is input guarenteed to be a single .java file?
 for var in "$@"
         do
         javaInput=("${javaInput[@]}" "$var")   
 done 

 # 2. Compile and run Java files. If errors, display & exit.

 # 3. Save output from Java files

 # 4. Translate Java files
 for ((a=0; a < ${#javaInput[@]}; a++)) 
	do
         java xtc.oop.Translator -translate ${javaInput[a]}
 #	 java -jar midterm-translator.jar -translate -debug ${javaFiles[0]}
 done
 
 # 5. Get outputted CPP files
 cppOutput=()

 # 6. Compile and run CPP files. If errors, display & exit.

 # 7. Save output from CPP files

 # 8. Compare Java and CPP output
 pass=0
 if diff diff1.cpp diff2.cpp > /dev/null ; then
 	pass=${pass+1}
	echo "Success! Outputs were the same."
 else 
 	echo "Bummer. Outputs were different."
 fi
 
 # FIXME: Isn't there just one output file?
 # echo "Number of translations that passed: ${pass}"
 # echo "Out of: ${#javaInput[@]}"
 read -p "End of program - Press Enter to quit."
