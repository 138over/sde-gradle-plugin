package com.one38over.util

import groovy.json.JsonSlurper

class JenkinsPluginIndex {
    static Map getUpdateCentral(String url) {
        String data = url.toURL().text
        data = data.substring(18, data.length() - 2)
        return new JsonSlurper().parseText(data)
    }

    static Map getUpdates(Map plugin, String url) {
        Map centralPluginIndex = getUpdateCentral(url)
        Map pluginIndex = [:]
        plugin.each { k,v ->
            pluginIndex[k] = centralPluginIndex["plugins"][v]
        }
        return pluginIndex
    }
}
