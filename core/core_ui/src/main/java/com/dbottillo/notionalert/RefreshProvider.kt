package com.dbottillo.notionalert

interface RefreshProvider {
    fun start()
    fun stop()
    fun immediate()
}
