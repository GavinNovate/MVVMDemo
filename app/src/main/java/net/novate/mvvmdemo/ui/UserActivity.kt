package net.novate.mvvmdemo.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import net.novate.mvvmdemo.R
import net.novate.mvvmdemo.data.User
import net.novate.mvvmdemo.remote.RemoteResult
import net.novate.mvvmdemo.remote.fold

class UserActivity : AppCompatActivity() {

    // 注意 Fragment 不要用 by lazy { findViewById(xxx) } 可能会出 bug

    private val name: TextView by lazy { findViewById(R.id.name) }
    private val age: TextView by lazy { findViewById(R.id.age) }

    private val button1: Button by lazy { findViewById(R.id.button1) }
    private val button2: Button by lazy { findViewById(R.id.button2) }
    private val button3: Button by lazy { findViewById(R.id.button3) }

    private val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        initView()
        initData()
    }

    private fun initView() {
        button1.setOnClickListener {
            viewModel.login("101")
        }
        button2.setOnClickListener {
            viewModel.login("102")
        }
        button3.setOnClickListener {
            viewModel.login("103")
        }
    }

    private fun initData() {
        lifecycleScope.launchWhenCreated {
            viewModel.user.collect {
                name.text = it.name
                age.text = it.age
            }
        }
    }

    private companion object {

        private val RemoteResult<User>.name: String
            get() = fold(
                onLoading = { "姓名：加载中..." },
                onSuccess = { "姓名：${it.name}" },
                onFailure = { "姓名：出错了!!!" }
            )

        private val RemoteResult<User>.age: String
            get() = fold(
                onLoading = { "年龄：加载中..." },
                onSuccess = { "年龄：${it.age}" },
                onFailure = { "年龄：出错了!!!" }
            )
    }
}