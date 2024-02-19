package com.dbottillo.lifeos.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BlockParagraph(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "block_id") val blockId: String,
    @ColumnInfo(name = "index") val index: Int,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "text") val text: String
)
