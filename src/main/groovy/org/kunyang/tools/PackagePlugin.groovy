package org.kunyang.tools

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

/**
 * 插件入口，当该插件被加载时apply方法将被调用
 * @author swallow
 * @since 2016.6.23
 */
class PackagePlugin implements Plugin<Project> {
    private Project project;

    def getPropFile() {
        return new File(project.getProjectDir().toString() + '\\buildCfg.properties')
    }

    def updatePropFile(modifyVersion, packageTime, versionName) {
        File propFile = getPropFile();
        if (!propFile.exists())
            return;
        def printWriter = propFile.newPrintWriter();
        printWriter.write("# 修正版本号，每天清零" + '\n');
        printWriter.write('MODIFIED_VERSION = ' + modifyVersion + '\n');
        printWriter.write('PACK_TIME = ' + packageTime + '\n');
        printWriter.write('LAST_VERSION = ' + versionName + '\n');
        printWriter.flush()
        printWriter.close()
    }

    def getNow() {
        return new Date().format("yyyyMMdd")
    }

    def getModifyVersion(versionName) {
        File propFile = getPropFile();
        //If the file not exists, create it.
        if (!propFile.exists()) {
            propFile.createNewFile();
            updatePropFile(1, getNow(), project.extensions.android.defaultConfig.versionName, 1);
        }
        def Properties prop = new Properties();
        prop.load(new FileInputStream(propFile));
        def lastPackTime = prop.getProperty('PACK_TIME');
        def lastVersion = prop.getProperty('LAST_VERSION');
        def buildVerion = prop.getProperty('MODIFIED_VERSION');
        if (!getNow().equals(lastPackTime) || !lastVersion.equals(versionName))
            buildVerion = "1";
        return buildVerion;
    }

    def getMergedFlavorName(variant) {
        String mergeFlavorName = '-'
        def flavorDimensionList = project.android.flavorDimensionList;
        if (flavorDimensionList != null) {
            def dimensionsNum = flavorDimensionList.size()
            for (int i=0; i<dimensionsNum; i++) {
                def flavor = variant.productFlavors[i]
                if (flavor == null)
                    continue
                mergeFlavorName += flavor.name + '-'
            }
            mergeFlavorName = mergeFlavorName.substring(0, mergeFlavorName.lastIndexOf('-'))
        }
        return mergeFlavorName;
    }

    def isAssemble(buildType) {
        Gradle gradle = project.getGradle()
        String  tskReqStr = gradle.getStartParameter().getTaskRequests().toString()
        println 'kkkkkk  '+tskReqStr + '  '+buildType
        return tskReqStr.contains('assemble') && tskReqStr.toLowerCase().contains(buildType)
    }

    def nameApk(variant) {
        def buildType = variant.buildType.name;
        def outputFile = variant.outputs[0].outputFile
        def versionName = variant.mergedFlavor.versionName;
        def mergedFlavorName = getMergedFlavorName(variant)
        def pkgTime = getNow();
        def modifyVersion = getModifyVersion(versionName)
        def fileName = project.apkPkg.nameHead +
                '_' +
                versionName +
                mergedFlavorName+
                ".build-"+
                pkgTime+
                modifyVersion+
                "."+
                buildType+
                ".apk"
        println 'gggggggg   '+fileName
        variant.outputs[0].outputFile = new File(outputFile.parent, fileName)
        if (isAssemble(buildType))
            modifyVersion++
        updatePropFile(modifyVersion, pkgTime, versionName)
    }

    @Override
    void apply(Project project) {
        this.project = project;
        project.extensions.create('apkPkg', PackagePluginExtension)

        project.android.applicationVariants.all { variant->
            nameApk(variant)
        }
    }
}
