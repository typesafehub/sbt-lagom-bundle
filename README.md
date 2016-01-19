# Lagom Bundle Plugin

## Introduction

A plugin that uses [sbt-bundle](https://github.com/sbt/sbt-bundle) to produce a ConductR bundle for Lagom.

The declared descriptor names are automatically pulled from the Lagom services and added to the bundle configuration.   

## Local Usage with Lagom helloworld project

To create a bundle for the [Lagom helloworld](https://github.com/typesafehub/reactive-services-platform/tree/master/samples/helloworld) project follow these steps:

1. Clone `sbt-lagom-bundle` and publish the current version `0.1.0-SNAPSHOT` locally:
 
    ```
    git clone https://github.com/typesafehub/sbt-lagom-bundle
    cd sbt-lagom-bundle
    sbt publish-local
    ```
2. Add `sbt-lagom-bundle` plugin to the `plugins.sbt` of helloworld:

    ```
    addSbtPlugin("com.typesafe.sbt" % "sbt-lagom-bundle" % "0.1.0-SNAPSHOT")
    ```
3. Declare the required ConductR settings in the `build.sbt` of helloworld:

    ```
    import ByteConversions._
    BundleKeys.nrOfCpus := 2.0
    BundleKeys.memory := 32.MiB
    BundleKeys.diskSpace := 300.MiB
    ```
4. Create the bundle for the helloworld project
    
    ```
    cd reactive-service-platform/samples/helloworld
    sbt bundle:dist
    ```
    
This creates the ConductR bundle in the directory `reactive-services-platform/samples/helloworld/target/bundle`.    

## Release Usage

Declare the plugin (typically in a `plugins.sbt`):

```scala
addSbtPlugin("com.typesafe.sbt" % "sbt-lagom-bundle" % "0.1.0")
```

Now you can produce a bundle:

```
bundle:dist
```

For more information check out [sbt-bundle](https://github.com/sbt/sbt-bundle).

&copy; Typesafe Inc., 2014-2016