# MEA 3D API Service Archetype
This project generates a Spring Boot server, client, and empty implementation module given a SwaggerHub API.

## Before You Start

### Set Up Swaggerhub API Keys
You will need to configure your maven environment to use your Swaggerhub API key.

**Step 1:
Locate your SwaggerHub API key. It will be at this URL (https://app.swaggerhub.com/settings/apiKey) when you are logged in.

**Step 2:
In your ```$USER_HOME/.m2``` there should be a ```settings.xml``` file. If not, create one. Add the following to it:

```xml
<settings>
  <profiles>
    <profile>
      <id>inject-swaggerhub-key</id>
      <properties>
        <swaggerhub.api.key>put.your.api.key.here.</swaggerhub.api.key>
      </properties>
    </profile>
  </profiles>
 
  <activeProfiles>
    <activeProfile>inject-swaggerhub-key</activeProfile>
  </activeProfiles>
</settings>
```

### Build Prerequisite Projects
You will also need to build the ```kmdp-models``` project locally.

First, check out the latest ```kmdp-models``` repository:

```git clone https://github.com/API4KBs/kmdp-models```

In the ```kmdp-models``` directory, build the project:

```cd kmdp-models```
```mvn clean install```

## Generating a Project
**Step 1: Build this archetype:** ``mvn clean install``

**Step 2: Generate the project:** In a different directory, start the archetype generation: ``mvn archetype:generate -DarchetypeArtifactId=kmdp-service-archetype -DarchetypeGroupId=edu.mayo.mea3.archetype -DarchetypeVersion=1.0-SNAPSHOT``

**Step 3: Answer the interactive questions:**
* 'groupId' - the Maven groupId of the generated project 
* 'artifactId' - the Maven artifactId of the generated project 
* 'version' - the version of your project (default here is usually fine)
* 'package' - the default package 
* 'apiName' - the "name" of the SwaggerHub API 
* 'apiOwner' - the "owner" of the SwaggerHub API 
* 'apiVersion' - the "version" of the SwaggerHub API 

* 'modelDependencyGroupId' - the KMDP Model groupId
* 'modelDependencyArtifactId' - the KMDP Model artifactId
* 'modelDependencyVersion' - the KMDP Model version


**Step 4: Build the generated project.** Change directories to the generated directory (usually named whatever you put for 'artifactId') and run ``mvn clean install``

