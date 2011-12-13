#!/bin/bash  

clear

 echo  "Test Script: Translate all test files."

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

 source setup.sh
 make
 

  # 1. Get Java file(s) from the examples folder
 cd src/xtc/oop/examples
 for file in *.java ; do
	JavaFiles=("${JavaFiles[@]}" "$file")
 done
 cd ..
 cd ..
 cd ..
 cd ..

 
 
 # 2. Put Java files into output folder
directoryExamples='src/xtc/oop/examples/'
 for var in "${JavaFiles[@]}"
     do
     #echo "$var"
     cp "$directoryExamples$var" "output/"$var""
	 # FIXME: For some programs, exits without printing err below
	 #java -cp output/ "$fileName" > "output/j.txt" || { echo "--- ERR: Java runtime error"; return 1; }
	 #echo "--- Ran $var and saved output to output/j.txt"
done 


 # 3. Translate Java files 
 directoryOutputs='output/'
 for java in "${JavaFiles[@]}"
     do
     java xtc.oop.Translator -translate $directoryOutputs$java

	 echo "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&  Translated input file: $java"
done 
 

 # 5. Get .cpp files from output/
 cd output/
 for file in *.cc ; do
	cppOutput=("${cppOutput[@]}" "$file")
 done


# 5.5 Copy Grimm's java_lang etc. files to output/
 cp ../src/xtc/oop/include/* .

 # 6. Compile CPP files. Form string for cpp arguments
failed=()
numbad=0
  for file in "${cppOutput[@]}"
     do
	echo "***************************************************  Compiling $file java_lang.cc"
    g++ $file java_lang.cc || { echo "*************************************************** ERR: CPP compile time error for file $file"; failed[numbad]=$file; numbad=`expr $numbad + 1`; }
done 
	echo "The following cpp files failed to compile:"
	echo ${failed[@]}

 # 7. Run and save output from CPP files
 # ./a.out > "c.txt" || { echo "--- ERR: CPP runtime error"; cd ..; return 1; }
 #   echo "--- Ran $allCPP java_lang.cc and saved output to output/c.txt"


 #  Return to xtc/  
 cd ..
 return 0;

# read -p "End of program - Press Enter to quit."


