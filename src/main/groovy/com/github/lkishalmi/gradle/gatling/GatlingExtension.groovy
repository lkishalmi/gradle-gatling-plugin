package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Project

/**
 *
 * @author Laszlo Kishalmi
 */
class GatlingExtension {

    def toolVersion = '2.1.7'
    def jvmArgs = [
        '-server', 
        '-XX:+UseThreadPriorities',
        '-XX:ThreadPriorityPolicy=42',
        '-Xms512M',
        '-Xmx512M',
        '-Xmn100M',
        '-XX:+HeapDumpOnOutOfMemoryError',
        '-XX:+AggressiveOpts',
        '-XX:+OptimizeStringConcat',
        '-XX:+UseFastAccessorMethods',
        '-XX:+UseParNewGC',
        '-XX:+UseConcMarkSweepGC',
        '-XX:+CMSParallelRemarkEnabled',
        '-Djava.net.preferIPv4Stack=true',
        '-Djava.net.preferIPv6Addresses=false'
    ]
    
    def gatlingSourceSetName = 'gatling'
    
    File simulationsDir
    File dataDir
    File bodiesDir
    File reportsDir
    File confDir
    String runDescription

    boolean mute = true
}

