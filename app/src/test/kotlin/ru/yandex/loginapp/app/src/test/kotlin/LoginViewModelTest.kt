package ru.yandex.loginapp.app.src.test.kotlin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.yandex.loginapp.LoginScreenState
import ru.yandex.loginapp.LoginViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Default`() {
        assertEquals(LoginScreenState.Default, viewModel.state.value)
    }

    @Test
    fun `login with empty fields should emit EmptyFieldsError`() = runTest {
        viewModel.login("", "")
        advanceUntilIdle()

        assertEquals(LoginScreenState.EmptyFieldsError, viewModel.state.value)
    }

    @Test
    fun `login with invalid email should emit EmailValidationError`() = runTest {
        viewModel.login("invalid-email", "password123")
        advanceUntilIdle()

        assertEquals(LoginScreenState.EmailValidationError, viewModel.state.value)
    }

    @Test
    fun `login should emit states in correct order`() = runTest {
        val states = mutableListOf<LoginScreenState>()
        val job = launch {
            viewModel.login("valid@email.com", "password123")

            viewModel.state.collect { states.add(it) }
        }

        advanceUntilIdle()
        job.cancel()

        assertEquals(
            listOf(
                LoginScreenState.Default,
                LoginScreenState.Loading,
                LoginScreenState.Success
            ),
            states
        )
    }

    @Test
    fun `isEmailValid should return true for valid emails`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name+tag@domain.co",
            "first.last@sub.domain.com"
        )

        validEmails.forEach { email ->
            assertTrue(viewModel.isEmailValid(email))
        }
    }

    @Test
    fun `isEmailValid should return false for invalid emails`() {
        val invalidEmails = listOf(
            "plainstring",
            "@no-username.com",
            "spaces in@domain.com"
        )

        invalidEmails.forEach { email ->
            assertFalse(viewModel.isEmailValid(email))
        }
    }
}