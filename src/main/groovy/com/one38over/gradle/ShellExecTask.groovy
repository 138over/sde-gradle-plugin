package com.one38over.gradle

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class ShellExecTask extends DefaultTask {
    @Input
    Map config = [:]

    @TaskAction
    void exec() {
        println this.group
        println this.description
        println this.dependsOn
        println JsonOutput.prettyPrint(JsonOutput.toJson(config))
        Integer result = 0 
    }
}
