package com.example.myapplication.data.repository

import com.example.myapplication.data.api.RetrofitClient
import com.example.myapplication.data.model.Invoice
import com.example.myapplication.data.model.Order
import com.example.myapplication.data.model.Report
import com.example.myapplication.data.model.RevenuePeriod
import com.example.myapplication.data.model.common.NetworkResult
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.InventoryListQuery
import com.example.myapplication.data.model.inventory.InventorySort
import com.example.myapplication.data.repository.inventory.InventoryRepository
import com.example.myapplication.data.repository.inventory.InventoryRepositoryImpl
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportRepository(
    private val inventoryRepository: InventoryRepository = InventoryRepositoryImpl(),
    private val orderRepository: OrderRepository = OrderRepository(RetrofitClient.apiService),
    private val invoiceRepository: InvoiceRepository = InvoiceRepository(RetrofitClient.apiService)
) {

    suspend fun getInventoryReport(): NetworkResult<Report> {
        val firstPage = inventoryRepository.list(
            InventoryListQuery(sort = InventorySort.SOLD_COUNT, limit = PAGE_SIZE)
        )
        if (firstPage is NetworkResult.Error) return firstPage

        val initialPage = (firstPage as NetworkResult.Success).data
        val items = initialPage.items.toMutableList()
        val totalPages = initialPage.pagination.totalPages

        for (page in 2..totalPages) {
            when (
                val nextPage = inventoryRepository.list(
                    InventoryListQuery(page = page, limit = PAGE_SIZE, sort = InventorySort.SOLD_COUNT)
                )
            ) {
                is NetworkResult.Success -> items += nextPage.data.items
                is NetworkResult.Error -> return nextPage
            }
        }

        return when (val alerts = inventoryRepository.getAlerts()) {
            is NetworkResult.Success -> {
                val revenue = loadRevenueSummary()
                NetworkResult.Success(
                    Report(
                        dailyRevenue = revenue.daily,
                        fortnightRevenue = revenue.fortnight,
                        monthlyRevenue = revenue.monthly,
                        financialError = revenue.errorMessage,
                        totalReferences = items.size,
                        totalUnits = items.sumOf(InventoryDto::quantity),
                        inventoryCost = items.sumOfPrice { item ->
                            item.unitPrice.multiply(item.quantity.toBigDecimal())
                        },
                        potentialSales = items.sumOfPrice { item ->
                            item.salePrice.multiply(item.quantity.toBigDecimal())
                        },
                        lowStockCount = alerts.data.lowStockCount,
                        outOfStockCount = alerts.data.outOfStockCount,
                        lowStockParts = items.filter { item ->
                            item.status.contains("bajo", ignoreCase = true)
                        },
                        outOfStockParts = items.filter { item ->
                            item.status.contains("agot", ignoreCase = true)
                        },
                        topSellingParts = items
                            .sortedWith(
                                compareByDescending<InventoryDto> { it.soldCount }
                                    .thenBy { it.name.orEmpty() }
                            )
                            .take(TOP_PARTS_LIMIT)
                    )
                )
            }
            is NetworkResult.Error -> alerts
        }
    }

    private suspend fun loadRevenueSummary(): RevenueSummary {
        val ordersResult = runCatching { orderRepository.getOrders() }
        val invoicesResult = runCatching { invoiceRepository.getInvoices() }
        val orders = ordersResult.getOrDefault(emptyList())
        val invoices = invoicesResult.getOrDefault(emptyList())

        return RevenueSummary(
            daily = RevenuePeriod(
                sales = invoices.invoiceRevenueFor(RevenueRange.DAY),
                workOrders = orders.workOrderRevenueFor(RevenueRange.DAY)
            ),
            fortnight = RevenuePeriod(
                sales = invoices.invoiceRevenueFor(RevenueRange.FORTNIGHT),
                workOrders = orders.workOrderRevenueFor(RevenueRange.FORTNIGHT)
            ),
            monthly = RevenuePeriod(
                sales = invoices.invoiceRevenueFor(RevenueRange.MONTH),
                workOrders = orders.workOrderRevenueFor(RevenueRange.MONTH)
            ),
            errorMessage = listOfNotNull(
                ordersResult.exceptionOrNull()?.let { "órdenes de trabajo" },
                invoicesResult.exceptionOrNull()?.let { "ventas" }
            ).takeIf { it.isNotEmpty() }?.joinToString(
                prefix = "No fue posible actualizar "
            )
        )
    }

    private fun List<Invoice>.invoiceRevenueFor(range: RevenueRange): BigDecimal =
        asSequence()
            .filter { invoice -> !invoice.isCancelled() }
            .filter { invoice -> invoice.date.isWithin(range) }
            .fold(BigDecimal.ZERO) { total, invoice -> total + BigDecimal.valueOf(invoice.total) }

    private fun List<Order>.workOrderRevenueFor(range: RevenueRange): BigDecimal =
        asSequence()
            .filter { order -> order.isCompleted() }
            .filter { order -> order.deliveryDate.isWithin(range) }
            .fold(BigDecimal.ZERO) { total, order -> total + BigDecimal.valueOf(order.total) }

    private fun Invoice.isCancelled(): Boolean = status.lowercase(Locale.ROOT).let { normalized ->
        normalized.contains("anulad") || normalized.contains("cancel")
    }

    private fun Order.isCompleted(): Boolean = status.lowercase(Locale.ROOT).let { normalized ->
        normalized.contains("entreg") || normalized.contains("finaliz")
    }

    private fun String?.isWithin(range: RevenueRange): Boolean {
        val date = toCalendarOrNull() ?: return false
        val today = Calendar.getInstance().atStartOfDay()

        return when (range) {
            RevenueRange.DAY -> date.isSameDay(today)
            RevenueRange.FORTNIGHT -> {
                val start = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -14) }
                !date.before(start) && !date.after(today)
            }
            RevenueRange.MONTH ->
                date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    date.get(Calendar.MONTH) == today.get(Calendar.MONTH)
        }
    }

    private fun String?.toCalendarOrNull(): Calendar? {
        val dateValue = this?.substringBefore('T')?.substringBefore(' ')?.takeIf { it.isNotBlank() }
            ?: return null
        val parsed = runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(dateValue)
        }.getOrNull() ?: return null
        return Calendar.getInstance().apply {
            time = parsed
            atStartOfDay()
        }
    }

    private fun Calendar.atStartOfDay(): Calendar = apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun Calendar.isSameDay(other: Calendar): Boolean =
        get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

    private fun List<InventoryDto>.sumOfPrice(value: (InventoryDto) -> BigDecimal): BigDecimal =
        fold(BigDecimal.ZERO) { total, item -> total + value(item) }

    private data class RevenueSummary(
        val daily: RevenuePeriod,
        val fortnight: RevenuePeriod,
        val monthly: RevenuePeriod,
        val errorMessage: String?
    )

    private enum class RevenueRange {
        DAY,
        FORTNIGHT,
        MONTH
    }

    private companion object {
        const val PAGE_SIZE = 100
        const val TOP_PARTS_LIMIT = 5
    }
}
