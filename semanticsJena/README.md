# Jena Engine Project managed by Maven 
This is a Maven-based Java project.

## Setup Instructions (with Terminal)

1. Ensure you have **Java 17+** and **Maven** installed.

2. Open a terminal and navigate to the project directory:
	```bash
	cd [Path_to_your_project]
	```

3. Run the following command to download dependencies and build:
	```bash
	mvn clean install
	```
4. Run the application:
	```bash
	mvn exec:java -Dexec.mainClass="com.example.Main"
	```

Maven will automatically download dependencies specified in pom.xml.


