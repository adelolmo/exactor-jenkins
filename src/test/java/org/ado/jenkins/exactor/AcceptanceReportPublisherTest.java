/*
 *  Copyright 2015 Andoni del Olmo
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ado.jenkins.exactor;

import hudson.model.FreeStyleProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

/**
 * @author Andoni del Olmo
 */
public class AcceptanceReportPublisherTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private AcceptanceReportPublisher publisher;
    private FreeStyleProject project;

    @Before
    public void setUp() throws Exception {
        this.publisher = new AcceptanceReportPublisher();
        this.project = j.createFreeStyleProject();
        this.project.getPublishersList().add(this.publisher);
    }

    @Test
    public void testPerform() throws Exception {
        assertEquals("display name", "Acceptance Test Reporting", publisher.getDescriptor().getDisplayName());
    }

    @Test
    public void testNeedsToRunAfterFinalized() throws Exception {

    }

    @Test
    public void testGetRequiredMonitorService() throws Exception {

    }

    @Test
    public void testGetDescriptor() throws Exception {

    }
}
