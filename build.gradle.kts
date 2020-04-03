buildscript {
    repositories {
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
    }
}

plugins {
    kotlin("jvm") version "1.3.71"

    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    maven { setUrl("https://maven.aliyun.com/repository/public") }
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))

    implementation(vertx("core"))
    implementation(vertx("web"))
    implementation(vertx("config"))
//    implementation(vertx("config-hocon"))
    implementation(vertx("auth-oauth2"))
    implementation(vertx("lang-kotlin"))
    implementation(vertx("lang-kotlin-coroutines"))
    implementation(vertx("health-check"))
    implementation(vertx("web-templ-thymeleaf"))
    implementation(vertx("redis-client"))
    compile(vertx("pg-client"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    implementation("org.thymeleaf:thymeleaf:3.0.11.RELEASE")
}

tasks {
    compileKotlin {
        kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+NewInference")
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+NewInference")
        kotlinOptions.jvmTarget = "1.8"
    }
}

application {
    mainClassName = "MainKt"
    applicationDefaultJvmArgs += "-ea"

}

fun DependencyHandler.vertx(module: String, version: String = "4.0.0-milestone4") =
    "io.vertx:vertx-$module:$version"