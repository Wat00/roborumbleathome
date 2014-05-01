@echo off
java -Djava.util.logging.config.file=logging.properties -cp ..\roborumbleathome-coordinator\target\roborumbleathome-coordinator-0.0.1-SNAPSHOT.jar;target\roborumbleathome-worker-0.0.1-SNAPSHOT.jar;C:\Java\literumble-1.8.1.0-1\libs\robocode.jar roborumbleathome.worker.Main
pause