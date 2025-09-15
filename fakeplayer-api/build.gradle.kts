plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}
dependencies {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
    }


    dependencies {
        paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
    }
}