package com.example.theweather.screens.main

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theweather.data.DataOrException
import com.example.theweather.data.WeatherDataWithTimestamp
import com.example.theweather.location.LocationPermissionState
import com.example.theweather.model.WeatherData.WeatherApiResponse
import com.example.theweather.repository.WeatherRepository
import com.example.theweather.utils.isFirstRun
import com.example.theweather.utils.updateFirstRunFlag
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val weatherRepository: WeatherRepository, @ApplicationContext private val context: Context): ViewModel() {



    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)


    private val _currentLocation = MutableLiveData<Pair<Double, Double>?>(null)
    val currentLocation: LiveData<Pair<Double, Double>?> = _currentLocation

    private val _isCachedDataAvailable = MutableStateFlow<Boolean?>(null) // null indicates loading/not checked
    val isCachedDataAvailable: StateFlow<Boolean?> = _isCachedDataAvailable


    private val _locationPermissionState = MutableStateFlow(LocationPermissionState.UNDETERMINED)
    val locationPermissionState: StateFlow<LocationPermissionState> = _locationPermissionState

    private val _isConnected = MutableLiveData<Boolean>(true)
    val isConnected: LiveData<Boolean> = _isConnected

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager



    private val _cachedWeatherData = MutableLiveData<WeatherApiResponse?>()
    val cachedWeatherData: LiveData<WeatherApiResponse?> = _cachedWeatherData

    private val _weatherData = MutableStateFlow(
        WeatherDataWithTimestamp(
            data = DataOrException(loading = true), timestamp = System.currentTimeMillis()
        )
    )

    val weatherData: StateFlow<WeatherDataWithTimestamp> = _weatherData

    private var fetchingJob: Job? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d("MainViewModel", "Network available")
            _isConnected.postValue(true)
        }

        override fun onLost(network: Network) {
            Log.d("MainViewModel", "Network lost")
            _isConnected.postValue(false)
        }
    }


    init {

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        checkCachedDataAvailability()
    }


    fun isLocationServiceEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun updateLocationPermissionState(state: LocationPermissionState) {
        _locationPermissionState.value = state
    }


    fun requestCurrentLocation() {

        Log.e("MainViewModel", "loaction request started")

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500).apply {
            setMinUpdateDistanceMeters(2000.0F)
            setGranularity(Granularity.GRANULARITY_FINE)
            setWaitForAccurateLocation(false)
        }.build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    _currentLocation.postValue(Pair(location.latitude, location.longitude))
                    break // Use the first location result
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            // Handle case where location permissions are not granted
            Log.e("MainViewModel", "Location permission not granted", e)
        }
    }

    private fun checkCachedDataAvailability() = viewModelScope.launch {
        _isCachedDataAvailable.value = weatherRepository.isCachedDataAvailable()
    }

    suspend fun fetchAndUpdateWeather(lat: Double, lon: Double) {
        val data = weatherRepository.getWeather(lat, lon)
        _weatherData.emit(WeatherDataWithTimestamp(data, System.currentTimeMillis()))
    }

    fun fetchCachedWeatherData() {
        viewModelScope.launch {
            val cachedData = weatherRepository.getCachedWeatherData()
            _cachedWeatherData.postValue(cachedData)
        }
    }

    fun refreshWeatherData(lat: Double, lon: Double) {
        viewModelScope.launch {
            _currentLocation.value?.let { location ->
                fetchAndUpdateWeather(location.first, location.second)
            }
        }
    }

    fun startPeriodicFetching(lat: Double, lon: Double, intervalMillis: Long =  60 * 1000) {
        fetchingJob?.cancel()
        fetchingJob = viewModelScope.launch {
            while (isActive) {
                _currentLocation.value?.let { location ->
                    fetchAndUpdateWeather(location.first, location.second)
                }
                delay(intervalMillis)

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fetchingJob?.cancel() // Ensures the job is cancelled when ViewModel is cleared
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }



}




