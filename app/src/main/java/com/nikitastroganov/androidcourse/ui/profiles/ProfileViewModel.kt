package com.nikitastroganov.androidcourse.ui.profiles

import androidx.lifecycle.viewModelScope
import com.nikitastroganov.androidcourse.entity.User
import com.nikitastroganov.androidcourse.interactor.AuthInteractor
import com.nikitastroganov.androidcourse.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authInteractor: AuthInteractor
) : BaseViewModel() {

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)

    fun eventsFlow(): Flow<Event> {
        return _eventChannel.receiveAsFlow()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authInteractor.logout()
            } catch (error: Throwable) {
                Timber.e(error)
                _eventChannel.send(Event.LogoutError(error))
            }
        }
    }

    fun getUser(): User {
        // mock
        return User(
            id = 0,
            userName = "vasyapupkin228",
            firstName = "Василий",
            lastName = "Пупкин",
            avatarUri = "https://www.thiswaifudoesnotexist.net/example-55555.jpg",
            groupName = null
        )
    }

    sealed class Event {
        data class LogoutError(val error: Throwable) : Event()
    }
} 