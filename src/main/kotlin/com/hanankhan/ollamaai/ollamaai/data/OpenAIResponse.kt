package com.hanankhan.ollamaai.ollamaai.data

import org.json.JSONArray
import org.json.JSONObject

data class OpenAIResponse(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val profession: String,
    val message: String,
    val behaviours: List<String>,
    val goals: List<String>,
    val frustrations: List<String>,
    val backgroundColor: String,
) {
    constructor(jsonString: String) : this(
        firstName = JSONObject(jsonString).getString("firstname"),
        lastName = JSONObject(jsonString).getString("lastname"),
        age = JSONObject(jsonString).getInt("age"),
        profession = JSONObject(jsonString).getString("profession"),
        message = JSONObject(jsonString).getString("message"),
        behaviours = JSONObject(jsonString).getJSONArray("behaviours").toStringList(),
        goals = JSONObject(jsonString).getJSONArray("goals").toStringList(),
        frustrations = JSONObject(jsonString).getJSONArray("frustrations").toStringList(),
        backgroundColor = JSONObject(jsonString).getString("backgroundColor"),
    )

    lateinit var profileImage: String
}

fun OpenAIResponse.toJson(): JSONObject {
    return JSONObject().apply {
        put("firstname", firstName)
        put("lastname", lastName)
        put("age", age)
        put("profession", profession)
        put("message", message)
        put("behaviours", JSONArray(behaviours))
        put("goals", JSONArray(goals))
        put("frustrations", JSONArray(frustrations))
        put("backgroundColor", backgroundColor)
        put("profileImage", profileImage)
    }
}

fun JSONArray.toStringList(): List<String> =
    List(length()) { getString(it) }
