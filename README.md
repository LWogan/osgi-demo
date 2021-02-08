# OSGi Launcher and Sandbox Installer Demo

### Build
```gradlew build``` to build the project and copy the subproject bundles built to the launchers resources folder

### Schema Design
https://github.com/corda/platform-eng-design/blob/lorcan/cpk-schema-design/core/corda-5/corda-5.0/osgi/cpk-schema-design.md

### Launcher
Main.kt will:
- create a Hibernate session
- treat the defined resource dirs of bundles as CPK/Sandbox
- each dir saved as a CPK to DB
- all bundles in each dir saved to DB with a reference to it's CPK. For simplicity all main bundles dependencies are stored in one dir.
- launch an OSGi framework
- read "CPK"s from the DB, install the bundles to their own sandbox
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

#### Log reader Bundle
- additional example of OSGi Service
- centralises logging via the OSGi Logger Service
- other methods of logging are available. this was included for demo purposes.

#### Sandbox Bundle 
Sandbox functionality exposed in a bundle to be installed into the network. 