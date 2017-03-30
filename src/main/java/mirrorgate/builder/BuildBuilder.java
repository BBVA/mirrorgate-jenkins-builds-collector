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

    public BuildBuilder(AbstractBuild<?, ?> build, boolean isComplete, boolean buildChangeSet) {
        this.build = build;
        this.isComplete = isComplete;
        this.buildChangeSet = buildChangeSet;
        createBuildRequest();
    }

    public BuildBuilder(Run<?, ?> run, BuildStatus result, boolean buildChangeSet) {
        this.run = run;
        this.result = result;
        this.buildChangeSet = buildChangeSet;
        if (run instanceof AbstractBuild) {
            this.build = (AbstractBuild<?, ?>) run;
            createBuildRequest();
        } else {
            createBuildRequestFromRun();
        }
    }

    private void createBuildRequestFromRun() {
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

    private void createBuildRequest() {
        request = new BuildDataCreateRequest();
        request.setNumber(MirrorGateUtils.getBuildNumber(build));
        request.setStartTime(build.getStartTimeInMillis());
        if (isComplete) {
            request.setBuildStatus(build.getResult().toString());
            request.setDuration(build.getDuration());
            request.setEndTime(build.getStartTimeInMillis() + build.getDuration());
        } else {
            request.setBuildStatus(BuildStatus.InProgress.toString());
        }
        
        parseBuildUrl(MirrorGateUtils.getBuildUrl(build), request);
    }

    public BuildDataCreateRequest getBuildData() {
        return request;
    }

    private void parseBuildUrl(String buildUrl, BuildDataCreateRequest request) {
        String[] buildInfo = buildUrl.split("/job/");
        request.setBuildUrl(buildUrl);
        request.setProjectName(buildInfo[1]);
        request.setRepoName(buildInfo[2]);
        request.setBranch(buildInfo[3].split("/")[0]);    
    }

}
