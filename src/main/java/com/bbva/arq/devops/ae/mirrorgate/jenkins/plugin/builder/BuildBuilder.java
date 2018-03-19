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

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.listener.MirrorGateRunListener;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildDTO;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildStatus;
import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils.MirrorGateUtils;
import hudson.model.Cause.UserIdCause;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildBuilder {

    private static final Logger LOG = Logger.getLogger(BuildBuilder.class.getName());

    private final Run<?, ?> run;
    private BuildDTO request;
    private final BuildStatus result;

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

        // Get culprits if build comes from a SCM Source
        setCulprits(run);

        try {
            MirrorGateUtils.parseBuildUrl(MirrorGateUtils.getBuildUrl(run), request);
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public BuildDTO getBuildData() {
        return request;
    }

    private void setCulprits(Run run) {

        List<String> culprits = new ArrayList<>(0);

        // Get culprits from the causes of the build
        run.getCauses().forEach((cause -> {
            switch (cause.getClass().getSimpleName()) {
                case "UserIdCause":
                    if (!culprits.contains(((UserIdCause) cause).getUserName())) {
                        culprits.add(((UserIdCause) cause).getUserName());
                    }
                    break;
            }
        }));

        // Use introspective class to avoid plugins compatibility problems
        try {
            Method method = run.getClass().getMethod("getChangeSets");
            method.setAccessible(true);
            ((List<ChangeLogSet>) method.invoke(run, new Object[]{})).forEach(cset -> {
                for (Object object : ((ChangeLogSet) cset).getItems()) {
                    ChangeLogSet.Entry change = (ChangeLogSet.Entry) object;
                    if (!culprits.contains(change.getAuthor().getFullName())) {
                        culprits.add(change.getAuthor().getFullName());
                    }
                }
            });
        } catch (SecurityException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
            Logger.getLogger(MirrorGateRunListener.class.getName()).log(Level.SEVERE, null, ex);
        }

        request.setCulprits(culprits);
    }

}
