/**
 * Copyright (C) 2012-2015 Joe Lauer, Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fizzed.ninja.rocker;

import com.fizzed.ninja.rocker.views.common_error;
import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.RockerModelCallback;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.runtime.RockerRuntime;
import com.fizzed.rocker.RockerTemplate;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.CompileDiagnostic;
import com.fizzed.rocker.runtime.CompileDiagnosticException;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import java.util.Map;

import javax.inject.Singleton;

import ninja.Context;
import ninja.Result;
import ninja.utils.NinjaProperties;

import org.slf4j.Logger;

import com.google.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import ninja.exceptions.InternalServerErrorException;
import ninja.template.TemplateEngine;
import ninja.template.TemplateEngineHelper;
import ninja.utils.Message;
import ninja.utils.NinjaConstant;
import ninja.utils.ResponseStreams;
import org.slf4j.LoggerFactory;

@Singleton
public class TemplateEngineRocker implements TemplateEngine {
    static private final Logger log = LoggerFactory.getLogger(TemplateEngineRocker.class);
    
    private final String CONF_RELOAD_KEY = "rocker.reload";
    private final String FILE_SUFFIX = ".rocker.html";
    private final String CONTENT_TYPE = "text/html";

    private final NinjaRockerContext ninjaRockerContext;
    private final String fileSuffix;
    private final String contentType;
    private final Map<String, Class<? extends DefaultRockerModel>> commonErrorTemplates;
    
    @Inject
    public TemplateEngineRocker(NinjaRockerContext ninjaRockerContext,
                                TemplateEngineHelper templateEngineHelper,
                                NinjaProperties ninjaProperties) throws Exception {
        this.ninjaRockerContext = ninjaRockerContext;
        this.fileSuffix = FILE_SUFFIX;
        this.contentType = CONTENT_TYPE;
        
        // setup default error templates
        this.commonErrorTemplates = new HashMap<>();
        this.commonErrorTemplates.put(NinjaConstant.LOCATION_VIEW_FTL_HTML_INTERNAL_SERVER_ERROR, common_error.class);
        this.commonErrorTemplates.put(NinjaConstant.LOCATION_VIEW_FTL_HTML_BAD_REQUEST, common_error.class);
        this.commonErrorTemplates.put(NinjaConstant.LOCATION_VIEW_FTL_HTML_FORBIDDEN, common_error.class);
        this.commonErrorTemplates.put(NinjaConstant.LOCATION_VIEW_FTL_HTML_NOT_FOUND, common_error.class);
        this.commonErrorTemplates.put(NinjaConstant.LOCATION_VIEW_FTL_HTML_UNAUTHORIZED, common_error.class);
 
        if (ninjaProperties.isDev()) {
            Boolean reload = ninjaProperties.getBooleanWithDefault(CONF_RELOAD_KEY, true);
            
            if (reload) {
                // try to set reloading to true in dev mode
                RockerRuntime.getInstance().setReloading(reload);
            }
        }
        
        loadCustomErrorTemplates();
    }
    
    public final void loadCustomErrorTemplates() {
        Class<DefaultRockerModel> internalServerErrorClass = getCustomErrorViewClass("views.system.internal_server_error");
        if (internalServerErrorClass != null) {
            this.commonErrorTemplates.put(NinjaConstant.LOCATION_VIEW_FTL_HTML_INTERNAL_SERVER_ERROR, internalServerErrorClass);
        }
        
        Class<DefaultRockerModel> badRequestClass = getCustomErrorViewClass("views.system.bad_request");
        if (badRequestClass != null) {
            this.commonErrorTemplates.put(NinjaConstant.LOCATION_VIEW_FTL_HTML_BAD_REQUEST, badRequestClass);
        }
        
        Class<DefaultRockerModel> forbiddenClass = getCustomErrorViewClass("views.system.forbidden");
        if (forbiddenClass != null) {
            this.commonErrorTemplates.put(NinjaConstant.LOCATION_VIEW_FTL_HTML_FORBIDDEN, forbiddenClass);
        }
        
        Class<DefaultRockerModel> notFoundClass = getCustomErrorViewClass("views.system.not_found");
        if (notFoundClass != null) {
            this.commonErrorTemplates.put(NinjaConstant.LOCATION_VIEW_FTL_HTML_NOT_FOUND, notFoundClass);
        }
        
        Class<DefaultRockerModel> unauthorizedClass = getCustomErrorViewClass("views.system.unauthorized");
        if (unauthorizedClass != null) {
            this.commonErrorTemplates.put(NinjaConstant.LOCATION_VIEW_FTL_HTML_UNAUTHORIZED, unauthorizedClass);
        }
    }
    
    public Class<DefaultRockerModel> getCustomErrorViewClass(String className) {
        try {
            return (Class<DefaultRockerModel>)Class.forName(className);
        } catch (Throwable t) {
            // do nothing
            return null;
        }
    }
    
    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public String getSuffixOfTemplatingEngine() {
        return this.fileSuffix;
    }

    @Override
    public void invoke(Context context, Result result) {
        //log.info("invoke() with templateName: " + result.getTemplate());
        
        Object object = result.getRenderable();
        
        // various types of objects renderable may store
        Message valueMessage = null;
        DefaultRockerModel model = null;
        
        
        // if the object is null we simply render an empty map...
        if (object == null) {            
            throw renderingOrRuntimeException("Renderable was null. You must pass an instance of a NinjaRockerTemplate the Result.render() method.", null);
        }
        else if (object instanceof DefaultRockerModel) {
            model = (DefaultRockerModel)object;   
        }
        else if (object instanceof RockerTemplate) {
            throw renderingOrRuntimeException("Only templates with a parent class type of NinjaRockerTemplate are supported by this engine. " +
                                        "Did you forget to configure your rocker maven build plugin to 'extendsClass' from com.fizzed.ninja.rocker.NinjaRockerTemplate?", null);
        }
        else if (object instanceof Message) {
            // acceptable in case of system errors...
            valueMessage = (Message)object;
        }

        //
        // until NinjaFramework makes these template names configurable -- these
        // need to be handled by creating instances of correct Rocker template
        //
        if (model == null) {
            String templateName = result.getTemplate();
            
            if (templateName != null && commonErrorTemplates.containsKey(templateName)) {
                Class<? extends DefaultRockerModel> commonErrorType = commonErrorTemplates.get(templateName);

                DefaultRockerModel errorModel = null;

                try {
                    errorModel = commonErrorType.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw renderingOrRuntimeException("Unable to create template class " + commonErrorType, e);
                }

                if (errorModel instanceof common_error) {

                    ((common_error)errorModel).message(valueMessage);

                }

                model = errorModel;
            } else {
                throw renderingOrRuntimeException("Renderable object was not of class type 'NinjaRockerTemplate'. Rather it was of type '" + object.getClass().getSimpleName() + "'. You must pass an instance of NinjaRockerTemplate to the Result.render() method.", null);
            }
        }
        
        // create the N variable
        final NinjaRocker N = new NinjaRocker(ninjaRockerContext, context, result);
        
        // register callback so we can inject what we need into template
        // prior to rendering, but after template was generated
        model.__callback(new RockerModelCallback() {
            @Override
            public void onRender(RockerTemplate template) {
                if (template instanceof NinjaRockerTemplate) {
                    NinjaRockerTemplate ninjaTemplate = (NinjaRockerTemplate)template;
                    ninjaTemplate.N = N;
                }
            }
        });
        
        // looks like jetty gives us a single shot to write out the bytes
        // to the underlying output stream
        RockerOutput out = null;
        
        try {
            out = model.render();
        } catch (CompileDiagnosticException e) {
            throwRenderingException(context, result, e);
        } catch (RenderingException e) {
            throwRenderingException(context, result, e);
        }
        
        
        // set content type if not set
        if (result.getContentType() == null) {
            switch (out.getContentType()) {
                case HTML:
                    result.html();
                    break;
                case RAW:
                    result.contentType(Result.APPLICATION_OCTET_STREAM);
                    break;
            }
        }

        
        ArrayOfByteArraysOutput abao = (ArrayOfByteArraysOutput)out;
        
        // rendering was successful, finalize headers, and write it to output
        ResponseStreams responseStreams = context.finalizeHeaders(result);
        
        try (OutputStream os = responseStreams.getOutputStream()) {
            os.write(abao.toByteArray());
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new InternalServerErrorException(e);
        }
    }
    
    public void throwRenderingException(
            Context context,
            Result result,
            CompileDiagnosticException cause) {
        
        // likely project source code??
        CompileDiagnostic cd = cause.getDiagnostics().get(0);
        
        // analyze the underlying cause(s)
        // with auto reloading the exceptions can be numerous
        
        // rocker may have figured out what part of the template caused the
        // compilation problem so we'll throw a better exception
        if (cd.getTemplateLineNumber() >= 0) {
            throw TemplateEngineRocker.renderingOrRuntimeException(
                    cause.getMessage(),
                    cause,
                    result,
                    "Rocker compile exception",
                    cd.getTemplateFile().getAbsolutePath(),
                    (int)cd.getTemplateLineNumber()
            );
        } else {
            throw TemplateEngineRocker.renderingOrRuntimeException(
                    cause.getMessage(),
                    cause,
                    result,
                    "Rocker compile exception",
                    cd.getJavaFile().getAbsolutePath(),
                    (int)cd.getJavaLineNumber()
            );
        }
    }
    
    public void throwRenderingException(
            Context context,
            Result result,
            RenderingException cause) {
        
        // likely project source code
        String sourcePath = cause.getTemplatePath() + "/" + cause.getTemplateName();
        
        // analyze the underlying cause(s)
        // with auto reloading the exceptions can be numerous
        
        throw TemplateEngineRocker.renderingOrRuntimeException(
                cause.getMessage(),
                cause,
                result,
                "Rocker rendering exception",
                sourcePath,
                cause.getSourceLine()
        );
    }
    
    static public RuntimeException renderingOrRuntimeException(String message, Exception cause) throws RuntimeException {
        
        return renderingOrRuntimeException(message, cause, null, null, null, -1);
        
    }

    static public RuntimeException renderingOrRuntimeException(String message,
                                                        Exception cause,
                                                        Result result,
                                                        String title,
                                                        String sourcePath,
                                                        int lineOfError) {
        try {
            // are we running in Ninja w/ rendering exceptions?
            Class<?> ninjaRenderingExceptionClass
                = TemplateEngineRocker.class.getClassLoader().loadClass("ninja.exceptions.RenderingException");
            
            Constructor<?> constructor = ninjaRenderingExceptionClass.getDeclaredConstructor(
                    String.class, Throwable.class, Result.class, String.class, String.class, int.class);
            
            return (RuntimeException)constructor.newInstance(message,
                                                                cause,
                                                                result,
                                                                title,
                                                                sourcePath,
                                                                lineOfError);
        }
        catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.warn("Unable to throw new ninja.exceptions.RenderingException, falling back to old RuntimeException", e);
            
            // throw normal runtime exception, but build useful message
            String errMessage = new StringBuilder()
                .append(title)
                .append(": ")
                .append(message)
                .append(" from ")
                .append(sourcePath)
                .append(" @line ")
                .append(lineOfError)
                .toString();
            
            return new RuntimeException(errMessage, cause);
        }
    }
}
