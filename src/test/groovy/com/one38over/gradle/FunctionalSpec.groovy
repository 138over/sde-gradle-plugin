package com.one38over.gradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Shared
import static org.gradle.testkit.runner.TaskOutcome.*

class FunctionalSpec extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    @Shared Map testConfig
    @Shared File buildConfig
    @Shared File buildFile

    def setup() {
        testConfig = new JsonSlurper().parseText(new File('src/test/resources/sde.json').text)
        buildConfig = testProjectDir.newFile('sde.json')
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "validate external json task configuration"() {
        given:
        buildConfig.write JsonOutput.prettyPrint(JsonOutput.toJson(testConfig))

        expect:
        println(buildConfig.text)
    }

    def "validate GradleRunner configuration"() {
        given:
        buildFile << """                      
            
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['tasks', '--all'])
                .withPluginClasspath()
                .build()

        then:
        printf("Output:%s\n", result.output)
        result.task(":tasks").outcome == SUCCESS
    }

    def "validate SDE Plugin configuration"() {
        given:
        buildConfig.write JsonOutput.prettyPrint(JsonOutput.toJson(testConfig))
        buildFile << """ 
        plugins {
            id 'com.one38over.sde'
        }                           
        """
        new File(testProjectDir.root.absolutePath).eachFile() { file->
            println file.getAbsolutePath()
            println file.text
        }

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['tasks', '--all'])
                .withPluginClasspath()
                .build()

        then:
        printf("Output:%s\n", result.output)
        result.task(":tasks").outcome == SUCCESS
    }

    def "validate SDE Task and Plugin configuration"() {
        given:
        buildConfig.write JsonOutput.prettyPrint(JsonOutput.toJson(testConfig))
        buildFile << """ 
        plugins {
            id 'com.one38over.sde'
        }                           
        """
        new File(testProjectDir.root.absolutePath).eachFile() { file->
            println file.getAbsolutePath()
            println file.text
        }

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['tasks', '--all', 'A'])
                .withPluginClasspath()
                .build()

        then:
        printf("Output:%s\n", result.output)
        result.task(":tasks").outcome == SUCCESS
    }
}
