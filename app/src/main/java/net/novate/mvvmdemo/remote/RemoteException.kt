package net.novate.mvvmdemo.remote

/**
 * 封装远程调用异常，通过 [code] 和 [message] 描述远程调用异常，该异常不会填充堆栈信息
 */
open class RemoteException(open val code: Int, override val message: String? = null) : RuntimeException() {

    override fun fillInStackTrace(): Throwable = this

    override fun toString(): String {
        return javaClass.name + ": code=$code" + if (localizedMessage.isNullOrEmpty()) "" else " message=$message"
    }
}