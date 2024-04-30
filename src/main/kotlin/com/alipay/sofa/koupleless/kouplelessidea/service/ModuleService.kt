package com.alipay.sofa.koupleless.kouplelessidea.service

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.IoUtil
import cn.hutool.core.thread.ThreadUtil
import cn.hutool.core.util.StrUtil
import com.alipay.sofa.koupleless.kouplelessidea.ui.ContentPanel
import com.alipay.sofa.koupleless.kouplelessidea.ui.IDEUtil
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.io.File
import java.io.IOException

object ModuleService {

    fun createModule(myProject: Project, appName: String, moduleName: String, type: String, modulePackage:String, moduleGroupId:String, moduleArtifactId: String,
                     archetypeGroupId:String?="com.alipay.sofa.koupleless", archetypeArtifactId:String?="koupleless-common-module-archetype",
                     archetypeVersion:String?="1.1.0", uploadMetaInfo:Boolean=true):Process? {
        val contentPanel = myProject.service<ContentPanel>()

        val targetDirPath = myProject.basePath + "/.KouplelessIDE"
        val targetScriptPath = myProject.basePath + "/.KouplelessIDE/initModule_IDEA.sh"
        val targetDir = File(targetDirPath)
        val scriptURL = this.javaClass.classLoader.getResource("bin/initModule_IDEA.sh")
        val scriptFile = try {
            scriptURL.openStream()
        } catch (e: Exception) {
            return null
        }
        try {
            targetDir.mkdir()
        } catch (e: Exception) {
            removeTmpFile(null, targetDir)
            return null
        }
        val targetScript = try {
            File(targetScriptPath)
        } catch (e: Exception) {
            return null
        }
        try {
            targetScript.createNewFile()
            FileUtil.writeFromStream(scriptFile, targetScript)
        } catch (e: Exception) {
            removeTmpFile(targetScript, targetDir)
            return null
        }
        val command = StrUtil.builder().append("sh ").append(targetScriptPath).append(" ").append(moduleName)
                .append(" ").append(archetypeGroupId).append(" ").append(archetypeArtifactId).append(" ").append(archetypeVersion)
                .append(" ").append(modulePackage).append(" ").append(moduleGroupId).append(" ").append(moduleArtifactId).toString()

        contentPanel.printLog("命令：${command} 路径:${myProject.basePath}")
        val process =
            try {
//                val sourceCommand="source /etc/profile;source ~/.bash_profile;source ~/.bashrc;"
//                GeneralCommandLine("/bin/bash", "-c", sourceCommand+command).withWorkDirectory(myProject.basePath).createProcess()
                GeneralCommandLine("/bin/bash", "-c", "-l",command).withWorkDirectory(myProject.basePath).createProcess()
            } catch (e: Exception) {
                removeTmpFile(targetScript, targetDir)
                return null
            }

        val inputStream = process!!.inputStream
        val errorStream = process!!.errorStream
        val errorReader = IoUtil.getUtf8Reader(errorStream)
        val inputReader = IoUtil.getUtf8Reader(inputStream)


        ThreadUtil.execute {
            try {
                while (inputReader.readLine().also {
                        contentPanel.printMavenLog(it)
                    } != null) {

                }
            } catch (e: IOException) {
                contentPanel.printMavenLog(e.message ?: "")
            } finally {
                inputReader.close()
                inputStream.close()
            }
        }

        ThreadUtil.execute {
            try {
                while (errorReader.readLine().also { contentPanel.printMavenErrorLog(it) } != null) {

                }
            } catch (e: IOException) {
                contentPanel.printMavenErrorLog(e.message)
            } finally {
                errorReader.close()
                errorStream.close()
            }
        }

        process.onExit()?.thenAcceptAsync {
            FileUtil.del(targetScriptPath)
            FileUtil.del(targetDirPath)
            when (it.exitValue()) {
                0 -> {
                    if(uploadMetaInfo){
                        myProject.baseDir?.findChild("arkmodule")?.refresh(false, true)

                        IDEUtil.showInfoBalloon("模块创建成功", myProject)
                    }
                }

                else -> {
                    IDEUtil.showErrorBalloon("创建共库模块失败，请查看执行日志", myProject)
                    contentPanel.printMavenErrorLog("创建共库模块失败，请查看执行日志")
                }
            }
        }
        return process
    }

