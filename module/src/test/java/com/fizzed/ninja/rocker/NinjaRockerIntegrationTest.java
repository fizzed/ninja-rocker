/*
 * Copyright 2016 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.ninja.rocker;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.HashMap;
import ninja.standalone.NinjaJetty;
import ninja.standalone.Standalone;
import ninja.standalone.StandaloneHelper;
import ninja.utils.NinjaMode;
import ninja.utils.NinjaTestBrowser;
import ninja.utils.NinjaTestServer;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class NinjaRockerIntegrationTest {
    
    @Test
    public void verifyChunkedTransferEncodingNotUsed() throws Exception {
        // find standalone and create it
        try (NinjaTestServer ninjaTestServer = new NinjaTestServer()) {
            NinjaTestBrowser browser = new NinjaTestBrowser();
            HttpResponse response = browser
                .makeRequestAndGetResponse(ninjaTestServer.getBaseUrl() + "/", new HashMap<String,String>());
            String body = EntityUtils.toString(response.getEntity());
            
            assertThat(body, containsString("Hi!"));
            assertThat(response.getFirstHeader("Transfer-Encoding"), is(nullValue()));
        }
    }
    
}
