package conf;

import com.fizzed.ninja.rocker.NinjaRockerModule;
import com.google.inject.AbstractModule;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        install(new NinjaRockerModule());
    }

}