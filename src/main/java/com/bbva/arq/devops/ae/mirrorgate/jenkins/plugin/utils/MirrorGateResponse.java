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

import org.apache.commons.lang3.StringUtils;

public class MirrorGateResponse {
    private final int responseCode;
    private final String responseValue;

    public MirrorGateResponse(int responseCode, String responseValue) {
        this.responseCode = responseCode;
        this.responseValue = responseValue;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseValue() {
        return responseValue;
    }

    @Override
    public String toString() {
        String resp = "Response Code: " + responseCode + ". ";
        if (StringUtils.isEmpty(responseValue)) {
            return resp;
        }
        return resp + "Response Value= " + responseValue;
    }
}
