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

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Andoni del Olmo
 */
public class AcceptanceReportPublisherDescriptor extends BuildStepDescriptor<Publisher> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptanceReportPublisherDescriptor.class.getName());

    private URLChecker urlChecker = new JavaNetURLChecker();

    public AcceptanceReportPublisherDescriptor() {
        super(AcceptanceReportPublisher.class);
        load();
    }

    @Override
    public String getDisplayName() {
        return "Acceptance Test Reporting";
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Override
    public String getHelpFile() {
        return "/plugin/exactor-jenkins/help/help-globalConfig.html";
    }

    public FormValidation doCheckReportBaseUrl(@QueryParameter final String value)
            throws IOException, ServletException {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("doCheckReportBaseUrl [" + value + "]");

        if (StringUtils.isEmpty(value))
            return FormValidation.error("Please set a URL base path.");
        if (!value.contains("http"))
            return FormValidation.error("Please set a valid URL base path.");
        return FormValidation.ok();
    }

    public FormValidation doTestConnection(@QueryParameter("reportBaseUrl") final String reportBaseUrl,
                                           @QueryParameter("serverAuthUsername") final String serverAuthUsername,
                                           @QueryParameter("serverAuthPassword") final String serverAuthPassword) throws IOException, ServletException {
        this.urlChecker.setServerAuth(ServerAuth.getEncodedAuth(serverAuthUsername, serverAuthPassword));
        if (this.urlChecker.isUrlReachable(reportBaseUrl)) {
            return FormValidation.ok("Success");
        }
        return FormValidation.error("Cannot establish connection");
    }

    public FormValidation doCheckPublishDirectory(@QueryParameter final String value)
            throws IOException, ServletException {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("doCheckPublishDirectory [" + value + "]");

        if (StringUtils.isEmpty(value))
            return FormValidation.error("Please set a valid directory.");
        File directory = new File(value);
        if (!directory.exists() || !directory.isDirectory())
            return FormValidation.error("Please set an existing directory.");
        return FormValidation.ok();
    }

    public FormValidation doCheckReportLocation(@QueryParameter final String value)
            throws IOException, ServletException {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("doCheckReportLocation [" + value + "]");

        if (StringUtils.isEmpty(value))
            return FormValidation.error("Please set a valid location.");
        final File directory = new File(FilenameUtils.getFullPath(value));
        if (!directory.exists() || !directory.isDirectory())
            return FormValidation.error(String.format("Please set a valid location. Directory \"%s\" not valid.", directory));
        return FormValidation.ok();
    }

    public FormValidation doCheckLogLocation(@QueryParameter final String value)
            throws IOException, ServletException {
        if (LOGGER.isDebugEnabled())
            LOGGER.info("doCheckLogLocation [" + value + "]");

        if (StringUtils.isEmpty(value))
            return FormValidation.error("Please set a valid location.");
        final File directory = new File(FilenameUtils.getFullPath(value));
        if (!directory.exists() || !directory.isDirectory())
            return FormValidation.error(String.format("Please set a valid location. Directory \"%s\" not valid.", directory));
        return FormValidation.ok();
    }

    public FormValidation doCheckProject(@QueryParameter final String value) {
        if (LOGGER.isDebugEnabled())
            LOGGER.info("doCheckLogPath [" + value + "]");
        return FormValidation.ok();
    }

    public ListBoxModel doFillProjectItems() {
        ListBoxModel items = new ListBoxModel();
        List<String> jobNames = getProjects();
        LOGGER.debug("Job list [" + jobNames + "].");
        ListBoxModel.Option option;
        for (String job : jobNames) {
            option = new ListBoxModel.Option(job, job);
            items.add(option);
        }
        return items;
    }

    private List<String> getProjects() {
        List<String> projectList = new ArrayList<String>();
        Collection<String> jobNames = Jenkins.getInstance().getJobNames();
        for (String jobName : jobNames) {
            if (StringUtils.containsNone(jobName, "/")) {
                projectList.add(jobName);
            }
        }
        return projectList;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        save();
        return super.configure(req, formData);
    }
}