package com.example.myapplication.ui.viewmodel

import com.example.myapplication.data.model.Client
import java.text.Normalizer
import java.util.Locale

const val CLIENT_SEARCH_SUGGESTION_LIMIT = 8

internal fun filterClientsForQuery(
    clients: List<Client>,
    query: String
): List<Client> {
    val tokens = normalizeSearchText(query)
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)

    if (tokens.isEmpty()) return clients

    return clients.filter { client ->
        val searchableFields = listOf(
            client.name.orEmpty(),
            client.lastName.orEmpty(),
            "${client.name.orEmpty()} ${client.lastName.orEmpty()}",
            client.document.orEmpty(),
            client.phone.orEmpty(),
            client.id.orEmpty()
        ).map(::normalizeSearchText)

        tokens.all { token ->
            searchableFields.any { field -> field.contains(token) }
        }
    }
}

internal fun normalizeSearchText(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .trim()
        .lowercase(Locale.ROOT)
