# ReVerb

ReVerb is a program that automatically identifies and extracts binary 
relationships from English sentences. ReVerb is designed for Web-scale 
information extraction, where the target relations cannot be specified in 
advance and speed is important. 

ReVerb takes raw text as input, and outputs (argument1, relation phrase, 
argument2) triples. For example, given the sentence "Bananas are an excellent 
source of potassium," ReVerb will extract the triple (bananas, be source of, 
potassium). 

More information is available at the ReVerb homepage: 
<http://reverb.cs.washington.edu>

## Quick Start
If you want to run ReVerb on a small amount of text without modifying its 
source code, we provide an executable jar file that can be run from the command 
line. Follow these steps to get started:

1.  Download the latest ReVerb jar from 
<http://reverb.cs.washington.edu/reverb-latest.jar>

2.  Run `java -Xmx512m -jar reverb-latest.jar yourfile.txt`.

3.  Run `java -Xmx512m -jar reverb-latest.jar -h` for more options.

## Building
Building ReVerb from source requires Apache Maven (<http://maven.apache.org>). 
Run this command to download the required dependencies, compile, and create a 
single executable jar file.

    mvn clean compile assembly:single

The compiled class files will be put in the `target/classes` directory. The 
single executable jar file will be written to 
`target/reverb-core-*-jar-with-dependencies.jar` where `*` is replaced with
the version number.  

## Command Line Interface
Once you have built ReVerb, you can run it from the command line.

The command line interface to ReVerb takes plain text or HTML as input, and 
outputs a tab-separated table of output. Each row in the output represents a 
single extracted (argument1, relation phrase, argument2) triple, plus metadata. 
The output has the following columns:

1. The filename (or `stdin` if the source is standard input)
2. The sentence number this extraction came from. 
3. Argument1 words, space separated
4. Relation phrase words, space separated
5. Argument2 words, space separated
6. The start index of argument1 in the sentence. For example, if the value is 
`i`, then the first word of argument1 is the `i-1`th word in the sentence.
7. The end index of argument1 in the sentence. For example, if the value is 
`j`, then the last word of argument1 is the `j`th word in the sentence.
8. The start index of relation phrase.
9. The end index of relation phrase.
10. The start index of argument2.
11. The end index of argument2.
12. The confidence that this extraction is correct. The higher the number, the 
more trustworthy this extraction is.
13. The words of the sentence this extraction came from, space-separated.
14. The part-of-speech tags for the sentence words, space-separated. 
15. The chunk tags for the sentence words, space separated. These represent a 
shallow parse of the sentence. 
16. A normalized version of arg1. See the `BinaryExtractionNormalizer` javadoc 
for details about how the normalization is done.
17. A normalized version of rel.
18. A normalized version of arg2.

For example:

    $ echo "Bananas are an excellent source of potassium." | 
        ./reverb -q | tr '\t' '\n' | cat -n
     1  stdin
     2  1
     3  Bananas
     4  are an excellent source of
     5  potassium
     6  0
     7  1
     8  1
     9  6
    10  6
    11  7
    12  0.9999999997341693
    13  Bananas are an excellent source of potassium .
    14  NNS VBP DT JJ NN IN NN .
    15  B-NP B-VP B-NP I-NP I-NP I-NP I-NP O
    16  bananas
    17  be source of
    18  potassium

For a list of options to the command line interface to ReVerb, run `reverb -h`. 

### Examples

#### Running ReVerb on small set of files
    ./reverb file1 file2 file3 ...

#### Running ReVerb on standard input
    ./reverb < input

#### Running ReVerb on HTML files
The `--strip-html` flag (short version: `-s`) removes tags from the input 
before running ReVerb. 

    ./reverb --strip-html myfile.html

#### Running ReVerb on a list of files
You may have an entire directory structure that you want to run ReVerb on. 
ReVerb takes approximately 10 seconds to initialize, so it is not efficient to 
start a new process for each file. To pass ReVerb a list of paths, use the `-f` 
switch:

    # Run ReVerb on all files under mydir/
    find mydir/ -type f | ./reverb -f

## Java Interface
To include ReVerb as a library in your own project, please take a look at the 
example class `ReVerbExample` in the 
`src/main/java/edu/washington/cs/knowitall/examples` directory. 

When running code that calls ReVerb, make sure to increase the Java Virtual 
Machine heap size by passing the argument `-Xmx512m` to java. ReVerb loads 
multiple models into memory, and will be significantly slower if the heap size 
is not large enough.

## Using Eclipse
To modify the ReVerb source code in Eclipse, use Apache Maven to create the 
appropraite project files:

    mvn eclipse:eclipse

Then, start Eclipse and navigate to File > Import. Then, under General, select 
"Existing Projects into Workspace". Then point Eclipse to the main ReVerb 
directory.

## Including ReVerb as a Dependency
If you want to start a new project that depends on ReVerb, first create a new
skeleton project using Maven. The following command will ask you to fill in
the details of your project name, etc.:

    mvn archetype:generate

Next, add a new repository to the project `pom.xml` file. You can do this by
adding the following XML under the `<project>` element:

    <repositories>
      <repository>
        <id>knowitall</id>
        <url>http://knowitall.cs.washington.edu/maven2</url>
      </repository>
    </repositories>

Finally, add ReVerb as a dependency. Do this by adding the following XML under
the `<project>` element:

    <dependencies>
      <dependency>
        <groupId>edu.washington.cs.knowitall</groupId>
        <artifactId>reverb-core</artifactId>
        <version>1.3.0</version>
      </dependency>
    </dependencies>

Your final `pom.xml` file should look something like this:

    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>
    
      <groupId>mygroup</groupId>
      <artifactId>myartifact</artifactId>
      <version>1.0-SNAPSHOT</version>
      <packaging>jar</packaging>

      <name>myartifact</name>
      <url>http://maven.apache.org</url>
    
      <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      </properties>
    
      <repositories>
        <repository>
          <id>knowitall</id>
          <url>http://knowitall.cs.washington.edu/maven2</url>
        </repository>
      </repositories>
    
      <dependencies>
        <dependency>
          <groupId>junit</groupId>
          <artifactId>junit</artifactId>
          <version>3.8.1</version>
          <scope>test</scope>
        </dependency>
        <dependency>
          <groupId>edu.washington.cs.knowitall</groupId>
          <artifactId>reverb-core</artifactId>
          <version>1.3.0</version>
        </dependency>  
      </dependencies>
    </project>

You should be able to include ReVerb in your code now. You can try this out by
including `import edu.washington.cs.knowitall.extractor.ReVerbExtractor` in 
your program.

## Retraining the Confidence Function
ReVerb includes a class for training new confidence functions, given a list of 
labeled examples, called `ReVerbClassifierTrainer`. Example code for training a 
new confidence function `confFunction` is shown below - the non-trivial part is 
likely to be converting your labeled data to an 
`Iterable<LabeledBinaryExtraction>`.

Example Pseudocode:

    // Provide your labeled data here
    Iterable<LabeledBinaryExtraction> myLabeledData = ??? 
    ReVerbClassifierTrainer trainer = 
        new ReVerbClassifierTrainer(myLabeledData);
    Logistic classifier = trainer.getClassifier();
    ReVerbConfFunction confFunction = new ReVerbConfFunction(classifier);
     // confFunction is ready to use here.
    double conf = confFunction.getConf(extraction);

If you already have a list of binary labeled ReVerb extractions, it should be 
easy to convert them to `ChunkedBinaryExtraction` objects, and then to 
`LabeledBinaryExtraction` objects (see the constructors for these classes). 
Also note that ReVerb includes a `LabeledBinaryExtractionReader` and `Writer` 
class. You may wish to (re-)serialize your data using 
`LabeledBinaryExtractionWriter` - this will put it in the same format as all 
previous data used to train ReVerb confidence functions, and it will be easy to 
read in the future with `LabeledBinaryExtractionReader`. 


## Help and Contact
For more information, please visit the ReVerb homepage at the University of 
Washington: <http://reverb.cs.washingotn.edu>.

## Contributors
* Anthony Fader <http://www.cs.washington.edu/homes/afader>
* Michael Schmitz <http://www.schmitztech.com/>
* Robert Bart (rbart at cs.washington.edu)
* Janara Christensen <http://www.cs.washington.edu/homes/janara>
* Niranjan Balasubramanian <http://www.cs.washington.edu/homes/niranjan>
* Jonathan Berant <http://www.cs.tau.ac.il/~jonatha6>

## Citing ReVerb
If you use ReVerb in your academic work, please cite ReVerb with the following 
BibTeX citation:

    @inproceedings{ReVerb2011,
      author =   {Anthony Fader and Stephen Soderland and Oren Etzioni},
      title =    {Identifying Relations for Open Information Extraction},
      booktitle =    {Proceedings of the Conference of Empirical Methods
                      in Natural Language Processing ({EMNLP} '11)},
      year =     {2011},
      month =    {July 27-31},
      address =  {Edinburgh, Scotland, UK}
    }
