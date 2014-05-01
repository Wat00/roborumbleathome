@echo off
java -Djava.util.logging.config.file=logging.properties -cp target/roborumbleathome-coordinator-0.0.1-SNAPSHOT.jar;../codesize/target/codesize-0.0.1-SNAPSHOT.jar roborumbleathome.coordinator.controller.Main
rem -jar target\roborumbleathome-coordinator-0.0.1-SNAPSHOT.jar
pause