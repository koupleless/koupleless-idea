plugins {
    // Kotlin Gradle plugin (KGP) and Kotlin version mapping: https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.changelog") version "2.0.0"
    // to build into IntelliJ plugin
    id("org.jetbrains.intellij") version "1.17.3"
    // to generate by OpenAPI
    id("org.openapi.generator") version "6.2.1"
    // to report test coverage
//    id("jacoco")
}

group = "com.alipay.sofa.koupleless"
version = "1.0-SNAPSHOT"

configurations.all {
    exclude(group = "com.fasterxml.jackson.core")
    exclude(group = "com.fasterxml.jackson.jaxrs")
    exclude(group = "com.fasterxml.jackson.dataformat")
    exclude(group = "com.fasterxml.jackson.module")
}

repositories {
    mavenLocal()

    maven("https://maven.aliyun.com/repository/jcenter")
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies{
    implementation("cn.hutool:hutool-all:5.8.25")
    implementation(kotlin("stdlib"))
    // OpenAPI 生成使用
    implementation("io.swagger:swagger-annotations:1.6.6")
    // 修改 pom 使用
    implementation("org.apache.maven:maven-model:3.6.3")
    // 修改 class 使用
    implementation("com.github.javaparser:javaparser-core:3.25.6")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.6")
    // 修改 yml 使用
    implementation("org.yaml:snakeyaml:2.1")
    // 修改 properties 使用
    implementation("org.apache.commons:commons-configuration2:2.9.0")
    implementation("commons-beanutils:commons-beanutils:1.9.4")
    implementation("commons-jxpath:commons-jxpath:1.3")

    // json 序列化
    implementation("com.alibaba:fastjson:1.2.83")
    // 测试使用
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.1")
    testImplementation("io.mockk:mockk:1.12.4")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.5")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("yaml", "java"))
    updateSinceUntilBuild.set(false)
}

//jacoco {
//    toolVersion = "0.8.11"
//}
//
//tasks.test {
//    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
//}
//tasks.jacocoTestReport {
////    dependsOn(tasks.test) // tests are required to run before generating the report
//    reports {
//        xml.required.set(true)
//        csv.required.set(false)
//        html.required.set(true)
//        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
//    }
//}

tasks.buildSearchableOptions {
    enabled = false
}

tasks {
    runIde{
        maxHeapSize = "4g"
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
