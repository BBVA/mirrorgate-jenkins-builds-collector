#!groovy
JENKINS_PLUGIN_PACKAGE = "mirrorgate-publisher.hpi"

node ('internal-global') {
  try {

      withCredentials([[$class: 'FileBinding', credentialsId: 'artifactory-maven-settings-global', variable: 'M2_SETTINGS']]) {
        sh 'mkdir $WORKSPACE/.m2 || true'        
        sh 'cp -f ${M2_SETTINGS} $WORKSPACE/.m2/settings.xml'
      }

      stage(' Checkout SCM ') {
         checkout(scm)
      }

      stage('API - Clean app') {
        sh """
          ./gradlew clean
        """
      }

      stage('API - Build app') {
        sh """
          ./gradlew build
        """
      }

      stage('API - Run tests') {
        sh """
          ./gradlew test jacocoTestReport
        """
      }

      stage(' Publish app ') {
      	step([$class: "ArtifactArchiver", artifacts: "./build/libs/${JENKINS_PLUGIN_PACKAGE}", fingerprint: true])
      }
      
      stage(' Deploy to Jenkins ') {
      	if (env.BRANCH_NAME == "master") {
      	  withCredentials([[$class: 'UsernamePasswordMultiBinding',
                          credentialsId: 'bot-jenkins-ldap',
                          usernameVariable: 'JENKINS_USER',
                          passwordVariable: 'JENKINS_PWD']]){

            JENKINS_HOST="globaldevtools.bbva.com"
                
            // sh "curl ifconfig.co"
            sh "curl -i -F file=@build/libs/${JENKINS_PLUGIN_PACKAGE} https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/jenkins-api/pluginManager/uploadPlugin"
            // sh "curl -kX POST https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/safeRestart" 
         }
      	
        }else {
      	  withCredentials([[$class: 'UsernamePasswordMultiBinding',
                          credentialsId: 'bot-dev-jenkins-ldap',
                          usernameVariable: 'JENKINS_USER',
                          passwordVariable: 'JENKINS_PWD']]){
            JENKINS_HOST="dev.globaldevtools.bbva.com"

            sh "curl ifconfig.co"
            sh "curl -i -k -F file=@build/libs/${JENKINS_PLUGIN_PACKAGE} https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/jenkins-api/pluginManager/uploadPlugin"
            sh "curl -kX POST https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/safeRestart"
      	  }
        }
      }

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

      throw e;
  } 
}
