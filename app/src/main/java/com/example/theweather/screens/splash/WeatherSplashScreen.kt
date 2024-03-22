package com.example.theweather.screens.splash


import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.theweather.R
import com.example.theweather.navigation.WeatherScreens
import kotlinx.coroutines.delay

@Composable
fun WeatherSplashScreen(navController: NavController){

    val scale = remember {
        Animatable(0f)
    }

    LaunchedEffect(key1 = true, block = {
        scale.animateTo(targetValue = 1f,
            animationSpec = tween(
                durationMillis = 700,
                easing = {
                    OvershootInterpolator(8f)
                        .getInterpolation(it)
                }))

            delay(1500L)
            navController.navigate(WeatherScreens.MainScreen.name)
    } )


    Surface(modifier = Modifier
        .padding(10.dp)
        .size(350.dp)
        .scale(scale.value),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(width = 1.dp, color = Color.LightGray)
    ) {
        
        Column(modifier = Modifier.padding(2.dp),
               horizontalAlignment = Alignment.CenterHorizontally,
               verticalArrangement = Arrangement.Center ) {
            
                Image(painter = painterResource(id = R.drawable.splashscreenicon),
                    contentDescription = "Icon for splash screen",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(120.dp))

                Text(text = "Welcome to TheWeatherApp",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.LightGray
                )
        }

    }
}