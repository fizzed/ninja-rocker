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

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import ninja.Context;
import ninja.Result;
import ninja.Router;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.utils.NinjaProperties;

/**
 * Builds immutable DefaultNinjaRocker instances.
 * 
 * @author joelauer
 */
public class NinjaRockerFactoryImpl implements NinjaRockerFactory {
    
    @Override
    public DefaultNinjaRocker create(NinjaProperties ninjaProperties,
                                     Router router,
                                     Messages messages,
                                     Lang ninjaLang,
                                     Context context,
                                     Result result) {
        
        String lang = null;
        Map<String,String> session;
        Map<String,String> flash;
        Locale locale;
        String contextPath;
        
        // set language from framework
        Optional<String> language = ninjaLang.getLanguage(context, Optional.of(result));
        if (language.isPresent()) {
            lang = language.get();
        }
        
        Optional<String> requestLang = ninjaLang.getLanguage(context, Optional.of(result));
        
        locale = ninjaLang.getLocaleFromStringOrDefault(requestLang);
     
        // put all entries of the session cookie to the map.
        // You can access the values by their key in the cookie
        // For eg: @session.get("key")
        if (context.getSession() != null && !context.getSession().isEmpty()) {
            session = context.getSession().getData();
        } else {
            session = Collections.EMPTY_MAP;
        }
        
        contextPath = context.getContextPath();
        
        // flash code copied directly from ninja.template.TemplateEngineFreemarker
        // maybe we can get Ninja project to move it into a helper class eventually...
        // this is a convenience way of allowing flash messages to be translated
        flash = Maps.newHashMap();
        
        if (context.getFlashScope() != null) {
            for (Map.Entry<String, String> entry : context.getFlashScope().getCurrentFlashCookieData().entrySet()) {

                String messageValue = null;

                Optional<String> messageValueOptional = messages.get(entry.getValue(), context, Optional.of(result));

                if (!messageValueOptional.isPresent()) {
                    messageValue = entry.getValue();
                } else {
                    messageValue = messageValueOptional.get();
                }

                // new way
                flash.put(entry.getKey(), messageValue);
            }
        }
        
        return new DefaultNinjaRocker(ninjaProperties, router, messages, result, locale, context, contextPath, lang, session, flash);
    }
    
}
