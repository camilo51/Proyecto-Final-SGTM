package com.example.myapplication.data.api

import com.example.myapplication.data.model.Appointment
import com.example.myapplication.data.model.Brand
import com.example.myapplication.data.model.Client
import com.example.myapplication.data.model.Employee
import com.example.myapplication.data.model.Inventory
import com.example.myapplication.data.model.Invoice
import com.example.myapplication.data.model.Motorcycle
import com.example.myapplication.data.model.Order
import com.example.myapplication.data.model.Reminder
import com.example.myapplication.data.model.Report
import com.example.myapplication.data.model.User
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

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
    suspend fun getClients(): List<Client>

    @GET("clients/{id}")
    suspend fun getClient(
        @Path("id") id: String
    ): Client

    @POST("clients")
    suspend fun createClient(
        @Body client: Client
    ): Client

    @PUT("clients/{id}")
    suspend fun updateClient(
        @Path("id") id: String,
        @Body client: Client
    ): Client

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
    suspend fun getInventory(): List<Inventory>

    @GET("inventory/{id}")
    suspend fun getInventoryItem(
        @Path("id") id: String
    ): Inventory

    @POST("inventory")
    suspend fun createInventoryItem(
        @Body inventory: Inventory
    ): Inventory

    @PUT("inventory/{id}")
    suspend fun updateInventoryItem(
        @Path("id") id: String,
        @Body inventory: Inventory
    ): Inventory

    @DELETE("inventory/{id}")
    suspend fun deleteInventoryItem(
        @Path("id") id: String
    )


    // =========================
    // ORDERS
    // =========================

    @GET("orders")
    suspend fun getOrders(): List<Order>

    @GET("orders/{id}")
    suspend fun getOrder(
        @Path("id") id: String
    ): Order

    @POST("orders")
    suspend fun createOrder(
        @Body order: Order
    ): Order

    @PUT("orders/{id}")
    suspend fun updateOrder(
        @Path("id") id: String,
        @Body order: Order
    ): Order

    @DELETE("orders/{id}")
    suspend fun deleteOrder(
        @Path("id") id: String
    )


    // =========================
    // INVOICES
    // =========================

    @GET("invoices")
    suspend fun getInvoices(): List<Invoice>

    @GET("invoices/{id}")
    suspend fun getInvoice(
        @Path("id") id: String
    ): Invoice

    @POST("invoices")
    suspend fun createInvoice(
        @Body invoice: Invoice
    ): Invoice

    @PUT("invoices/{id}")
    suspend fun updateInvoice(
        @Path("id") id: String,
        @Body invoice: Invoice
    ): Invoice

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


    // =========================
    // REPORTS
    // =========================

    @GET("reports")
    suspend fun getReports(): List<Report>

    @GET("reports/{id}")
    suspend fun getReport(
        @Path("id") id: String
    ): Report
}