package com.example.swipy.domain.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String
)

class LocationManager(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    

    suspend fun getCurrentLocation(): Result<LocationData> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }
        
        return try {
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
            
            if (location == null) {
                return Result.failure(Exception("Unable to get location"))
            }
            
            val address = getAddressFromLocation(location.latitude, location.longitude)
            
            Result.success(LocationData(
                latitude = location.latitude,
                longitude = location.longitude,
                city = address?.locality ?: address?.subAdminArea ?: "",
                country = address?.countryName ?: ""
            ))
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): Address? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        continuation.resume(addresses.firstOrNull())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
    

    suspend fun searchLocation(query: String): Result<List<LocationData>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        
        return try {
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName(query, 5) { addresses ->
                        continuation.resume(addresses)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 5) ?: emptyList()
            }
            
            val locationDataList = addresses.mapNotNull { address ->
                val city = address.locality ?: address.subAdminArea ?: return@mapNotNull null
                val country = address.countryName ?: return@mapNotNull null
                
                LocationData(
                    latitude = address.latitude,
                    longitude = address.longitude,
                    city = city,
                    country = country
                )
            }
            
            Result.success(locationDataList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

    suspend fun validateLocation(city: String, country: String): Result<LocationData> {
        if (city.isBlank() || country.isBlank()) {
            return Result.failure(IllegalArgumentException("City and country cannot be empty"))
        }
        
        return try {
            val query = "$city, $country"
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName(query, 1) { addresses ->
                        continuation.resume(addresses)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 1) ?: emptyList()
            }
            
            val address = addresses.firstOrNull()
                ?: return Result.failure(Exception("Location not found"))
            
            Result.success(LocationData(
                latitude = address.latitude,
                longitude = address.longitude,
                city = address.locality ?: address.subAdminArea ?: city,
                country = address.countryName ?: country
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000
    }
}

