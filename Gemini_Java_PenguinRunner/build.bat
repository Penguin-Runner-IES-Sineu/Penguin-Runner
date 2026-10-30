@echo off
if not exist "bin" mkdir bin
javac -encoding UTF-8 -d bin src/gemini/penguinrunner/*.java src/gemini/penguinrunner/*/*.java
echo Build complete.
pause

