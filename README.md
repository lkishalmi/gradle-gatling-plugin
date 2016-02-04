# gradle-gatling-plugin
Gatling Plugin for Gradle

## Usage
```groovy
plugins {
  id "com.github.lkishalmi.gatling" version "0.1.2"
}
```

## Source Sets
This plugin does not use source sets at the moment. It uses the `simulationsDir` of the Gatling extension to set up the the compiler task for Gatling.

## Tasks

| Task Name               | Dependencies   | Type         | Description                              |
| ----------------------- | -------------- | ------------ | ---------------------------------------- |
| gatling                 | gatlingCompile | Gatling      | Starts the default Gatling simulation(s) |
| gatlingCompile          | All tasks which produce the gatling classpath. This includes the jar task for project dependencies included in the gatling configuration. | ScalaCompile | Compiles the Gatling simulation scripts  |
| gatling*SimulationName* | gatlingCompile | Gatling      | Starts the named Gatling simulation      |

## Project Layout

Gatling plugin - default project layout

| Directory                  | Meaning                                  |
| -------------------------- | ---------------------------------------- |
| `src/gatling/simulations` | Gatling simulation sources               |
| `src/gatling/data`         | Simulation data                          |
| `src/gatling/bodies`       | Request bodies                           |
| `src/gatling/conf`         | Configuration dir for gatling (optional) |

## Dependency Management

This plugin defines a `gatling` configuration which by default contains the whole Gattling runtime.
Required dependencies shall be added to this configuration. It would be used both for compile and
execution of Gatling Simulations.

The used Gatling version can be configured in the project's Gatling Extension:

```groovy
gatling.toolVersion = '2.1.7'
```

## Gatling Task

Gatling Task is very similar task to JavaExec task. It executes a Gatling simulation by calling 
`io.gatling.app.Gatling` task according to the specified properties.

Properties

| Name           | Type    | Default                       | Description |
| -------------- | ------- | ----------------------------- | -----------
| bodiesDir      | File    | src/gatling/bodies            | Path for the request bodies      |
| classesDir     | File    | gatlingCompile.destinationDir | Path of the compiled simulations |
| confDir        | File    | src/gatling/conf              | Path to Gatling configuration files. (Optional) |
| dataDir        | File    | src/gatling/data              | Path for the simulation data     |
| mute           | boolean | true                          | Interactive mode switch.         |
| reportsDir     | File    | $buildDir/reports/gatling     | Path to create reports.          |
| runDescription | String  | (null)                        | The description for this Gatling run. |
| simulation     | String  | (null)                        | The simulation to be executed.   |
| simulationsDir | File    | src/gatling/simulations       | Path to the simulation scripts   |

#####The default values of these properties can be configured throughout the project's gatling extension using the same property names.
