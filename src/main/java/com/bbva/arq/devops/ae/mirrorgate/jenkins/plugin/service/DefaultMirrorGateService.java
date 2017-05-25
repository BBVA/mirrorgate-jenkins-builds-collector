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

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildDataCreateRequest;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateUtils;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.RestCall;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.plugins.mirrorgate.MirrorGatePublisher;
import org.apache.commons.httpclient.HttpStatus;

public class DefaultMirrorGateService implements MirrorGateService {

    private static final Logger LOG = Logger.getLogger(DefaultMirrorGateService.class.getName());

    public DefaultMirrorGateService() {
        super();
    }

    @Override
    public MirrorGateResponse publishBuildData(BuildDataCreateRequest request) {
        String responseValue;
        int responseCode = HttpStatus.SC_NO_CONTENT;
        try {
            String jsonString = new String(MirrorGateUtils.convertObjectToJsonBytes(request));
            RestCall restCall = buildRestCall();
            RestCall.Response callResponse = restCall.makeRestCallPost(getMirrorGateAPIUrl() + "/api/builds", jsonString);
            responseCode = callResponse.getResponseCode();
            responseValue = callResponse.getResponseString().replaceAll("\"", "");
            if (responseCode != HttpStatus.SC_CREATED) {
                LOG.log(Level.SEVERE, "mirrorGate: Build Publisher post may have failed. Response: {0}", responseCode);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "mirrorGate: Error posting to mirrorGate", e);
            responseValue = "";
        }

        return new MirrorGateResponse(responseCode, responseValue);
    }

    @Override
    public boolean testConnection(String hostUrl) {
        RestCall restCall = buildRestCall();
        RestCall.Response callResponse = restCall.makeRestCallGet(hostUrl + "/health");
        int responseCode = callResponse.getResponseCode();

        if (responseCode == HttpStatus.SC_OK) return true;

        LOG.log(Level.WARNING, "mirrorGate Test Connection Failed. Response: {0}", responseCode);
        return false;
    }

    private String getMirrorGateAPIUrl(){
        MirrorGatePublisher.DescriptorImpl mirrorGateDesc = Jenkins.getInstance().getDescriptorByType(MirrorGatePublisher.DescriptorImpl.class);
        return mirrorGateDesc.getMirrorGateAPIUrl();
    }

    protected RestCall buildRestCall() {
        return new RestCall();
    }
}
