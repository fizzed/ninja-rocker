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
import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.Rocker;
import com.fizzed.rocker.RockerModelCallback;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.runtime.RockerRuntime;
import com.fizzed.rocker.RockerTemplate;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.CompileDiagnostic;
import com.fizzed.rocker.runtime.CompileDiagnosticException;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.fizzed.rocker.runtime.ParserException;
import java.util.Map;

import javax.inject.Singleton;

import ninja.Context;
import ninja.Result;
import ninja.utils.NinjaProperties;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import ninja.Router;
import ninja.exceptions.InternalServerErrorException;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.template.TemplateEngine;
import ninja.template.TemplateEngineHelper;
import ninja.utils.Message;
import ninja.utils.NinjaConstant;
import ninja.utils.ResponseStreams;
import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.LoggerFactory;

@Singleton
public class TemplateEngineRocker implements TemplateEngine {
    static private final Logger log = LoggerFactory.getLogger(TemplateEngineRocker.class);
    
    private final String CONF_RELOAD_KEY = "rocker.reload";
    private final String FILE_SUFFIX = ".rocker.html";
    private final String CONTENT_TYPE = "text/html";

    private final String fileSuffix;
    private final String contentType;
    private final Map<String, Class<? extends DefaultRockerModel>> commonErrorTemplates;
    
    private final Router router;
    private final Messages messages;
    private final NinjaProperties ninjaProperties;
    private final Provider<Lang> langProvider;
    private final TemplateEngineHelper templateEngineHelper;
    
    
    @Inject
    public TemplateEngineRocker(Router router,
                                Messages messages,
                                NinjaProperties ninjaProperties,
                                PrettyTime prettyTime,
                                Provider<Lang> langProvider,
                                TemplateEngineHelper templateEngineHelper) throws Exception {
        this.router = router;
        this.messages = messages;
        this.ninjaProperties = ninjaProperties;
        this.langProvider = langProvider;
        this.templateEngineHelper = templateEngineHelper;
        
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
    
    // workaround until Ninja lets you override these constant template names
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
        try {
            doInvoke(context, result);
        } catch (CompileDiagnosticException e) {
            throwRenderingException(context, result, e);
        } catch (ParserException e) {
            throwRenderingException(context, result, e);
        } catch (RenderingException e) {
            throwRenderingException(context, result, e);
        }
    }
    
    public void doInvoke(final Context context, final Result result) {
        //log.info("invoke() with templateName: " + result.getTemplate());
        
        Object object = result.getRenderable();
        
        // various types of objects renderable may store
        Message valueMessage = null;
        DefaultRockerModel model = null;
        
        
        if (object == null || object instanceof Map) {            
            // assume this was a dynamic call
            String templateName = result.getTemplate();
            
            if (templateName == null) {
                templateName = this.templateEngineHelper.getTemplateForResult(context.getRoute(), result, fileSuffix);
                // we expect leading / to be stripped for rocker to find it
                if (templateName.startsWith("/")) {
                    templateName = templateName.substring(1);
                }
                
                // set back on result for possible debugging later
                result.template(templateName);
            }
            
            BindableRockerModel bindableModel = Rocker.template(templateName);
            
            if (object != null) {
                bindableModel.bind((Map)object);
            }
            
            model = (DefaultRockerModel)bindableModel.getModel();
        }
        else if (object instanceof DefaultRockerModel) {
            model = (DefaultRockerModel)object;   
        }
        else if (object instanceof BindableRockerModel) {
            // unwrap bindable model and get the real rocker model
            BindableRockerModel bindableModel = (BindableRockerModel)object;
            model = (DefaultRockerModel)bindableModel.getModel();
        }
        else if (object instanceof Message) {
            // system error???
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
        
        // create the 'N' variable
        final NinjaRocker N = new NinjaRocker(ninjaProperties, router, messages, langProvider.get(), context, result);
        
        // register callback so we can inject what we need into template
        // prior to rendering, but after template was generated
        model.__callback(new RockerModelCallback() {
            @Override
            public void onRender(RockerTemplate template) {
                if (template instanceof NinjaRockerTemplate) {
                    NinjaRockerTemplate ninjaTemplate = (NinjaRockerTemplate)template;
                    ninjaTemplate.N = N;
                    ninjaTemplate.injectContext(ninjaProperties, router, messages, langProvider.get(), context, result);
                }
            }
        });
        
        // looks like jetty gives us a single shot to write out the bytes
        // to the underlying output stream
        RockerOutput out = model.render();

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

        // TODO: charset?
        
        ArrayOfByteArraysOutput abao = (ArrayOfByteArraysOutput)out;
        
        // rendering was successful, finalize headers, and write it to output
        ResponseStreams responseStreams = context.finalizeHeaders(result);
        
        // TODO: we should hopefully be able to optimize the writing of bytes
        
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
            ParserException cause) {

        throw TemplateEngineRocker.renderingOrRuntimeException(
                    cause.getMessage(),
                    cause,
                    result,
                    "Rocker template parsing exception",
                    cause.getTemplatePath(),
                    cause.getLineNumber()
            );
    }
    
    public void throwRenderingException(
            Context context,
            Result result,
            RenderingException cause) {
        
        // likely project source code
        String sourcePath = cause.getTemplatePath() + "/" + cause.getTemplateName();
        
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
