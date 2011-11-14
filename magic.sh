#!/bin/bash  

 clear
 source setup.sh
 make
 clear

 echo  Test Script: Compares Java output to translated CPP output.

 # 0. Make output directory
if [ ! -d "output" ]
       then
           mkdir "output"
	   echo "Created directory output/"
      else
           echo "Files will write to output/"
fi

  # 1. Get Java file(s) from command line and add to array javaInput
 javaInput=()

function failure()
{
    echo "$@" >&2
    # FIXME: Don't completely exit terminal
    exit 1
#    read -p "Try again - Press Enter to quit."
}

 # FIXME: Assumes .java files, what if directory? 
 # Is input guarenteed to be a single .java file?
 for var in "$@"
         do
         # 2. Compile and run Java files. If errors, display & exit.
         cp $var "output/$var"
         javac -d "output/" "output/$var" || failure "--- ERR: Java compile time error"
         # 3. Save output from Java files
	 java "output/$var" > "output/jOut.txt" || failure "--- ERR: Java runtime error"

         javaInput=("${javaInput[@]}" "$var")   
 done 

 # 4. Translate Java files
 for ((a=0; a < ${#javaInput[@]}; a++)) 
	do
         java xtc.oop.Translator -translate ${javaInput[a]}
 #	 java -jar midterm-translator.jar -translate -debug ${javaFiles[0]}
 done
 
 # 5. Get outputted CPP files from output/
 cppOutput=()
 cd output/
 for file in 'dir -d *.cpp' ; do
	cppOutput=("${cppOutput[@]}" "$file")
 done

 # 6. Compile and run CPP files. If errors, display & exit.
 # FIXME: How to compile all cpp files?
 # g++ cppOutput() || failure "--- ERR: CPP compile time error"

 # 7. Save output from CPP files
 # ./a.out > "cOut.txt" || failure "--- ERR: CPP runtime error"

 # 8. Compare Java and CPP output
 pass=0
 if diff jOut.txt cOut.txt > /dev/null ; then
 	pass=${pass+1}
	echo "Success! Outputs were the same."
 else 
 	echo "Bummer. Outputs were different."
 fi

 # Not necessary, but return to xtc/  
 cd ..


 # FIXME: Isn't there just one output file?
 # echo "Number of translations that passed: ${pass}"
 # echo "Out of: ${#javaInput[@]}"
 read -p "End of program - Press Enter to quit."
