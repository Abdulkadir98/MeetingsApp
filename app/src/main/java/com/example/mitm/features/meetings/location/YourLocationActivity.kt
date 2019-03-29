package com.example.mitm.features.meetings.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import com.example.mitm.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import org.jetbrains.anko.find
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import android.util.Log
import kotlinx.android.synthetic.main.activity_your_location.*
import android.support.v7.widget.DividerItemDecoration
import android.view.View
import com.example.mitm.features.utils.Constants.Companion.LATITUTDE
import com.example.mitm.features.utils.Constants.Companion.LONGITUDE
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import org.jetbrains.anko.toast
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


private const val apiKey = "AIzaSyA9lm6NyGiP4ZCWYTxZnbeZESH6AGpVXjo"
private lateinit var placesClient: PlacesClient

private lateinit var resultReceiver: YourLocationActivity.AddressResultReceiver

private lateinit var placesRecyclerView: RecyclerView
private lateinit var adapter: LocationAdapter

private val REQUEST_CHECK_SETTINGS: Int = 12
private const val RC_LOCATION = 21


data class Location(var fullAddress: String, val primaryAddress: String, val secondaryAddress: String, val placeId: String)
data class Coordinate(val latitude: Double, val Longitude: Double)

class YourLocationActivity : AppCompatActivity(), LocationAdapter.OnListItemClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_location)

        Places.initialize(this, apiKey)
        placesClient = Places.createClient(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        resultReceiver = AddressResultReceiver(Handler())


        placesRecyclerView = find(R.id.list_search)
        val linearLayoutManager = LinearLayoutManager(this)
        placesRecyclerView.layoutManager = linearLayoutManager

        val dividerItemDecoration = DividerItemDecoration(placesRecyclerView.context,
                linearLayoutManager.orientation)

        adapter = LocationAdapter(mutableListOf(), this)

        placesRecyclerView.addItemDecoration(dividerItemDecoration)
        placesRecyclerView.adapter = adapter

        searchPlace.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                saveLocationBtn.isEnabled = s.toString().trim().isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (count > 0) {
                    requestPlaces(s?.toString()!!.trim())
                }
                else
                    saveLocationBtn.isEnabled = false
            }

        })


        currentLocationContainer.setOnClickListener {
            requestUserLocation()
        }

        saveLocationBtn.setOnClickListener {
            if (coordinate != null) {

                //setResult(LOCATION_RECEIVED, )
                Intent().apply {
                    putExtra(LATITUTDE, coordinate!!.latitude)
                    putExtra(LONGITUDE, coordinate!!.Longitude)
                    setResult(Activity.RESULT_OK, this)
                    finish()
                }
            }
        }

    }


    fun requestPlaces(query: String) {

        val token = AutocompleteSessionToken.newInstance()
        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                //.setLocationRestriction(bounds)
                .setSessionToken(token)
                .setQuery(query)
                .build()

        val locations = mutableListOf<Location>()
        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            for (prediction in response.autocompletePredictions) {

                val primaryAddress = prediction.getPrimaryText(null).toString()
                val secondaryAddress = prediction.getSecondaryText(null).toString()
                val fullAddress = prediction.getFullText(null).toString()
                val location = Location(primaryAddress = primaryAddress, secondaryAddress = secondaryAddress,
                        fullAddress = fullAddress, placeId = prediction.placeId)
                locations.add(location)

            }

            adapter.setLocations(locations)
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e("Your location", "Place not found: " + exception.statusCode)
            }
        }

        //placesClient.fetchPlace()
    }

    private var coordinate: Coordinate?  = null

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {

        progressBar.visibility = View.VISIBLE
        saveLocationBtn.isEnabled = false

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: android.location.Location? ->
                        // Got last known location. In some rare situations this can be null.
                        if (location == null) {
                            createLocationRequest()

                        }

                        else {
                            coordinate =  Coordinate(location.latitude, location.longitude)

                            val intent = Intent(this, FetchAddressIntentService::class.java).apply {
                                putExtra(Constants.ADDRESS_RECEIVER, resultReceiver)
                                putExtra(Constants.LOCATION_DATA_EXTRA, location)
                            }
                            startService(intent)
                        }


                        //toast("${location?.latitude} ${location?.longitude}")
                    }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@YourLocationActivity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {

            createLocationRequest()
        }
    }

    private var addressOutput = ""

    internal inner class AddressResultReceiver(handler: Handler) : ResultReceiver(handler) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {

            // Display the address string
            // or an error message sent from the intent service.
            addressOutput = resultData?.getString(Constants.ADDRESS_RESULT_DATA_KEY) ?: ""
            displayAddressOutput(addressOutput)

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {

                progressBar.visibility = View.INVISIBLE
                saveLocationBtn.isEnabled = true
                toast(getString(R.string.address_found))

            }

        }
    }

    override fun onListItemClick(location: Location) {

        progressBar.visibility = View.VISIBLE
        saveLocationBtn.isEnabled = false

        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.builder(location.placeId, placeFields).build()


        placesClient.fetchPlace(request).addOnSuccessListener { response ->

            progressBar.visibility = View.INVISIBLE
            saveLocationBtn.isEnabled = true

            val place = response.place
            Log.i("Your location", place.latLng?.latitude.toString())
            val latitude = place.latLng?.latitude!!
            val longitude = place.latLng?.longitude!!

            coordinate = Coordinate(latitude, longitude)
            displayAddressOutput(location.fullAddress)



        }.addOnFailureListener {
            exception ->
            if (exception is ApiException) {

                val statusCode = exception.statusCode

                Log.e("Your location", exception.message)
            }
        }


    }


    fun displayAddressOutput(address: String) {

        saveLocationBtn.isEnabled = true
        searchPlace.setText(address)
        adapter.setLocations(emptyList())
    }

    @AfterPermissionGranted(RC_LOCATION)
    fun requestUserLocation() {

        val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (EasyPermissions.hasPermissions(this, *perms)) {

            createLocationRequest()
        }
        else {
            EasyPermissions.requestPermissions(this, "Required to obtain your current location",  RC_LOCATION, *perms)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }
}
