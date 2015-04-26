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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Andoni del Olmo
 */
public class AcceptanceReportActionTest {

    private AcceptanceReportAction unitUnderTest;
    private AcceptanceReportActionFactory actionFactory;

    @Mock
    private URLChecker urlCheckerMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.actionFactory = new AcceptanceReportActionFactory(this.urlCheckerMock);
    }

    @Test
    public void testDoesReportExists_true() throws Exception {
        when(this.urlCheckerMock.isUrlReachable("http://host/at_report_p1-265.html")).thenReturn(true);
        this.unitUnderTest = this.actionFactory.createAcceptanceReportAction();
        assertTrue("ACT Report exist", this.unitUnderTest.getDoesReportExists());
    }

    @Test
    public void testDoesReportExists_false() throws Exception {
        when(this.urlCheckerMock.isUrlReachable("http://host/at_report_p1-265.html")).thenReturn(false);
        this.unitUnderTest = this.actionFactory.createAcceptanceReportAction();
        assertFalse("ACT Report does not exist", this.unitUnderTest.getDoesReportExists());
    }

    @Test
    public void testGetReportURL_normal() throws Exception {
        this.unitUnderTest = this.actionFactory.createAcceptanceReportAction();
        assertEquals("ACT Report URL", "http://host/at_report_p1-265.html", this.unitUnderTest.getReportURL());
    }

    @Ignore
    @Test
    public void testGetLogURL_wrongReportBasePathURL() throws Exception {
        this.actionFactory.setReportBasePath("wrong URL");
        this.unitUnderTest = this.actionFactory.createAcceptanceReportAction();
        assertEquals("log URL", "http://host/at_out-265.log", this.unitUnderTest.getLogURL());
    }

    @Test
    public void testGetLogURL_normal() throws Exception {
        this.unitUnderTest = this.actionFactory.createAcceptanceReportAction();
        assertEquals("log URL", "http://host/at_out-265.log", this.unitUnderTest.getLogURL());
    }
}

class AcceptanceReportActionFactory {

    private URLChecker urlChecker;
    private String reportBasePath = "http://host/";
    private String reportFileName = "at_report_p1-265.html";
    private int buildVersion = 265;
    private String logPath = "at_out-265.log";
    private String publishDirectory = "\\Program Files (x86)\\Apache Software Foundation\\Apache2.2\\htdocs\\";

    AcceptanceReportActionFactory(URLChecker urlChecker) {
        this.urlChecker = urlChecker;
    }

    public AcceptanceReportAction createAcceptanceReportAction() {
        AcceptanceReportAction acceptanceReportAction = new AcceptanceReportAction(this.reportBasePath, "auth",
                reportFileName, logPath);
        acceptanceReportAction.setUrlChecker(this.urlChecker);
        return acceptanceReportAction;
    }

    public void setReportBasePath(String reportBasePath) {
        this.reportBasePath = reportBasePath;
    }

    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    public void setBuildVersion(int buildVersion) {
        this.buildVersion = buildVersion;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public void setPublishDirectory(String publishDirectory) {
        this.publishDirectory = publishDirectory;
    }
}
