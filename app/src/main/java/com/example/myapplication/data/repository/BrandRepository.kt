package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.Brand

open class BrandRepository(
    private val apiService: ApiService
) {

    open suspend fun getBrands(): List<Brand> {
        return apiService.getBrands()
    }

    open suspend fun createBrand(brand: Brand): Brand {
        return apiService.createBrand(brand)
    }

    open suspend fun updateBrand(id: String, brand: Brand): Brand {
        return apiService.updateBrand(id, brand)
    }

    open suspend fun deleteBrand(id: String) {
        apiService.deleteBrand(id)
    }
}
