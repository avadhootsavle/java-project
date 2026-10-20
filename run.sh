#!/bin/bash
# compile (.java in src -> .class in classes) and run on Mac / Linux
javac -d classes -cp "lib/*" src/*.java && java -cp "classes:lib/*" AirlineMain
