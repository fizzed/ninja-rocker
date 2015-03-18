/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fizzed.ninja.rocker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javax.inject.Singleton;
import ninja.Router;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.utils.NinjaProperties;
import org.ocpsoft.prettytime.PrettyTime;

/**
 *
 * @author joelauer
 */

@Singleton
public class NinjaRockerContextImpl implements NinjaRockerContext {
    
    private final Router router;
    private final Messages messages;
    private final Provider<Lang> langProvider;
    private final NinjaProperties ninjaProperties;
    private final PrettyTime prettyTime;
    
    @Inject
    public NinjaRockerContextImpl(Router router,
                                    Messages messages,
                                    Provider<Lang> langProvider,
                                    NinjaProperties ninjaProperties,
                                    PrettyTime prettyTime) {
        this.router = router;
        this.messages = messages;
        this.langProvider = langProvider;
        this.ninjaProperties = ninjaProperties;
        this.prettyTime = prettyTime;
    }

    @Override
    public Router getRouter() {
        return this.router;
    }

    @Override
    public Messages getMessages() {
        return this.messages;
    }

    @Override
    public Provider<Lang> getLangProvider() {
        return this.langProvider;
    }

    @Override
    public NinjaProperties getNinjaProperties() {
        return ninjaProperties;
    }

    @Override
    public PrettyTime getPrettyTime() {
        return prettyTime;
    }
    
}
