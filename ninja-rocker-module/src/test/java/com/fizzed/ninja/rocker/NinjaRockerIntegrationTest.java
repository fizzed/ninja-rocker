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

import java.util.HashMap;
import ninja.NinjaTest;
import ninja.utils.SwissKnife;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class NinjaRockerIntegrationTest extends NinjaTest {
    
    @Test
    public void contentTypeAndCharset() throws Exception {
        HttpResponse response = this.ninjaTestBrowser
            .makeRequestAndGetResponse(
                ninjaTestServer.getBaseUrl() + "/", new HashMap<String,String>());
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getFirstHeader("Content-Type").getValue().toLowerCase(), is("text/html; charset=utf-8"));
        assertThat(response.getFirstHeader("Content-Length"), is(not(nullValue())));
        assertThat(Integer.valueOf(response.getFirstHeader("Content-Length").getValue()), greaterThan(0));
    }
    
    @Test
    public void chunkedTransferEncodingNotUsed() throws Exception {
        HttpResponse response = this.ninjaTestBrowser
        .makeRequestAndGetResponse(
            ninjaTestServer.getBaseUrl() + "/", new HashMap<String,String>());
        String body = EntityUtils.toString(response.getEntity());
            
        assertThat(body, containsString("Hi!"));
        assertThat(response.getFirstHeader("Transfer-Encoding"), is(nullValue()));
        assertThat(response.getFirstHeader("Content-Length"), is(not(nullValue())));
        assertThat(Integer.valueOf(response.getFirstHeader("Content-Length").getValue()), greaterThan(0));
    }
    
}
