package mirrorgate.builder;

import static mirrorgate.utils.MirrorGateUtils.getRepoBranch;

import java.util.List;
import java.util.logging.Logger;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import com.capitalone.dashboard.model.BuildStatus;
import com.capitalone.dashboard.model.SCM;
import com.capitalone.dashboard.request.BuildDataCreateRequest;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import mirrorgate.utils.MirrorGateUtils;

public class BuildBuilder {

    private static final Logger logger = Logger.getLogger(BuildBuilder.class.getName());
    private AbstractBuild<?, ?> build;
    private Run<?, ?> run;
    private String jenkinsName;
    private TaskListener listener;
    private boolean isComplete;
    private BuildDataCreateRequest request;
    private BuildStatus result;
    boolean buildChangeSet;

    public BuildBuilder(AbstractBuild<?, ?> build, String jenkinsName, TaskListener listener, boolean isComplete, boolean buildChangeSet) {
        this.build = build;
        this.jenkinsName = jenkinsName;
        this.listener = listener;
        this.isComplete = isComplete;
        this.buildChangeSet = buildChangeSet;
        createBuildRequest();
    }

    public BuildBuilder(Run<?, ?> run, String jenkinsName, TaskListener listener, BuildStatus result, boolean buildChangeSet) {
        this.run = run;
        this.jenkinsName = jenkinsName;
        this.listener = listener;
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
        request.setNiceName(jenkinsName);
        request.setJobName(MirrorGateUtils.getJobName(run));
        request.setBuildUrl(MirrorGateUtils.getBuildUrl(run));
        request.setJobUrl(MirrorGateUtils.getJobUrl(run));
        request.setInstanceUrl(MirrorGateUtils.getInstanceUrl(run, listener));
        request.setNumber(MirrorGateUtils.getBuildNumber(run));
        request.setStartTime(run.getStartTimeInMillis());
        request.setBuildStatus(result.toString());

        if (!result.equals(BuildStatus.InProgress)) {
            request.setDuration(System.currentTimeMillis() - run.getStartTimeInMillis());
            request.setEndTime(System.currentTimeMillis());
            if (buildChangeSet) {
                request.setCodeRepos(getRepoBranch(run));
                WorkflowRun wr = (WorkflowRun) run;
                request.setSourceChangeSet(getCommitList(wr.getChangeSets()));
            }
        }
    }

    private void createBuildRequest() {
        request = new BuildDataCreateRequest();
        request.setNiceName(jenkinsName);
        request.setJobName(MirrorGateUtils.getJobName(build));
        request.setBuildUrl(MirrorGateUtils.getBuildUrl(build));
        request.setJobUrl(MirrorGateUtils.getJobUrl(build));
        request.setInstanceUrl(MirrorGateUtils.getInstanceUrl(build, listener));
        request.setNumber(MirrorGateUtils.getBuildNumber(build));
        request.setStartTime(build.getStartTimeInMillis());
        if (isComplete) {
            request.setBuildStatus(build.getResult().toString());
            request.setDuration(build.getDuration());
            request.setEndTime(build.getStartTimeInMillis() + build.getDuration());
            if (buildChangeSet) {
                request.setCodeRepos(getRepoBranch(build));
                request.setSourceChangeSet(getCommitList(build.getChangeSets()));
            }
        } else {
            request.setBuildStatus(BuildStatus.InProgress.toString());
        }
    }

    public BuildDataCreateRequest getBuildData() {
        return request;
    }

    private List<SCM> getCommitList(List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets) {
        CommitBuilder commitBuilder = new CommitBuilder(changeLogSets);
        return commitBuilder.getCommits();
    }


}
