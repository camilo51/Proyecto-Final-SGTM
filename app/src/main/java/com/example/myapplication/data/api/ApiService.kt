package com.example.myapplication.data.api

import com.example.myapplication.data.model.Item
import com.example.myapplication.data.model.User
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.Path
import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.UserDto
import com.google.gson.annotations.SerializedName
interface ApiService {

    // Item CRUD

    @GET("item")
    suspend fun getItems(): List<Item>

    @GET("item/{id}")
    suspend fun getItem(@Path("id") id: String): Item

    @POST("item")
    suspend fun createItem(@Body item: Item): Item

    @PUT("item/{id}")
    suspend fun updateItem(
        @Path("id") id: String,
        @Body item: Item
    ): Item

    @DELETE("item/{id}")
    suspend fun deleteItem(@Path("id") id: String)

    // User CRUD

    @GET("user")
    suspend fun getUsers(): List<User>

    @GET("user/{id}")
    suspend fun getUser(@Path("id") id: String): User

    @POST("user")
    suspend fun createUser(@Body user: User): User

    @PUT("user/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body user: User
    ): User

    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") id: String)

    // Login
    @POST("auth/login")
    suspend fun login( @Body login: LoginRequest): UserDto
}