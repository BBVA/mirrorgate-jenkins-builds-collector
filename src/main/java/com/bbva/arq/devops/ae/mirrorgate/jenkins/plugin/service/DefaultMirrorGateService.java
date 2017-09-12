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
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateUtils;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.RestCall;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.httpclient.HttpStatus;

public class DefaultMirrorGateService implements MirrorGateService {

    private static final Logger LOG = Logger.getLogger(DefaultMirrorGateService.class.getName());

    public DefaultMirrorGateService() {
        super();
    }

    @Override
    public MirrorGateResponse publishBuildData(BuildDTO request) {
        try {
            MirrorGateResponse callResponse = buildRestCall().makeRestCallPost(
                    MirrorGateUtils.getMirrorGateAPIUrl() + "/api/builds",
                    new String(MirrorGateUtils.convertObjectToJsonBytes(request)),
                    MirrorGateUtils.getMirrorGateUser(),
                    MirrorGateUtils.getMirrorGatePassword());

            if (callResponse.getResponseCode() != HttpStatus.SC_CREATED) {
                LOG.log(Level.SEVERE, "MirrorGate: Build Publisher post may have failed. Response: {0}", callResponse.getResponseCode());
            }
            return callResponse;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "MirrorGate: Error posting to mirrorGate", e);
            return new MirrorGateResponse(HttpStatus.SC_CONFLICT, "");
        }
    }

    @Override
    public MirrorGateResponse testConnection() {
        MirrorGateResponse callResponse = buildRestCall().makeRestCallGet(
                MirrorGateUtils.getMirrorGateAPIUrl() + "/health",
                MirrorGateUtils.getMirrorGateUser(),
                MirrorGateUtils.getMirrorGatePassword());
        return new MirrorGateResponse(callResponse.getResponseCode(),
                callResponse.getResponseValue().replaceAll("\"", ""));
    }

    @Override
    public MirrorGateResponse sendBuildDataToExtraEndpoints(BuildDTO request, String URL){

        try{
            return buildRestCall().makeRestCallPost(URL, new String(MirrorGateUtils.convertObjectToJsonBytes(request)),null, null);
        }catch (IOException e){
            LOG.log(Level.SEVERE, "MirrorGate: Error posting to" + URL, e);
            return new MirrorGateResponse(HttpStatus.SC_CONFLICT, "");
        }
    }

    protected RestCall buildRestCall() {
        return new RestCall();
    }
}
