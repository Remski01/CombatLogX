repositories {
    maven {
        name = "uskyblock-repo"
        url = uri("https://www.uskyblock.ovh/maven/uskyblock/")
    }
}

dependencies {
    compileOnly("com.github.rlf:uSkyBlock-API:2.11.0")
}
