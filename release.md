# kmdp-openapi-spring-archetype
##Release Instructions

Affected variables:
* project.parent.version
* project.version (SELF)
        

### Release Branch
1. Set root POM's version and parent.version to desired fixed version
  * The version MUST match the ${kmdp.archetype.version} variable in the BOM

### Nex Dev Branch
1. Set parent and project to the next desired version
  * Use mvn versions:set and update-child-modules to ensure all children are updated
