# comp4321project-g9-2022s

Make sure mvn works

Ensure that line 89 of the pom.xml file carries this line:
<mainClass>com.project.app.Crawler</mainClass>

Then, run the following commands one after the other.
mvn package
mvn exec:java

"mvn package" will build the files, and "mvn exec:java" will run them

Please ensure that that there is no db folder before running, as it will make them output incorrect.
If there is need to run the program twice, make sure you delete the db folder each time before running it!