package com.example.myapplication.profile

import com.example.myapplication.data.api.ApiService
import com.example.myapplication.data.model.ChangePasswordRequest
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.UserDto
import com.example.myapplication.data.model.common.ApiResponse
import com.example.myapplication.data.repository.LoginRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.ui.viewmodel.ProfileViewModel
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ProfileViewModelTest {

    @Test
    fun loadProfile_success_updatesUser() {
        val expected = user()
        val viewModel = viewModel(login = FakeLoginRepository(currentUser = expected))

        viewModel.loadProfile()

        assertEquals(expected, viewModel.uiState.value.user)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun loadProfile_error_exposesFriendlyMessage() {
        val viewModel = viewModel(login = FakeLoginRepository(failure = IOException()))

        viewModel.loadProfile()

        assertEquals("No hay conexión con el servidor.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun updateProfile_usesUserRepositoryAndUpdatesState() {
        val updated = User("7", "Danilo actualizado", "nuevo@sgtm.com")
        val repository = FakeUserRepository(updated)
        val viewModel = viewModel(
            login = FakeLoginRepository(currentUser = user()),
            users = repository
        )
        viewModel.loadProfile()

        viewModel.updateProfile("Danilo actualizado", "nuevo@sgtm.com")

        assertEquals(1, repository.updateCalls)
        assertEquals("Danilo actualizado", viewModel.uiState.value.user?.name)
        assertEquals("Información actualizada correctamente", viewModel.uiState.value.successMessage)
    }

    @Test
    fun changePassword_rejectsMismatchedConfirmationWithoutRequest() {
        val login = FakeLoginRepository()
        val viewModel = viewModel(login = login)

        viewModel.changePassword("actual", "nueva1234", "diferente")

        assertEquals(0, login.changePasswordCalls)
        assertEquals("La confirmación de contraseña no coincide.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun changePassword_mapsBadCurrentPassword() {
        val login = FakeLoginRepository(
            passwordFailure = HttpException(Response.error<Any>(400, "incorrect password".toResponseBody()))
        )
        val viewModel = viewModel(login = login)

        viewModel.changePassword("incorrecta", "nueva1234", "nueva1234")

        assertEquals(1, login.changePasswordCalls)
        assertEquals("La contraseña actual no es correcta.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun changePassword_success_showsConfirmation() {
        val login = FakeLoginRepository()
        val viewModel = viewModel(login = login)

        viewModel.changePassword("actual", "nueva1234", "nueva1234")

        assertTrue(viewModel.uiState.value.errorMessage == null)
        assertEquals("Contraseña actualizada correctamente", viewModel.uiState.value.successMessage)
    }

    private fun viewModel(
        login: FakeLoginRepository = FakeLoginRepository(),
        users: FakeUserRepository = FakeUserRepository()
    ) = ProfileViewModel(
        loginRepository = login,
        userRepository = users,
        testScope = CoroutineScope(Dispatchers.Unconfined)
    )

    private fun user() = UserDto(
        id = "7",
        name = "Danilo",
        email = "danilo@sgtm.com",
        role = "Administrador"
    )
}

private class FakeLoginRepository(
    private val currentUser: UserDto? = null,
    private val failure: Exception? = null,
    private val passwordFailure: Exception? = null
) : LoginRepository(noOpApiService) {
    var changePasswordCalls = 0

    override suspend fun getCurrentUser(): UserDto {
        failure?.let { throw it }
        return currentUser ?: UserDto(id = "7", name = "Danilo", email = "danilo@sgtm.com", role = "Administrador")
    }

    override suspend fun changePassword(request: ChangePasswordRequest): ApiResponse<Any?> {
        changePasswordCalls++
        passwordFailure?.let { throw it }
        return ApiResponse(success = true, data = "ok")
    }
}

private class FakeUserRepository(
    private val updated: User = User("7", "Danilo", "danilo@sgtm.com")
) : UserRepository(noOpApiService) {
    var updateCalls = 0

    override suspend fun updateUser(id: String, user: User): User {
        updateCalls++
        return updated
    }
}

private val noOpApiService: ApiService = Proxy.newProxyInstance(
    ApiService::class.java.classLoader,
    arrayOf(ApiService::class.java)
) { _, method, _ ->
    error("Llamada inesperada a ApiService: ${method.name}")
} as ApiService
