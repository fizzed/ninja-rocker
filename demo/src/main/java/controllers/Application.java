package controllers;

import com.fizzed.rocker.Rocker;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import ninja.Context;
import ninja.FilterWith;
import ninja.Ninja;
import ninja.Result;
import ninja.Results;
import ninja.SecureFilter;
import ninja.exceptions.BadRequestException;
import ninja.i18n.Lang;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.utils.NinjaProperties;
import ninja.validation.Validation;
import org.slf4j.Logger;

/**
 *
 * @author joelauer
 */
@Singleton
public class Application {
    
    private final NinjaProperties ninjaProperties;
    private final Ninja ninja;
    private final Lang lang;
    private final Logger log;
    
    @Inject
    public Application(NinjaProperties ninjaProperties, Ninja ninja, Lang lang, Logger log) {
        this.ninjaProperties = ninjaProperties;
        this.ninja = ninja;
        this.lang = lang;
        this.log = log;
    }
    
    public Result index() {
        return Results.ok().render(
            views.index.template()
        );
    }
    
    public Result ninja(Context context, @Param("lang") String l) {
        context.getSession().put("user", "Joe");
        
        // add a flash message
        context.getFlashScope().put("success", "Success message via flash!");
        context.getFlashScope().put("error", "Error message via flash!");
        
        Result result = Results.ok().render(
            views.ninja.template()
        );
        
        if (l != null) {
            lang.setLanguage(l, result);
        } else {
            lang.setLanguage("en", result);
        }
        
        return result;
    }
    
    public Result dynamic(Context context, @Param("test") int test, Validation validation) {
        
        /**
        // rocker way (bad templates immediately trigger exception)
        return Results.ok().render(
            Rocker.template("views/dynamic.rocker.html")
                .bind("name", "Joe")
                .bind("count", 1)
        );
        */
        
        // ninja way (with auto calculating of template names)
        Map<String,Object> values = new HashMap<>();
        values.put("name", "Joe");
        values.put("count", 1);
        
        return Results.ok()
            //.template("views/Application/dynamic2.rocker.html")
            .render(values);
    }
    
    public Result app_error() {
        throw new RuntimeException("Error during controller action");
    }
    
    public Result template_error() {
        return Results.ok().render(
            views.template_error.template(null)
        );
    }
    
    public Result bad_request() {
        throw new BadRequestException("Bad request for controller");
    }
    
    @FilterWith(SecureFilter.class) 
    public Result forbidden() {
        return Results.TODO();
    }
    
    public Result not_authorized(Context context) {
        return ninja.getUnauthorizedResult(context);
    }
    
    // copied verbatim from ninja-core code (they use a private method)
    static public String readNinjaVersion() {
        
        // location of the properties file
        String LOCATION_OF_NINJA_BUILTIN_PROPERTIES = "ninja/ninja-builtin.properties";
        // and the key inside the properties file.
        String NINJA_VERSION_PROPERTY_KEY = "ninja.version";
        
        String ninjaVersion;
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(LOCATION_OF_NINJA_BUILTIN_PROPERTIES)){

            Properties prop = new Properties();
            prop.load(stream);
            
            ninjaVersion = prop.getProperty(NINJA_VERSION_PROPERTY_KEY);
        
        } catch (Exception e) {
            //this should not happen. Never.
            throw new RuntimeException(new Error("Something is wrong with your build. Cannot find resource " + LOCATION_OF_NINJA_BUILTIN_PROPERTIES));
        }
        
        return ninjaVersion;
        
    }
    
}
