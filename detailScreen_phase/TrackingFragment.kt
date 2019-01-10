package com.myzonebuyer.fragment.detailScreen_phase


import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.myzonebuyer.R
import com.myzonebuyer.databinding.FgDspTackingFragBinding
import com.myzonebuyer.fragment.home_phase.BaseMapFragment
import com.myzonebuyer.utils.Const
import com.myzonebuyer.utils.GoogleApisHandle
import com.toxsl.volley.toolbox.RequestParams
import io.nlopez.smartlocation.OnLocationUpdatedListener
import kotlinx.android.synthetic.main.fg_dsp_tacking_frag.*
import org.json.JSONObject


class TrackingFragment : BaseMapFragment(), OnLocationUpdatedListener, GoogleApisHandle.OnPolyLineReceived {
    private var location: Location? = null
    var binding: FgDspTackingFragBinding? = null
    private var map: GoogleMap? = null
    private var myMarker: Marker? = null
    private var currentMarker: Marker? = null
    var product_id = 0
    var order_id = 0
    var latitude = 0.0
    var longitude = 0.0
    var orgLT: LatLng? = null
    var desLT: LatLng? = null
    var googleApisHandle: GoogleApisHandle? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null && args.containsKey("order_id")) {
            order_id = args.getInt("order_id", 0)
            product_id = args.getInt("product_id", 0)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.tracking))
        return inflater.inflate(R.layout.fg_dsp_tacking_frag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }


    private fun initUI() {
        googleApisHandle = GoogleApisHandle.getInstance(baseActivity)
        setUpMap()

        currentLocIV.setOnClickListener {
            focusMarkerLocation()
        }

        startLocationUpdates(this)


    }

    private fun getTrackDetail() {
        val params = RequestParams()
        params.put("order_id", order_id)
        params.put("product_id", product_id)
        syncManager.sendToServer(Const.ORDER_TRACK, params, this)
    }


    override fun mapInstance(map: GoogleMap) {
        this.map = map
    }


    private fun setMyMarker(mLocation: Location, mMap: GoogleMap) {
        val mLatLng = getLatLngFromLocation(mLocation)
        if (mLatLng != null) {
            orgLT = mLatLng
            if (myMarker == null) {
                val options = MarkerOptions().anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_blu_pointer)).draggable(false).rotation(0f).position(mLatLng)
                myMarker = mMap.addMarker(options)
            } else {
                //                myMarker.setPosition(mLatLng);
                animateMarkerToGB(mLatLng, myMarker!!, false, mMap)
            }
            val cameraPosition = CameraPosition.builder().target(mLatLng).zoom(17.5f).bearing(mLocation.bearing).tilt(0f).build()
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null)
        }
    }


    override fun onLocationUpdated(location: Location?) {
        if (this.location == null)
            this.location = location
        log("onLocationUpdated  fired >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> accuracy " + if (location != null) location.accuracy + location.latitude + location.longitude else " location null")
        if (map != null && location != null && location.hasAccuracy())
            setMyMarker(location, map!!)
        this.location = location

    }


    override val mapId: Int
        get() = R.id.map

    override fun setYourMarker(location: Location, map: GoogleMap) {
        setMyMarker(location, map)
        getTrackDetail()

    }

    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        if (jsonObject?.getString("url") == Const.ORDER_TRACK) {
            if (jsonObject.getInt("status") == Const.STATUS_OK) {
                latitude = jsonObject.getDouble("latitude")
                longitude = jsonObject.getDouble("longitude")
                desLT = LatLng(latitude, longitude)
                drawPolyLine(orgLT!!, desLT!!, this)
            } else {
                elseErrorMsg(jsonObject)
            }
        }
    }


    override fun onPolyLineReceived(origin: LatLng?, destination: LatLng?, routeMap: GoogleMap?) {
        showCabCurrentLocation(origin!!)
        setDestinationMarker(destination!!)
    }

    private fun setDestinationMarker(destination: LatLng) {
        if (map != null) {
            if (currentMarker == null) {
                currentMarker = map!!.addMarker(MarkerOptions().position(destination).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_green_pointer)))
                currentMarker!!.showInfoWindow()
            } else {
                currentMarker!!.position = destination
                currentMarker!!.showInfoWindow()
            }
        }

    }

    private fun showCabCurrentLocation(latLng: LatLng) {
        if (map != null) {
            if (myMarker == null) {
                myMarker = map!!.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_red_pointer)))
            } else {
                animateMarkerToGB(latLng, myMarker!!, false, map!!)      //update driver location
            }

            val cameraPosition = CameraPosition.builder().target(latLng).zoom(22.5f).tilt(0f).build()
            map!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null)

        }
    }


}
