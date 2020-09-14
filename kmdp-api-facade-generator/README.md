# KMDP API Facade Generator

## Getting Started

We use Swagger-codegen to create the Java client/server interfaces, where those interfaces expose the ReST nature of the client/server interaction, the 'facade' project creates one more layer of indirection

## Dependencies

### swagger-codegen

Swagger-Codegen Package is the Swagger Codegen project, which allows generation of API client libraries (SDK generation), server stubs and documentation automatically given an OpenAPI Spec.

``` xml
<dependency>
    <groupId>io.swagger</groupId>
    <artifactId>swagger-codegen</artifactId>
</dependency>
```

### junit-jupiter

Aggregates all JUnit Jupiter modules. JUnit Jupiter is the combination of the new programming model and extension model for writing tests and extensions in JUnit 5. The Jupiter sub-project provides a TestEngine for running Jupiter based tests on the platform

``` xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
</dependency>
```
