package org.kunyang.tools

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 插件入口，当该插件被加载时apply方法将被调用
 * @author swallow
 * @since 2016.6.23
 */
class PackagePlugin implements Plugin<Project> {
    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        project.extensions.create('apkPkg', PackagePluginExtension)

        project.afterEvaluate {
            //Package beta apk, modifyVersion++, betaVersion++
            project.tasks.assembleBeta.doLast {
                def newModifyVersion = Integer.parseInt(getModifyVersion()) + 1
                def newPakageTime = getNow()
                def versionName = project.extensions.android.defaultConfig.versionName
                def betaVersion = Integer.parseInt(getBetaVersion()) + 1
                updatePropFile(newModifyVersion, newPakageTime, versionName, betaVersion)
            }

            //Package release apk, modifyVersion++
            project.tasks.assembleRelease.doLast {
                def newModifyVersion = Integer.parseInt(getModifyVersion()) + 1
                def newPakageTime = getNow()
                def versionName = project.extensions.android.defaultConfig.versionName
                def betaVersion = getBetaVersion()
                updatePropFile(newModifyVersion, newPakageTime, versionName, betaVersion)
            }

            //Package debug apk
            project.tasks.assembleDebug.doLast {
                //Do nothing
            }
        }

        //Rename
        project.android.applicationVariants.all { variant ->
            nameApk(variant)
        }
    }

    def getModifyVersion(versionName) {
        def lastPackTime = getProperty('PACK_TIME')
        def lastVersion = getProperty('LAST_VERSION')
        def modifyVersion = getProperty('MODIFIED_VERSION')
        if (!getNow().equals(lastPackTime) || !lastVersion.equals(versionName))
            modifyVersion = "1";
        return modifyVersion;
    }

    def updatePropFile(modifyVersion, packageTime, versionName) {
        setProperty('MODIFIED_VERSION', modifyVersion)
        setProperty('PACK_TIME', packageTime)
        setProperty('LAST_VERSION', versionName)
    }

    def getNow() {
        return new Date().format("yyyyMMdd")
    }

    def nameApk(variant) {
        def buildType = variant.buildType.name;
        def outputFile = variant.outputs[0].outputFile;
        def versionName = project.extensions.android.defaultConfig.versionName;
        def pkgTime = getNow();
        def modifyVersion = getModifyVersion();
        def fileName = new StringBuilder(project.apkPkg.nameHead)
                .append("_")
                .append(versionName)
                .append(".build-")
                .append(pkgTime)
                .append(modifyVersion)
                .append(".")
                .append(buildType)
                .append(".apk")
                .build()
        variant.outputs[0].outputFile = new File(outputFile.parent, fileName)
        updatePropFile(++modifyVersion, pkgTime, versionName);
    }
}
