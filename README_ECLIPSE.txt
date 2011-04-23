How to set up ReVerb as a project in Eclipse.

These instructions have been tested on the following setup:
- Mac OS X 10.5
- Eclipse Helios Service Release 1
- Subeclipse plugin 1.6 http://subclipse.tigris.org/

(1) Check out the nlp-svn project by issuing this command on the command line:

    cd CHECKOUT_DIR
    svn co svn+ssh://USER@turingc.cs.washington.edu/projects/WebWare7/nlp-svn    
where CHECKOUT_DIR is where you will store the project and USER is your 
username.

(2) Import the project into Eclipse:

    - Go to File > Import...
    - Under "General", choose "Existing Projects into Workspace".
    - Under "Select root directory:" pick 
        CHECKOUT_DIR/nlp-svn/trunk/projects/java/reverb
    - Click "Finish".

(3) The project should now be in the Eclipse Package Explorer at the left. 
    However, there should be a red "!" over the project name, indicating that
    there are build errors. To fix this:

    - Right-click the project to open the context menu.
    - Go to Build Path > Configure Build Path...
    - Click on the "Libraries" tab. There should be a list of build path
      entries that start with "NLP_TRUNK". This variable needs to be set.
    - Click on "Add Variable...".
    - Click on "Configure Variables...".
    - Click on "New...".
    - Enter "NLP_TRUNK" into the "Name:" field.
    - Enter the folder CHECKOUT_DIR/nlp-trunk for the "Path:" value.
    - Click "OK" on all of the open windows.
    - If Eclipse offers to rebuild the project, click "OK". 

You should now be able to make changes to the project and commit via SVN.
