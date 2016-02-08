# Lagom Bundle Plugin

## Introduction

A plugin that uses [sbt-bundle](https://github.com/sbt/sbt-bundle) to produce a ConductR bundle for Lagom.

The declared descriptor names are automatically extracted from the Lagom services and added to the bundle configuration.   

## Usage

1. Add the plugin to the `plugins.sbt`:

    ```scala
    addSbtPlugin("com.typesafe.sbt" % "sbt-lagom-bundle" % "1.0.0")
    ```
2. Ensure your service implementation projects have enabled the `LagomJava` sbt plugin:  

    ```scala
    lazy val fooImpl = (project in file("foo-impl")).enablePlugins(LagomJava)
    ```
3. Finally, produce a ConductR bundle for each of your service with:

    ```
    bundle:dist
    ```

## TO BE REMOVED: Local Usage with Lagom project

This plugin is not yet published. Also this plugin depends on `lagom` which is not yet published. Therefore, it is necessary to first publish these plugins locally before using it with a Lagom sample project: 

Follow these instructions to create a bundle for a Lagom project, e.g. [chirper](https://github.com/typesafehub/lagom/tree/master/samples/chirper):

1. Clone the current master version of `lagom` and publish the snapshot version `0.1.0-SNAPSHOT` locally. 
 
    ```
    git clone git@github.com:typesafehub/lagom.git
    cd lagom
    sbt publish-local
    ```
2. Clone a specific version of `sbt-lagom-bundle` and publish the current version `0.1.0-SNAPSHOT` locally:
 
    ```
    git clone https://github.com/typesafehub/sbt-lagom-bundle
    cd sbt-lagom-bundle
    git checkout 1.0
    sbt publish-local
    ```
3. Start the ConductR sandbox via the `conductr-cli`:
    
    ```
    sandbox run 1.1.1 -p 9000 -p 9042 -p 9300 -p 9200 -f logging
    ```
4. Create `cassandra-configuration` for your Lagom project to run a cassandra bundle on ConductR:
    
    ```
    cd my-lagom-project
    sbt cassandra-configuration:dist
    conduct load cassandra cassandra-configuration-{hash}.zip
    conduct run cassandra
    ```
5. Create ConductR bundles for Lagom project and run it on ConductR:
    
    ```
    cd my-lagom-project
    sbt bundle:dist
    conduct load my-service-impl/target/bundle/my-service-impl-v1-{hash}.zip
    conduct run my-service-impl
    ```

## Advanced Usage

For more information check out [sbt-bundle](https://github.com/sbt/sbt-bundle).

&copy; Typesafe Inc., 2014-2016