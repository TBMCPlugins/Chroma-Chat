package buttondevteam.chat.components.formatter.formatting

import buttondevteam.lib.chat.TellrawSerializableEnum

abstract class TellrawSerializer {
    class TwEnum : com.google.gson.TypeAdapter<TellrawSerializableEnum?>() {
        @kotlin.Throws(java.io.IOException::class)
        override fun read(reader: com.google.gson.stream.JsonReader?): TellrawSerializableEnum? {
            throw java.lang.UnsupportedOperationException()
        }

        @kotlin.Throws(java.io.IOException::class)
        override fun write(writer: com.google.gson.stream.JsonWriter, enumval: TellrawSerializableEnum?) {
            if (enumval == null) writer.nullValue()
            else writer.value(enumval.getName())
        }
    }

    class TwCollection : com.google.gson.JsonSerializer<kotlin.collections.MutableCollection<*>?> {
        override fun serialize(
            src: kotlin.collections.MutableCollection<*>?,
            typeOfSrc: java.lang.reflect.Type?,
            context: com.google.gson.JsonSerializationContext
        ): com.google.gson.JsonElement? {
            if (src == null || src.isEmpty()) return null

            val array = com.google.gson.JsonArray()

            for (child in src) {
                val element = context.serialize(child)
                array.add(element)
            }

            return array
        }
    }

    class TwBool : com.google.gson.TypeAdapter<kotlin.Boolean?>() {
        @kotlin.Throws(java.io.IOException::class)
        override fun read(reader: com.google.gson.stream.JsonReader?): kotlin.Boolean? {
            throw java.lang.UnsupportedOperationException()
        }

        @kotlin.Throws(java.io.IOException::class)
        override fun write(writer: com.google.gson.stream.JsonWriter, `val`: kotlin.Boolean) {
            if (`val`) writer.value(`val`)
            else writer.nullValue()
        }
    }
}
