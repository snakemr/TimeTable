import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "1.1.1"
}

group = "me.demo"
version = "1.0"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TimeTable"
            packageVersion = "1.0.0"
            vendor = "Andrey Svetlichny"
            copyright = "© 2022 Andrey Svetlichny. All rights reserved."
            description = "https://www.lrmk.ru/tt"
            windows {
                iconFile.set(File("ic_launcher_lrmk.ico"))
                upgradeUuid = "a8967fe8-3542-490c-b312-106af006651e"
                menuGroup = "TimeTable"
                perUserInstall = true
            }
        }
    }
}