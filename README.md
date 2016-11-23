# number-encoding

## Building number-encoding locally

	git clone https://github.com/alexaopgit/number-encoding.git
	cd number-encoding
	./mvnw package


## Running number-encoding locally
You can specify paths to files in parameters on start
    
    dictionary - path to the dictionary file
    input - path to the input file

	cd target
	java -jar numberencoding-0.0.1-SNAPSHOT.jar dictionary=<path_to_dictionary> input=<path_to_input>
	

Example:

    java -jar numberencoding-0.0.1-SNAPSHOT.jar dictionary=./dictionary.txt input=./input.txt
    
The result directed to system.out