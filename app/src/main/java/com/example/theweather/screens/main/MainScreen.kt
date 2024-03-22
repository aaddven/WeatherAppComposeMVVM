package com.example.theweather.screens.main


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.theweather.R
import com.example.theweather.model.WeatherData.WeatherApiResponse
import com.example.theweather.utils.formatDecimals
import com.example.theweather.utils.formatTimestamp
import com.example.theweather.utils.isFirstRun
import com.example.theweather.utils.updateFirstRunFlag
import com.example.theweather.widgets.HumidityWindRow
import com.example.theweather.widgets.ShowLocationServicesDialog
import com.example.theweather.widgets.SunRiseSunSetRow
import com.example.theweather.widgets.WeatherStateImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(navController: NavController,mainViewModel: MainViewModel = hiltViewModel()){




    val context = LocalContext.current

    // We will ask for location permissions only on first run
    val isFirstRun = remember { isFirstRun(context) }

    // Observing Data
    val isCachedDataAvailable by mainViewModel.isCachedDataAvailable.collectAsState()
    val currentLocation = mainViewModel.currentLocation.observeAsState().value
    val isConnected by mainViewModel.isConnected.observeAsState(initial = true)
    val cachedWeatherData by mainViewModel.cachedWeatherData.observeAsState()
    val weatherDataState = mainViewModel.weatherData.collectAsState().value


    val permissionState = rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)

    val showPermissionExplanationDialog = remember { mutableStateOf(false) }




    LaunchedEffect(key1 = Unit) {
        when {
            isFirstRun -> {
                permissionState.launchPermissionRequest()
                updateFirstRunFlag(context)
            }
            !permissionState.hasPermission && isCachedDataAvailable != true -> {

                showPermissionExplanationDialog.value = true
            }
            !permissionState.hasPermission && isCachedDataAvailable == true -> {

                Log.d("MainScreen",  "No permission but cached data available")
            }
        }
    }


    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isCachedDataAvailable) {
        val locationServicesEnabled = mainViewModel.isLocationServiceEnabled()

        showDialog = !locationServicesEnabled && isCachedDataAvailable != true
    }

    if (showDialog) {
        ShowLocationServicesDialog {
            showDialog = false
        }
    }

    LaunchedEffect(key1 = Unit) {
        mainViewModel.fetchCachedWeatherData()
    }


    LaunchedEffect(permissionState.hasPermission, mainViewModel.isLocationServiceEnabled()) {
        if (permissionState.hasPermission && mainViewModel.isLocationServiceEnabled() && currentLocation == null) {
            mainViewModel.requestCurrentLocation()
            Log.d("MainScreen",  "Fetching location")
        }
    }

    LaunchedEffect(key1 = currentLocation, key2 = isConnected) {
        if (currentLocation != null && isConnected) {
            // Triggering periodic fetching with current location
            currentLocation.let {
                mainViewModel.startPeriodicFetching(it.first, it.second)
                Log.d("MainScreen",  "Periodic fetching started...")
            }
        }
    }


    when {
        isCachedDataAvailable == true -> {
            Log.d("MainScreen", "Cached data available: $isCachedDataAvailable")

            if(weatherDataState.data.data == null){

                cachedWeatherData?.let { cachedData ->
                    showData(cachedData, formatTimestamp(weatherDataState.timestamp), true, isConnected) {
                        mainViewModel.currentLocation.value?.let {
                            mainViewModel.refreshWeatherData(it.first, it.second)
                        }
                    }
                }

               
            }else if (weatherDataState.data.data != null){
                Log.d("MainScreen", "From Cache to new ")

                showData(weatherDataState.data.data!!, formatTimestamp(weatherDataState.timestamp),false, isConnected) {
                    mainViewModel.currentLocation.value?.let {
                        mainViewModel.refreshWeatherData(it.first, it.second)
                    }
                }
            }else {
                Text(text = "Something went wrong")
            }

            Log.d("MainScreen", "Cant move to new data")
        }
        weatherDataState.data.loading == true -> {
            CircularProgressIndicator()
        }
        weatherDataState.data.data != null -> {
            // Show fetched data
            showData(weatherDataState.data.data!!, formatTimestamp(weatherDataState.timestamp),false, isConnected) {
                mainViewModel.currentLocation.value?.let {
                    mainViewModel.refreshWeatherData(it.first, it.second)
                }
            }
            Log.d("MainScreen", "Newer data")
        }
        else -> {
            Text("Unable to fetch weather data. Please check your connection and try again.")
        }
    }

}








@Composable
fun showData(data: WeatherApiResponse,timestamp: String , isCache: Boolean,connectivityStatus: Boolean,
             onRefresh: () -> Unit) {

    val imageUrl = "https://openweathermap.org/img/wn/${data.list[0].weather[0].icon}.png"

    Surface(modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(4.dp)    ) {

        Column(modifier = Modifier
            .padding(1.dp)
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally) {

            Box(modifier = Modifier
                .padding(1.dp)
                .fillMaxWidth()){

                Card (modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp),
                    colors = CardDefaults.cardColors(Color.White)){

                    Row(modifier = Modifier
                        .padding(2.dp)
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.Center){


                        Text(
                            text = if (isCache) "Cached Data" else "Fetched Data",
                            color = DarkGray,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(6.dp)
                        )




                    }

                }

            }

            Box(modifier = Modifier
                .padding(1.dp)
                .fillMaxWidth()){

                Card (modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp),
                    colors = CardDefaults.cardColors(Color.DarkGray)){

                    Row(modifier = Modifier
                        .padding(2.dp)
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween){

                        Text(text = "Updated: ${timestamp}",
                            style = MaterialTheme.typography.labelLarge,
                            color = White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(6.dp))


                        Text(
                            text = if (connectivityStatus) "Connected" else "No Internet",
                            color = if (connectivityStatus) Green else Red,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(6.dp)
                        )

                        IconButton(onClick = { onRefresh() }) {

                            Icon(
                                painter = painterResource(id = R.drawable.refresh),
                                contentDescription = "Refresh",
                                tint = White
                            )
                        }





                    }

                }

            }

            Box(modifier = Modifier
                .padding(1.dp)
                .fillMaxWidth()){
                

                Card(modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {

                    Column(
                        modifier = Modifier.padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {


                        Row(modifier = Modifier
                            .padding(2.dp)
                            .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween){

                            Text(text = "${data.city.name}",
                                style = MaterialTheme.typography.labelLarge,
                                color = DarkGray,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(6.dp))

                            Text(text = "${data.list[0].dt_txt.subSequence(11,16)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Gray,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Light,
                                modifier = Modifier.padding(6.dp))

                        }



                        Divider()

                        Surface(modifier = Modifier
                            .padding(8.dp)
                            .size(230.dp),
                            shape = CircleShape,
                            color = Color(0xFFFFC400)) {

                            Column(verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally) {

                                WeatherStateImage(imageUrl = imageUrl)

                                Text(text = formatDecimals(data.list[0].main.temp)+"ºC",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold)

                                Text(text = "${data.list[0].weather[0].main}",
                                    fontSize = 16.sp,
                                    fontStyle = FontStyle.Italic)

                                Text(text = "(${data.list[0].weather[0].description})",
                                    fontSize = 16.sp,
                                    color = DarkGray,
                                    fontStyle = FontStyle.Normal)

                            }

                        }


                        HumidityWindRow(data = data.list[0])

                        SunRiseSunSetRow(data = data.city)

                    }
                }
            }

        }
    }

}


