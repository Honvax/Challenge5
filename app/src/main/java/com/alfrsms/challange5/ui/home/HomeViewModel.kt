package com.alfrsms.challange5.ui.home

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alfrsms.challange5.data.local.database.user.UserEntity
import com.alfrsms.challange5.data.network.model.HomeMovie
import com.alfrsms.challange5.data.network.model.search.Search
import com.alfrsms.challange5.data.repository.MovieRepository
import com.alfrsms.challange5.data.repository.UserRepository
import com.alfrsms.challange5.wrapper.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val movieRepository: MovieRepository, private val userRepository: UserRepository): ViewModel() {

    private val _searchResult = MutableLiveData<Resource<Search>>()
    val searchResult: LiveData<Resource<Search>> = _searchResult

    private val _homeMovieListResult = MutableLiveData<Resource<List<HomeMovie>>>()
    val homeMovieListResult: LiveData<Resource<List<HomeMovie>>> get() = _homeMovieListResult

    private val _userByIdResult = MutableLiveData<UserEntity>()
    val userByIdResult: LiveData<UserEntity> get() = _userByIdResult

    init {
        getHomeMovieList()
    }

    fun getHomeMovieList() {
        viewModelScope.launch(Dispatchers.IO) {
            _homeMovieListResult.postValue(Resource.Loading())
            //delay(1000)
            val popular = movieRepository.getPopular()
            val topRated = movieRepository.getTopRated()
            val upcoming = movieRepository.getUpcoming()

            val homeMovieList = mutableListOf<HomeMovie>()
            homeMovieList.add(HomeMovie(title = "Popular", results = popular.payload))
            homeMovieList.add(HomeMovie(title = "Top Rated", results = topRated.payload))
            homeMovieList.add(HomeMovie(title = "Upcoming", results = upcoming.payload))
            viewModelScope.launch(Dispatchers.Main) {
                _homeMovieListResult.postValue(Resource.Success(homeMovieList))
            }
        }
    }

    fun getUserId(): Long {
        return userRepository.getUserId()
    }

    fun getUserById(id: Long) {
        viewModelScope.launch {
            _userByIdResult.postValue(userRepository.getUserById(id))
        }
    }

    fun searchMovie(query: String) {
        Log.d("searchMovie", "searchMovie")
        viewModelScope.launch(Dispatchers.IO) {
            val data = movieRepository.searchMovie(query)
            viewModelScope.launch(Dispatchers.Main) {
                _searchResult.postValue(data)
            }
        }
    }
    var listStateParcel: Parcelable? = null

    fun saveListState(parcel: Parcelable) {
        listStateParcel = parcel
    }
}