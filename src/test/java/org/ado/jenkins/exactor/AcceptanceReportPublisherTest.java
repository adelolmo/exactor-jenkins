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

import hudson.EnvVars;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andoni del Olmo
 */
public class AcceptanceReportPublisherTest {

    private static final String PUBLISH_DIRECTORY = "/var/www/html/";
    private static final String REPORT_FILENAME = "at_report_p1.html";
    private static final String REPORT_PATH = "/tmp/" + REPORT_FILENAME;
    private static final String LOG_FILENAME = "at_out.log";
    private static final String LOG_PATH = "/tmp/" + LOG_FILENAME;
    private static final String JOB_NAME = "theJob";

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private AbstractBuild buildMock;

    private AcceptanceReportPublisher publisher;
    private FreeStyleProject project;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.publisher = new AcceptanceReportPublisher();
        this.project = j.createFreeStyleProject();
        this.project.getPublishersList().add(this.publisher);

        final EnvVars envVars = new EnvVars();
        envVars.put("JOB_NAME", JOB_NAME);
        when(buildMock.getEnvironment(any(TaskListener.class))).thenReturn(envVars);

        FileUtils.touch(new File(temporaryFolder.getRoot(), REPORT_PATH));
        FileUtils.touch(new File(temporaryFolder.getRoot(), LOG_PATH));
        FileUtils.forceMkdir(new File(temporaryFolder.getRoot(), PUBLISH_DIRECTORY));
        FileUtils.forceMkdir(new File(temporaryFolder.getRoot(), PUBLISH_DIRECTORY + "/" + JOB_NAME));
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
    public void testPerform_noExpandEnvironmentVariables() throws Exception {
        int buildVersion = 265;
        publisher = createPublisher("http://host/subdomain", PUBLISH_DIRECTORY);
        final MatrixProject project = j.createMatrixProject("project");
        project.getPublishersList().add(this.publisher);
        when(buildMock.getProject()).thenReturn(project);
        when(buildMock.getNumber()).thenReturn(buildVersion);

        assertTrue(publisher.perform(buildMock, null, null));

        verify(buildMock).addAction(argThat(new ReportActionMatcher("http://host/subdomain/at_report_p1-265.html", "http://host/subdomain/at_out-265.log")));
        final File root = temporaryFolder.getRoot();
        assertTrue("html report was copied", new File(root, PUBLISH_DIRECTORY + "at_report_p1-265.html").exists());
        assertTrue("log was copied", new File(root, PUBLISH_DIRECTORY + "at_out-265.log").exists());
    }

    @Test
    public void testPerform_expandEnvironmentVariables() throws Exception {
        int buildVersion = 265;
        publisher = createPublisher("http://host/$JOB_NAME", PUBLISH_DIRECTORY + "/$JOB_NAME");
        final MatrixProject project = j.createMatrixProject("project");
        project.getPublishersList().add(this.publisher);
        when(buildMock.getProject()).thenReturn(project);
        when(buildMock.getNumber()).thenReturn(buildVersion);

        assertTrue(publisher.perform(buildMock, null, null));

        verify(buildMock).addAction(argThat(new ReportActionMatcher("http://host/theJob/at_report_p1-265.html", "http://host/theJob/at_out-265.log")));
        final File root = temporaryFolder.getRoot();
        assertTrue("html report was copied", new File(root, PUBLISH_DIRECTORY + "theJob/at_report_p1-265.html").exists());
        assertTrue("log was copied", new File(root, PUBLISH_DIRECTORY + "theJob/at_out-265.log").exists());
    }

    @Test
    public void testGetDescriptor() throws Exception {

    }

    private AcceptanceReportPublisher createPublisher(String reportBaseUrl, String publishDirectory) {
        final AcceptanceReportPublisher.CredentialsBlock credentialsBlock = new AcceptanceReportPublisher.CredentialsBlock("username", "password");
        return new AcceptanceReportPublisher(reportBaseUrl,
                credentialsBlock,
                getTestDirectory(publishDirectory),
                getTestDirectory(REPORT_PATH),
                getTestDirectory(LOG_PATH),
                "project");
    }

    private String getTestDirectory(String filename) {
        return new File(temporaryFolder.getRoot(), filename).getAbsolutePath();
    }

    private class ReportActionMatcher extends BaseMatcher<AcceptanceReportAction> {
        private String logUrl;
        private String reportUrl;
        private AcceptanceReportAction actual;

        public ReportActionMatcher(String reportUrl, String logUrl) {
            this.logUrl = logUrl;
            this.reportUrl = reportUrl;
        }

        public boolean matches(Object item) {
            if (item instanceof AcceptanceReportAction) {
                actual = (AcceptanceReportAction) item;

                return (StringUtils.equals(reportUrl, actual.getReportURL())
                        && StringUtils.equals(logUrl, actual.getLogURL()));
            } else {
                return false;
            }
        }

        public void describeTo(Description description) {
            description.appendText("logUrl - expected ").appendValue(logUrl).appendText(" but was ").appendValue(actual.getLogURL())
                    .appendText("\n reportUrl - expected ").appendValue(reportUrl).appendText(" but was ").appendValue(actual.getReportURL());
        }
    }
}
