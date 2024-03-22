package com.example.theweather.widgets

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.theweather.R
import com.example.theweather.model.WeatherData.City
import com.example.theweather.model.WeatherData.WeatherItem
import com.example.theweather.utils.formatDate
import com.example.theweather.utils.formatDateTime
import com.example.theweather.utils.formatDecimals

@Composable
fun WeatherStateImage(imageUrl: String){
    Image(painter = rememberAsyncImagePainter(imageUrl), contentDescription = "Icon Image", modifier = Modifier.size(70.dp))
}

@Composable
fun HumidityWindRow(data: WeatherItem) {
    Card(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.5.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(modifier = Modifier.padding(4.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.humidityicon),
                    contentDescription = "Humidity Icon",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(2.dp)
                )

                Text(
                    text = "${data.main.humidity}%",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelLarge
                )

            }

            Row(modifier = Modifier.padding(4.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.pressureicon),
                    contentDescription = "Pressure Icon",
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text =  data.main.pressure.toString() + " mbar",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelLarge
                )
            }


            Row(modifier = Modifier.padding(4.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.windicon),
                    contentDescription = "Wind Icon",
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = formatDecimals(data.wind.speed * 3.6) + " km/h",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun SunRiseSunSetRow(data: City) {
    Card(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.5.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(modifier = Modifier.padding(4.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.sunriseicon),
                    contentDescription = "Sunrise Icon",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(2.dp)
                )

                Text(
                    text = formatDateTime(data.sunrise),
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelLarge
                )

            }

            Text(
                text = "- Indian Standard Time -",
                color = Color.LightGray,
                fontWeight = FontWeight.W900,
                style = MaterialTheme.typography.labelSmall
            )

            Row(modifier = Modifier.padding(4.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.sunseticon),
                    contentDescription = "Sunset Icon",
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = formatDateTime(data.sunset),
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun WeatherDetailRow(data: WeatherItem) {

    val imageUrl = "https://openweathermap.org/img/wn/${data.weather[0].icon}.png"

    Surface(
        Modifier
            .padding(3.dp)
            .fillMaxWidth(),
        shape = RectangleShape,
        color = Color.White, shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(1.dp), verticalArrangement = Arrangement.Center) {
                Text(formatDate(data.dt).split(",")[0], color = Color.DarkGray)
                Text(data.dt_txt.subSequence(11,16).toString(),
                    fontWeight = FontWeight.Light , color = Color.Gray
                )
            }

            WeatherStateImage(imageUrl = imageUrl)

            Surface(modifier = Modifier.padding(0.dp),
                shape = CircleShape,
                color = Color(0xFF000000)
            ) {
                Text(data.weather[0].description,
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontStyle = FontStyle.Italic)
            }

            Text(text = buildAnnotatedString {
                withStyle(style = SpanStyle(
                    color = Color.Blue.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
                ){ append(formatDecimals(data.main.temp) + "ºC")
                } }, modifier = Modifier.padding(5.dp))

        }
    }
}


@Composable
fun ShowLocationServicesDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Location Services") },
        text = { Text("No Cached Data! Location services are required to fetch the latest weather data.")},
        confirmButton = {
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                onDismiss()
            }) {
                Text("Location Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


