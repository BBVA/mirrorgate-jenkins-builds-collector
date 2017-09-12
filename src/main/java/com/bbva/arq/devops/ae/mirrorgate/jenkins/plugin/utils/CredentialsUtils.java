/*
 * Copyright 2017 Banco Bilbao Vizcaya Argentaria, S.A..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.security.ACL;
import java.util.Collections;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;


public class CredentialsUtils {

    private CredentialsUtils() {
    }

    /**
     * Get the token from the credentials identified by the given id.
     *
     * @param credentialsId The id for the credentials
     * @return Jenkins credentials
     */
    public static StringCredentials getTokenCredentials(final String credentialsId) {
        return getJenkinsCredentials(credentialsId, StringCredentials.class);
    }

    /**
     * Get the credentials identified by the given id from the Jenkins
     * credential store.
     *
     * @param <T>
     * @param credentialsId The id for the credentials
     * @param credentialsClass The class of credentials to return
     * @return Jenkins credentials
     */
    public static <T extends Credentials> T getJenkinsCredentials(
            final String credentialsId, final Class<T> credentialsClass) {

        if (StringUtils.isEmpty(credentialsId)) {
            return null;
        }

        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(credentialsClass,
                        Jenkins.getInstance(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(credentialsId)
        );
    }
}
