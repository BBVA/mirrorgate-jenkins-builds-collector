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


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildDTO;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.RestCall;
import java.io.IOException;
import java.util.Arrays;
import jenkins.model.Jenkins;
import jenkins.plugins.mirrorgate.MirrorGatePublisher;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class})
public class DefaultMirrorGateServiceTest extends TestCase {

    @Mock
    Jenkins jenkins;

    @Mock
    MirrorGatePublisher.DescriptorImpl descriptor;

    @Mock
    HttpClient htppClient;

    @Spy
    RestCall restCall = new RestCall();

    @Spy
    DefaultMirrorGateService service = new DefaultMirrorGateService();

    private static final String MIRRORGATE_URL = "http://localhost:8080/mirrorgate";

    @Before
    @Override
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
        PowerMockito.when(jenkins.getDescriptorByType(any()))
                .thenReturn(descriptor);

        htppClient = mock(HttpClient.class);

        when(restCall.getHttpClient()).thenReturn(htppClient);
        when(service.buildRestCall()).thenReturn(restCall);
    }

    @Test
    public void testSuccessfulPublishBuildDataTest() throws IOException {
        when(htppClient.executeMethod(any(PostMethod.class)))
                .thenReturn(HttpStatus.SC_OK);

        MirrorGateResponse response = service.publishBuildData(makeBuildRequest());

        assertEquals(HttpStatus.SC_OK, response.getResponseCode());
    }

    @Test
    public void testFailedPublishBuildDataTest() throws IOException {
        when(htppClient.executeMethod(any(PostMethod.class)))
                .thenReturn(HttpStatus.SC_NOT_FOUND);

        MirrorGateResponse response = service.publishBuildData(makeBuildRequest());

        assertEquals(HttpStatus.SC_NOT_FOUND, response.getResponseCode());
    }

    @Test
    public void testSuccessConnectionTest() throws IOException {
        when(htppClient.executeMethod(any(GetMethod.class)))
                .thenReturn(HttpStatus.SC_OK);

        assertEquals(service.testConnection().getResponseCode(),
                HttpStatus.SC_OK);
    }

    @Test
    public void testFailedConnectionTest() throws IOException {
        when(htppClient.executeMethod(any(GetMethod.class)))
                .thenReturn(HttpStatus.SC_NOT_FOUND);

        assertEquals(service.testConnection().getResponseCode(),
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
        build.setCulprits(Arrays.asList("foo"));
        return build;
    }

}
