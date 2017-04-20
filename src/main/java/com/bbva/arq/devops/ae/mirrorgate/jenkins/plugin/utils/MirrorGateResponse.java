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
        if (StringUtils.isEmpty(responseValue)) return resp;
        return resp + "Response Value= " + responseValue;
    }
}
