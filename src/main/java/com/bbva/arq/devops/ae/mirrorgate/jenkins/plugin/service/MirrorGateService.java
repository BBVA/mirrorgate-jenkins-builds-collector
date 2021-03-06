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

public interface MirrorGateService {

    /**
     * Publish Build Data to an endpoint.
     *
     * @param requestBuild Build to Publish
     * @return MirrorGateResponse
     */
    MirrorGateResponse publishBuildData(BuildDTO requestBuild);

    /**
     * Test a endpoint availability.
     *
     * @param mirrorGateAPIUrl   API URL of MirrorGate (It will be added a /health to the URL)
     * @param credentialsId      Jenkins credentialsId
     * @return MirrorGateResponse
     */
    MirrorGateResponse testConnection(String mirrorGateAPIUrl, String credentialsId);

    /**
     * Publish Build Data to an endpoint.
     *
     * @param requestBuild Build to send to extra endpoints
     * @param url          The URL to send the build
     * @return Boolean
     */
    MirrorGateResponse sendBuildDataToExtraEndpoints(BuildDTO requestBuild, String url);
}
