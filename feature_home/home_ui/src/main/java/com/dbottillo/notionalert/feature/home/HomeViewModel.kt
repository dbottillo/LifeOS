package com.dbottillo.notionalert.feature.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.dbottillo.notionalert.ApiResult
import com.dbottillo.notionalert.Lce
import com.dbottillo.notionalert.Todo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    val data: LiveData<Lce<Todo>> = liveData(Dispatchers.IO) {
        emit(Lce.Loading)
        when (val res = repository.get()) {
            is ApiResult.Success -> emit(Lce.Data(res.data))
            is ApiResult.Error -> emit(Lce.Error(res.exception))
        }
    }
}
