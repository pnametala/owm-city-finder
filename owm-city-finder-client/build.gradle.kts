dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("com.squareup.okhttp3:okhttp:${properties["okhttp_version"]}")
    compile("com.google.code.gson:gson:${properties["gson_version"]}")
    compile("org.slf4j:slf4j-api:${properties["slf4j_version"]}")
    testCompile("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("owm-city-finder-client")
