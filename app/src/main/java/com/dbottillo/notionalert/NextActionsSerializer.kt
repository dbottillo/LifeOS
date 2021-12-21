package com.dbottillo.notionalert

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object NextActionsSerializer : Serializer<NextActions> {
    override val defaultValue: NextActions = NextActions.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): NextActions {
        try {
            return NextActions.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: NextActions, output: OutputStream) = t.writeTo(output)
}