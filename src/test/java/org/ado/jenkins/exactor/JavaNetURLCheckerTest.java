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

import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * @author Andoni del Olmo
 */
@Ignore(value = "the urls must be mocked!!")
public class JavaNetURLCheckerTest {

    private JavaNetURLChecker unitUnderTest;

    @Test
    public void testIsUrlReachable() throws Exception {
        assertTrue("is reachable", unitUnderTest.isUrlReachable("http://www.google.com"));
    }

    @Test
    public void testIsUrlReachable_withAuth() throws Exception {
        unitUnderTest.setServerAuth(ServerAuth.getEncodedAuth("username", "password"));
        assertTrue("is reachable", unitUnderTest.isUrlReachable("https://company.com/acceptance"));
    }
}