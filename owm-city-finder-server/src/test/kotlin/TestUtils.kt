package com.gitlab.mvysny.owmcityfinder.server

import com.github.mvysny.dynatest.DynaNodeGroup

fun DynaNodeGroup.usingApp() {
    beforeGroup {
        if (!CityDatabase.exists()) {
            CityListJsonCache.initCache()
            CityDatabase.index()
        }
    }
}
