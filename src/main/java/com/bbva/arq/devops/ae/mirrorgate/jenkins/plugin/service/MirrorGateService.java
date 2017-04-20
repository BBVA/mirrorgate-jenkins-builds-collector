package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildDataCreateRequest;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;

public interface MirrorGateService {
    MirrorGateResponse publishBuildData(BuildDataCreateRequest request);

    boolean testConnection(String hostUrl);
}
