package com.example.myapplication.audit

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.AuditFilters
import com.example.myapplication.data.model.AuditLog
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.common.PaginatedResult
import com.example.myapplication.data.model.common.PaginationDto
import com.example.myapplication.data.repository.AuditRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.ui.viewmodel.AuditViewModel
import java.lang.reflect.Proxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AuditViewModelTest {

    @Test
    fun loadInitialData_success_populatesLogsAndFilterOptions() {
        val auditRepository = FakeAuditRepository(
            pages = mapOf(1 to successPage(listOf(log("1"))))
        )
        val users = FakeUserRepository(listOf(user("10", "Danilo")))
        val viewModel = viewModel(auditRepository, users)

        viewModel.loadInitialData()

        assertEquals(listOf("1"), viewModel.uiState.value.auditLogs.map { it.id })
        assertEquals(1, viewModel.uiState.value.totalRecords)
        assertEquals(listOf("CREAR_ORDEN"), viewModel.uiState.value.actions)
        assertEquals(listOf("orders"), viewModel.uiState.value.tables)
        assertEquals(listOf("10"), viewModel.uiState.value.users.map { it.id })
    }

    @Test
    fun loadAuditLogs_error_showsRepositoryMessage() {
        val viewModel = viewModel(
            FakeAuditRepository(pages = mapOf(1 to NetworkResult.Error("No hay conexión con el servidor.")))
        )

        viewModel.loadInitialData()

        assertEquals("No hay conexión con el servidor.", viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.auditLogs.isEmpty())
    }

    @Test
    fun applyFilters_sendsSearchUserActionAndModuleToRepository() {
        val repository = FakeAuditRepository(pages = mapOf(1 to successPage(emptyList())))
        val viewModel = viewModel(repository)
        viewModel.loadInitialData()

        viewModel.onSearchQueryChange("danilo")
        viewModel.onUserFilterChange("10")
        viewModel.onActionFilterChange("CREAR_ORDEN")
        viewModel.onTableFilterChange("orders")
        viewModel.applyFilters()

        val filters = repository.requests.last().filters
        assertEquals("danilo", filters.search)
        assertEquals("10", filters.userId)
        assertEquals("CREAR_ORDEN", filters.action)
        assertEquals("orders", filters.tableName)
    }

    @Test
    fun invalidDateRange_isRejectedWithoutRequest() {
        val repository = FakeAuditRepository(pages = mapOf(1 to successPage(emptyList())))
        val viewModel = viewModel(repository)
        viewModel.loadInitialData()
        val callsBefore = repository.requests.size

        viewModel.onFromDateChange("2026-09-20")
        viewModel.onToDateChange("2026-09-18")

        assertFalse(viewModel.applyFilters())
        assertEquals(callsBefore, repository.requests.size)
        assertEquals("La fecha Desde no puede ser posterior a Hasta.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun clearFilters_resetsValuesAndLoadsFirstPage() {
        val repository = FakeAuditRepository(pages = mapOf(1 to successPage(emptyList())))
        val viewModel = viewModel(repository)
        viewModel.loadInitialData()
        viewModel.onSearchQueryChange("orden")
        viewModel.onFromDateChange("2026-09-01")
        viewModel.onToDateChange("2026-09-18")
        viewModel.onUserFilterChange("10")
        viewModel.onActionFilterChange("CREAR_ORDEN")
        viewModel.onTableFilterChange("orders")

        viewModel.clearFilters()

        val state = viewModel.uiState.value
        assertFalse(state.hasActiveFilters)
        assertEquals(AuditFilters(), repository.requests.last().filters)
        assertEquals(1, repository.requests.last().page)
    }

    @Test
    fun loadNextPage_appendsRecordsWhenMorePagesExist() {
        val repository = FakeAuditRepository(
            pages = mapOf(
                1 to successPage(listOf(log("1")), page = 1, totalPages = 2, total = 2),
                2 to successPage(listOf(log("2")), page = 2, totalPages = 2, total = 2)
            )
        )
        val viewModel = viewModel(repository)

        viewModel.loadInitialData()
        viewModel.loadNextPage()

        assertEquals(listOf("1", "2"), viewModel.uiState.value.auditLogs.map { it.id })
        assertFalse(viewModel.uiState.value.hasMore)
    }

    @Test
    fun repository_mapsUnauthorizedAndForbiddenResponses() = runBlocking {
        val unauthorized = AuditRepository(api(401)).getAuditLogs(AuditFilters(), page = 1)
        val forbidden = AuditRepository(api(403)).getAuditLogs(AuditFilters(), page = 1)

        assertEquals("Tu sesión expiró. Inicia sesión nuevamente.", (unauthorized as NetworkResult.Error).message)
        assertEquals("No tienes permisos para consultar la auditoría.", (forbidden as NetworkResult.Error).message)
    }

    private fun viewModel(
        auditRepository: FakeAuditRepository = FakeAuditRepository(),
        userRepository: FakeUserRepository = FakeUserRepository()
    ) = AuditViewModel(
        auditRepository = auditRepository,
        userRepository = userRepository,
        testScope = CoroutineScope(Dispatchers.Unconfined)
    )

    private fun successPage(
        logs: List<AuditLog>,
        page: Int = 1,
        totalPages: Int = 1,
        total: Int = logs.size
    ) = NetworkResult.Success(
        PaginatedResult(logs, PaginationDto(total = total, page = page, limit = 20, totalPages = totalPages))
    )

    private fun log(id: String) = AuditLog(
        id = id,
        userId = "10",
        userName = "Danilo",
        action = "CREAR_ORDEN",
        tableName = "orders",
        createdAt = "2026-09-18 10:00:00"
    )

    private fun user(id: String, name: String) = User(id = id, name = name, email = "$name@sgtm.com")
}

private class FakeAuditRepository(
    private val pages: Map<Int, NetworkResult<PaginatedResult<AuditLog>>> = mapOf(
        1 to NetworkResult.Success(PaginatedResult(emptyList<AuditLog>(), PaginationDto()))
    ),
    private val actions: NetworkResult<List<String>> = NetworkResult.Success(listOf("CREAR_ORDEN")),
    private val tables: NetworkResult<List<String>> = NetworkResult.Success(listOf("orders"))
) : AuditRepository(noOpApiService) {
    data class Request(val filters: AuditFilters, val page: Int)

    val requests = mutableListOf<Request>()

    override suspend fun getAuditLogs(
        filters: AuditFilters,
        page: Int,
        limit: Int
    ): NetworkResult<PaginatedResult<AuditLog>> {
        requests += Request(filters, page)
        return pages[page] ?: NetworkResult.Success(
            PaginatedResult(emptyList<AuditLog>(), PaginationDto(page = page))
        )
    }

    override suspend fun getActions(): NetworkResult<List<String>> = actions

    override suspend fun getTables(): NetworkResult<List<String>> = tables
}

private class FakeUserRepository(
    private val users: List<User> = emptyList()
) : UserRepository(noOpApiService) {
    override suspend fun getUsers(): List<User> = users
}

private fun api(code: Int): ApiService = Proxy.newProxyInstance(
    ApiService::class.java.classLoader,
    arrayOf(ApiService::class.java)
) { _, method, _ ->
    if (method.name == "getAuditLogs") {
        Response.error<Any>(code, "{\"message\":\"backend\"}".toResponseBody())
    } else {
        error("Llamada inesperada a ApiService: ${method.name}")
    }
} as ApiService

private val noOpApiService: ApiService = Proxy.newProxyInstance(
    ApiService::class.java.classLoader,
    arrayOf(ApiService::class.java)
) { _, method, _ ->
    error("Llamada inesperada a ApiService: ${method.name}")
} as ApiService
