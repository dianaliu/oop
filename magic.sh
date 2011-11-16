#!/bin/bash  

 clear
 source setup.sh
 make
 clear

 echo  Test Script: Compares Java output to translated CPP output.

 # 0. Make/clean output directory
if [ ! -d "output" ]
       then
           mkdir "output"
	   echo "Created directory output/"
      else
	   echo "Found existing directory output/"
           read -p "rm all files in output/ (y/n)?" choice
           case "$choice" in 
             y|Y ) echo "rm-ing all files in output/"
		   rm output/*;;
             n|N ) echo "please rename existing output/ and try again"
		   exit 1;;
             * ) echo "invalid";;
           esac

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

 # FIXME: Assumes single .java file input. Consider files, dir, and import statements.
 for var in "$@"
         do
         # 2. Compile and run Java files.
         cp $var "output/$var"

	 cd output/
         javac $var || failure "--- ERR: Java compile time error"
         # 3. Save output from Java files
	 j=java
	 fileName=${var%.$j} # remove .java 
	 cd ..

	 java -cp output/ $fileName > "output/jOut.txt" || failure "--- ERR: Java runtime error"
	 echo "--- Successfully compiled and ran $var"


         javaInput=("${javaInput[@]}" "$var")   
 done 

 # 4. Translate Java files
 for ((a=0; a < ${#javaInput[@]}; a++)) 
	do
         java xtc.oop.Translator -translate ${javaInput[a]}
 #	 java -jar midterm-translator.jar -translate -debug ${javaFiles[0]}
 done
 
 # 5. Get .cpp files from output/
 cd output/

 cppOutput=()
 for file in *.cpp ; do
	cppOutput=("${cppOutput[@]}" "$file")
 done

# 5.5 Copy Grimm's java_lang etc. files to output/
 cp ../src/xtc/oop/include/* .

 # 6. Compile CPP files. Form string for cpp arguments
 allCPP=""
 for((i=0; i < ${#cppOutput[@]}; i++))
 do
     allCPP="${allCPP} ${cppOutput[i]}"
 done
  g++ $allCPP || failure "--- ERR: CPP compile time error"

 # 7. Run and save output from CPP files
  ./a.out > "cOut.txt" || failure "--- ERR: CPP runtime error"

 # 8. Compare Java and CPP output
 if diff jOut.txt cOut.txt > /dev/null ; then
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


