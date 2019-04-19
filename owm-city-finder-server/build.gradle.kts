dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("io.javalin:javalin:${properties["javalin_version"]}")
    compile("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
    testCompile("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("owm-city-finder-server")
