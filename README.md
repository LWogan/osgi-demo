# OSGi Launcher and Installer Demo

### Build
```gradlew build``` to build the project and copy the subproject bundles built to the launchers resources folder

### Run
Main.kt will:
- create a hibernate session
- save all bundles in the defined resource folders to the bundles table
- launch an OSGi framework
- read bundles from the DB, install and start them
- greetings, yo and log-reader bundles communicate via OSGi Declarative Services
 