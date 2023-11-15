package com.dbottillo.notionalert.network

interface RefreshProvider {
    fun start()
    fun stop()
    fun immediate()
}
