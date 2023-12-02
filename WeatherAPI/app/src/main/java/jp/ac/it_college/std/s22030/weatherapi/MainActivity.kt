package jp.ac.it_college.std.s22030.weatherapi

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import jp.ac.it_college.std.s22030.weatherapi.api.Client.get
import jp.ac.it_college.std.s22030.weatherapi.api.Client.getFromLocation
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var _loc by mutableStateOf<Location?>(null)
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGrantedFineLocation =
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val isGrantedCoarseLocation =
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        if (isGrantedFineLocation || isGrantedCoarseLocation) {
            requestLocationUpdates()
            return@registerForActivityResult
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000
        ).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.also { location ->
                    _loc = location
                }
            }
        }

        setContent {
            MyApplicationTheme {
                Main(location = _loc)
            }
        }
    }

    private fun requestLocationUpdates() {
        val isGrantedFineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val isGrantedCoarseLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (isGrantedFineLocation || isGrantedCoarseLocation) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
            return
        }
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onResume() {
        super.onResume()
        requestLocationUpdates()
    }

    override fun onPause() {
        fusedLocationClient.removeLocationUpdates(locationCallback)

        super.onPause()
    }
}

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(location: Location?) {
    var expanded by remember { mutableStateOf(false) }
    val items = Prefecture.items
    var selectedItem by remember { mutableStateOf(items[2130037]) }
    var selectedItemClone by remember { mutableStateOf(selectedItem) }
    var cityId by remember {
        mutableIntStateOf(2130037)
    }
    var weatherResponse by remember { mutableStateOf<WeatherResponse?>(null) }

    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                }
            ) {
                TextField(
                    value = selectedItem.toString(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEach { (key, _) ->
                        DropdownMenuItem(
                            text = { Text(text = items[key].toString()) },
                            onClick = {
                                selectedItem = items[key]
                                selectedItemClone = items[key]
                                expanded = false
                                cityId = key
                            }
                        )
                    }
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            weatherResponse = get(cityId)
                            selectedItemClone = selectedItem
                        }
                    },

                    ) {
                    Text("Click me")
                }
//                Spacer(modifier = Modifier.padding(horizontal = 16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            weatherResponse =
                                getFromLocation(location?.latitude, location?.longitude)
                            selectedItemClone = "現在地（どこかわかりまへん）"
                        }
                    },
                ) {
                    Text("現在地から")
                }
            }

            weatherResponse?.let {
                Text(text = "都市名: $selectedItemClone")
                Spacer(modifier = Modifier.padding(bottom = 20.dp))

                it.list.forEachIndexed { index, weatherData ->
                    val sdf = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm")
                    val date = java.util.Date(weatherData.dt * 1000)
                    val f = sdf.format(date)

                    Column(
                        modifier = Modifier
                            .paint(
                                painterResource(id = R.drawable.mm),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                            )
                            .fillMaxSize()
                            .padding(top = 30.dp, bottom = 30.dp, start = 15.dp)
                    ) {
                        Text(text = "${index + 1}米")
                        Text(text = "予測時刻: $f")
                        Text(text = "温度: ${weatherData.main.temp}℃")
                        Text(text = "体感気温: ${weatherData.main.feelsLike}℃")
                        Text(text = "陸上気圧: ${weatherData.main.pressure}Pa")
                        Text(text = "湿度: ${weatherData.main.humidity}%")
                        Text(text = "天気: ${weatherData.weather.firstOrNull()?.description}")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Icon")
                            AsyncImage(
                                model = "https://openweathermap.org/img/wn/${weatherData.weather.firstOrNull()?.icon}@2x.png",
                                contentDescription = null,
                            )
                        }
                        Text(text = "風速: ${weatherData.wind.speed}m/s")
                        Text(text = "風向: ${weatherData.wind.deg}")
                        Text(text = "瞬間風速: ${weatherData.wind.gust}kt")
                        Text(text = "降水確率: ${weatherData.pop * 100}%")
                        if (weatherData.snow?.snowVolume != null) {
                            Text(text = "積雪量: ${weatherData.snow.snowVolume}mm")

                        }
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_3A)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
//        Main()
    }
}