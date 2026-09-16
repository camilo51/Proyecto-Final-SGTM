package com.example.myapplication.data.api

import com.example.myapplication.data.model.Appointment
import com.example.myapplication.data.model.Brand
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Employee
import com.example.myapplication.data.model.Invoice
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.Order
import com.example.myapplication.data.model.Reminder
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.ForgotPasswordRequest
import com.example.myapplication.data.model.ForgotPasswordResponse
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.model.LogoutResponse
import com.example.myapplication.data.model.RegisterRequest
import com.example.myapplication.data.model.RegisterResponse
import com.example.myapplication.data.model.ResetPasswordRequest
import com.example.myapplication.data.model.ResetPasswordResponse
import com.example.myapplication.data.model.common.ApiResponse
import com.example.myapplication.data.model.common.PaginatedApiResponse
import com.example.myapplication.data.model.inventory.CreateInventoryRequest
import com.example.myapplication.data.model.inventory.DeleteInventoryRequest
import com.example.myapplication.data.model.inventory.InventoryAlertDto
import com.example.myapplication.data.model.inventory.InventoryDto
import com.example.myapplication.data.model.inventory.InventoryMovementDto
import com.example.myapplication.data.model.inventory.StockAdjustmentRequest
import com.example.myapplication.data.model.inventory.StockEntryRequest
import com.example.myapplication.data.model.inventory.StockMovementResultDto
import com.example.myapplication.data.model.inventory.StockOutputRequest
import com.example.myapplication.data.model.inventory.UpdateInventoryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // =========================
    // AUTHENTICATION
    // =========================

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String?
    ): LogoutResponse

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): ForgotPasswordResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): ResetPasswordResponse


    // =========================
    // USERS
    // =========================

    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("users/{id}")
    suspend fun getUser(
        @Path("id") id: String
    ): User

    @POST("users")
    suspend fun createUser(
        @Body user: User
    ): User

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body user: User
    ): User

    @DELETE("users/{id}")
    suspend fun deleteUser(
        @Path("id") id: String
    )


    // =========================
    // CLIENTS
    // =========================

    @GET("clients")
    suspend fun getClients(): ApiResponse<List<Client>>

    @GET("clients/{id}")
    suspend fun getClient(
        @Path("id") id: String
    ): ApiResponse<Client>

    @POST("clients")
    suspend fun createClient(
        @Body client: Client
    ): ApiResponse<Client>

    @PUT("clients/{id}")
    suspend fun updateClient(
        @Path("id") id: String,
        @Body client: Client
    ): ApiResponse<Client>

    @DELETE("clients/{id}")
    suspend fun deleteClient(
        @Path("id") id: String
    )


    // =========================
    // EMPLOYEES
    // =========================

    @GET("employees")
    suspend fun getEmployees(): List<Employee>

    @GET("employees/{id}")
    suspend fun getEmployee(
        @Path("id") id: String
    ): Employee

    @POST("employees")
    suspend fun createEmployee(
        @Body employee: Employee
    ): Employee

    @PUT("employees/{id}")
    suspend fun updateEmployee(
        @Path("id") id: String,
        @Body employee: Employee
    ): Employee

    @DELETE("employees/{id}")
    suspend fun deleteEmployee(
        @Path("id") id: String
    )


    // =========================
    // APPOINTMENTS
    // =========================

    @GET("appointments")
    suspend fun getAppointments(): List<Appointment>

    @GET("appointments/{id}")
    suspend fun getAppointment(
        @Path("id") id: String
    ): Appointment

    @POST("appointments")
    suspend fun createAppointment(
        @Body appointment: Appointment
    ): Appointment

    @PUT("appointments/{id}")
    suspend fun updateAppointment(
        @Path("id") id: String,
        @Body appointment: Appointment
    ): Appointment

    @DELETE("appointments/{id}")
    suspend fun deleteAppointment(
        @Path("id") id: String
    )


    // =========================
    // MOTORCYCLES
    // =========================

    @GET("motorcycles")
    suspend fun getMotorcycles(): List<Motorcycle>

    @GET("motorcycles/{id}")
    suspend fun getMotorcycle(
        @Path("id") id: String
    ): Motorcycle

    @POST("motorcycles")
    suspend fun createMotorcycle(
        @Body motorcycle: Motorcycle
    ): Motorcycle

    @PUT("motorcycles/{id}")
    suspend fun updateMotorcycle(
        @Path("id") id: String,
        @Body motorcycle: Motorcycle
    ): Motorcycle

    @DELETE("motorcycles/{id}")
    suspend fun deleteMotorcycle(
        @Path("id") id: String
    )


    // =========================
    // BRANDS
    // =========================

    @GET("brands")
    suspend fun getBrands(): List<Brand>

    @POST("brands")
    suspend fun createBrand(
        @Body brand: Brand
    ): Brand

    @PUT("brands/{id}")
    suspend fun updateBrand(
        @Path("id") id: String,
        @Body brand: Brand
    ): Brand

    @DELETE("brands/{id}")
    suspend fun deleteBrand(
        @Path("id") id: String
    )


    // =========================
    // INVENTORY
    // =========================

    @GET("inventory")
    suspend fun getInventoryPage(
        @Query("search") search: String?,
        @Query("category") category: String?,
        @Query("brand") brand: String?,
        @Query("status") status: String?,
        @Query("sort") sort: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<PaginatedApiResponse<InventoryDto>>

    @GET("inventory/categories")
    suspend fun getInventoryCategories(): Response<ApiResponse<List<String>>>

    @GET("inventory/brands")
    suspend fun getInventoryBrands(): Response<ApiResponse<List<String>>>

    @GET("inventory/alerts")
    suspend fun getInventoryAlerts(
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<InventoryAlertDto>>>

    @GET("inventory/{id}")
    suspend fun getInventoryDetail(
        @Path("id") id: Long
    ): Response<ApiResponse<InventoryDto>>

    @GET("inventory/{id}/movements")
    suspend fun getInventoryMovements(
        @Path("id") id: Long,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<PaginatedApiResponse<InventoryMovementDto>>

    @POST("inventory")
    suspend fun createInventory(
        @Body request: CreateInventoryRequest
    ): Response<ApiResponse<InventoryDto>>

    @PUT("inventory/{id}")
    suspend fun updateInventory(
        @Path("id") id: Long,
        @Body request: UpdateInventoryRequest
    ): Response<ApiResponse<InventoryDto>>

    @HTTP(method = "DELETE", path = "inventory/{id}", hasBody = true)
    suspend fun deleteInventory(
        @Path("id") id: Long,
        @Body request: DeleteInventoryRequest
    ): Response<Unit>

    @POST("inventory/{id}/entry")
    suspend fun registerInventoryEntry(
        @Path("id") id: Long,
        @Body request: StockEntryRequest
    ): Response<ApiResponse<StockMovementResultDto>>

    @POST("inventory/{id}/output")
    suspend fun registerInventoryOutput(
        @Path("id") id: Long,
        @Body request: StockOutputRequest
    ): Response<ApiResponse<StockMovementResultDto>>

    @POST("inventory/{id}/adjustment")
    suspend fun registerInventoryAdjustment(
        @Path("id") id: Long,
        @Body request: StockAdjustmentRequest
    ): Response<ApiResponse<StockMovementResultDto>>


    // =========================
    // ORDERS
    // =========================

    @GET("orders")
    suspend fun getOrders(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): PaginatedApiResponse<Order>

    @GET("orders/{id}")
    suspend fun getOrder(
        @Path("id") id: String
    ): ApiResponse<Order>

    @POST("orders")
    suspend fun createOrder(
        @Body order: Order
    ): ApiResponse<Order>

    @PUT("orders/{id}")
    suspend fun updateOrder(
        @Path("id") id: String,
        @Body order: Order
    ): ApiResponse<Order>

    @DELETE("orders/{id}")
    suspend fun deleteOrder(
        @Path("id") id: String
    )


    // =========================
    // INVOICES
    // =========================

    @GET("invoices")
    suspend fun getInvoices(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): PaginatedApiResponse<Invoice>

    @GET("invoices/{id}")
    suspend fun getInvoice(
        @Path("id") id: String
    ): ApiResponse<Invoice>

    @POST("invoices")
    suspend fun createInvoice(
        @Body invoice: Invoice
    ): ApiResponse<Invoice>

    @PUT("invoices/{id}")
    suspend fun updateInvoice(
        @Path("id") id: String,
        @Body invoice: Invoice
    ): ApiResponse<Invoice>

    @DELETE("invoices/{id}")
    suspend fun deleteInvoice(
        @Path("id") id: String
    )


    // =========================
    // REMINDERS
    // =========================

    @GET("reminders")
    suspend fun getReminders(): List<Reminder>

    @GET("reminders/{id}")
    suspend fun getReminder(
        @Path("id") id: String
    ): Reminder

    @POST("reminders")
    suspend fun createReminder(
        @Body reminder: Reminder
    ): Reminder

    @PUT("reminders/{id}")
    suspend fun updateReminder(
        @Path("id") id: String,
        @Body reminder: Reminder
    ): Reminder

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(
        @Path("id") id: String
    )

}
