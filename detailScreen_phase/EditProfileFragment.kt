package com.myzonebuyer.fragment.detailScreen_phase

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.gson.Gson
import com.myzonebuyer.R
import com.myzonebuyer.activity.BaseActivity
import com.myzonebuyer.databinding.FgDspEditProfileBinding
import com.myzonebuyer.extensions.*
import com.myzonebuyer.fragment.BaseFragment
import com.myzonebuyer.model.ProfileData
import com.myzonebuyer.utils.Const
import com.myzonebuyer.utils.ImageUtils
import com.toxsl.volley.toolbox.RequestParams
import com.zfdang.multiple_images_selector.ImagesSelectorActivity
import com.zfdang.multiple_images_selector.SelectorSettings
import kotlinx.android.synthetic.main.fg_dsp_edit_profile.*
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException


class EditProfileFragment : BaseFragment(), BaseActivity.PermCallback, ImageUtils.ImageSelectCallback {
    var binding: FgDspEditProfileBinding? = null
    private var image_file: File? = null
    private var phone: String? = ""
    private var mResults = ArrayList<String?>()
    private var lat = 0.0
    var lng = 0.0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbarMain(true, getString(R.string.edit_profile))
        if (binding == null)
            binding = DataBindingUtil.inflate(inflater, R.layout.fg_dsp_edit_profile, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        submitBT.setOnClickListener(this)
        imgCIV.setOnClickListener(this)
        cityET.setOnClickListener(this)
        mobileET.setOnClickListener(this)

        if (store.getBoolean("is_seller", false)) {
            submitBT.background = ContextCompat.getDrawable(baseActivity, R.drawable.background_red)
        }

        getProfileApi()

    }

    private fun getProfileApi() {
        syncManager.sendToServer(Const.API_GET_PROFILE, null, this)
    }


    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.submitBT -> {
                if (isValidate()) {
                    hitUpdateProfileApi()
                }
            }

            R.id.imgCIV -> {
                val intent = Intent(baseActivity, ImagesSelectorActivity::class.java)
                intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 1)
                intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, true)
                intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, mResults)
                baseActivity.startActivityForResult(intent, Const.REQUEST_CODE)
            }

            R.id.cityET -> {
                try {
                    val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(baseActivity)
                    baseActivity.startActivityForResult(intent, Const.PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: GooglePlayServicesRepairableException) {
                    log(e.message)
                } catch (e: GooglePlayServicesNotAvailableException) {
                    log(e.message)
                }
            }

            R.id.verifyTV -> {

            }

            R.id.mobileET -> {
                baseActivity.replaceFragment(ChangePhoneNumberFragment(), R.id.frame_container)
            }
        }
    }

    private fun hitUpdateProfileApi() {
        val args = RequestParams()
        args.put("first_name", firstET.checkString())
        args.put("last_name", lastET.checkString())
        args.put("address", cityET.checkString())
        try {
            args.put("profile_file", image_file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        when {
            maleRB.isChecked -> args.put("gender", Const.GENDER_MALE)
            else -> args.put("gender", Const.GENDER_FEMALE)
        }
        args.put("latitude", lat)
        args.put("longitude", lng)
        syncManager.sendToServer(Const.API_UPDATE_PROFILE, args, this)
    }

    private fun isValidate(): Boolean {
        when {
            firstET.isBlank() -> showToastOne(getString(R.string.enter_first_name))
            lastET.isBlank() -> showToastOne(getString(R.string.enter_lats_name))
            cityET.isBlank() -> showToastOne(getString(R.string.choose_city))
            else -> return true
        }
        return false
    }


    override fun permGranted(resultCode: Int) {
        if (resultCode == 123) {
            val builder = ImageUtils.ImageSelect.Builder(baseActivity, this, 234)
            builder.start()
        }
    }

    override fun permDenied(resultCode: Int) {

    }


    override fun onImageSelected(imagePath: String?, resultCode: Int) {
        if (resultCode == 234) {
            val bitmap = ImageUtils.imageCompress(imagePath)
            imgCIV.setImageBitmap(bitmap)
            image_file = ImageUtils.bitmapToFile(bitmap, baseActivity)
        }

    }


    override fun onSyncSuccess(controller: String?, action: String?, status: Boolean, jsonObject: JSONObject?) {
        super.onSyncSuccess(controller, action, status, jsonObject)
        try {
            if (jsonObject?.getString("url") == Const.API_GET_PROFILE) {
                if (jsonObject.getInt("status") == Const.STATUS_OK) {
                    val profiledata = Gson().fromJson(jsonObject.getJSONObject("details").toString(), ProfileData::class.java)
                    binding!!.profileData = profiledata
                    when {
                        !profiledata.profileFile!!.isEmpty() -> imgCIV.loadFromUrl(baseActivity, profiledata.profileFile!!, R.mipmap.ic_default_user)
                        else -> imgCIV.loadFromDrawable(baseActivity, R.mipmap.ic_default_user)
                    }

                    when {
                        profiledata.phoneVerified == 0 -> {
                            verifyTV.text = getString(R.string.not_varified)
                            verifyTV.setTextColor(ContextCompat.getColor(baseActivity, R.color.red))
                            verifyTV.setOnClickListener(this)
                            phone = profiledata.contactNo.toString()
                        }
                        else -> {
                            verifyTV.text = getString(R.string.verified)
                            verifyTV.setTextColor(ContextCompat.getColor(baseActivity, R.color.Green))
                            verifyTV.setOnClickListener(null)
                        }

                    }
                    if (profiledata.contactNo != null && profiledata.contactNo!!.isNotEmpty() && profiledata.contactNo != "null") {
                        mobileET.setText(baseActivity.getString(R.string.phone_plus, profiledata.contactNo))
                    }
                    cityET.setText(profiledata.address)
                } else {
                    elseErrorMsg(jsonObject)
                }
            } else if (jsonObject?.getString("url") == Const.API_UPDATE_PROFILE) {
                if (jsonObject.getInt("status") == Const.STATUS_OK) {
                    showToastOne(getString(R.string.profile_updated_success))
                    baseActivity.supportFragmentManager.popBackStack()
                } else {
                    elseErrorMsg(jsonObject)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getResult(resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                val place = PlaceAutocomplete.getPlace(baseActivity, data)
                cityET.setText(place.address)
                lat = place.latLng.latitude
                lng = place.latLng.longitude
            }
            PlaceAutocomplete.RESULT_ERROR -> {
                val status = PlaceAutocomplete.getStatus(baseActivity, data)
                log(status.statusMessage)
            }
        }

    }

    fun getResultInfo(data: Intent) {
        mResults.clear()
        mResults = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS)

        for (i in mResults) {
            val bitmap = ImageUtils.imageCompress(mResults[0])
            image_file = ImageUtils.bitmapToFile(bitmap, baseActivity)
            binding!!.imgCIV.loadFromFile(baseActivity, image_file, R.mipmap.ic_default_user)

        }
    }

}



