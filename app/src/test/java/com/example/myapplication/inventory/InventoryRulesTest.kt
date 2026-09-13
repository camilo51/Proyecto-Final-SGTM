package com.example.myapplication.inventory

import com.example.myapplication.data.model.common.PaginatedApiResponse
import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.InventoryAlertDto
import com.example.myapplication.data.model.inventory.InventoryListQuery
import com.example.myapplication.data.model.inventory.StockMovementAction
import com.example.myapplication.data.repository.inventory.InventoryAlertRules
import com.example.myapplication.data.repository.inventory.InventoryHttpErrorPolicy
import com.example.myapplication.data.repository.inventory.InventoryQueryPolicy
import com.example.myapplication.ui.viewmodel.inventory.InventoryForm
import com.example.myapplication.ui.viewmodel.inventory.InventoryValidator
import com.example.myapplication.ui.viewmodel.inventory.StockMovementValidator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InventoryRulesTest {

    @Test
    fun inventoryDtoDeserializesMoneyAsBigDecimal() {
        val item = Gson().fromJson(
            """{"id":22,"code":"CAD-001","quantity":10,"min_stock":3,"unit_price":85000,"sale_price":120000,"status":"Disponible"}""",
            InventoryDto::class.java
        )

        assertEquals(22L, item.id)
        assertEquals(BigDecimal("85000"), item.unitPrice)
        assertEquals(BigDecimal("120000"), item.salePrice)
    }

    @Test
    fun paginatedInventoryResponseDeserializesItemsAndPagination() {
        val type = object : TypeToken<PaginatedApiResponse<InventoryDto>>() {}.type
        val response: PaginatedApiResponse<InventoryDto> = Gson().fromJson(
            """{"success":true,"message":"OK","data":[{"id":1,"quantity":0,"min_stock":5,"unit_price":0,"sale_price":0,"status":"Agotado"}],"pagination":{"total":1,"page":1,"limit":20,"totalPages":1}}""",
            type
        )

        assertTrue(response.success)
        assertEquals(1, response.data.size)
        assertEquals(1, response.pagination?.totalPages)
    }

    @Test
    fun validCodeIsAccepted() {
        val errors = InventoryValidator.validate(InventoryForm(code = "CAD-001"))
        assertFalse(errors.containsKey("code"))
    }

    @Test
    fun invalidCodeIsRejected() {
        val errors = InventoryValidator.validate(InventoryForm(code = "código inválido"))
        assertTrue(errors.containsKey("code"))
    }

    @Test
    fun salePriceCannotBeLowerThanUnitPrice() {
        val errors = InventoryValidator.validate(InventoryForm(unitPrice = "120000", salePrice = "85000"))
        assertTrue(errors.containsKey("salePrice"))
    }

    @Test
    fun negativeStockIsRejected() {
        val errors = InventoryValidator.validate(InventoryForm(quantity = "-1"))
        assertTrue(errors.containsKey("quantity"))
    }

    @Test
    fun categoryMustComeFromServerListWhenProvided() {
        val errors = InventoryValidator.validate(
            InventoryForm(category = "Inventada"),
            categories = listOf("Motor", "Frenos")
        )
        assertTrue(errors.containsKey("category"))
    }

    @Test
    fun outputCannotExceedVisibleStock() {
        val error = StockMovementValidator.validate(StockMovementAction.OUTPUT, 6, "Uso en orden", 5)
        assertTrue(error?.contains("más unidades") == true)
    }

    @Test
    fun adjustmentAllowsZeroAsAbsoluteQuantity() {
        val error = StockMovementValidator.validate(StockMovementAction.ADJUSTMENT, 0, "Conteo físico", 4)
        assertNull(error)
    }

    @Test
    fun entryRequiresPositiveQuantity() {
        assertTrue(StockMovementValidator.validate(StockMovementAction.ENTRY, 0, "Compra", 0) != null)
    }

    @Test
    fun listQueryKeepsFiltersAndNormalizesPagination() {
        val query = InventoryQueryPolicy.normalize(
            InventoryListQuery(search = " cadena ", category = " Motor ", page = 0, limit = 500)
        )

        assertEquals("cadena", query.search)
        assertEquals("Motor", query.category)
        assertEquals(1, query.page)
        assertEquals(100, query.limit)
    }

    @Test
    fun alertsAreSummarizedFromBackendStatus() {
        val summary = InventoryAlertRules.summarize(
            listOf(
                InventoryAlertDto(status = "Stock bajo"),
                InventoryAlertDto(status = "Agotado"),
                InventoryAlertDto(status = "Disponible")
            )
        )

        assertEquals(1, summary.lowStockCount)
        assertEquals(1, summary.outOfStockCount)
    }

    @Test
    fun httpConflictPreservesConflictMeaning() {
        assertTrue(InventoryHttpErrorPolicy.messageFor(409).contains("historial"))
    }

    @Test
    fun httpAuthorizationErrorsAreClassified() {
        assertTrue(InventoryHttpErrorPolicy.isUnauthorized(401))
        assertTrue(InventoryHttpErrorPolicy.messageFor(403).contains("permisos"))
        assertTrue(InventoryHttpErrorPolicy.messageFor(404).contains("no existe"))
    }

    @Test
    fun httpDeleteTreats204AsSuccess() {
        assertTrue(InventoryHttpErrorPolicy.isSuccessfulDelete(204))
    }

    @Test
    fun deleteApiContractAcceptsAdministrativeReasonBody() {
        Retrofit.Builder()
            .baseUrl("https://example.test/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .validateEagerly(true)
            .build()
            .create(ApiService::class.java)
    }

}
