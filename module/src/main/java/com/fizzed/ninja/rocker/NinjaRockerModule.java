package com.fizzed.ninja.rocker;

import com.google.inject.AbstractModule;
import ninja.template.TemplateEngine;

/**
 * 
 * Bindings for Rocker template engine
 * 
 * @author jjlauer
 *
 */
public class NinjaRockerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NinjaRockerContext.class).to(NinjaRockerContextImpl.class);
        bind(TemplateEngine.class).to(TemplateEngineRocker.class);
    }
    
}