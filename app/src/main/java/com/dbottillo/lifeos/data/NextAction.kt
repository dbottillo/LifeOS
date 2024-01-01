package com.dbottillo.lifeos.data

data class NextActions(val actions: List<NextAction>)

data class NextAction(val text: String, val color: String, val url: String)
