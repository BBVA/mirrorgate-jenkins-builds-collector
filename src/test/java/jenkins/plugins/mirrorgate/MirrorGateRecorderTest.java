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

package jenkins.plugins.mirrorgate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import jenkins.model.Jenkins;
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
@PrepareForTest({Jenkins.class})
public class MirrorGateRecorderTest {

    @Mock
    Jenkins jenkins;

    @Mock
    MirrorGateService service;

    @Spy
    MirrorGateRecorderStub.DescriptorImplStub descriptor = spy(
            new MirrorGateRecorderStub.DescriptorImplStub()
    );

    private static final String MIRRORGATE_URL = "http://localhost:8080/mirrorgate";

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.when(Jenkins.get()).thenReturn(jenkins);
        PowerMockito.when(jenkins.getDescriptorByType(any()))
                .thenReturn(descriptor);
    }

    @Test
    public void testDoTestOKConnectionTest() {
        when(service.testConnection(MIRRORGATE_URL, null))
                .thenReturn(new MirrorGateResponse(HttpStatus.SC_OK, ""));
        when(descriptor.getMirrorGateService()).thenReturn(service);

        final FormValidation result = descriptor.doTestConnection(
                MIRRORGATE_URL, null);

        assertEquals(Kind.OK, result.kind);
    }

    @Test
    public void testDoTestErrorConnectionTest() {
        when(service.testConnection(MIRRORGATE_URL, null))
                .thenReturn(new MirrorGateResponse(HttpStatus.SC_NOT_FOUND, ""));
        when(descriptor.getMirrorGateService()).thenReturn(service);

        final FormValidation result = descriptor.doTestConnection(
                MIRRORGATE_URL, null);

        assertEquals(Kind.ERROR, result.kind);
    }

}
