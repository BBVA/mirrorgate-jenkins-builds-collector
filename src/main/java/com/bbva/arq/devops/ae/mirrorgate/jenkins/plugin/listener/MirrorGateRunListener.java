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

package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.listener;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

@Extension
public class MirrorGateRunListener extends RunListener<Run<?, ?>> {

    private static final Logger LOG = Logger.getLogger(MirrorGateRunListener.class.getName());

    private final MirrorGateListenerHelper helper;

    public MirrorGateRunListener() {
        this.helper = new MirrorGateListenerHelper();

        LOG.fine(">>> MirrorGateRunListener Initialised");
    }

    @Override
    public void onStarted(final Run run, final TaskListener listener) {
        LOG.fine("onStarted starts");

        helper.sendBuild(run, listener);

        LOG.fine("onStarted ends");
    }

    @Override
    public void onCompleted(final Run run, final @Nonnull TaskListener listener) {
        LOG.fine("onCompleted starts");

        helper.sendBuild(run, listener);

        LOG.fine("onCompleted ends");
    }

}
