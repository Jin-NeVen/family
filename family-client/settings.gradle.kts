import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/skyway/android-sdk")
            credentials {
                // skyway maven packageの配布はgithub経由のため、
                // 予め自分のgithub accountの秘密情報をlocal.propertiesをセットしてください
                val localProperties = Properties()
                file("local.properties").inputStream().use {
                    localProperties.load(it)
                }
                username = localProperties.getProperty("com.github.user", "")
                // github personal access tokenの発行はこちらを参考にしてください：
                // https://docs.github.com/ja/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens
                password = localProperties.getProperty("com.github.token", "")
            }
        }
    }
}

rootProject.name = "Family"
include(":app")
 