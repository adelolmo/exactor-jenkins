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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Implementation of the URLChecker using java.net classes.
 *
 * @author Andoni del Olmo
 */
public class JavaNetURLChecker implements URLChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaNetURLChecker.class.getName());

    private String encodedAuthBytes;

    public void setServerAuth(String encodedAuthBytes) {
        this.encodedAuthBytes = encodedAuthBytes;
    }

    public boolean isUrlReachable(String reportUrl) {
        boolean isReportReachable = false;
        try {
            URL url = new URL(reportUrl);
            URLConnection connection = url.openConnection();
            setRequestAuthIfNeeded(connection);
            connection.connect();
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("ResponseCode[" + httpConnection.getResponseCode() + "]");
                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    isReportReachable = true;
            }
        } catch (Exception e) {
            isReportReachable = false;
        }
        return isReportReachable;
    }

    private void setRequestAuthIfNeeded(URLConnection connection) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("encoded auth string [" + encodedAuthBytes + "]");
        if (encodedAuthBytes != null) {
            connection.setRequestProperty("Authorization", "Basic " + encodedAuthBytes);
        }
    }
}