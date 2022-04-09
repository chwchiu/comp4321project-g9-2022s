# comp4321project-g9-2022s
Hello! Welcome to the Phase 1 readme!

Please follow these steps to run the crawler:

1. Make sure mvn works, install it if needed, and add to path.

2. Ensure that line 89 of the pom.xml file carries this line:
<mainClass>com.project.app.App</mainClass>

Update the path of the database to match the one for your environment in Line56 of Crawler.java. 

Then, run the following commands one after the other.

1. mvn clean package

2. mvn exec:java

"mvn package" will build the files, and "mvn exec:java" will run them

Please ensure that that there is no db folder before running, as it will make them output incorrect.
If there is need to run the program twice, make sure you delete the db folder each time before running it!
