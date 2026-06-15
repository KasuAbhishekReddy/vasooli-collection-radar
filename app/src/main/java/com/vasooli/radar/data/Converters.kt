package com.vasooli.radar.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun toType(v: String): EntryType = EntryType.valueOf(v)
    @TypeConverter fun fromType(t: EntryType): String = t.name
}
