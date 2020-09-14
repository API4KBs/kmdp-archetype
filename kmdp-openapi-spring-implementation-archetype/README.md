# KMDP OpenAPI Spring Implementation Archetype

## Getting Started

This project generates an implementation shell for a KMDP generated service.

### Generating an Implementation Project
**Step 1: Build this archetype:** ``mvn clean install``

**Step 2: Generate the project:** In a different directory, start the archetype generation: ``mvn archetype:generate -DarchetypeArtifactId=kmdp-implementation-archetype -DarchetypeGroupId=edu.mayo.kmdp.archetype -DarchetypeVersion=1.0-SNAPSHOT``

**Step 3: Answer the interactive questions:**
* 'groupId' - the Maven groupId of the generated project 
* 'artifactId' - the Maven artifactId of the generated project 
* 'version' - the version of your project (default here is usually fine)
* 'package' - the default package 

* 'serviceGroupId' - the groupId of the KMDP Service 
* 'serviceArtifactId' - the artifactId of the KMDP Service 
* 'serviceVersion' - the version of the KMDP service 

_NOTE_: ```serviceGroupId```, ```serviceArtifactId```, and ```serviceVersion``` should match the ```groupId```, ```artifactId```, and ```version``` parameters respectively that were used to build a service using the ```service-archetype``` archetype.


**Step 4: Build the generated project.** Change directories to the generated directory (usually named whatever you put for 'artifactId') and run ``mvn clean install``

### Building the Implementation
There should be a package there with one (or possibly more) files in the classpath names ending in ```ApiDelegate```.
These are interfaces and should be implemented in your module.

You must also register them as Spring components. This is done by adding the ```@org.springframework.stereotype.Component``` annotation, for example:

```java
@Component
public class RepositoryAdapter implements ReposApiDelegate {
    //...
}
```

**Start the server.** 
Run ``mvn spring-boot:run``. If everything worked, when the server starts you should be able to go to ``http://localhost:8080`` and see your service.
