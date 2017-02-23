#!groovy
JENKINS_PLUGIN_REPO = "ssh://git@globaldevtools.bbva.com:7999/bgdfm/jenkins_plugin_collector.git"
JENKINS_PLUGIN_DIR = "hygieia-jenkins-plugin"
JENKINS_PLUGIN_BASEDIR = "jenkins_plugin"
HYGIEIA_BASEDIR = "hygieia" 
HYGIEIA_REPO = "https://github.com/capitalone/Hygieia.git"
JENKINS_PLUGIN_PACKAGE = "hygieia-publisher.hpi"

node ('global') {
  try {

      hygieiaBuildPublishStep buildStatus: 'InProgress'

      withCredentials([[$class: 'FileBinding', credentialsId: 'artifactory-maven-settings-global', variable: 'M3_SETTINGS']]) {
        sh 'mkdir $WORKSPACE/.m3'        
	    sh 'cp -f ${M3_SETTINGS} $WORKSPACE/.m3/settings.xml'
      }

      stage('-------- Checkout SCM ---------') {
        dir (JENKINS_PLUGIN_BASEDIR) {
        	git url: "${JENKINS_PLUGIN_REPO}", branch: 'develop'
        }
       //dir (HYGIEIA_BASEDIR) {
       // 	git url: "${HYGIEIA_REPO}", branch: 'master'
       // 	sh "rm ${JENKINS_PLUGIN_DIR} -Rf"
       // 	sh "mkdir ${JENKINS_PLUGIN_DIR}"
       // 	sh "cp ${WORKSPACE}/${JENKINS_PLUGIN_BASEDIR}/* ${WORKSPACE}/${HYGIEIA_BASEDIR}/${JENKINS_PLUGIN_DIR} -R"
       // 	sh "sed -i '/<module>collectors*\$/d' pom.xml"
       // 	sh "sed -i '/<module>UI*\$/d' pom.xml"
       //}
      }

      //stage('---------- Compile Hygieia Core -----------') {
      //	dir (HYGIEIA_BASEDIR) {
      //   sh "cd core; mvn clean install"
      //  }
      //}

      stage('----------- Build app -----------') {
        withMaven(maven: 'M3', mavenLocalRepo: '$WORKSPACE/.m3/repository', mavenSettingsFilePath: '$WORKSPACE/.m3/settings.xml') {

          sh "cd ${JENKINS_PLUGIN_BASEDIR}"
          sh "mvn test"
          sh "mvn clean package"
		}        
      }

      stage('------------ Publish app -----------') {
      	step([$class: "ArtifactArchiver", artifacts: "${HYGIEIA_BASEDIR}/${JENKINS_PLUGIN_DIR}/target/${JENKINS_PLUGIN_PACKAGE}", fingerprint: true])
      }
      
      hygieiaBuildPublishStep buildStatus: 'Success'

  } catch(Exception e) {
      sh """
      curl -X POST \
      -H 'Content-type: application/json' \
      --data '{
        \"attachments\":[
            {
              \"fallback\":\"${env.JOB_NAME} - Build # ${env.BUILD_NUMBER} - FAILURE!\",
              \"color\":\"#D00000\",
              \"fields\":[
                  {
                    \"title\":\"${env.JOB_NAME}\",
                    \"value\":\"<${env.BUILD_URL}|Build # ${env.BUILD_NUMBER} - FAILURE!>\",
                    \"short\":false
                  }
              ]
            }
        ]
      }' \
      https://hooks.slack.com/services/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
      """

      hygieiaBuildPublishStep buildStatus: 'Failure'

      throw e;
  } 
}