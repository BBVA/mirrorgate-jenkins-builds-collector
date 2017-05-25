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

package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.builder;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildDTO;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildStatus;
import hudson.model.Run;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateUtils;

public class BuildBuilder {

    private Run<?, ?> run;
    private BuildDTO request;
    private BuildStatus result;

    public BuildBuilder(Run<?, ?> run, BuildStatus result) {
        this.run = run;
        this.result = result;
        createBuildRequest();
    }

    private void createBuildRequest() {
        request = new BuildDTO();
        request.setNumber(MirrorGateUtils.getBuildNumber(run));
        request.setStartTime(run.getStartTimeInMillis());
        request.setBuildStatus(result.toString());

        if (!result.equals(BuildStatus.InProgress)) {
            request.setDuration(System.currentTimeMillis() - run.getStartTimeInMillis());
            request.setEndTime(System.currentTimeMillis());
        }
        
        parseBuildUrl(MirrorGateUtils.getBuildUrl(run), request);
    }
    
    public BuildDTO getBuildData() {
        return request;
    }

    private void parseBuildUrl(String buildUrl, BuildDTO request) {
        String[] buildInfo = buildUrl.split("/job/");
        request.setBuildUrl(buildUrl);
        request.setProjectName(buildInfo[1].split("/")[0]);
        
        /* A Job show branchs of a repository */
        if(buildInfo.length == 3) {
            request.setRepoName(buildInfo[1].split("/")[0]);
            request.setBranch(buildInfo[2].split("/")[0]);
        }
        
        /* A Job show many repositositories*/
        if(buildInfo.length > 3) {
            request.setRepoName(buildInfo[2].split("/")[0]);
            request.setBranch(buildInfo[3].split("/")[0]);
        }
    }

}
