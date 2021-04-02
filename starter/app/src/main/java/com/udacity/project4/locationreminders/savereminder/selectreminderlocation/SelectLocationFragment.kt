package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var startSnackbar: Snackbar
    private var marker: Marker? = null
    private var customMarkerSnackbarShown = false
    private var moveMarkerSnackbarShown = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_select_location,
                container,
                false
            )

        binding.viewModel = _viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?

        mapFragment?.getMapAsync(this)

        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.createReminderButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setMapStyle(
            MapStyleOptions
                .loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
        )

        enableMyLocation()
        initMapLocation()

        setMapLongClick(map)
        setPoiClick(map)
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            marker?.remove()

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )

            customMarkerSnackbarShown = true

            binding.locationNameEt.visibility = View.VISIBLE
            binding.locationNameEt.setText("")

            binding.createReminderButton.visibility = View.VISIBLE


            if (!moveMarkerSnackbarShown) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.move_marker),
                    Snackbar.LENGTH_LONG
                ).show()
                moveMarkerSnackbarShown = true
            }
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            marker?.remove()

            if (startSnackbar.isShown) {
                startSnackbar.dismiss()
            }

            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            marker.let {
                it?.showInfoWindow()
            }

            if (!customMarkerSnackbarShown) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.custom_marker),
                    Snackbar.LENGTH_LONG
                ).show()
                customMarkerSnackbarShown = true
            }

            binding.locationNameEt.visibility = View.GONE

            binding.createReminderButton.visibility = View.VISIBLE
        }
    }

    private fun onLocationSelected() {
        val position = marker?.position
        val name = if (marker?.title == getString(R.string.dropped_pin))
            binding.locationNameEt.text.toString() else marker?.title

        _viewModel.selectedPOI.value = PointOfInterest(
            position?.let { LatLng(it.latitude, position.longitude) },
            null,
            name
        )
        _viewModel.latitude.value = position?.latitude
        _viewModel.longitude.value = position?.longitude
        _viewModel.reminderSelectedLocationStr.value = name

        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }


    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            initMapLocation()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMapLocation() {
        if (map.isMyLocationEnabled) {
            val criteria = Criteria()
            val location = locationManager.getLastKnownLocation(
                locationManager.getBestProvider(
                    criteria,
                    false
                )!!
            )
            if (location != null) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), START_CAMERA_ZOOM
                    )
                )
                startSnackbar = Snackbar.make(
                    binding.root,
                    getString(R.string.select_poi),
                    Snackbar.LENGTH_INDEFINITE
                )
                startSnackbar.show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                showDeclinedPermissionDialog()
            }
        }
    }

    private fun showDeclinedPermissionDialog() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(R.string.location_required_error)
            .setMessage(getString(R.string.permission_denied_explanation))
            .setPositiveButton("OK") { _, _ ->
                findNavController().popBackStack()
            }
            .setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val START_CAMERA_ZOOM = 17.0f
        private const val TAG = "SelectLocationFragment"
    }
}
