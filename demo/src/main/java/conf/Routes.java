package conf;

import ninja.AssetsController;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import controllers.Application;

public class Routes implements ApplicationRoutes {

    @Override
    public void init(Router router) {
        router.GET().route("/").with(Application.class, "index");
        router.GET().route("/ninja").with(Application.class, "ninja");
        
        router.GET().route("/app_error").with(Application.class, "app_error");
        router.GET().route("/template_error").with(Application.class, "template_error");
        router.GET().route("/bad_request").with(Application.class, "bad_request");
        router.GET().route("/forbidden").with(Application.class, "forbidden");
        router.GET().route("/not_authorized").with(Application.class, "not_authorized");
        
        router.GET().route("/assets/w/{fileName: .*}").with(AssetsController.class, "serveWebJars");
        router.GET().route("/assets/{fileName: .*}").with(AssetsController.class, "serveStatic");
    }
    
}