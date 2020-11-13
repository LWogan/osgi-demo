# OSGi Launcher and Installer Demo

### Build
```gradlew build``` to build the project and copy the subproject bundles built to the launchers resources folder

### Run
Main.kt will:
- create a hibernate session
- save all bundles in the defined resource folders to the bundles table
- An OSGi framework work will be started
- bundles will be read from the DB, installed and started
- greetings, yo and log-reader bundles communicate via OSGi Declarative Services
 