rootProject.name = "fakeplayer-parent"

include(
        "fakeplayer-api",
        "fakeplayer-core",
        "fakeplayer-dist",
        "fakeplayer-v1_21",
        "fakeplayer-v1_21_1",
        "fakeplayer-v1_21_6",
        "fakeplayer-v1_21_7",
        "fakeplayer-v1_21_8"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}