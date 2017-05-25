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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import java.util.Arrays;
import java.util.Collection;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Job.class})
public class MirrorGateItemListenerTest extends TestCase {

    @Mock
    Item item = mock(Item.class);

    @Mock
    MirrorGateService service = mock(MirrorGateService.class);

    @Spy
    MirrorGateItemListener listener = new MirrorGateItemListener();

    Job[] jobs = new Job[10];

    private final MirrorGateResponse responseOk = new MirrorGateResponse(HttpStatus.SC_CREATED, "");
    private final MirrorGateResponse responseError = new MirrorGateResponse(HttpStatus.SC_NOT_FOUND, "");

    private String buildSample = "http://localhost:8080/job/MirrorGate/job/mirrorgate-jenkins-builds-collector/job/test/5/";

    @Before
    @Override
    public void setUp() {
        Arrays.fill(jobs, createMockingJob());

        when(item.getAllJobs()).thenReturn((Collection) Arrays.asList(jobs));
        when(listener.getMirrorGateService()).thenReturn(service);
    }

    @Test
    public void onDeletedTestWhenServiceResponseOK() {
        when(service.publishBuildData(any())).thenReturn(responseOk);

        listener.onDeleted(item);

        verify(service, times(jobs.length)).publishBuildData(any());
    }

    @Test
    public void onDeletedTestWhenServiceResponseError() {
        when(service.publishBuildData(any())).thenReturn(responseError);

        listener.onDeleted(item);

        verify(service, times(jobs.length)).publishBuildData(any());
    }

    private Job createMockingJob() {
        Job job = PowerMockito.mock(Job.class);
        Run build = mock(Run.class);

        when(job.getLastBuild()).thenReturn(build);
        PowerMockito.when(job.getAbsoluteUrl()).thenReturn(buildSample);
        when(build.getParent()).thenReturn(job);

        return job;
    }
}

