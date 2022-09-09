package net.novate.mvvmdemo.api

import kotlinx.coroutines.delay
import net.novate.mvvmdemo.data.User
import net.novate.mvvmdemo.remote.RemoteException

interface UserApi {

    suspend fun getUser(id: String): User

    companion object : UserApi by FakeUserApi()
}

private class FakeUserApi : UserApi {

    private val users: Map<String, User> = mapOf(
        "101" to User("101", "张三", 23),
        "102" to User("102", "李四", 24),
        "103" to User("103", "王五", 25),
    )

    override suspend fun getUser(id: String): User {
        delay(1000)
        return users[id] ?: throw RemoteException(404)
    }
}