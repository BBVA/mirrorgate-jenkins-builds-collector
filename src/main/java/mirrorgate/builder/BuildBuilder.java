package mirrorgate.builder;

import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildDataCreateRequest;
import com.bbva.arq.devops.ae.mirrorgate.core.model.BuildStatus;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import mirrorgate.utils.MirrorGateUtils;

public class BuildBuilder {

    private AbstractBuild<?, ?> build;
    private Run<?, ?> run;
    private boolean isComplete;
    private BuildDataCreateRequest request;
    private BuildStatus result;
    boolean buildChangeSet;

    public BuildBuilder(Run<?, ?> run, BuildStatus result, boolean buildChangeSet) {
        this.run = run;
        this.result = result;
        this.buildChangeSet = buildChangeSet;
        createBuildRequest();
    }

    private void createBuildRequest() {
        request = new BuildDataCreateRequest();
        request.setNumber(MirrorGateUtils.getBuildNumber(run));
        request.setStartTime(run.getStartTimeInMillis());
        request.setBuildStatus(result.toString());

        if (!result.equals(BuildStatus.InProgress)) {
            request.setDuration(System.currentTimeMillis() - run.getStartTimeInMillis());
            request.setEndTime(System.currentTimeMillis());
        }
        
        parseBuildUrl(MirrorGateUtils.getBuildUrl(run), request);
    }

    public BuildDataCreateRequest getBuildData() {
        return request;
    }

    private void parseBuildUrl(String buildUrl, BuildDataCreateRequest request) {
        String[] buildInfo = buildUrl.split("/job/");
        request.setBuildUrl(buildUrl);
        request.setProjectName(buildInfo[1].split("/")[0]);
        if(buildInfo.length > 2) {
            request.setRepoName(buildInfo[2].split("/")[0]);
            request.setBranch(buildInfo[3].split("/")[0]);
        }
    }

}
