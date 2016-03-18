# Lagom Bundle Plugin

[![Build Status](https://api.travis-ci.org/typesafehub/sbt-lagom-bundle.png?branch=master)](https://travis-ci.org/typesafehub/sbt-lagom-bundle)

## Introduction

A plugin that uses [sbt-bundle](https://github.com/sbt/sbt-bundle) to produce a ConductR bundle for Lagom.

The declared descriptor names are automatically extracted from the Lagom services and added to the bundle configuration.   

## Usage

1. Add the plugin to the `plugins.sbt`:

    ```scala
    addSbtPlugin("com.typesafe.sbt" % "sbt-lagom-bundle" % "1.0.2")
    ```
2. Ensure your service implementation projects have enabled the `LagomJava` sbt plugin:  

    ```scala
    lazy val fooImpl = (project in file("foo-impl")).enablePlugins(LagomJava)
    ```
3. Finally, produce a ConductR bundle for each of your service with:

    ```
    bundle:dist
    ```

## Settings

The following settings are available via `LagomBundleKeys`:

Setting                  | Description
-------------------------|------------
conductrBundleLibVersion | The version of conductr-bundle-lib to depend on. Defaults to 1.4.1.
endpointsPort            | Declares the port for each service endpoint that gets exposed to the outside world, e.g. http://:9000/myservice. Defaults to 9000.
    
## Advanced Usage

For more information configuring bundles please check out [sbt-bundle](https://github.com/sbt/sbt-bundle).

&copy; Lightbend Inc., 2014-2016
