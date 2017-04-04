#!groovy
JENKINS_PLUGIN_REPO = "ssh://git@globaldevtools.bbva.com:7999/bgdfm/jenkins_plugin_collector.git"
JENKINS_PLUGIN_DIR = "mirrorGate-jenkins-plugin"
JENKINS_PLUGIN_BASEDIR = "jenkins_plugin_collector"
JENKINS_PLUGIN_PACKAGE = "mirrorgate-publisher.hpi"
JENKINS_HOST="globaldevtools.bbva.com"

def mirrorGateBuildPublishStep(buildStatus) {  
  sh """

    echo '{'    > _msg.json
    echo '    \"number\" : \"${env.BUILD_NUMBER}\",'    >> _msg.json
    echo '    \"timestamp\" : ' >> _msg.json
    date +%s >> _msg.json
    echo , >> _msg.json
    echo '    \"buildUrl\" : \"${env.BUILD_URL}\",'   >> _msg.json
    echo '    \"buildStatus\" : \"$buildStatus\",' >> _msg.json
    echo '    \"projectName\" : \"MirrorGate\",'  >> _msg.json
    echo '    \"repoName\" : \"jenkins-plugin-collector\",' >> _msg.json
    echo '    \"branch\" : \"${env.GIT_BRANCH}\"'  >> _msg.json
    echo '}'    >> _msg.json

    cat _msg.json

    curl -H "Content-Type: application/json" -X POST -d @_msg.json http://dev-mirrorgate-alb-167643159.eu-west-1.elb.amazonaws.com/mirrorgate/builds

  """
}

node ('internal-global') {
  try {

      mirrorGateBuildPublishStep ('InProgress')

      withCredentials([[$class: 'FileBinding', credentialsId: 'artifactory-maven-settings-global', variable: 'M2_SETTINGS']]) {
        sh 'mkdir $WORKSPACE/.m2 || true'        
        sh 'cp -f ${M2_SETTINGS} $WORKSPACE/.m2/settings.xml'
      }

      stage(' Checkout SCM ') {

       dir (JENKINS_PLUGIN_BASEDIR) {
         checkout(scm)
       }
      }

      stage(' Build app ') {
        withMaven(mavenLocalRepo: '$WORKSPACE/.m2/repository', mavenSettingsFilePath: '.m2/settings.xml') {
          dir (JENKINS_PLUGIN_BASEDIR) {
            sh "mvn test"
            sh "mvn clean package"
          }
        }        
      }

      stage(' Publish app ') {
      	step([$class: "ArtifactArchiver", artifacts: "${JENKINS_PLUGIN_BASEDIR}/target/${JENKINS_PLUGIN_PACKAGE}", fingerprint: true])
      }
      
      mirrorGateBuildPublishStep ('Success')

      stage(' Deploy to Jenkins ') {
      	if (env.BRANCH_NAME == "master") {
      	  withCredentials([[$class: 'UsernamePasswordMultiBinding',
                          credentialsId: 'bot-jenkins-ldap',
                          usernameVariable: 'JENKINS_USER',
                          passwordVariable: 'JENKINS_PWD']]){

      	  	JENKINS_HOST="globaldevtools.bbva.com"
      	    dir (JENKINS_PLUGIN_BASEDIR) {

      	  	  //sh "curl ifconfig.co"
      	      //echo "curl -i -F file=@target/${JENKINS_PLUGIN_PACKAGE} https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/jenkins-api/pluginManager/uploadPlugin"
      	      sh "curl -i -F file=@target/${JENKINS_PLUGIN_PACKAGE} https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/jenkins-api/pluginManager/uploadPlugin"
      	      //echo "curl -kX POST https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/safeRestart"
      	      //sh "curl -kX POST https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/safeRestart"
      	    }
      	  }
      	
        }else {
      	  withCredentials([[$class: 'UsernamePasswordMultiBinding',
                          credentialsId: 'bot-dev-jenkins-ldap',
                          usernameVariable: 'JENKINS_USER',
                          passwordVariable: 'JENKINS_PWD']]){
      	  	JENKINS_HOST="dev.globaldevtools.bbva.com"
      	    dir (JENKINS_PLUGIN_BASEDIR) {

      	  	  sh "curl ifconfig.co"
      	      //echo "curl -i -F file=@target/${JENKINS_PLUGIN_PACKAGE} https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/jenkins-api/pluginManager/uploadPlugin"
      	      sh "curl -i -k -F file=@target/${JENKINS_PLUGIN_PACKAGE} https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/jenkins-api/pluginManager/uploadPlugin"
      	      //echo "curl -kX POST https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/safeRestart"
      	      sh "curl -kX POST https://${JENKINS_USER}:${JENKINS_PWD}@${JENKINS_HOST}/safeRestart"
      	    }
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

      mirrorGateBuildPublishStep ('Failure')

      throw e;
  } 
}
