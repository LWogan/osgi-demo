# OSGi Launcher and Sandbox Installer Demo

### Build
```gradlew clean build``` to build the project and copy the subproject bundles built to the launchers resources folder

### Launcher
Main.kt will:
- create a Hibernate session
- treat the defined resource dirs of bundles as CPKs
- each dir saved as a CPK to DB
- all bundles in each dir saved to DB with a reference to it's CPK
- launch an OSGi framework
- read "CPKs" from the DB, install the CPK bundles to their own sandbox
- give all sandboxes visibility to each other
- start up all installed bundles from all sandboxes
- greetings, yo and log-reader bundles communicate in their activator classes via OSGi Declarative Services
 
#### Greetings Bundle
- exposes (an unused) public api com.example.osgi.grettings.api
- hides api impl as package private com.example.osgi.grettings.impl
- calls out to YoService OSGi service in activator at startup

#### Yo Bundle
- exposes OSGi service com.example.osgi.yo.service
- hides service impl com.example.osgi.yo.impl
- registers yo service OSGi framework in activator

#### Log reader Bundle
- additional example of OSGi Service
- centralises logging via the OSGi Logger Service
- other methods of logging are available. this was included for demo purposes.
