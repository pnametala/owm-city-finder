package com.gitlab.mvysny.owmcityfinder.server

import com.github.mvysny.dynatest.DynaNodeGroup

fun DynaNodeGroup.usingApp() {
    beforeGroup {
        CityListJsonCache.initCache()
        CityDatabase.index()
    }
}
