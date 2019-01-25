/*
 * Copyright 2017 Banco Bilbao Vizcaya Argentaria, S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

node('global') {
    withEnv(['CI=true']) {

        stage('Checkout SCM') {
            checkout(scm)
        }

        stage('Clean') {
            sh "./gradlew clean"
        }

        stage('Build plugin') {
            sh "./gradlew build jpi"
        }

        stage('Run tests') {
            sh "./gradlew test jacocoTestReport"
        }
    }

    if(env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop') {

        stage('Publish plugin') {

            try{

                withCredentials([usernamePassword(
                                  credentialsId   : "bot-mirrorgate-st",
                                  usernameVariable: 'mavenUser',
                                  passwordVariable: 'mavenPassword')]) {

                    if(env.BRANCH_NAME == 'develop'){

                        sh "./gradlew uploadArchives"

                    } else if(env.BRANCH_NAME == 'master'){
                        withCredentials([usernamePassword(
                                        credentialsId   : 'bot-mirrorgate-gpg',
                                        usernameVariable: 'GPG_ID',
                                        passwordVariable: 'GPG_PASSWORD'),
                                    file(
                                        credentialsId: 'mirrorgate-secring',
                                        variable: 'FILE'),]) {
                                            sh "./gradlew uploadArchive -Dorg.gradle.project.signing.keyId=$GPG_ID -Dorg.gradle.project.signing.password=$GPG_PASSWORD -Dorg.gradle.project.signing.secretKeyRingFile=$FILE"
                                        }
                    }

                }

            } catch(Exception e) {
                logger warning "Error publishing plugin {}" e
                currentBuild.result = "UNSTABLE"
            }
        }
    }

}
