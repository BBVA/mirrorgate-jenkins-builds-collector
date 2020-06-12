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

package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateUtils;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import jenkins.model.Jenkins;
import jenkins.plugins.mirrorgate.MirrorGateRecorder;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
// Needed to run PowerMockito with Java 11 https://github.com/mockito/mockito/issues/1562
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest({Job.class, Jenkins.class, MirrorGateUtils.class})
public class MirrorGateListenerHelperTest {

    @Mock
    Jenkins jenkins;

    @Mock
    MirrorGateRecorder.DescriptorImpl descriptor;

    @Mock
    MirrorGateService service;

    @Spy
    MirrorGateListenerHelper helper = new MirrorGateListenerHelper();

    private final MirrorGateResponse responseOk
            = new MirrorGateResponse(HttpStatus.SC_CREATED, "");
    private final MirrorGateResponse responseError
            = new MirrorGateResponse(HttpStatus.SC_NOT_FOUND, "");

    private static final String MIRRORGATE_URL = "http://localhost:8080/mirrorgate";

    private static final String BUILD_SAMPLE = "http://localhost:8080/job/MirrorGate"
            + "/job/mirrorgate-jenkins-builds-collector/job/test/5/";

    private static final String EXTRA_URL = "http://localhost:8080/test, http://localhost:8080/test2,   ";

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.when(Jenkins.get()).thenReturn(jenkins);
        PowerMockito.when(jenkins.getDescriptorByType(any()))
                .thenReturn(descriptor);

        PowerMockito.when(MirrorGateUtils.getMirrorGateAPIUrl())
                .thenReturn(MIRRORGATE_URL);
        PowerMockito.when(MirrorGateUtils.getUsernamePasswordCredentials())
                .thenReturn(null);
        PowerMockito.when(MirrorGateUtils.getExtraUrls())
                .thenReturn(EXTRA_URL);

        when(helper.getMirrorGateService()).thenReturn(service);
    }

    @Test
    public void sendBuildResponseOK() {
        when(service.publishBuildData(any())).thenReturn(responseOk);
        when(service.sendBuildDataToExtraEndpoints(any(), any())).thenReturn(responseOk);

        helper.sendBuild(createMockingBuild());

        verify(service, times(1)).publishBuildData(any());
    }

    @Test
    public void sendBuildResponseError() {
        when(service.publishBuildData(any())).thenReturn(responseError);
        when(service.sendBuildDataToExtraEndpoints(any(), any())).thenReturn(responseOk);

        helper.sendBuild(createMockingBuild());

        verify(service, times(1)).publishBuildData(any());
    }

    @Test
    public void sendBuildFromJobResponseOK() {
        when(service.publishBuildData(any())).thenReturn(responseOk);
        when(service.sendBuildDataToExtraEndpoints(any(), any())).thenReturn(responseOk);

        helper.sendBuildFromJob(createMockingJob());

        verify(service, times(1)).publishBuildData(any());
    }

    @Test
    public void sendBuildFromJobResponseError() {
        when(service.publishBuildData(any())).thenReturn(responseError);
        when(service.sendBuildDataToExtraEndpoints(any(), any())).thenReturn(responseError);

        helper.sendBuildFromJob(createMockingJob());

        verify(service, times(1)).publishBuildData(any());
    }

    @Test
    public void sendBuildFromItemResponseOK() {
        when(service.publishBuildData(any())).thenReturn(responseOk);
        when(service.sendBuildDataToExtraEndpoints(any(), any())).thenReturn(responseOk);

        final Job<?, ?>[] jobs = new Job[new Random().nextInt(10)];
        Arrays.fill(jobs, createMockingJob());

        helper.sendBuildFromItem(createMockingItem(jobs));

        verify(service, times(jobs.length)).publishBuildData(any());
    }

    @Test
    public void sendBuildFromItemResponseError() {
        when(service.publishBuildData(any())).thenReturn(responseError);
        when(service.sendBuildDataToExtraEndpoints(any(), any())).thenReturn(responseError);

        final Job<?, ?>[] jobs = new Job[new Random().nextInt(10)];
        Arrays.fill(jobs, createMockingJob());

        helper.sendBuildFromItem(createMockingItem(jobs));

        verify(service, times(jobs.length)).publishBuildData(any());
    }

    private Item createMockingItem(final Job<?, ?>[] jobs) {
        Item item = mock(Item.class);

        when(item.getAllJobs()).thenReturn((Collection) Arrays.asList(jobs));
        return item;
    }

    private Job<?, ?> createMockingJob() {
        final Job job = PowerMockito.mock(Job.class);
        final Run build = mock(Run.class);

        when(job.getLastBuild()).thenReturn(build);
        PowerMockito.when(job.getAbsoluteUrl()).thenReturn(BUILD_SAMPLE);
        when(build.getParent()).thenReturn(job);

        return job;
    }

    private Run<?, ?> createMockingBuild() {
        final Job job = PowerMockito.mock(Job.class);
        final Run run = mock(Run.class);

        when(job.getLastBuild()).thenReturn(run);
        PowerMockito.when(job.getAbsoluteUrl()).thenReturn(BUILD_SAMPLE);
        when(run.getParent()).thenReturn(job);

        return run;
    }
}

