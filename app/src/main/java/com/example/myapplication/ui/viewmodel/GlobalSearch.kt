package com.example.myapplication.ui.viewmodel

enum class GlobalSearchModule(val label: String) {
    CLIENTS("Clientes"),
    MOTORCYCLES("Motocicletas"),
    ORDERS("Órdenes"),
    INVENTORY("Inventario"),
    INVOICES("Facturación"),
    EMPLOYEES("Empleados"),
    USERS("Usuarios"),
    REPORTS("Reportes"),
    AUDIT("Auditoría")
}

data class GlobalSearchResult(
    val module: GlobalSearchModule,
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val searchableText: String = listOf(id, title, subtitle.orEmpty()).joinToString(" ")
)

internal fun filterGlobalSearchResults(
    results: List<GlobalSearchResult>,
    query: String,
    limit: Int = 12
): List<GlobalSearchResult> {
    val tokens = normalizeSearchText(query)
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)

    if (tokens.isEmpty()) return emptyList()

    return results.filter { result ->
        val searchableText = normalizeSearchText(
            "${result.module.label} ${result.searchableText}"
        )
        tokens.all(searchableText::contains)
    }.take(limit)
}
