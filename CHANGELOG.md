Ninja Framework + Rocker Templates by Fizzed
============================================

#### 0.12.0 - 2016-02-04

 - Bump to Rocker v0.11.1
 - Bump to Ninja v5.3.1
 - Ninja `N` variable now exposes `context` as public
 - Application-specific template support and examples!
 - Added support for accessing previously hidden variables in NinjaRocker by
   it being an actual instance of (DefaultNinjaRocker) where those properties
   have public methods.
 - Simpler code for rendering templates in engine
 - Improved future compat against Rocker changes for its default output
   type for rendering templates   

#### 0.11.1 - 2016-01-20

 - Bump to Rocker v0.10.5
 - Bump to Ninja v5.3.0

#### 0.11.0 - 2015-11-20

 - Bump to Rocker v0.10.3
 - Improved support for calling dynamic templates

#### 0.10.1 - 2015-09-28

 - Bump to Rocker v0.10.1
 - Added support for flash variables via `N.flash.get("name")`

#### 0.10.0 - 2015-09-24

 - Major refactor to support v0.10.0 of Rocker
 - Added support for Rocker hot reloading automatically if Ninja in dev mode
 - Added support for Ninja authenticity tokens
 - Added support for Ninja method of dynamic templates and property binding
 - Improved exception handling and display of hot reload parsing & compile
   exceptions -- template source code now displayed!

#### 0.9.1 - 2015-09-18
 - Improved documentation
 - More error checking on common misuse of module with helpful solutions in
   exception messages

#### 0.9.0 - 2015-03-18
 - Initial release
