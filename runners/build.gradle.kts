plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "1.9.0"
    `kotlin-dsl`
    application
}
kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("bachish:parser-compare:0.1")
    implementation("com.github.ajalt.clikt:clikt:4.4.0")

    testImplementation(kotlin("test")) // optional but fine
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testImplementation("errecfuzz:app")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // https://mvnrepository.com/artifact/com.charleskorn.kaml/kaml-jvm
    // add yaml support to kotlinx serialization
    implementation("com.charleskorn.kaml:kaml-jvm:0.85.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("errecfuzz:app")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.16")

}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("runners.FuzrecRunnerKt")
    applicationDefaultJvmArgs = listOf(
        "-Xmx4g", "-Xms4g",
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED"
    )
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-Xmx8g", "-Xms4g",
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
    )
}

//tasks.register<runners.runners.RunOneFile>("progs")

