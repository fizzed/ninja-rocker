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
import com.google.inject.Injector;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import ninja.AssetsController;
import ninja.Context;
import ninja.Result;
import ninja.Router;
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
abstract public class NinjaRocker {
    
    // hidden to template during render (but accessible in DefaultNinjaRocker)
    protected final Injector injector;
    protected final NinjaProperties ninjaProperties;
    protected final Router router;
    protected final Messages messages;
    protected final Result result;
    protected final Locale locale;
    protected PrettyTime prettyTime;                        // lazy loaded
    
    // visible to template during render
    public final Context context;
    public final String contextPath;
    public final String lang;
    public final Map<String,String> session;
    public final Map<String,String> flash;
    
    public NinjaRocker(Injector injector,
                       NinjaProperties ninjaProperties,
                       Router router,
                       Messages messages,
                       Result result,
                       Locale locale,
                       Context context,
                       String contextPath,
                       String lang,
                       Map<String,String> session,
                       Map<String,String> flash) {
        this.injector = injector;
        this.ninjaProperties = ninjaProperties;
        this.router = router;
        this.messages = messages;
        this.result = result;
        this.locale = locale;
        this.context = context;
        this.contextPath = contextPath;
        this.lang = lang;
        this.session = session;
        this.flash = flash;
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

    public String reverseRoute(MethodReference methodReference) {
        return router.getReverseRoute(methodReference);
    }

    public String reverseRoute(MethodReference methodReference, Map<String, Object> map) {
        return router.getReverseRoute(methodReference,map);
    }

    public String reverseRoute(MethodReference methodReference, Object... objects) {
        return router.getReverseRoute(methodReference, objects);
    }

    public String reverseRoute(MethodReference methodReference, Optional<Map<String, Object>> optional) {
        return router.getReverseRoute(methodReference, optional);
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
            .orElse(messageKey);
        
        return messageValue;
    }
    
    public String i18n(String messageKey, Object... params) throws RenderingException {
        String messageValue = messages
            .get(messageKey, context, Optional.of(result), params)
            .orElse(messageKey);

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
