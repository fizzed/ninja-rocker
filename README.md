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

This project makes Rocker templates a first-class citizen to Ninja. All
Ninja-specific functionality is provided by way of the `N` variable that is
available to all templates. Here is a quick sample of what a `index.rocker.html`
template may look like

```html
@import controllers.Application

@args (String title)

<!DOCTYPE html>
<html lang="en">
<head>
    <title>@title</title>
    <link rel='stylesheet' href='@N.webJarsAt("bootstrap/3.3.2-1/css/bootstrap.min.css")'>
    <link rel='stylesheet' href='@N.assetsAt("css/app.css")'>
</head>
<body>
    Hi!
    <a href='@N.reverseRoute(Application.class, "index")'/>Home</a>
</body>
</html>
```

Once compiled into your project, you can call this template from your Ninja
controller.  Fully type safe and compile-time checked that the first parameter
is a String.

```java
public class Application {
    
    public Result index() {
        return Results.ok().render(
            views.index.template("Home")
        );
    }

}
```

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

Add the module to your conf/Module.java file. Once installed, Rocker will
replace the default FreeMarker template engine for all content with the type
of "text/html".

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

### Integrate Rocker with your build system

Since Rocker relies on compiled templates, you'll need to integrate Rocker's
parser & compiler into your build process.  Instructions are on the
[Rocker project](https://github.com/fizzed/rocker) site.  You can also check
out our demo application (more details below) to see it all together.

### Write templates

It's best to place your templates in the `views` folder of your application
with a suffix of `.rocker.html`.

## Demo

There is a Ninja app in the `demo` folder that demonstrates all the functionality
this module provides and it's a simple way to see how it works.  Run the following
in your shell (from the root project directory, not in `demo`):

    mvn -Pninja-run test

Once running, point your browser to http://localhost:8080/

If you'd like to see how simple hot-reloading works as you modify either the 
Java code or a Rocker template, open up a second shell and run the following
 (from the root project directory, not in `demo`):

    mvn fizzed-watcher:run

Any time you edit a file, the fizzed-watcher maven plugin will trigger a maven
<code>compile</code>.  This will also trigger Rocker to regenerate Java sources
for any changed templates.  Ninja will restart and your new changes will be
available.
