package com.example.myapplication

import com.example.myapplication.data.model.LoginData
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginTokenModelTest {

    @Test
    fun loginDataReadsTokenAliasesReturnedByTheApi() {
        val data = Gson().fromJson(
            """{"user":{"id":"1"},"token":"jwt-token"}""",
            LoginData::class.java
        )

        assertEquals("jwt-token", data.accessToken)
    }
}
