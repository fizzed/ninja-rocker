/*
 * Copyright 2015 Fizzed Inc.
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

import java.util.Locale;
import java.util.Map;
import ninja.Context;
import ninja.Result;
import ninja.Router;
import ninja.i18n.Messages;
import ninja.utils.NinjaProperties;

/**
 * Exposes variables for public access.
 * 
 * @author joelauer
 */
public class DefaultNinjaRocker extends NinjaRocker {

    public DefaultNinjaRocker(NinjaProperties ninjaProperties, Router router, Messages messages, Result result, Locale locale, Context context, String contextPath, String lang, Map<String, String> session, Map<String, String> flash) {
        super(ninjaProperties, router, messages, result, locale, context, contextPath, lang, session, flash);
    }
    
    // public access to variables hidden to templates by default
    
    public NinjaProperties getNinjaProperties() {
        return this.ninjaProperties;
    }
    
    public Router getRouter() {
        return this.router;
    }
    
    public Messages getMessages() {
        return this.messages;
    }
    
    public Result getResult() {
        return this.result;
    }
    
    public Locale getLocale() {
        return this.locale;
    }
    
}
