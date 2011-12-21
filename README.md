# ReVerb

ReVerb is a program that automatically identifies and extracts binary relationships from English sentences. ReVerb is designed for Web-scale information extraction, where the target relations cannot be specified in advance and speed is important. 

ReVerb takes raw text as input, and outputs (argument1, relation phrase, argument2) triples. For example, given the sentence "Oranges are high in vitamin C," ReVerb will extract the triple (oranges, are high in, vitamin c). 

More information is available at the ReVerb homepage: <http://reverb.cs.washington.edu>

## Quick Start
If you want to run ReVerb on a small amount of text without modifying its source code, we provide an executable jar file that can be run from the command line. Follow these steps to get started:

1.  Download the latest ReVerb jar from <http://reverb.cs.washington.edu/reverb-latest.jar>

2.  Run `java -Xmx512m -jar reverb.jar yourfile.txt`.

3.  Run `java -Xmx512m -jar reverb.jar -h` for more options.

## Building
Building ReVerb from source requires Apache Maven (<http://maven.apache.org>). Run this command to download the required dependencies, compile, and create a single executable jar file.

    mvn clean compile assembly:single

## Command Line Interface
Once you have built ReVerb, you can run it from the command line.

The command line interface to ReVerb takes plain text or HTML as input, and outputs a tab-separated table of output. Each row in the output represents a single extracted (argument1, relation phrase, argument2) triple, plus metadata. The output has the following columns:

1. The filename (or `stdin` if the source is standard input)
2. The sentence number this extraction came from. 
3. Argument1 words, space separated
4. Relation phrase words, space separated
5. Argument2 words, space separated
6. The start index of argument1 in the sentence. For example, if the value is `i`, then the first word of argument1 is the `i-1`th word in the sentence.
7. The end index of argument1 in the sentence. For example, if the value is `j`, then the last word of argument1 is the `j`th word in the sentence.
8. The start index of relation phrase.
9. The end index of relation phrase.
10. The start index of argument2.
11. The end index of argument2.
12. The confidence that this extraction is correct. The higher the number, the more trustworthy this extraction is.
13. The words of the sentence this extraction came from, space-separated.
14. The part-of-speech tags for the sentence words, space-separated. 
15. The chunk tags for the sentence words, space separated. These represent a shallow parse of the sentence. 

For example:

    $ echo "Olympia is the capital city of Washington." | ./bin/reverb -q -s | tr '\t' '\n' | cat -n
     1  stdin
     2  1
     3  Olympia
     4  is the capital city of
     5  Washington
     6  0
     7  1
     8  1
     9  6
    10  6
    11  7
    12  0.9999999999644988
    13  Olympia is the capital city of Washington .
    14  NNP VBZ DT NN NN IN NNP .
    15  B-NP B-VP B-NP I-NP I-NP I-NP I-NP O

For a list of options to the command line interface to ReVerb, run `/bin/reverb -h`. 

### Examples

#### Running ReVerb on small set of files
    ./bin/reverb file1 file2 file3 ...

#### Running ReVerb on standard input
    ./bin/reverb < input

#### Running ReVerb on HTML files
The `--strip-html` flag (short version: `-s`) removes tags from the input before running ReVerb. 

    ./bin/reverb --strip-html myfile.html

#### Running ReVerb on a list of files
You may have an entire directory structure that you want to run ReVerb on. ReVerb takes approximately 10 seconds to initialize, so it is not feasible to simply start a new process for each file. To pass ReVerb a list of paths, use the `-f` switch:

    # Run ReVerb on all files under mydir/
    find mydir/ -type f | ./bin/reverb -f

## Java Interface
To include ReVerb as a library in your own project, please take a look at the example class `ReVerbExample` in the `src/main/java/edu/washington/cs/knowitall/examples` directory. 

When running code that calls ReVerb, make sure to increase the Java Virtual Machine heap size by passing the argument `-Xmx512m` to java. ReVerb loads multiple models into memory, and will be significantly slower if the heap size is not large enough.

## Using Eclipse
To modify the ReVerb source code in Eclipse, use Apache Maven to create the appropraite project files:

    mvn eclipse:eclipse

Then, start Eclipse and navigate to File > Import. Then, under General, select "Existing Projects into Workspace". Then point Eclipse to the main ReVerb directory.

## Retraining the Confidence Function

## Help and Contact
For more information, please visit the ReVerb homepage at the University of Washington: <http://reverb.cs.washingotn.edu>.

## Contributors
* Anthony Fader (afader at cs.washington.edu)
* Michael Schmitz (schmmd at cs.washington.edu)
* Robert Bart (rbart at cs.washington.edu)
* Janara Christensen (janara at cs.washington.edu)
* Niranjan Balasubramanian (getniranj at yahoo.com)
* Jonathan Berant (jonatha6 at post.tau.ac.il)

## Citing ReVerb
If you use ReVerb in your academic work, please cite ReVerb with the following BibTeX citation:

    @inproceedings{ReVerb2011,
      author =   {Anthony Fader and Stephen Soderland and Oren Etzioni},
      title =    {Identifying Relations for Open Information Extraction},
      booktitle =    {Proceedings of the Conference of Empirical Methods
                      in Natural Language Processing ({EMNLP} '11)},
      year =     {2011},
      month =    {July 27-31},
      address =  {Edinburgh, Scotland, UK}
    }
