pluginManagement {
    repositories {
        mavenLocal()
        // mirror for gradle plugin in China
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://www.jetbrains.com/intellij-repository/releases")
        maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "koupleless-idea"
