package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private val runningQOrLater = Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q

    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.reminderTitle.value = binding.reminderTitle.text.toString()
            _viewModel.reminderDescription.value = binding.reminderDescription.text.toString()
            checkPermissions()
        }

        geofencingClient = LocationServices.getGeofencingClient(context!!)

        binding.saveReminder.setOnClickListener {
            _viewModel.reminderTitle.value = binding.reminderTitle.text.toString()
            _viewModel.reminderDescription.value = binding.reminderDescription.text.toString()

            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            val requestId = UUID.randomUUID().toString()

            if (latitude != null && longitude != null) {
                addGeofence(LatLng(latitude, longitude), requestId)
            }

            val reminder = ReminderDataItem(
                title,
                description,
                location,
                latitude,
                longitude,
                requestId
            )

            val validated = _viewModel.validateEnteredData(reminder)
            if (validated) {
                _viewModel.validateAndSaveReminder(
                    reminder
                )
            }
        }

        setUpObservers()
    }

    private fun setUpObservers() {
        _viewModel.selectedPOI.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.selectedLocation.text = it.name
            }
        }

        _viewModel.reminderTitle.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.reminderTitle.setText(it)
            }
        }
        _viewModel.reminderDescription.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.reminderDescription.setText(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

    private fun checkPermissions() {
        val hasForegroundLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasForegroundLocationPermission) {
            val hasBackgroundLocationPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if ((hasBackgroundLocationPermission && runningQOrLater) || (hasForegroundLocationPermission && !runningQOrLater)) {
                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
            } else {
                showPermissionsDialog(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_REQUEST_CODE,
                    getString(R.string.background_rationale_title),
                    getString(R.string.background_rationale)
                )
            }
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionsDialog(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_REQUEST_CODE,
                getString(R.string.location_required_error),
                getString(R.string.permission_denied_explanation)
            )
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_REQUEST_CODE
            )
        }
    }

    private fun showPermissionsDialog(
        permissions: Array<String>,
        requestCode: Int,
        title: String,
        message: String
    ) {
        REQUIRED_PERMISSIONS
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(
                message
            )
            .setPositiveButton("OK") { _, _ ->
                requestPermissions(
                    permissions,
                    requestCode
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
                showPermissionDeniedSnackbar()
            }
        val dialog = builder.create()
        dialog.show()
    }


    private fun allPermissionsGranted(permissions: Array<String>) =
        permissions.all {
            activity?.baseContext?.let { it1 ->
                ContextCompat.checkSelfPermission(
                    it1, it
                )
            } == PackageManager.PERMISSION_GRANTED
        }


    private fun showPermissionDeniedSnackbar() {
        val snackbar = Snackbar.make(
            binding.root,
            getString(R.string.permission_denied_explanation),
            Snackbar.LENGTH_LONG
        )
        val snackbarView = snackbar.view
        val textview =
            snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textview.maxLines = 4

        snackbar.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (runningQOrLater) {
            when {
                allPermissionsGranted(REQUIRED_PERMISSIONS_Q) -> {
                    _viewModel.navigationCommand.value =
                        NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
                }
                requestCode == FINE_LOCATION_REQUEST_CODE && grantResults[0] != PackageManager.PERMISSION_GRANTED -> {
                    showPermissionDeniedSnackbar()
                }
                else -> {
                    checkPermissions()
                }
            }
        } else {
            if (allPermissionsGranted(REQUIRED_PERMISSIONS)) {
                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
            } else {
                showPermissionDeniedSnackbar()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(
        latLng: LatLng,
        requestId: String
    ) {
        val geofence = buildGeofence(latLng, requestId)
        val geofencingRequest = buildGeofenceRequest(geofence)
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "Geofence added to GeofencingClient")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, e.message.toString())
            }
    }

    private fun buildGeofence(latLng: LatLng, requestId: String): Geofence {
        return Geofence.Builder()
            .setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS_IN_METERS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setRequestId(requestId)
            .setLoiteringDelay(3000)
            .build()
    }

    private fun buildGeofenceRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    companion object {
        private const val FINE_LOCATION_REQUEST_CODE = 99
        private const val BACKGROUND_LOCATION_REQUEST_CODE = 100
        private const val TAG = "SaveReminderFragment"
        private const val GEOFENCE_RADIUS_IN_METERS = 30f
        internal const val ACTION_GEOFENCE_EVENT =
            "SaveReminderFragment.locationreminder.action.ACTION_GEOFENCE_EVENT"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        private val REQUIRED_PERMISSIONS_Q = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }
}
