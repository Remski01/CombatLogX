java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    maven {
        name = "codemc-public"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("world.bentobox:bentobox:1.21.0-SNAPSHOT")
    compileOnly("world.bentobox:bskyblock:1.15.2-SNAPSHOT")
}