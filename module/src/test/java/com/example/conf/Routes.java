package com.example.conf;

import ninja.Router;
import ninja.application.ApplicationRoutes;
import com.example.controllers.ApplicationController;

public class Routes implements ApplicationRoutes {

    @Override
    public void init(Router router) {
        router.GET().route("/").with(ApplicationController.class, "index");
    }
    
}