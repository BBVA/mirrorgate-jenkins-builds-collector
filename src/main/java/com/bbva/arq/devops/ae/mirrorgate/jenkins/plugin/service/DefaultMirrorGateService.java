package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildDataCreateRequest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpStatus;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateUtils;
import jenkins.model.Jenkins;
import jenkins.plugins.mirrorgate.MirrorGatePublisher;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.RestCall;

//import org.json.simple.JSONArray;

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
            RestCall.RestCallResponse callResponse = restCall.makeRestCallPost(getMirrorGateAPIUrl() + "/build", jsonString);
            responseCode = callResponse.getResponseCode();
            responseValue = callResponse.getResponseString().replaceAll("\"", "");
            if (responseCode != HttpStatus.SC_CREATED) {
                LOG.log(Level.SEVERE, "mirrorGate: Build Publisher post may have failed. Response: {0}", responseCode);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "mirrorGate: Error posting to mirrorGate", e);
            responseValue = "";
        }

        return new MirrorGateResponse(responseCode, responseValue);
    }

    @Override
    public boolean testConnection(String hostUrl) {
        RestCall restCall = buildRestCall();
        RestCall.RestCallResponse callResponse = restCall.makeRestCallGet(hostUrl + "/health");
        int responseCode = callResponse.getResponseCode();

        if (responseCode == HttpStatus.SC_OK) return true;

        LOG.log(Level.WARNING, "mirrorGate Test Connection Failed. Response: {0}", responseCode);
        return false;
    }
    
    private String getMirrorGateAPIUrl(){
        if(Jenkins.getInstance() != null) {
            MirrorGatePublisher.DescriptorImpl mirrorGateDesc = Jenkins.getInstance().getDescriptorByType(MirrorGatePublisher.DescriptorImpl.class);
            return mirrorGateDesc.getMirrorGateAPIUrl();
        }   
        
        return "";
    }
    
    protected RestCall buildRestCall() {
        return new RestCall();
    }
}
