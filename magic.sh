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
		   return 1;;
             * ) echo "invalid";;
           esac

           echo "Files will write to output/"
fi

  # 1. Get Java file(s) from command line and add to array javaInput
 javaInput=()

 # 2. Compile and run Java files.
 for var in "$@"
         do
         cp "$var" "output/"$var""

	 cd output/

	 echo "--- Received input file: $var"
         javac $var || echo "--- ERR: Java compile time error"; cd ..; return 1;
	     
         # 3. Save output from Java files
	 j=java
	 fileName=${var%.$j} # remove .java 
	 cd ..

	 # FIXME: For some programs, exits without printing err below
	 java -cp . output/"$fileName" > "output/jOut.txt" || 
	   echo "--- ERR: Java runtime error"; return 1;

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
  g++ $allCPP || echo"--- ERR: CPP compile time error"; cd ..; return 1;

 # 7. Run and save output from CPP files
  ./a.out > "cOut.txt" || echo "--- ERR: CPP runtime error"; cd ..; return 1;

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


