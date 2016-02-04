Ninja Framework + Rocker Templates by Fizzed
============================================

[![Build Status](https://travis-ci.org/fizzed/ninja-rocker.svg?branch=master)](https://travis-ci.org/fizzed/ninja-rocker)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.fizzed/ninja-rocker/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.fizzed/ninja-rocker)

[Fizzed, Inc.](http://fizzed.com) (Follow on Twitter: [@fizzed_inc](http://twitter.com/fizzed_inc))

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

## Two-minute drill

There is a Ninja app in the `demo` folder that demonstrates all the functionality
this module provides and it's a simple way to see how it works.  This project 
uses [Blaze](https://github.com/fizzed/blaze) to help script tasks. Run the
following in your shell (from the root project directory, not in `demo`):

    java -jar blaze.jar demo

Once running, point your browser to http://localhost:8080/

## Setup

### Add dependency

Add the ninja-rocker-module dependency to your Maven pom.xml

```xml
<dependency>
    <groupId>com.fizzed</groupId>
    <artifactId>ninja-rocker-module</artifactId>
    <version>0.12.0</version>
</dependency>

<!-- for hot-reloading support only during development -->
<dependency>
    <groupId>com.fizzed</groupId>
    <artifactId>rocker-compiler</artifactId>
    <version>0.10.5</version>
    <scope>provided</scope>
</dependency>

```

### Add module to conf/Module.java

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

### Add maven plugin

Rocker ties into Maven with a plugin to parse templates and generate the Java
source during the <code>generate-sources</code> phase.  In order to access
the `N` variable and access Ninja features in your templates, it's critical
you configure the `extendsClass` variable as below.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.fizzed</groupId>
            <artifactId>rocker-maven-plugin</artifactId>
            <version>0.10.5</version>
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
    </plugins>
</build>
```

For more detailed information on Rocker and its maven plugin, please visit the
[Rocker project](https://github.com/fizzed/rocker) site.

### Exclude rocker templates as a resource

Ninja recommends including everything except .java files from <code>src/main/java</code>
by default.  Since Rocker's templates are compiled, this isn't necessary and you
can safely exclude Rocker templates from your final build.

```xml
<build>
    <resources>
        <resource>
            <directory>src/main/java</directory>
            <includes>
                <include>**/*</include>
            </includes>
            <!-- add rocker template exclude below -->
            <excludes>
                <exclude>**/*.java</exclude>
                <exclude>**/*.rocker.html</exclude>
            </excludes>
        </resource>
        <resource>
            <directory>src/main/resources</directory>
            <includes>
                <include>**/*</include>
            </includes>
        </resource>
    </resources>
</build>
```

### Exclude rocker compiled templates from triggering Ninja SuperDevMode restart

By default, Ninja's SuperDevMode watches all .class files in your <code>target/classes</code>
directory.  Any modification to the contents of that directory will trigger the
Ninja HTTP server to restart.  Rocker's templates are compiled and with hot
reload enabled, Rocker will recompile and reload your templates without requiring
a JVM restart.  Unfortunately, Ninja's defaults will still trigger a restart
since Rocker will recompile and change the contents of <code>target/classes</code>.
As long as you stick to the convention that any class in the <code>views</code>
package is a rocker template, you can exclude these classes:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.ninjaframework</groupId>
            <artifactId>ninja-maven-plugin</artifactId>
            <version>5.3.0</version>
            <configuration>
                <useDefaultExcludes>true</useDefaultExcludes>
                <excludes>
                    <exclude>(.*)rocker.html$</exclude>
                    <exclude>(.*)views/(.*).class$</exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Write templates

It's best to place your templates in the `views` folder of your application
with a suffix of `.rocker.html`.

## Ninja variable

Easiest way to discover all the properties and methods available in the `N`
variable is to take a look at [NinjaRocker.java](https://github.com/fizzed/ninja-rocker/blob/master/module/src/main/java/com/fizzed/ninja/rocker/NinjaRocker.java).

## Application-specific templates

Looking for the ultimate integration of Ninja into your application?  Create your
own application-specific template that subclasses `NinjaRockerTemplate` and
expose any number of useful variables and/or methods to any of your templates.

The demo has an example of how to do it [here](demo/src/main/java/utils/ApplicationRockerTemplate.java).

### Write your own application-specific template

Create a new class called `utils.ApplicationRockerTemplate`. This class will
subclass `com.fizzed.ninja.rocker.NinjaRockerTemplate` and then it will need to
override two methods to participate in the rendering process.

```java
package utils;

import com.fizzed.ninja.rocker.DefaultNinjaRocker;
import com.fizzed.ninja.rocker.NinjaRockerTemplate;
import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.RockerTemplate;
import com.fizzed.rocker.RockerUtils;

abstract public class ApplicationRockerTemplate extends NinjaRockerTemplate {

    public ApplicationRocker A;
    
    public ApplicationRockerTemplate(RockerModel model) {
        super(model);
    }
    
    /**
     * Apply NinjaRocker to template immediately before rendering. Best place
     * to setup your own application-specific properties or methods that rely
     * on Ninja context, router, messages, etc.
     * @param N The ninja rocker instance
     */
    @Override
    public void __apply(DefaultNinjaRocker N) {
        super.__apply(N);
        this.A = new ApplicationRocker(N);
    }

    /**
     * Associate this template with another template during the rendering
     * process.  This occurs when Template A calls or includes Template B.
     * Usually, you simply want to copy over the variables you created in
     * the __apply method.
     * @param template The template to associate us with
     */
    @Override
    protected void __associate(RockerTemplate template) {
        super.__associate(template);
        ApplicationRockerTemplate applicationTemplate
            = RockerUtils.requireTemplateClass(template, ApplicationRockerTemplate.class);
        this.A = applicationTemplate.A;
    }
    
}
```

(demo/src/main/java/utils/ApplicationRockerTemplate.java)
(demo/src/main/java/utils/ApplicationRocker.java)

### Your templates need to extend your application-specific template

There are two ways you can instruct a template to extend a specific superclass.
First, you can do it in the maven plugin:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.fizzed</groupId>
            <artifactId>rocker-maven-plugin</artifactId>
            <version><!-- version here --></version>
            <executions>
                <execution>
                    <id>generate-rocker-templates</id>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <extendsClass>utils.ApplicationRockerTemplate</extendsClass>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Alternatively, your template can set an option for itself

```html
@option extendsClass=utils.ApplicationRockerTemplate

@args (String title)

<h1>@title</h1>
```

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

## License

Copyright (C) 2015 Fizzed, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.
