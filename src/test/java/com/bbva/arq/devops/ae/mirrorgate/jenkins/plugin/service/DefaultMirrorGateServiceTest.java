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

package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service;


import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildDTO;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.RestCall;
import jenkins.model.Jenkins;
import jenkins.plugins.mirrorgate.MirrorGateRecorder;
import junit.framework.TestCase;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, HttpClients.class})
public class DefaultMirrorGateServiceTest extends TestCase {

    @Mock
    Jenkins jenkins;

    @Mock
    MirrorGateRecorder.DescriptorImpl descriptor;

    @Mock
    CloseableHttpClient httpClient;

    @Mock
    CloseableHttpResponse httpResponse;

    @Mock
    StatusLine statusLine;

    @Spy
    private RestCall restCall = new RestCall();

    @Spy
    private DefaultMirrorGateService service = new DefaultMirrorGateService();

    private static final String MIRRORGATE_URL = "http://localhost:8080/mirrorgate";

    @Before
    @Override
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.mockStatic(HttpClients.class);

        PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
        PowerMockito.when(jenkins.getDescriptorByType(any())).thenReturn(descriptor);

        httpClient = mock(CloseableHttpClient.class);
        httpResponse = mock(CloseableHttpResponse.class);
        statusLine = mock(StatusLine.class);

        PowerMockito.when(HttpClients.createDefault()).thenReturn(httpClient);

        when(service.buildRestCall()).thenReturn(restCall);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(new StringEntity("Test", Charset.defaultCharset()));
    }

    @Test
    public void testSuccessfulPublishBuildDataTest() throws IOException {
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpClient.execute(any(HttpPost.class), any(HttpClientContext.class)))
                .thenReturn(httpResponse);

        MirrorGateResponse response = service.publishBuildData(makeBuildRequest());

        assertEquals(HttpStatus.SC_OK, response.getResponseCode());
    }

    @Test
    public void testFailedPublishBuildDataTest() throws IOException {
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(httpClient.execute(any(HttpPost.class), any(HttpClientContext.class)))
                .thenReturn(httpResponse);

        MirrorGateResponse response = service.publishBuildData(makeBuildRequest());

        assertEquals(HttpStatus.SC_NOT_FOUND, response.getResponseCode());
    }

    @Test
    public void testSuccessConnectionTest() throws IOException {
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpClient.execute(any(HttpGet.class), any(HttpClientContext.class)))
                .thenReturn(httpResponse);

        assertEquals(service.testConnection(MIRRORGATE_URL, null).getResponseCode(),
                HttpStatus.SC_OK);
    }

    @Test
    public void testFailedConnectionTest() throws IOException {
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(httpClient.execute(any(HttpGet.class), any(HttpClientContext.class)))
                .thenReturn(httpResponse);

        assertEquals(service.testConnection(MIRRORGATE_URL, null).getResponseCode(),
                HttpStatus.SC_NOT_FOUND);
    }

    private BuildDTO makeBuildRequest() {
        BuildDTO build = new BuildDTO();
        build.setNumber("1");
        build.setBuildUrl("buildUrl");
        build.setStartTime(3);
        build.setEndTime(8);
        build.setDuration(5);
        build.setBuildStatus("Success");
        build.setCulprits(Collections.singletonList("foo"));
        return build;
    }

}
