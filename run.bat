@echo off
REM compile (.java in src -> .class in classes) and run on Windows
javac -d classes -cp "lib/*" src\*.java
if errorlevel 1 goto end
java -cp "classes;lib/*" AirlineMain
:end
pause
