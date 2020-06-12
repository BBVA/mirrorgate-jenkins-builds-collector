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

package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class RestCall {

    private static final Logger LOGGER = Logger.getLogger(RestCall.class.getName());

    public MirrorGateResponse makeRestCallPost(String url, String jsonString, String user, String password) {

        HttpClientContext localContext = HttpClientContext.create();
        HttpPost post = new HttpPost(url);
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            if (user != null && password != null) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(user, password));
                localContext.setCredentialsProvider(credentialsProvider);
            }

            StringEntity requestEntity = new StringEntity(
                    jsonString,
                    ContentType.APPLICATION_JSON);
            post.setEntity(requestEntity);

            try (CloseableHttpResponse response = client.execute(post, localContext)) {
                int responseCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                return new MirrorGateResponse(responseCode, responseString);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "MirrorGate: Error posting to MirrorGate", e);
        } finally {
            post.releaseConnection();
        }
        return new MirrorGateResponse(HttpStatus.SC_BAD_REQUEST, "");
    }

    public MirrorGateResponse makeRestCallGet(String url, String user, String password) {

        HttpGet get = new HttpGet(url);
        HttpClientContext localContext = HttpClientContext.create();
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            if (user != null && password != null) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new
                        UsernamePasswordCredentials(user, password));
                localContext.setCredentialsProvider(credentialsProvider);
            }

            try (CloseableHttpResponse response = client.execute(get, localContext)) {
                int responseCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                return new MirrorGateResponse(responseCode, responseString);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "MirrorGate: Error connecting to MirrorGate", e);
        } finally {
            get.releaseConnection();
        }
        return new MirrorGateResponse(HttpStatus.SC_BAD_REQUEST, "");
    }

}