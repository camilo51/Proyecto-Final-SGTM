package com.example.myapplication.inventory

import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.common.PaginatedResult
import com.example.myapplication.data.model.common.PaginationDto
import com.example.myapplication.data.model.inventory.CreateInventoryRequest
import com.example.myapplication.data.model.inventory.DeleteInventoryRequest
import com.example.myapplication.data.model.inventory.InventoryAlertSummary
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.InventoryListQuery
import com.example.myapplication.data.model.inventory.InventoryMovementsPage
import com.example.myapplication.data.model.inventory.InventoryPartReference
import com.example.myapplication.data.model.inventory.StockAdjustmentRequest
import com.example.myapplication.data.model.inventory.StockEntryRequest
import com.example.myapplication.data.model.inventory.StockMovementResultDto
import com.example.myapplication.data.model.inventory.StockOutputRequest
import com.example.myapplication.data.model.inventory.UpdateInventoryRequest
import com.example.myapplication.data.repository.inventory.InventoryRepository
import com.example.myapplication.ui.viewmodel.inventory.InventoryFormViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryFormViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createAvoidsDoubleSubmissionAndUsesBackendResult() = runTest {
        val repository = FakeInventoryRepository()
        val viewModel = InventoryFormViewModel(repository)
        advanceUntilIdle()
        viewModel.updateForm("code") { it.copy(code = "CAD-001") }
        viewModel.updateForm("category") { it.copy(category = "Motor") }
        viewModel.updateForm("unitPrice") { it.copy(unitPrice = "85000") }
        viewModel.updateForm("salePrice") { it.copy(salePrice = "120000") }

        viewModel.create()
        viewModel.create()
        advanceUntilIdle()

        assertEquals(1, repository.createCalls)
        assertNotNull(viewModel.uiState.value.savedItem)
    }

    @Test
    fun createDoesNotCallRepositoryWhenLocalValidationFails() = runTest {
        val repository = FakeInventoryRepository()
        val viewModel = InventoryFormViewModel(repository)
        advanceUntilIdle()
        viewModel.updateForm("unitPrice") { it.copy(unitPrice = "100", salePrice = "99") }

        viewModel.create()

        assertEquals(0, repository.createCalls)
        assertNotNull(viewModel.uiState.value.fieldErrors["salePrice"])
    }

    @Test
    fun updateUsesRepositoryAfterLoadingExistingItem() = runTest {
        val repository = FakeInventoryRepository()
        val viewModel = InventoryFormViewModel(repository)
        advanceUntilIdle()
        viewModel.loadForEdit(1)
        advanceUntilIdle()

        viewModel.update(1)
        advanceUntilIdle()

        assertEquals(1, repository.updateCalls)
        assertNotNull(viewModel.uiState.value.savedItem)
    }
}

private class FakeInventoryRepository : InventoryRepository {
    var createCalls = 0
    var updateCalls = 0
    private val item = InventoryDto(
        id = 1,
        code = "CAD-001",
        category = "Motor",
        quantity = 0,
        minStock = 5,
        unitPrice = BigDecimal.ZERO,
        salePrice = BigDecimal.ZERO,
        status = "Agotado"
    )

    override suspend fun list(query: InventoryListQuery): NetworkResult<PaginatedResult<InventoryDto>> = NetworkResult.Success(
        PaginatedResult<InventoryDto>(emptyList(), PaginationDto())
    )

    override suspend fun getCategories(): NetworkResult<List<String>> = NetworkResult.Success(listOf("Motor"))
    override suspend fun getBrands(): NetworkResult<List<String>> = NetworkResult.Success(emptyList<String>())
    override suspend fun getAlerts(status: String?): NetworkResult<InventoryAlertSummary> = NetworkResult.Success(InventoryAlertSummary())
    override suspend fun getDetail(id: Long): NetworkResult<InventoryDto> = NetworkResult.Success(item)
    override suspend fun getMovements(id: Long, page: Int, limit: Int): NetworkResult<InventoryMovementsPage> = NetworkResult.Success(
        InventoryMovementsPage(emptyList(), PaginationDto())
    )

    override suspend fun create(request: CreateInventoryRequest): NetworkResult<InventoryDto> {
        createCalls++
        return NetworkResult.Success(item.copy(code = request.code))
    }

    override suspend fun update(id: Long, request: UpdateInventoryRequest): NetworkResult<InventoryDto> {
        updateCalls++
        return NetworkResult.Success(item)
    }
    override suspend fun registerEntry(id: Long, request: StockEntryRequest): NetworkResult<StockMovementResultDto> = NetworkResult.Success(StockMovementResultDto())
    override suspend fun registerOutput(id: Long, request: StockOutputRequest): NetworkResult<StockMovementResultDto> = NetworkResult.Success(StockMovementResultDto())
    override suspend fun registerAdjustment(id: Long, request: StockAdjustmentRequest): NetworkResult<StockMovementResultDto> = NetworkResult.Success(StockMovementResultDto())
    override suspend fun delete(id: Long, request: DeleteInventoryRequest): NetworkResult<Unit> = NetworkResult.Success(Unit)
    override suspend fun searchAvailableParts(search: String, page: Int, limit: Int): PaginatedResult<InventoryPartReference> = PaginatedResult(emptyList(), PaginationDto())
}
