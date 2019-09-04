/*
 * Copyright 2011-2018 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.collection.JavaConverters._
import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean
import java.util

class GatlingDebugSimulation extends Simulation {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._
  import org.json4s.jackson.Serialization
  import org.json4s.jackson.Serialization._

  implicit val formats = DefaultFormats

  private val heapMemory = ManagementFactory.getMemoryMXBean.getHeapMemoryUsage

  println(s"""@@@@.heap {"min": ${heapMemory.getInit}, "max": ${heapMemory.getMax}}""")

  private val jvmArgs  = ManagementFactory.getRuntimeMXBean.getInputArguments

  println(s"@@@@.jvm ${write(jvmArgs.asScala.filterNot(_.startsWith("-D")))}}")

  println(s"@@@@.env ${write(System.getenv.asScala)}}")

  println(s"@@@@.sys ${write(System.getProperties.asScala)}}")

  setUp(scenario("Scenario Name").pause(1).inject(atOnceUsers(0)))
}
