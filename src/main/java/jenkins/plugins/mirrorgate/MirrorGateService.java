package jenkins.plugins.mirrorgate;

import com.capitalone.dashboard.request.BuildDataCreateRequest;

public interface MirrorGateService {
    MirrorGateResponse publishBuildData(BuildDataCreateRequest request);

    boolean testConnection();
}
