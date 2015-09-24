
package com.fizzed.ninja.rocker;

import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.RockerTemplate;
import com.fizzed.rocker.runtime.DefaultRockerTemplate;

/**
 * Base class for all Rocker templates targeted for the Ninja Framework.
 * Exposes the 'N' variable to all templates for Ninja.
 * 
 * @author joelauer
 */
public abstract class NinjaRockerTemplate extends DefaultRockerTemplate {

    public NinjaRocker N;
    
    public NinjaRockerTemplate(RockerModel model) {
        super(model);
    }
    
    /**
     * Associates this template for processing within the context of another
     * template.  This happens when TemplateA calls or includes TemplateB.  
     * TemplateB needs to share variables from TemplateA.
     * @param context The template calling this template during a render
     */
    @Override
    protected void __associate(RockerTemplate context) {
        super.__associate(context);
        
        if (context instanceof NinjaRockerTemplate) {
            NinjaRockerTemplate ninjaContext = (NinjaRockerTemplate)context;
            this.N = ninjaContext.N;
        }
        else {
            throw new IllegalArgumentException("Unable to associate (context was not an instance of " + NinjaRockerTemplate.class.getCanonicalName() + ")");
        }
    }
}
