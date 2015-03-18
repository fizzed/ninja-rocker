Ninja Framework + Rocker Templates by Fizzed
============================================

 - [Fizzed, Inc.](http://fizzed.com)
 - Joe Lauer (Twitter: [@jjlauer](http://twitter.com/jjlauer))

## Overview

Integration of [Rocker templates](https://github.com/fizzed/rocker) with the
[Ninja Framework](https://github.com/ninjaframework/ninja). Rocker is a Java 8
optimized, near zero-copy rendering, speedy template engine that produces
statically typed, plain java object templates that are compiled along with the
rest of your project.

This project makes Rocker a first-class citizen to Ninja.  You can access utility
properties and methods for Ninja using the ```@N``` variable.  For example,
the following would lookup an asset:

    @args ()

    @N.assetsAt("css/app.css")

## Setup

### Add dependency

Add the ninja-rocker-module dependency to your pom.xml:

```xml
<dependency>
    <groupId>com.fizzed</groupId>
    <artifactId>ninja-rocker-module</artifactId>
    <version>0.9.0</version>
</dependency>
```

### Install module

Add the module to your conf/Module.java file:

```java
package conf;

import com.fizzed.ninja.rocker.NinjaRockerModule;
import com.google.inject.AbstractModule;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        install(new NinjaRockerModule());
    }

}
```

### Write templates

It's best to place your templates in the `views` folder of your application
with a suffix of `.rocker.html`.

## Demo

There is a Ninja app in the `demo` folder that demonstrates all the functionality
this module provides and it's a simple way to see how it works.  Run the following
in your shell:

    mvn -Pninja-run test

Once running, point your browser to http://localhost:8080/

If you'd like to see how simple hot-reloading works as you modify either the 
Java code or a Rocker template, open up a second shell and run:

    mvn fizzed-watcher:run

Any time you edit a file, the fizzed-watcher maven plugin will trigger a maven
<code>compile</code>.  This will also trigger Rocker to regenerate Java sources
for any changed templates.  Ninja will restart and your new changes will be
available.
