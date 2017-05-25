# MirrorGate plugin for Jenkins

![MirrorGate](./media/images/logo-ae.png)

# Development instructions

Run unit tests

    ./gradlew test

Create an HPI file to install in Jenkins (HPI file will be in `target/mirrorgate-publisher.hpi`).

    ./gradlew clean build 

# Important
The main project is JDK 1.8 compiled, if you have Jenkins running on previous Java versions, make sure to recompile core package with that previous version and then build this Jenkins plugin.

## Jenkins pipeline 
1. Install the plugin by using "Advanced" option in Jenkins Plugin Management option to manually upload the file
from local disk.
2. Restart jenkins.
3. Configure Global MirrorGate Publisher in Jenkins Manage Jenkins/Configure System. Enter MirrorGate API url such
 as `http://localhost:8080/mirrorgate/api`. There is no API token implented at this time and it is work in progress.

![Image](media/images/mirrorgate-jenkins-plugin-global-configuration.png)
