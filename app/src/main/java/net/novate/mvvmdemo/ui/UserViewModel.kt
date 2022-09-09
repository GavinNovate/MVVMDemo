package net.novate.mvvmdemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.novate.mvvmdemo.api.UserApi
import net.novate.mvvmdemo.data.User
import net.novate.mvvmdemo.remote.RemoteResult
import net.novate.mvvmdemo.remote.loading

class UserViewModel : ViewModel() {

    private val userApi: UserApi = UserApi

    private val _user: MutableStateFlow<RemoteResult<User>> = MutableStateFlow(RemoteResult.loading())

    val user: StateFlow<RemoteResult<User>> = _user

    /**
     * 登录
     */
    fun login(id: String) {
        viewModelScope.launch {
            loading {
                userApi.getUser(id)
            }.collect {
                _user.value = it
            }
        }
    }
}