# gradle-gatling-plugin
Gatling Plugin for Gradle

## Usage
```groovy
plugins {
  id "com.github.lkishalmi.gatling" version "0.1"
}
```

## Source Sets
This plugin does not use source sets at the moment. It uses the `simulationsDir` of the Gatling extension to set up the the compiler task for Gatling.

## Tasks

| Task Name               | Dependencies   | Type         | Description                              |
| ----------------------- | -------------- | ------------ | ---------------------------------------- |
| gatling                 | gatlingCompile | Gatling      | Starts the default Gatling simulation(s) |
| gatlingCompile          | All tasks which produce the gatling classpath. This includes the jar task for project dependencies included in the gatling configuration. | ScalaCompile | Compiles the Gatling simulation scripts  |
| getling*SimulationName* | gatlingCompile | Gatling      | Starts the named Gatling simulation      |

## Project Layout

Gatling plugin - default project layout

| Directory                  | Meaning                                  |
| -------------------------- | ---------------------------------------- |
| `src/gattling/simulations` | Gatling simulation sources               |
| `src/gatling/data`         | Simulation data                          |
| `src/gatling/bodies`       | Request bodies                           |
| `src/gatling/conf`         | Configuration dir for gatling (optional) |

## Dependency Management
## Gatling Extension

### Gatling Task

Properties

| Name           | Type    | Default                       | Description |
| -------------- | ------- | ----------------------------- | -----------
| classesDir     | File    | gatlingCompile.destinationDir | Path of the compiled simulations |
| dataDir        | File    | src/gatling/data              | Path for the simulation data     |
| bodiesDir      | File    | src/gatling/bodies            | Path for the request bodies      |
| reportsDir     | File    | $buildDir/reports/gatling     | Path to create reports.
| simulationsDir | File    | src/gatling/simulations       |
| confDir        | File    | src/gatling/conf              |
| runDescription | String  | (null)                        |
| simulation     | String  | (null)                        |
| mute           | boolean | true                          |

