#!/bin/bash

mvn versions:set -DnewVersion=$1
mvn versions:update-child-modules
