package com.zxm965.cullpear.core.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zxm965.cullpear.core.model.UiState
import kotlinx.coroutines.launch

abstract class LoadingViewModel<T> : ViewModel() {
    private val mutableState = mutableStateOf<UiState<T>>(UiState.Loading)
    val state: State<UiState<T>> = mutableState

    protected fun load(loader: suspend () -> T) {
        viewModelScope.launch {
            mutableState.value = UiState.Loading
            mutableState.value = try {
                UiState.Success(loader())
            } catch (error: Exception) {
                UiState.Error(error.message ?: "内容加载失败，请稍后重试。")
            }
        }
    }
}
