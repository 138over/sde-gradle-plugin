package com.one38over.gradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.GradleException


class SdePlugin implements Plugin<Project> {

    Map readJsonTaskConfiguration(File file) {
        return new JsonSlurper().parseText(file.text)
    }

    void apply(Project project) {
        Map config = readJsonTaskConfiguration(new File(project.rootDir, 'sde.json'))

        //println JsonOutput.prettyPrint(JsonOutput.toJson(config))

        TaskHandler taskHandler = new TaskHandler(project)

        config.sde.each { String componentName, Map componentConfiguration ->
            componentConfiguration.task.each { String taskName, Map taskConfiguration ->
                try {
                    taskHandler.assign(componentName, taskName, taskConfiguration)
                } catch (GradleException e) {
                    throw new GradleException("SdePlugin: ${e.message}")
                }
            }
        }
    }
}
