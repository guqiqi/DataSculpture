# DataSculpture

## Resources

- DataSculpture: Main Folder
  - Processing: Main Processing resources
    - fluid_left: Work of the left screen
    - fluid_right: Work of the right screen
    - fluid_simulation: Work of the center screen
  - src/main/java: Main Java resources
    - dataCollector: Collect data form mindwave headset
    - fluidfrag: Resource file to render the work
    - processingDrawFromDB: Work run in Java(starts in main function) and read data from database
    - processingDrawFromFile: Work run in Java(starts in main function) and read data from txt file
  - pom.xml: Configuration file, determine the library needed

## Run using Processing
Open the .pde file with [Processing](https://processing.org/download/) Software, click run button

#### Notice

- If there is something wrong with the file path, you can check the path of the data file. For Mac OS user, you can open the terminal in the processing directory and input `pwd` to get the path of processing directory, and set the variable in line 104(i.e. fluid_file.pdefile) with "path(get form terminal)/data/test1.txt".

## Run using Java

Open .java file and run the main function

#### Notice

- The computer need to set up environment for Java
- The computer need to set up [Thinkgear Connector](http://developer.neurosky.com/docs/doku.php?id=thinkgear_connector_tgc)
- Data collector can only run in Windows system

