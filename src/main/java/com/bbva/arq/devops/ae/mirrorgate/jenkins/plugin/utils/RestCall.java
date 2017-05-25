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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;


public class RestCall {

    private static final Logger LOGGER = Logger.getLogger(RestCall.class.getName());

    //Fixme: Need refactoring to remove code duplication.
    public HttpClient getHttpClient() {
        return new HttpClient();
    }

    public Response makeRestCallPost(String url, String jsonString) {
        Response response;
        HttpClient client = getHttpClient();

        PostMethod post = new PostMethod(url);

        try {
            StringRequestEntity requestEntity = new StringRequestEntity(
                    jsonString,
                    "application/json",
                    "UTF-8");
            post.setRequestEntity(requestEntity);
            int responseCode = client.executeMethod(post);
            String responseString = post.getResponseBodyAsStream() != null ?
                    getResponseString(post.getResponseBodyAsStream()) : "";
            response = new Response(responseCode, responseString);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "MirrorGate: Error posting to MirrorGate", e);
            response = new Response(HttpStatus.SC_BAD_REQUEST, "");
        } finally {
            post.releaseConnection();
        }
        return response;
    }

    public Response makeRestCallGet(String url) {

        Response response;
        HttpClient client = getHttpClient();
        GetMethod get = new GetMethod(url);
        try {
            get.getParams().setContentCharset("UTF-8");
            int responseCode = client.executeMethod(get);
            String responseString = get.getResponseBodyAsStream() != null ?
                    getResponseString(get.getResponseBodyAsStream()) : "";
            response = new Response(responseCode, responseString);
        } catch (HttpException e) {
            LOGGER.log(Level.WARNING, "Error connecting to MirrorGate", e);
            response = new Response(HttpStatus.SC_BAD_REQUEST, "");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error connecting to MirrorGate", e);
            response = new Response(HttpStatus.SC_BAD_REQUEST, "");
        } finally {
            get.releaseConnection();
        }
        return response;
    }

    private String getResponseString(InputStream in) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] byteArray = new byte[1024];
        int count;
        while ((count = in.read(byteArray, 0, byteArray.length)) > 0) {
            outputStream.write(byteArray, 0, count);
        }
        return new String(outputStream.toByteArray(), "UTF-8");
    }

    public static class Response {
        private final int responseCode;
        private final String responseString;

        public Response(int responseCode, String responseString) {
            this.responseCode = responseCode;
            this.responseString = responseString;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public String getResponseString() {
            return responseString;
        }

    }

}