# MEA 3D API Service Archetypes
[![Build Status](https://travis-ci.com/API4KBs/kmdp-archetype.svg?branch=master)](https://travis-ci.com/API4KBs/kmdp-archetype)
[ ![Download](https://api.bintray.com/packages/api4kbs/API4KP-Mvn-Repo/kmdp-archetype/images/download.svg) ](https://bintray.com/api4kbs/API4KP-Mvn-Repo/kmdp-archetype/_latestVersion)

These two project generate KMDP services based on Swagger APIs. Running the ```mvn clean install``` from this directory will build both archetypes.

## service-archetype
An archetype that generates the following component from a Swagger API:
* A server client
* Service interfaces
* A base pom for implementations to extend
* A server framework for launching a SpringBoot REST server

## implementation-archetype
A companion to the service-archetype, this archetype generates the framework for implementing one of the above generated services.
