# KMD Platform OpenAPI Spring Archetypes

public kmdp-archetype build status:

[![Build Status](https://travis-ci.com/API4KBs/kmdp-archetype.svg?branch=master)](https://travis-ci.com/API4KBs/kmdp-archetype)
[ ![Download](https://api.bintray.com/packages/api4kbs/API4KP-Mvn-Repo/kmdp-archetype/images/download.svg) ](https://bintray.com/api4kbs/API4KP-Mvn-Repo/kmdp-archetype/_latestVersion)

## Getting Started

The service and implementation archetype projects generate KMD Platform (KMDP) services based on Swagger APIs. 

### Build (Maven)

Running ```mvn clean install``` from this directory will build both the service and implementation archetypes

### Profiles

- **id**: public
- **Active By Default**: false
- **Description**: Publishes to the open standard project https://github.com/API4KBs/kmdp-archetype

## SubModules

### KMDP OpenAPI Spring Service Archetype

An archetype that generates the following component from a Swagger API:
* A server client
* The service interfaces
* A base pom for implementations to extend
* A server framework for launching a SpringBoot REST server

### KMDP OpenAPI Spring Implementation Archetype

A companion to the service-archetype, this archetype generates the framework for implementing one of the above generated services

### KMDP API Facade Generator

We use Swagger-codegen to create the Java client/server interfaces, where those interfaces expose the ReST nature of the client/server interaction, the 'facade' project creates one more layer of indirection

## Documentation

- [Swagger.io OpenAPI Specification](https://swagger.io/specification/)
