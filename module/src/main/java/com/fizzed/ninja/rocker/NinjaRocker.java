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

import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.runtime.Raw;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import ninja.AssetsController;
import ninja.Context;
import ninja.Result;
import ninja.Router;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaProperties;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * Provides all <code>N</code> variables and methods available to templates
 * when running inside NinjaFramework.
 * 
 *  @N.isProd()
 *  @N.assetsAt("css/style.css")
 * 
 * @author joelauer
 */
public class NinjaRocker {
    
    // hidden from templates but not subclasses
    protected final NinjaProperties ninjaProperties;
    protected final Router router;
    protected final Messages messages;
    protected final Context context;
    protected final Result result;
    protected final Locale locale;
    protected PrettyTime prettyTime;
    
    // will be visible to template during rendering process as a property
    public final String lang;
    public final Map<String,String> session;
    public final String contextPath;
    public final Map<String,String> flash;
    
    public NinjaRocker(NinjaProperties ninjaProperties, Router router, Messages messages, Lang ninjaLang, Context context, Result result) {
        this.ninjaProperties = ninjaProperties;
        this.router = router;
        this.messages = messages;
        
         // context & result required for correct i18n method
        this.context = context;
        this.result = result;
        
        // set language from framework
        Optional<String> language = ninjaLang.getLanguage(context, Optional.of(result));
        if (language.isPresent()) {
            lang = language.get();
        } else {
            lang = null;
        }
        
        Optional<String> requestLang = ninjaLang.getLanguage(context, Optional.of(result));
        this.locale = ninjaLang.getLocaleFromStringOrDefault(requestLang);
     
        // put all entries of the session cookie to the map.
        // You can access the values by their key in the cookie
        // For eg: @session.get("key")
        if (context.getSession() != null && !context.getSession().isEmpty()) {
            this.session = context.getSession().getData();
        } else {
            this.session = Collections.EMPTY_MAP;
        }
        
        this.contextPath = context.getContextPath();
        
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
    }
    
    private Class<?> typeNameToClass(String typeName) {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException ex) {
            throw new RenderingException("Unable to find class for type name: " + typeName);
        }
    }
    
    public String reverseRoute(String typeName, String methodName) {
        return router.getReverseRoute(
            typeNameToClass(typeName),
            methodName);
    }
    
    public String reverseRoute(String typeName, String methodName, Object... params) {
        return router.getReverseRoute(
            typeNameToClass(typeName),
            methodName,
            params);
    }
    
    public String reverseRoute(Class<?> type, String methodName) {
        return router.getReverseRoute(
            type,
            methodName);
    }
    
    public String reverseRoute(Class<?> type, String methodName, Object... params) {
        return router.getReverseRoute(
            type,
            methodName,
            params);
    }
    
    public String assetsAt(String file) {
        return reverseRoute(AssetsController.class, "serveStatic", "fileName", file);
    }
    
    public String webJarsAt(String file) {
        return reverseRoute(AssetsController.class, "serveWebJars", "fileName", file);
    }
    
    public String i18n(String messageKey) throws RenderingException {
        String messageValue = messages
                .get(messageKey, context, Optional.of(result))
                .or(messageKey);
        
        return messageValue;
    }
    
    public String i18n(String messageKey, Object... params) throws RenderingException {
        String messageValue = messages
                .get(messageKey, context, Optional.of(result), params)
                .or(messageKey);

        return messageValue;
    }
    
    public String prettyTime(Date d) {
        // lazy load pretty time
        if (prettyTime == null) {
            prettyTime = new PrettyTime(locale);
        }
        return prettyTime.format(d);
    }

    public String authenticityToken() {
        return context.getSession().getAuthenticityToken();
    }
    
    public Raw authenticityForm() {
        String s = new StringBuilder()
                .append("<input type=\"hidden\" value=\"")
                .append(context.getSession().getAuthenticityToken())
                .append("\" name=\"" + NinjaConstant.AUTHENTICITY_TOKEN +  "\" />")
                .toString();
        return Raw.of(s);
    }
        
    // other custom stuff useful
    
    public boolean isProd() {
        return ninjaProperties.isProd();
    }
    
    public boolean isTest() {
        return ninjaProperties.isTest();
    }
    
    public boolean isDev() {
        return ninjaProperties.isDev();
    }
    
}
