package com.one38over.gradle

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project

class SdePlugin implements Plugin<Project> {

    Map readJsonTaskConfiguration(File file) {
        return new JsonSlurper().parseText(file.text)
    }

    void apply(Project project) {
        File file = new File('sde.json')
        printf("SDE Config File Plugin %s %s\n", project.rootDir, file.absolutePath)
        Map configuration = readJsonTaskConfiguration(new File(project.rootDir, 'sde.json'))

        configuration.sde.each { taskName,taskConfig ->
            project.tasks.register(taskName, ShellExecTask.class) {
                group = taskConfig.group
                description = taskConfig.description
                dependsOn = taskConfig.dependsOn
                config = taskConfig.config
            }
        }
    }
}