    fun createIndependentModule(myProject: Project, appName: String, moduleName: String, modulePackage: String, moduleGroupId:String,moduleArtifactId:String,
                     archetypeGroupId:String?="com.alipay.sofa.koupleless", archetypeArtifactId:String?="koupleless-common-module-archetype",
                     archetypeVersion:String?="1.1.0",moduleIndependentLocation:String):Process? {
        val contentPanel = myProject.service<ContentPanel>()

        val targetDirPath = myProject.basePath + "/.KouplelessIDE"
        val targetScriptPath = myProject.basePath + "/.KouplelessIDE/initIndependentModule_IDEA.sh"
        val targetDir = File(targetDirPath)
        val scriptURL = try {
            this.javaClass.classLoader.getResource("bin/initIndependentModule_IDEA.sh")
        } catch (e: Exception) {
            return null
        }
        val scriptFile = try {
            scriptURL.openStream()
        } catch (e: Exception) {
            return null
        }
        try {
            targetDir.mkdir()
        } catch (e: Exception) {
            removeTmpFile(null, targetDir)
            return null
        }
        val targetScript = try {
            File(targetScriptPath)
        } catch (e: Exception) {
            return null
        }
        try {
            targetScript.createNewFile()
            FileUtil.writeFromStream(scriptFile, targetScript)
        } catch (e: Exception) {
            removeTmpFile(targetScript, targetDir)
            return null
        }
        val command = StrUtil.builder().append("sh ").append(targetScriptPath).append(" ").append(moduleName)
                .append(" ").append(appName).append(" ").append(archetypeGroupId)
                .append(" ").append(archetypeArtifactId).append(" ").append(archetypeVersion)
                .append(" ").append(moduleGroupId).append(" ").append(moduleArtifactId).append(" ").append(modulePackage)
                .append(" ").append(moduleIndependentLocation).toString()

        contentPanel.printLog("命令：${command} 路径:${myProject.basePath}")
        val process =
            try {
                GeneralCommandLine("/bin/bash", "-c", "-l",command).withWorkDirectory(myProject.basePath).createProcess()
            } catch (e: Exception) {
                removeTmpFile(targetScript, targetDir)
                return null
            }

        val inputStream = process.inputStream
        val errorStream = process.errorStream
        val errorReader = IoUtil.getUtf8Reader(errorStream)
        val inputReader = IoUtil.getUtf8Reader(inputStream)


        ThreadUtil.execute {
            try {
                while (inputReader.readLine().also {
                        contentPanel.printMavenLog(it)
                    } != null) {

                }
            } catch (e: IOException) {
                contentPanel.printMavenLog(e.message ?: "")
            } finally {
                inputReader.close()
                inputStream.close()
            }
        }

        ThreadUtil.execute {
            try {
                while (errorReader.readLine().also { contentPanel.printMavenErrorLog(it) } != null) {

                }
            } catch (e: IOException) {
                contentPanel.printMavenErrorLog(e.message)
            } finally {
                errorReader.close()
                errorStream.close()
            }
        }

        process.onExit()?.thenAcceptAsync {
            FileUtil.del(targetScriptPath)
            FileUtil.del(targetDirPath)
            when (it.exitValue()) {
                0 -> {
                    IDEUtil.showInfoBalloon("模块创建成功", myProject)
                }

                else -> {
                    IDEUtil.showErrorBalloon("创建独立库模块失败，请查看执行日志", myProject)
                    contentPanel.printMavenErrorLog("创建独立库模块失败，请查看执行日志")
                }
            }
        }
        return process
    }

    private fun removeTmpFile(targetScript: File?, targetDir: File?) {
        try {
            if (targetScript != null) {
                FileUtil.del(targetScript)
            }
        } catch (e: Exception) {
        }
        try {
            if (targetDir != null) {
                FileUtil.del(targetDir)
            }
        } catch (e: Exception) {

        }
    }
}
