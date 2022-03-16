# comp4321project-g9-2022s
Hello! Welcome to the Phase 1 readme!

Please follow these steps to run the crawler:

1. Make sure mvn works, install it if needed, and add to path.

2. Ensure that line 89 of the pom.xml file carries this line:
<mainClass>com.project.app.Crawler</mainClass>

<<<<<<< HEAD
3. Please ensure that that there is no db folder before running, as it will make them output incorrect.
If there is need to run the program twice, make sure you delete the db folder each time before running it!

4. Then, run the following commands one after the other.
mvn package
mvn exec:java

("mvn package" will build the files, and "mvn exec:java" will run them)
=======
Update the path of the database to match the one for your environment in Line56 of Crawler.java. 

Then, run the following commands one after the other.

1. mvn package

2. mvn exec:java

"mvn package" will build the files, and "mvn exec:java" will run them

Please ensure that that there is no db folder before running, as it will make them output incorrect.
If there is need to run the program twice, make sure you delete the db folder each time before running it!

## Notes about the Phase 1 Submission:


**1.** We have more than 30 indexed pages in the spider_result.txt (We have 86 entries). That is because we ran the crawler with depth=2 in crawler.java, and because our crawler gets more than 30 pages from the first webpage, we have 86 entries total in our spider_result.txt. 

**2.** Our program does not yet check if two pages redirects to the same page, and thus there might be a few duplicate results in the current spider_result.txt. However, we are sure that there are more than 30 unique results. 
    
**3.** A lot of the websites have a Last Modified value of null. This is of no fault of the crawlers. Attached below is one website with no last modified value in the header and one that does have a last modified value. The spider_result.txt correctly displays the corresponding values for both. 
    
    
    
Website with NO last-modified value in the header. 
![Imgur Image](https://imgur.com/FSw9MRw.jpg)    
 
Website with last-modified value in the header.
![Imgur Image](https://imgur.com/rdk9wOw.jpg)
>>>>>>> 372294208bac3e3ae77f1b7cbbbfe980d50b8167
