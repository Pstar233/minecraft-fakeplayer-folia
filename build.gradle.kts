plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

allprojects {
    group = "io.github.hello09x.fakeplayer"
    version = "0.3.18"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://libraries.minecraft.net/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://jitpack.io")
        maven("https://repo.extendedclip.com/releases/")

        // 添加这些仓库以确保能找到所有依赖
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-core"))
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    dependencies {
        //compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")
    }

    tasks.processResources {
        filteringCharset = "UTF-8"
        // 移除 expand() 调用，或者只对特定文件进行过滤
        // filesMatching("**/*.yml") {
        //     expand(project.properties)
        // }
    }

}