package com.fizzed.ninja.rocker;

import com.google.inject.Provider;
import ninja.Router;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.utils.NinjaProperties;
import org.ocpsoft.prettytime.PrettyTime;

/**
 *
 * @author joelauer
 */

public interface NinjaRockerContext {
    
    Router getRouter();
    Messages getMessages();
    Provider<Lang> getLangProvider();
    NinjaProperties getNinjaProperties();
    PrettyTime getPrettyTime();
    
}
