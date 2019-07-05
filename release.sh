#!/bin/bash

mvn versions:update-parent
mvn versions:set -DnewVersion=$1
