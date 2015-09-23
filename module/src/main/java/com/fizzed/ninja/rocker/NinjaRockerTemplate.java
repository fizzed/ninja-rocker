
package com.fizzed.ninja.rocker;

import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.fizzed.rocker.runtime.DefaultRockerTemplate;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import ninja.Context;
import ninja.Result;
import ninja.exceptions.InternalServerErrorException;
import ninja.utils.ResponseStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * New base class for all Rocker templates targeted for the Ninja Framework.
 * Permits templates to be directly used as a <code>Renderable</code> in a
 * Result. Also adds all <i>implicit</i> variables that Ninja's default
 * FreeMarker template system includes.
 * 
 * @author joelauer
 * @param <T>
 */
public abstract class NinjaRockerTemplate<T extends NinjaRockerTemplate> extends DefaultRockerTemplate<T> {
    static private final Logger log = LoggerFactory.getLogger(NinjaRockerTemplate.class);
    
    public NinjaRocker N;
    
    public NinjaRockerTemplate(DefaultRockerModel model) {
        super(model);
    }
    
    @Override
    protected void __associate(DefaultRockerTemplate context) throws RenderingException {
        super.__associate(context);
        if (context instanceof NinjaRockerTemplate) {
            NinjaRockerTemplate ninjaTemplate = (NinjaRockerTemplate)context;
            this.N = ninjaTemplate.N;
        }
        else {
            throw new RenderingException("Template was not type " + NinjaRockerTemplate.class.getCanonicalName());
        }
    }
}
