package jenkins.plugins.mirrorgate;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildDataCreateRequest;

public interface MirrorGateService {
    MirrorGateResponse publishBuildData(BuildDataCreateRequest request);

    boolean testConnection();
}
