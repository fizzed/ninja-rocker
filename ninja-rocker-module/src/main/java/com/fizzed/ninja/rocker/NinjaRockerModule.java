package com.fizzed.ninja.rocker;

import com.google.inject.AbstractModule;

/**
 * Bindings for Rocker template engine
 * 
 * @author jjlauer
 */
public class NinjaRockerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TemplateEngineRocker.class);
    }
    
}