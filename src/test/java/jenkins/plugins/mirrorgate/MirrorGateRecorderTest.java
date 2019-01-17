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

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import junit.framework.TestCase;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class})
public class MirrorGateRecorderTest extends TestCase {

    @Mock
    Jenkins jenkins;

    @Mock
    private MirrorGateService service = mock(MirrorGateService.class);

    @Spy
    private MirrorGateRecorderStub.DescriptorImplStub descriptor
            = spy(new MirrorGateRecorderStub.DescriptorImplStub());

    private static final String MIRRORGATE_URL = "http://localhost:8080/mirrorgate";

    @Before
    @Override
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.when(Jenkins.get()).thenReturn(jenkins);
        PowerMockito.when(jenkins.getDescriptorByType(any()))
                .thenReturn(descriptor);
    }

    @Test
    public void testDoTestOKConnectionTest() {
        when(service.testConnection())
                .thenReturn(new MirrorGateResponse(HttpStatus.SC_OK, ""));
        when(descriptor.getMirrorGateService()).thenReturn(service);

        FormValidation result = descriptor.doTestConnection(
                MIRRORGATE_URL, null);

        assertEquals(FormValidation.Kind.OK, result.kind);
    }

    @Test
    public void testDoTestErrorConnectionTest() {
        when(service.testConnection())
                .thenReturn(new MirrorGateResponse(HttpStatus.SC_NOT_FOUND, ""));
        when(descriptor.getMirrorGateService()).thenReturn(service);

        FormValidation result = descriptor.doTestConnection(
                MIRRORGATE_URL, null);

        assertEquals(FormValidation.Kind.ERROR, result.kind);
    }

    @Test
    public void testDoTestConnectionWithoutServiveConnectionTest() {
        when(service.testConnection())
                .thenReturn(new MirrorGateResponse(HttpStatus.SC_NOT_FOUND, ""));
        when(descriptor.getMirrorGateService()).thenReturn(null);

        FormValidation result = descriptor.doTestConnection(
                MIRRORGATE_URL, null);

        assertEquals(FormValidation.Kind.ERROR, result.kind);
    }

}
