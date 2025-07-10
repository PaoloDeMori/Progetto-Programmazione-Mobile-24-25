package com.example.appranzo.ui.navigation

object Routes {
    const val LOGIN        = "login"
    const val REGISTER     = "register"
    const val MAIN         = "main"


    const val TAB_HOME      = "tab_home"
    const val TAB_FAVORITES = "tab_favorites"
    const val TAB_MAP       = "tab_map"
    const val MAP = "map/{lat}/{lng}"

    const val TAB_FRIENDS   = "tab_friends"
    const val TAB_BADGES    = "tab_badges"

    const val PROFILE  = "profile"
    const val PROFILE_DETAILS  = "profile_details"
    const val PROFILE_REVIEWS = "profile_reviews"


    const val SETTINGS = "settings"
    const val SETTINGS_THEME = "settings_theme"

    const val SEARCH = "search"
    const val SEARCH_RESULT = "search_result"

    const val  REVIEW = "review"
    const val REVIEW_WITH_ARG = "review/{restaurantId}"

    const val  REVIEW_DETAIL = "review_detail"
    const val REVIEW_DETAIL_WITH_ARG = "review_detail/{restaurantId}/{reviewId}"

}
