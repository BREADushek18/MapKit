package com.example.mapkit

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var locButton: FloatingActionButton
    private lateinit var toggleButton: ToggleButton
    lateinit var userLocationLayer: UserLocationLayer
    lateinit var mapObjectCollection: MapObjectCollection

    // Объект InputListener для обработки событий касания карты
    private val addInputListener = (object : InputListener {
        override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
            addPlacemarkOnMap(point)
        }

        override fun onMapLongTap(map: Map, point: Point) {
            addPlacemarkOnMap(point)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Установка API-ключа для Yandex.MapKit
        MapKitFactory.setApiKey("b101f532-6a1b-4f78-b926-7adb75a3491b")
        setContentView(R.layout.activity_main)

        // Инициализация Yandex.MapKit
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapview)
        Location() // Вызов функции для получения доступа к местоположению пользователя

        // Добавление коллекции объектов на карту
        mapObjectCollection = mapView.map.mapObjects.addCollection()

        // Добавление слушателя для обработки событий касания карты
        mapView.map.addInputListener(addInputListener)

        // Создание и настройка слоя пробок
        var trafficLayer = MapKitFactory.getInstance().createTrafficLayer(mapView!!.mapWindow)
        toggleButton = findViewById(R.id.toggleBtn)
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                trafficLayer.isTrafficVisible = true
            } else {
                trafficLayer.isTrafficVisible = false
            }
        }
    }

    // Функция для добавления метки на карту
    fun addPlacemarkOnMap(point: Point) {
        mapObjectCollection.addPlacemark(point)
    }

    // Функция для получения доступа к местоположению пользователя
    fun Location() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Если разрешение на доступ к точному местоположению предоставлено
                    locButton = findViewById(R.id.location)

                    // Создание слоя местоположения пользователя
                    userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
                    userLocationLayer.isVisible = true
                    locButton.setOnClickListener() {
                        // При нажатии на кнопку местоположения, перемещаем камеру на местоположение пользователя
                        mapView.map.move(
                            CameraPosition(userLocationLayer.cameraPosition()!!.getTarget(), 13.0f, 0.0f, 0.0f),
                            Animation(Animation.Type.SMOOTH, 2F),
                            null
                        )
                    }
                }
                permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Если разрешение на доступ к приблизительному местоположению предоставлено
                    // Можно добавить дополнительную логику здесь
                }
                else -> {
                    // Если разрешения не предоставлены
                }
            }
        }
        locationPermissionRequest.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    override fun onStart() {
        super.onStart()
        // Запуск MapKit при старте активности
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        // Остановка MapKit при остановке активности
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
