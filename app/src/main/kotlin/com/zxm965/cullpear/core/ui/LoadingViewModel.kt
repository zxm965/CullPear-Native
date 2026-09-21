package com.zxm965.cullpear.core.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zxm965.cullpear.core.model.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class LoadingViewModel<T> : ViewModel() {
    private val mutableState = mutableStateOf<UiState<T>>(UiState.Loading)
    val state: State<UiState<T>> = mutableState
    private val mutableRefreshing = mutableStateOf(false)
    val isRefreshing: State<Boolean> = mutableRefreshing
    private var loadJob: Job? = null

    protected fun load(loader: suspend () -> T) {
        if (loadJob?.isActive == true) return
        val previousState = mutableState.value as? UiState.Success<T>
        if (previousState == null) {
            mutableState.value = UiState.Loading
        } else {
            mutableRefreshing.value = true
        }
        loadJob = viewModelScope.launch {
            try {
                mutableState.value = UiState.Success(loader())
            } catch (error: Exception) {
                if (previousState == null) {
                    mutableState.value = UiState.Error(error.message ?: "内容加载失败，请稍后重试。")
                }
            } finally {
                mutableRefreshing.value = false
            }
        }
    }
}
