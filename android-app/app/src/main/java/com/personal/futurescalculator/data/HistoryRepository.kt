package com.personal.futurescalculator.data

import android.content.Context
import com.personal.futurescalculator.model.HistoryCategory
import com.personal.futurescalculator.model.HistoryField
import com.personal.futurescalculator.model.HistoryRecord
import com.personal.futurescalculator.model.HistorySection
import org.json.JSONArray
import org.json.JSONObject

class HistoryRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun load(): List<HistoryRecord> = runCatching {
        val array = JSONArray(preferences.getString(KEY_RECORDS, "[]"))
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    HistoryRecord(
                        id = item.getString("id"),
                        category = HistoryCategory.valueOf(item.getString("category")),
                        title = item.getString("title"),
                        summary = item.getString("summary"),
                        roiSummary = item.optString("roi_summary").takeIf { it.isNotBlank() },
                        savedAt = item.getLong("saved_at"),
                        favorite = item.optBoolean("favorite", false),
                        schemaVersion = item.optInt("schema_version", 1),
                        sections = parseSections(item.getJSONArray("sections")),
                        fingerprint = item.optString("fingerprint").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    fun save(records: List<HistoryRecord>) {
        val array = JSONArray()
        records.forEach { record ->
            array.put(
                JSONObject()
                    .put("id", record.id)
                    .put("category", record.category.name)
                    .put("title", record.title)
                    .put("summary", record.summary)
                    .put("roi_summary", record.roiSummary)
                    .put("saved_at", record.savedAt)
                    .put("favorite", record.favorite)
                    .put("fingerprint", record.fingerprint.orEmpty())
                    .put("schema_version", record.schemaVersion)
                    .put("sections", serializeSections(record.sections))
            )
        }
        preferences.edit().putString(KEY_RECORDS, array.toString()).apply()
    }

    private fun parseSections(array: JSONArray): List<HistorySection> = buildList {
        for (index in 0 until array.length()) {
            val section = array.getJSONObject(index)
            val fields = section.getJSONArray("fields")
            add(
                HistorySection(
                    title = section.getString("title"),
                    fields = buildList {
                        for (fieldIndex in 0 until fields.length()) {
                            val field = fields.getJSONObject(fieldIndex)
                            add(HistoryField(field.getString("label"), field.getString("value")))
                        }
                    }
                )
            )
        }
    }

    private fun serializeSections(sections: List<HistorySection>): JSONArray = JSONArray().apply {
        sections.forEach { section ->
            put(
                JSONObject()
                    .put("title", section.title)
                    .put("fields", JSONArray().apply {
                        section.fields.forEach { field ->
                            put(JSONObject().put("label", field.label).put("value", field.value))
                        }
                    })
            )
        }
    }

    private companion object {
        const val PREFERENCES = "history_records"
        const val KEY_RECORDS = "records"
    }
}
