package net.novate.mvvmdemo.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 封装远程调用的结果
 */
sealed interface RemoteResult<out T> {

    /**
     * Loading 状态
     *
     * @param value 上次的成功数据
     */
    data class Loading<out T>(val value: T? = null) : RemoteResult<T>

    /**
     * Success 状态
     *
     * @param value 成功数据
     */
    data class Success<out T>(val value: T) : RemoteResult<T>

    /**
     * Failure 状态
     *
     * @param error 失败异常
     */
    data class Failure<out T>(val error: Throwable) : RemoteResult<T>

    companion object {

        fun <T> loading(value: T? = null): RemoteResult<T> = Loading(value)

        fun <T> success(value: T): RemoteResult<T> = Success(value)

        fun <T> failure(error: Throwable): RemoteResult<T> = Failure(error)
    }
}

/**
 * 是否为 Loading 状态
 */
val RemoteResult<*>.isLoading: Boolean
    get() = this is RemoteResult.Loading

/**
 * 是否为 Success 状态
 */
val RemoteResult<*>.isSuccess: Boolean
    get() = this is RemoteResult.Success

/**
 * 是否为 Failure 状态
 */
val RemoteResult<*>.isFailure: Boolean
    get() = this is RemoteResult.Failure

/**
 * 获取数据；Success 状态返回成功数据，Loading 状态返回可能存在的上次的成功数据，Failure 状态返回 `null`
 */
fun <T> RemoteResult<T>.getOrNull(): T? = when (this) {
    is RemoteResult.Loading -> value
    is RemoteResult.Success -> value
    else -> null
}

/**
 * 获取异常；Failure 状态返回异常信息，其他状态返回 `null`
 */
fun <T> RemoteResult<T>.errorOrNull(): Throwable? = when (this) {
    is RemoteResult.Failure -> error
    else -> null
}

/**
 * 获取数据；Success 状态返回成功数据，Loading 状态会调用 [onLoading] 获取数据，Failure 状态会调用 [onFailure] 获取数据
 */
inline fun <T : R, R> RemoteResult<T>.getOrElse(
    onLoading: (value: T?) -> R,
    onFailure: (error: Throwable) -> R
): R = when (this) {
    is RemoteResult.Loading -> onLoading(value)
    is RemoteResult.Success -> value
    is RemoteResult.Failure -> onFailure(error)
}

/**
 * 获取数据，若为 `null` 则返回 [defaultValue]
 */
fun <T : R, R> RemoteResult<T>.getOrDefault(defaultValue: R): R = getOrNull() ?: defaultValue

/**
 * 收拢 `RemoteResult`，分别处理 [onLoading], [onSuccess], [onFailure] 三种情况，将数据转换为 [R] 类型的数据
 */
inline fun <T, R> RemoteResult<T>.fold(
    onLoading: (value: T?) -> R,
    onSuccess: (value: T) -> R,
    onFailure: (error: Throwable) -> R
): R = when (this) {
    is RemoteResult.Loading -> onLoading(value)
    is RemoteResult.Success -> onSuccess(value)
    is RemoteResult.Failure -> onFailure(error)
}

/**
 * 转换 [T] 类型的 [RemoteResult] 为 [R] 类型的 [RemoteResult]
 */
inline fun <T, R> RemoteResult<T>.map(transform: (value: T) -> R): RemoteResult<R> = when (this) {
    is RemoteResult.Loading -> RemoteResult.Loading(value?.let(transform))
    is RemoteResult.Success -> RemoteResult.Success(transform(value))
    is RemoteResult.Failure -> RemoteResult.Failure(error)
}

/**
 * 若为 Loading 状态则执行 [action]
 */
inline fun <T> RemoteResult<T>.onLoading(action: (value: T?) -> Unit): RemoteResult<T> = apply {
    if (this is RemoteResult.Loading) action(value)
}

/**
 * 若为 Success 状态则执行 [action]
 */
inline fun <T> RemoteResult<T>.onSuccess(action: (value: T) -> Unit): RemoteResult<T> = apply {
    if (this is RemoteResult.Success) action(value)
}

/**
 * 若为 Failure 状态则执行 [action]
 */
inline fun <T> RemoteResult<T>.onFailure(action: (error: Throwable) -> Unit): RemoteResult<T> =
    apply {
        if (this is RemoteResult.Failure) action(error)
    }

/**
 * 加载一个挂起函数 [action]，将返回值包装为 `Flow<RemoteResult<T>>` 类型；`Flow` 将会先返回 `Loading` 状态，再调用挂起函数返回挂起函数的结果
 *
 * @param value 用于初始的 Loading 状态的数据，通常是上一次调用的结果；默认为 null
 * @param action 挂起函数
 * @return 将会依次返回 `Loading` 状态和挂起函数的结果，挂起函数的结果可能是 [RemoteResult.Success] 或 [RemoteResult.Failure]
 */
fun <T> loading(value: T? = null, action: suspend () -> T): Flow<RemoteResult<T>> = flow {
    emit(RemoteResult.Loading(value))
    emit(runCatching { action() }.fold(
        onSuccess = { RemoteResult.Success(it) },
        onFailure = { RemoteResult.Failure(it) }
    ))
}