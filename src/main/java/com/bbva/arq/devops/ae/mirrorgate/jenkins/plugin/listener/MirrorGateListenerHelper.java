/*
 * Copyright 2017 Banco Bilbao Vizcaya Argentaria, S.A..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.listener;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.builder.BuildBuilder;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.console.ImgConsoleNote;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildStatus;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.DefaultMirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.service.MirrorGateService;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateResponse;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateUtils;
import hudson.console.MirrorGateHyperlinkNote;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpStatus;

class MirrorGateListenerHelper {

    private static final Logger LOG = Logger.getLogger(MirrorGateListenerHelper.class.getName());

    private final MirrorGateService service;

    public MirrorGateListenerHelper() {
        this.service = new DefaultMirrorGateService();
    }

    public void sendBuild(Run<?, ?> run) {
        this.sendBuildFromJob(run.getParent(), null);
    }

    public void sendBuild(Run<?, ?> run, TaskListener listener) {
        this.sendBuildFromJob(run.getParent(), listener);
    }

    public void sendBuildFromJob(Job<?, ?> job) {
        this.sendBuildFromJob(job, null);
    }

    private void sendBuildFromJob(Job<?, ?> job, TaskListener listener) {
        if (job != null && job.getLastBuild() != null) {
            BuildStatus status = job.isBuildable() ? (job.getLastBuild().getResult() != null
                    ? BuildStatus.fromString(job.getLastBuild().getResult().toString())
                    : BuildStatus.InProgress) : BuildStatus.Deleted;
            BuildBuilder builder = new BuildBuilder(job.getLastBuild(), status);
            MirrorGateResponse response = getMirrorGateService()
                    .publishBuildData(builder.getBuildData());

            String msg;
            Level level;
            if (response.getResponseCode() == HttpStatus.SC_CREATED) {
                msg = "MirrorGate: Published Build Complete Data. " + response.toString();
                level = Level.FINE;
            } else {
                msg = "MirrorGate: Build Status could not been sent to MirrorGate. Please contact with "
                    + "MirrorGate Team for further information (mirrorgate.group@bbva.com).";
                level = Level.WARNING;
            }

            if (listener != null && level == Level.FINE) {
                listener.getLogger().println("Follow this project's builds progress at: "
                        + createMirrorGateLink(builder.getBuildData().getProjectName()));

                listener.getLogger().println(msg);
            }
            LOG.log(level, msg);

            sendBuildExtraData(builder, listener);
        }
    }

    public void sendBuildFromItem(Item item) {
        item.getAllJobs().forEach(this::sendBuildFromJob);
    }

    private void sendBuildExtraData(BuildBuilder builder, TaskListener listener) {
        List<String> extraUrl = MirrorGateUtils.getURLList();

        extraUrl.forEach(u -> {
            MirrorGateResponse response = getMirrorGateService()
                    .sendBuildDataToExtraEndpoints(builder.getBuildData(), u);

            String msg = "POST to " + u + " succeeded!";
            Level level = Level.FINE;
            if (response.getResponseCode() != HttpStatus.SC_CREATED) {
                msg = "POST to " + u + " failed with code: " + response.getResponseCode();
                level = Level.WARNING;
            }

            if (listener != null && level == Level.FINE) {
                listener.getLogger().println(msg);
            }
            LOG.log(level, msg);
        });
    }

    private String createMirrorGateLink(String projectName) {
        String mirrorGateUrl = MirrorGateHyperlinkNote.encodeTo(
                MirrorGateUtils.getMirrorGateAPIUrl() + "/dashboard.html?board=" + projectName,
                "MirrorGate");

        String image = ImgConsoleNote.encodeTo(MirrorGateUtils.getBase64image());

        return mirrorGateUrl + " " + image;
    }

    MirrorGateService getMirrorGateService() {
        return service;
    }

}
