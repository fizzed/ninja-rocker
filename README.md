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
template would look like using a few of the most common Ninja features.

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
<script type="text/javascript">
@if (N.isProd()) {
    /* production-only code (e.g. google analytics) */
}
</script>
</html>
```

Once compiled into your project, you can call this template from your Ninja
controller.  Fully type safe and compile-time checked.

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
    <version>0.9.1</version>
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

## Ninja variable

Easiest way to discover all the properties and methods available in the `N`
variable is to take a look at [NinjaRocker.java](https://github.com/fizzed/ninja-rocker/blob/master/module/src/main/java/com/fizzed/ninja/rocker/NinjaRocker.java).

## Common issues

If your Ninja project compiles and runs, but you get a runtime error like this:

    ERROR c.f.n.rocker.TemplateEngineRocker - Unable to handle renderable not of type: class views.ApplicationController.helloWorld

You likely forgot to configure your rocker maven plugin to "extendsClass" from
code>com.fizzed.ninja.rocker.NinjaRockerTemplate</code>. See below for more info.

If your project won't compile and you see compiler warnings like:

    [ERROR] /fizzed/java-ninja-rocker/demo/target/generated-sources/rocker/views/ninja.java:[162,65] cannot find symbol
    [ERROR] symbol:   variable N

You most likely did not configure your rocker maven plugin to extend templates
from <code>com.fizzed.ninja.rocker.NinjaRockerTemplate</code> rather than the
default of <code>com.fizzed.rocker.runtime.DefaultRockerTemplate</code>.  The "N"
variable is defined in <code>com.fizzed.ninja.rocker.NinjaRockerTemplate</code>.

The configuration section for your rocker plugin for maven should look like this:

    <plugin>
        <groupId>com.fizzed</groupId>
        <artifactId>rocker-maven-plugin</artifactId>
        <version>0.9.0</version>
        <executions>
            <execution>
                <id>generate-rocker-templates</id>
                <goals>
                    <goal>generate</goal>
                </goals>
                <configuration>
                    <extendsClass>com.fizzed.ninja.rocker.NinjaRockerTemplate</extendsClass>
                </configuration>
            </execution>
        </executions>
    </plugin>
