# gwt-notabene-maven

Progetto svolto per l'esame di Ingegneria del Software del corso di Informatica per il Management a.a. 2024/2025

netstat -ano | findstr :9876
taskkill /PID ù /F

cd gwt-notabene-maven;
mvn -U -e gwt:codeserver -pl notabene-client -am


cd gwt-notabene-maven;
mvn -U jetty:run -pl notabene-server -am -Denv=dev

to run the application in developer mode (the URL is `http://localhost:8080/`). 

From now on, whenever you modify the code inside the client module a simple reload on the browser will update the running code on the fly.

If you modify the code inside the server module you can update the server-side part of the running application by issuing

mvn -U compile -pl *-server -am

netstat -ano | findstr :9876
taskkill /PID ù /F
