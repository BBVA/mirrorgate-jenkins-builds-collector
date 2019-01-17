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

package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils;

import com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.model.BuildDTO;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MirrorGateUtilsTest {

    private static final String PROJECT_NAME = "MirrorGate Project";
    private static final String REPOSITORY_NAME = "mirrorgate";
    private static final String BRANCH_NAME = "master";

    private static final String BUILD_URL1 = "https://fake/job/" + PROJECT_NAME + "/job/Fake Job/job/Fake Job/job/" + REPOSITORY_NAME + "/job/" + BRANCH_NAME + "/1/console";
    private static final String BUILD_URL2 = "https://fake/job/" + PROJECT_NAME + "/job/Fake Job/job/" + REPOSITORY_NAME + "/job/" + BRANCH_NAME + "/1/console";
    private static final String BUILD_URL3 = "https://fake/job/" + PROJECT_NAME + "/job/" + REPOSITORY_NAME + "/job/" + BRANCH_NAME + "/1/console";
    private static final String BUILD_URL4 = "https://fake/job/" + REPOSITORY_NAME + "/job/" + BRANCH_NAME + "/1/console";
    private static final String BUILD_URL5 = "https://fake/job/" + PROJECT_NAME + "/1/console";

    @Test
    public void parseBuildUrlTest() {
        BuildDTO build1 = new BuildDTO();
        BuildDTO build2 = new BuildDTO();
        BuildDTO build3 = new BuildDTO();
        BuildDTO build4 = new BuildDTO();
        BuildDTO build5 = new BuildDTO();

        MirrorGateUtils.parseBuildUrl(BUILD_URL1, build1);
        MirrorGateUtils.parseBuildUrl(BUILD_URL2, build2);
        MirrorGateUtils.parseBuildUrl(BUILD_URL3, build3);
        MirrorGateUtils.parseBuildUrl(BUILD_URL4, build4);
        MirrorGateUtils.parseBuildUrl(BUILD_URL5, build5);

        assertEquals(build1.getProjectName(), PROJECT_NAME);
        assertEquals(build1.getRepoName(), REPOSITORY_NAME);
        assertEquals(build1.getBranch(), BRANCH_NAME);

        assertEquals(build2.getProjectName(), PROJECT_NAME);
        assertEquals(build2.getRepoName(), REPOSITORY_NAME);
        assertEquals(build2.getBranch(), BRANCH_NAME);

        assertEquals(build3.getProjectName(), PROJECT_NAME);
        assertEquals(build3.getRepoName(), REPOSITORY_NAME);
        assertEquals(build3.getBranch(), BRANCH_NAME);

        assertEquals(build4.getProjectName(), REPOSITORY_NAME);
        assertEquals(build4.getRepoName(), REPOSITORY_NAME);
        assertEquals(build4.getBranch(), BRANCH_NAME);

        assertEquals(build5.getProjectName(), PROJECT_NAME);
        assertNull(build5.getRepoName());
        assertNull(build5.getBranch());
    }
}