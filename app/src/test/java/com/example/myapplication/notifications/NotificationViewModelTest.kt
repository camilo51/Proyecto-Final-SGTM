package com.example.myapplication.notifications

import com.example.myapplication.data.model.AuditFilters
import com.example.myapplication.data.model.AuditLog
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.common.PaginatedResult
import com.example.myapplication.data.model.common.PaginationDto
import com.example.myapplication.data.repository.AuditRepository
import com.example.myapplication.ui.viewmodel.NotificationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {

    @Test
    fun loadNotifications_onlyIncludesOperationalEvents() = runTest {
        val repository = FakeNotificationsAuditRepository(
            NetworkResult.Success(
                PaginatedResult(
                    items = listOf(
                        AuditLog(
                            id = "1",
                            action = "CAMBIAR_ESTADO",
                            tableName = "orders",
                            recordId = "15",
                            description = "OT-20260916-002 → Lista para entrega",
                            createdAt = "2026-09-18T15:59:00.000Z"
                        ),
                        AuditLog(
                            id = "2",
                            action = "LOGIN_EXITOSO",
                            description = "Inicio de sesión",
                            createdAt = "2026-09-18T15:58:00.000Z"
                        )
                    ),
                    pagination = PaginationDto(total = 1, page = 1, limit = 50, totalPages = 1)
                )
            )
        )
        val viewModel = NotificationViewModel(
            auditRepository = repository,
            testScope = this,
            inventoryRepository = null
        )

        viewModel.loadNotifications()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.notifications.size)
        assertEquals("Estado actualizado", viewModel.uiState.value.notifications.single().title)
        assertEquals(1, repository.requests)
    }

    @Test
    fun loadNotifications_unauthorizedShowsSessionMessage() = runTest {
        val viewModel = NotificationViewModel(
            auditRepository = FakeNotificationsAuditRepository(NetworkResult.Error("No autorizado", code = 401)),
            testScope = this,
            inventoryRepository = null
        )

        viewModel.loadNotifications()
        advanceUntilIdle()

        assertEquals("Tu sesión expiró. Inicia sesión nuevamente.", viewModel.uiState.value.errorMessage)
    }
}

private class FakeNotificationsAuditRepository(
    private val result: NetworkResult<PaginatedResult<AuditLog>>
) : AuditRepository() {
    var requests = 0

    override suspend fun getAuditLogs(
        filters: AuditFilters,
        page: Int,
        limit: Int
    ): NetworkResult<PaginatedResult<AuditLog>> {
        requests += 1
        return result
    }
}
