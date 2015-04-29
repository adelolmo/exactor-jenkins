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
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.util.LogTaskListener;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Andoni del Olmo
 */
public class AcceptanceReportPublisher extends Publisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptanceReportPublisher.class.getName());

    public String reportBaseUrl;
    public CredentialsBlock credentials;
    public String publishDirectory;
    public String reportLocation;
    public String logLocation;
    public String project;

    // only for testing, do not use!!
    public AcceptanceReportPublisher() {
    }

    @DataBoundConstructor
    public AcceptanceReportPublisher(String reportBaseUrl, CredentialsBlock credentials, String publishDirectory, String reportLocation, String logLocation, String project) {

        this.reportBaseUrl = reportBaseUrl;
        this.credentials = credentials;
        this.publishDirectory = publishDirectory;
        this.reportLocation = reportLocation;
        this.logLocation = logLocation;
        this.project = project;
    }

    private String getReportBaseUrl(EnvVars envVars) {
        String baseUrl = reportBaseUrl;
        if (LOGGER.isInfoEnabled())
            LOGGER.info("baseUrl[" + baseUrl + "] before");

        if (!reportBaseUrl.endsWith("/"))
            baseUrl = baseUrl.concat("/");

        if (LOGGER.isInfoEnabled())
            LOGGER.info("baseUrl[" + baseUrl + "] after");
        return envVars.expand(baseUrl);
    }

    private String getReportLocation(EnvVars envVars) {
        return envVars.expand(reportLocation);
    }

    private String getLogLocation(EnvVars envVars) {
        return envVars.expand(logLocation);
    }

    private String getPublishDirectory(EnvVars envVars) {
        String publishDirectory = envVars.expand(this.publishDirectory);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("publishDirectory[" + publishDirectory + "] before");

        if (!this.publishDirectory.endsWith(File.separator))
            publishDirectory = publishDirectory.concat(File.separator);

        if (LOGGER.isInfoEnabled())
            LOGGER.info("publishDirectory[" + publishDirectory + "] after");
        return publishDirectory;
    }

    private CredentialsBlock getCredentials() {
        return credentials;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        final EnvVars envVars = build.getEnvironment(new LogTaskListener(java.util.logging.Logger.getGlobal(), Level.INFO));
        String reportBaseUrlParam = getReportBaseUrl(envVars);
        String authCredentials = getAuthCredentials(credentials);
        String reportLocationParam = getReportLocation(envVars);
        String logLocationParam = getLogLocation(envVars);
        int buildNumberParam = getBuildNumber(build);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("perform > reportBaseUrl[" + reportBaseUrlParam
                    + "] auth[" + authCredentials
                    + "] reportLocationParam[" + reportLocationParam
                    + "] logLocation[" + logLocationParam
                    + "] buildNumber[" + buildNumberParam
                    + "].");

        final String reportFileName = getReportFileName(buildNumberParam, envVars);
        final String logFileName = getLogFileName(buildNumberParam, envVars);

        copyReportFile(reportLocationParam, reportFileName, envVars);
        copyLogFile(logLocationParam, logFileName, envVars);

        build.addAction(new AcceptanceReportAction(reportBaseUrlParam, authCredentials, reportFileName, logFileName));
        return true;
    }

    private int getBuildNumber(AbstractBuild<?, ?> build) {
        int buildNumber;
        if (StringUtils.equals(build.getProject().getName(), this.project)) {
            buildNumber = build.getNumber();
        } else {
            buildNumber = getBuildNumberForProject(this.project);
        }
        LOGGER.info("buildNumber [" + buildNumber + "].");
        return buildNumber;
    }

    private int getBuildNumberForProject(String projectName) {
        final List<Job> jobList = new ArrayList<Job>(Jenkins.getInstance().getItem(projectName).getAllJobs());
        return jobList.get(jobList.size() - 1).getLastCompletedBuild().getNumber();
    }

    private String getAuthCredentials(CredentialsBlock credentials) {
        if (credentials != null) {
            return ServerAuth.getEncodedAuth(credentials.getServerAuthUsername(), credentials.getServerAuthPassword());
        } else {
            return "";
        }
    }

    private void copyReportFile(String reportFileName, String fileName, EnvVars envVars) {
        copyFileToPublishDirectory(reportFileName, fileName, envVars);
    }

    private void copyLogFile(String logFileName, String fileName, EnvVars envVars) {
        copyFileToPublishDirectory(logFileName, fileName, envVars);
    }

    private void copyFileToPublishDirectory(String srcFile, String fileName, EnvVars envVars) {
        final String publishDirectory = getPublishDirectory(envVars);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Copying [{}] to [{}]...", publishDirectory, srcFile);
        final File destFile = new File(publishDirectory, fileName);
        try {
            FileUtils.copyFile(new File(srcFile), destFile, false);

            if (LOGGER.isInfoEnabled())
                LOGGER.info("Copy of [{}] to [{}] was successful.", srcFile, destFile.getAbsolutePath());

        } catch (IOException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("Cannot copy file [{}] to publishing directory [{}]. Reason: {}.",
                        logLocation, destFile.getAbsolutePath(), e.getMessage());
        }
    }

    private String getReportFileName(int buildVersion, EnvVars envVars) {
        return getFileNameWithVersion(envVars.expand(reportLocation), buildVersion);
    }

    private String getLogFileName(int buildVersion, EnvVars envVars) {
        return getFileNameWithVersion(envVars.expand(logLocation), buildVersion);
    }

    private String getFileNameWithVersion(String location, int buildVersion) {
        String name = FilenameUtils.getBaseName(location);
        String extension = FilenameUtils.getExtension(location);
        return name.concat(String.format("-%d", buildVersion)).concat(".").concat(extension);
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final AcceptanceReportPublisherDescriptor DESCRIPTOR = new AcceptanceReportPublisherDescriptor();

    public static class CredentialsBlock {
        private String serverAuthUsername;
        private String serverAuthPassword;

        @DataBoundConstructor
        public CredentialsBlock(String serverAuthUsername, String serverAuthPassword) {
            this.serverAuthUsername = serverAuthUsername;
            this.serverAuthPassword = serverAuthPassword;
        }

        public String getServerAuthUsername() {
            return serverAuthUsername;
        }

        public String getServerAuthPassword() {
            return serverAuthPassword;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CredentialsBlock that = (CredentialsBlock) o;

            if (serverAuthPassword != null ? !serverAuthPassword.equals(that.serverAuthPassword) : that.serverAuthPassword != null)
                return false;
            if (serverAuthUsername != null ? !serverAuthUsername.equals(that.serverAuthUsername) : that.serverAuthUsername != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = serverAuthUsername != null ? serverAuthUsername.hashCode() : 0;
            result = 31 * result + (serverAuthPassword != null ? serverAuthPassword.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("CredentialsBlock{");
            sb.append("serverAuthUsername [").append(serverAuthUsername).append(']');
            sb.append(", serverAuthPassword [").append(serverAuthPassword).append(']');
            sb.append('}');
            return sb.toString();
        }
    }
}