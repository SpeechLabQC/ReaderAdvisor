#!/bin/sh

# Save the name of the Language Model
file=$1
path=$2

# Only create the files if the User provides a valid argument name
if [ ! -z "$1" ] && [ ! -z "$2" ]; then
   # Linking Libraries
   export LD_LIBRARY_PATH=$path/.cmuclmtk:$path/.sphinxbase:$LD_LIBRARY_PATH
	# Check if the file exists
	if [ -f "$file.txt" ]; then
	    # Creating the vocabulary for the file
	    $path/text2wfreq < $file.txt | $path/wfreq2vocab > $file.vocab
	    # Check that the vocabulary file was created successfully
	    if [ -f "$file.vocab" ]; then
		# Generate the arpa format language models
		$path/text2idngram -vocab $file.vocab -idngram $file.idngram < $file.txt
		# Check that the idngram file exists
		if [ -f "$file.idngram" ]; then
		    # Create the Arpa Language Model
		    $path/idngram2lm -vocab_type 0 -idngram $file.idngram -vocab $file.vocab -arpa $file.lm
		    # Delete the .idngram and .vocab files
		    rm $file.idngram
		    rm $file.vocab
		    # Check that the Arpa Language Model was created successfully
		    if [ -f "$file.lm" ]; then
		        # Create the Binary Arpa Language Model
		        $path/sphinx_lm_convert -i $file.lm -o $file.DMP
			
		        exit 1
		    else
		        echo "$file.lm does not exists."
		    fi
		else
		    echo "$file.idngram does not exists."
		fi
	    else
		echo "$file.vocab does not exists."
	    fi
	else
	    echo "$file.txt does not exists. Try again when the file is created."
	fi
else
   echo "Usage : createLanguageModel.command <language_model_name> <path_to_executable_files>"
fi

exit -1
