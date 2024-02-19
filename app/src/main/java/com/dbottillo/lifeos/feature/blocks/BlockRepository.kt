package com.dbottillo.lifeos.feature.blocks

import com.dbottillo.lifeos.data.AppConstant
import com.dbottillo.lifeos.db.AppDatabase
import com.dbottillo.lifeos.db.BlockParagraph
import com.dbottillo.lifeos.network.ApiInterface
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class BlockRepository @Inject constructor(
    private val api: ApiInterface,
    private val db: AppDatabase
) {

    fun goalsBlock() = db.blockParagraphDao().getBlock(AppConstant.GOALS_BLOCK_ID)

    suspend fun loadGoals() {
        try {
            val response = api.queryBlock(AppConstant.GOALS_BLOCK_ID)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val paragraphs = body.results.mapIndexed { index, notionDatabaseBlock ->
                        val text = when (notionDatabaseBlock.type) {
                            "paragraph" -> notionDatabaseBlock.paragraph!!.text.first().plainText
                            "numbered_list_item" -> notionDatabaseBlock.numberedListItem!!.text.first().plainText
                            else -> throw UnsupportedOperationException("unrecognised block type")
                        }
                        BlockParagraph(
                            uid = notionDatabaseBlock.id,
                            blockId = AppConstant.GOALS_BLOCK_ID,
                            index = index,
                            type = notionDatabaseBlock.type,
                            text = text
                        )
                    }
                    db.blockParagraphDao().deleteAndInsertAll(AppConstant.GOALS_BLOCK_ID, paragraphs)
                }
            }
            val throwable = Throwable("${response.code()} ${response.message()}")
            Firebase.crashlytics.recordException(throwable)
        } catch (e: Exception) {
            val throwable = Throwable(e.message ?: e.toString())
            Firebase.crashlytics.recordException(throwable)
        }
    }
}
