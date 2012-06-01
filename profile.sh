#!/bin/bash

java -javaagent:lib/ext/jip-1.2-profile.jar -Dprofile.properties=bmvis2-profile.properties -jar dist/bmvis2-monolithic.jar $@
