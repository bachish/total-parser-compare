plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "total-comparator"
includeBuild("errecfuzz")
includeBuild("parser-compare")
includeBuild("runners")
