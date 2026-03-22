package com.obdstar.module.diag.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.CursorLoader
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import com.obdstar.common.core.Constants
import com.obdstar.common.core.IObdstarApplication
import com.obdstar.common.core.base.BaseRxActivity
import com.obdstar.common.core.utils.FileUtils
import com.obdstar.common.core.utils.LanguageUtils
import com.obdstar.common.core.utils.QRCodeUtil
import com.obdstar.common.ui.view.ObdstarKeyboard
import com.obdstar.common.ui.view.PgbDlg
import com.obdstar.common.ui.view.ToastUtil
import com.obdstar.module.diag.R
import com.obdstar.module.diag.abnormal_feedback.bean.StoreInfo
import com.obdstar.module.diag.adapters.DiagHistoryPhotoAdapter
import com.obdstar.module.diag.model.DiagHistoryBean
import com.obdstar.module.diag.ui.diagreport.UploadPdfBean
import com.obdstar.module.diag.v3.datastream3.adapter.ShDsTableAdapter
import com.obdstar.module.diag.v3.trimsupport.form.CustomSpinnerView
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.net.HttpURLConnection
import java.security.MessageDigest
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.jvm.java

/**
 * 历史记录
 */
@Route(path = "/diagCore/history")
class ShowDiagHistoryActivity : BaseRxActivity() {
    private val TAG = "ShowDiagHistoryActivity"
    val okHttpClient = OkHttpClient().newBuilder().retryOnConnectionFailure(false)
        .connectTimeout(5, TimeUnit.MINUTES).readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES).build()
    lateinit var mDpApplication: IObdstarApplication
    private var mContext: Context? = null
    private var mProgressBarDialog: PgbDlg? = null
    private var tv_brand: TextView? = null
    private var scroll_view: NestedScrollView? = null
    private var tv_title: AppCompatTextView? = null
    private var tv_brand_name: AppCompatTextView? = null
    private var tv_year: AppCompatTextView? = null
    private var tv_mileage: AppCompatTextView? = null
    private var tv_model: AppCompatTextView? = null
    private var tv_vin: AppCompatTextView? = null
    private var tvHours: AppCompatTextView? = null

    //    private ImageView tv_line_hint;
    private var gson: Gson? = null

    @JvmField
    @Autowired(name = "path")
    public var filePath = ""

    @JvmField
    @Autowired(name = "mBrandName")
    public var mBrandName = ""
    private lateinit var factory: LayoutInflater
    private lateinit var ll_root: ViewGroup
    private var mSharePop: PopupWindow? = null
    private var mTwoCodePop: PopupWindow? = null
    private var mEmailPop: Dialog? = null
    private val dropXOffsetShare =
        if (Constants.is5InchesDevice || Constants.is7InchesDevice) -60 else -104
    private val pdfHeights: MutableList<Int> = ArrayList() // 存每页pdf的高度
    private var a4Height = 0 // 每页pdf高度

    // 计算布局高度
    private var pdfHeight = 0
    private var con_car_information: ConstraintLayout? = null
    private var toDay: String? = null
    private lateinit var diagHistoryBean: DiagHistoryBean
    private var mPdfLoadurl // pdf下载路径
            : String? = null
    private var mPdfPath // pdf保存路径
            : String? = null
    private var iv_qrcode: ImageView? = null
    private var tv_time: AppCompatTextView? = null
    private var tv_main_state: AppCompatTextView? = null
    private var con_title: ConstraintLayout? = null
    private var mDisposable: Disposable? = null
    private var mDisposable2: Disposable? = null
    private var tvStoreName: TextView? = null
    private var tvStoreTel: TextView? = null
    private var tvStoreEmail: TextView? = null
    private var tvStoreAddress: TextView? = null
    private var mStoreInfo: StoreInfo? = null
    private val icons: MutableMap<String, Drawable?> = HashMap()
    private var mCancelTipPop: Dialog? = null

    private var root_view: View? = null

    /**
     * true：编辑模式 false: 只读模式
     */
    private var isEditMode: Boolean = false

    private lateinit var obdstarKeyboard: ObdstarKeyboard

    /**
     * 当前触摸 系统键盘的 文本编辑框
     */
    private var mCurrentEdit: EditText? = null

    private var ll_read_only_btn: View? = null
    // private var btn_edit_save: Button? = null

    // private var btn_edit_cancel: Button? = null
    private var ll_edit_bottom: View? = null
    private var btn_confirm: Button? = null
    private var btn_cancel: Button? = null
    private var spinner_repair_state: CustomSpinnerView? = null
    private var group_edit_mode_base_info: View? = null
    private var et_model: EditText? = null
    private var et_year: EditText? = null
    private var et_mileage: EditText? = null
    private var et_hours: EditText? = null
    private var et_vin: EditText? = null
    private var et_store_name: EditText? = null
    private var et_store_tel_num: EditText? = null
    private var et_store_email: EditText? = null
    private var et_store_address: EditText? = null

    private var et_owner: EditText? = null
    private var et_postal_code: EditText? = null
    private var et_phone: EditText? = null
    private var et_vehicle_plate: EditText? = null
    private var group_edit_mode_owner_info: View? = null

    private var tvOwner: AppCompatTextView? = null
    private var tvPostalCode: AppCompatTextView? = null
    private var tvPhone: AppCompatTextView? = null
    private var tvVehiclePlate: AppCompatTextView? = null

    private var tv_repair_remark: AppCompatTextView? = null
    private var et_repair_remark: EditText? = null
    private var tv_maintenance_staff: AppCompatTextView? = null
    private var et_maintenance_staff: EditText? = null

    private var picturePop: PopupWindow? = null
    private var rl_add_pic: View? = null

    private var originYear: String = ""

    private lateinit var mPhotoFile: File
    private var photoPath: String? = null
    private lateinit var repairStatesMap: Map<Int, String>
    private var originRepairState: Int = -1
    private val UPLOAD_PIC_CASE_CALL_ALBUM = 10001
    private val UPLOAD_PIC_CASE_CALL_CAMERA = 10002
    private var adapter: DiagHistoryPhotoAdapter? = null
    private var rv_add_photos: RecyclerView? = null
    private var v_empty_pic: View? = null

    /**
     * 最多添加6张图片
     */
    private val MAX_PHOTO = 6

    /**
     * 系统键盘 对应的 EditText
     */
    private var systemKeyboardETs: MutableList<EditText?> = mutableListOf()

    private var picPathList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_diag_history)
        //        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            filePath = bundle.getString("path");
//            mBrandName = bundle.getString("mBrandName");
//        }
        ARouter.getInstance().inject(this)
        mDpApplication = getApplication() as IObdstarApplication
        mStoreInfo = Gson().fromJson(
            mDpApplication["StoreInfo", ""], StoreInfo::class.java
        )
        mContext = this
        if (mDpApplication.languageType == LanguageUtils.CN) {
            BASE_URL = "http://drpcn.obdstar.com"
        } else {
            BASE_URL = "http://drpen.obdstar.com"
        }
        initView()
        if (!filePath.isNullOrEmpty() && !mBrandName.isNullOrEmpty()) {
            initLightMap()
            initData()
        }
    }

    private fun initLightMap() {
        icons[ShDsTableAdapter.IC_L_RED] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_l_red
        )
        icons[ShDsTableAdapter.IC_L_GREEN] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_l_green
        )
        icons[ShDsTableAdapter.IC_L_GRAY] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_l_gray
        )
        icons[ShDsTableAdapter.IC_S_ON] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_s_on
        )
        icons[ShDsTableAdapter.IC_S_OFF] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_s_off
        )
        icons[ShDsTableAdapter.IC_S_SUCCESS] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_s_success
        )
        icons[ShDsTableAdapter.IC_S_FAILURE] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_s_failure
        )
        icons[ShDsTableAdapter.IC_L_BLACK] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_s_black
        )
        icons[ShDsTableAdapter.IC_L_DARK_GREEN] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_s_dark_green
        )
        icons[ShDsTableAdapter.IC_L_YELLOW] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_s_yellow
        )
        icons[ShDsTableAdapter.IC_L_ORANGE] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_s_orane
        )
        icons[ShDsTableAdapter.IC_L_BLUE] = AppCompatResources.getDrawable(
            mContext!!, R.drawable.ic_s_blue
        )
    }

    private fun initView() {
        if (Constants.isDP8000Device) {
            (findViewById<View>(R.id.tv_title_a) as TextView).typeface = Typeface.DEFAULT_BOLD
        }
        mProgressBarDialog = PgbDlg(this, com.obdstar.common.ui.R.string.please_wait)
        root_view = findViewById(R.id.root_view)
        con_title = findViewById(R.id.con_title)
        tv_main_state = findViewById(R.id.tv_state)
        tv_time = findViewById(R.id.tv_time)
        tv_title = findViewById(R.id.tv_title)
        con_car_information = findViewById(R.id.con_car_information)
        scroll_view = findViewById(R.id.scroll_view)
        ll_root = findViewById(R.id.ll_root)
        tv_brand = findViewById(R.id.tv_brand)
        tv_brand_name = findViewById(R.id.tv_brand_name)
        tv_year = findViewById(R.id.tv_year)
        tv_mileage = findViewById(R.id.tv_mileage)
        tvHours = findViewById(R.id.tv_hours)
        tv_model = findViewById(R.id.tv_Model)
        tv_vin = findViewById(R.id.tv_vin)
        //        tv_line_hint = findViewById(R.id.tv_line_hint);
        tvStoreName = findViewById(R.id.tv_store_name)
        tvStoreTel = findViewById(R.id.tv_store_tel_num)
        tvStoreEmail = findViewById(R.id.tv_store_email)
        tvStoreAddress = findViewById(R.id.tv_store_address)

        // btn_edit_cancel = findViewById(R.id.btn_edit_cancel)
        // btn_edit_save = findViewById(R.id.btn_edit_save)
        ll_edit_bottom = findViewById(R.id.ll_edit_bottom)
        btn_confirm = findViewById(R.id.btn_confirm)
        btn_cancel = findViewById(R.id.btn_cancel)
        ll_read_only_btn = findViewById(R.id.ll_read_only_btn)
        spinner_repair_state = findViewById(R.id.spinner_repair_state)
        group_edit_mode_base_info = findViewById(R.id.group_edit_mode_base_info)
        et_model = findViewById(R.id.et_model)
        et_year = findViewById(R.id.et_year)
        et_mileage = findViewById(R.id.et_mileage)
        et_hours = findViewById(R.id.et_hours)
        et_vin = findViewById(R.id.et_vin)
        et_store_name = findViewById(R.id.et_store_name)
        et_store_tel_num = findViewById(R.id.et_store_tel_num)
        et_store_email = findViewById(R.id.et_store_email)
        et_store_address = findViewById(R.id.et_store_address)

        systemKeyboardETs = mutableListOf(
            et_model,
            et_year,
            et_mileage,
            et_hours,
            et_store_name,
            et_store_tel_num,
            et_store_email,
            et_store_address
        )

        findViewById<View>(R.id.iv_edit_mode).setOnClickListener {
            if (isFastClick) {
                return@setOnClickListener
            }
            changeEditMode(true)
        }
        btn_cancel?.setOnClickListener {
            mCancelTipPop?.show()
        }
        // 保存按钮
        btn_confirm?.setOnClickListener {
            hideSoftKeyboard()
            hideCustomKeyBoard()
            diagHistoryBean.apply {
                modelName = et_model?.text?.toString()?.takeIf { it.isNotBlank() } ?: modelName
                yearName = et_year?.text?.toString()?.takeIf { it.isNotBlank() } ?: yearName
                mileageVaule = et_mileage?.text?.toString()?.takeIf { it.isNotBlank() } ?: mileageVaule
                HoursVal = et_hours?.text?.toString()?.takeIf { it.isNotBlank() } ?: HoursVal
                vinName = et_vin?.text?.toString()?.takeIf { it.isNotBlank() } ?: vinName

                storeName = et_store_name?.text?.toString()?.takeIf { it.isNotBlank() } ?: storeName
                storePhone = et_store_tel_num?.text?.toString()?.takeIf { it.isNotBlank() } ?: storePhone
                storeEmail = et_store_email?.text?.toString()?.takeIf { it.isNotBlank() } ?: storeEmail
                storeAddress = et_store_address?.text?.toString()?.takeIf { it.isNotBlank() } ?: storeAddress

                ownerName = et_owner?.text?.toString()?.takeIf { it.isNotBlank() } ?: ownerName
                postalCode = et_postal_code?.text?.toString()?.takeIf { it.isNotBlank() } ?: postalCode
                phoneNumber = et_phone?.text?.toString()?.takeIf { it.isNotBlank() } ?: phoneNumber
                vehiclePlate = et_vehicle_plate?.text?.toString()?.takeIf { it.isNotBlank() } ?: vehiclePlate

                Remark = et_repair_remark?.text?.toString()?.takeIf { it.isNotBlank() } ?: Remark
                MaintenanceStaff = et_maintenance_staff?.text?.toString()?.takeIf { it.isNotBlank() } ?: MaintenanceStaff

                reportName = "$brandName $yearName $modelName"
                tv_title?.text = reportName

                val isYearChanged = yearName != originYear
                val isStateChanged = MaintainState != originRepairState
                if (isYearChanged || isStateChanged) {
                    renameFile(filePath, newYear = yearName, newMaintainState = MaintainState)
                    originYear = yearName
                    originRepairState = MaintainState
                }
                picPaths.clear()
                picPaths.addAll(picPathList)
            }

            changeEditMode(false)
            updateJsonFile()
        }

        findViewById<View>(R.id.iv_save).setOnClickListener { v: View? ->
            if (isFastClick) {
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(mPdfPath)) {
                savePdf()
            } else {
                ToastUtil.showToast(
                    mContext,
                    getString(R.string.sh_diag_report_pad_save_success),
                    Toast.LENGTH_SHORT
                )
            }
        }
        val iv_share = findViewById<ImageView>(R.id.iv_share)
        findViewById<ImageView>(R.id.iv_share).setOnClickListener { v: View? ->
            if (isFastClick) {
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(mPdfPath)) {
                ToastUtil.showToast(
                    mContext,
                    mContext!!.getString(R.string.sh_diag_report_please_save_first),
                    Toast.LENGTH_SHORT
                )
            } else {
                mSharePop!!.showAsDropDown(iv_share, dp2px(dropXOffsetShare.toFloat()), 10)
            }
        }
        findViewById<View>(R.id.iv_close).setOnClickListener { v: View? ->
            if (isFastClick) {
                return@setOnClickListener
            }
            finish()
        }

        spinner_repair_state?.setOnSpinnerItemSelectedListener(object : CustomSpinnerView.OnSpinnerItemSelectedListener {
            override fun onItemSelected(pos: Int, text: String, flag: Int) {
                diagHistoryBean.MaintainState = pos
            }
        })
        et_hours?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                hideSoftKeyboard()
                et_vin?.requestFocus()
                showCustomKeyboard(et_vin!!)
                true
            } else {
                false
            }
        }
        et_vin?.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                hideSoftKeyboard()
                showCustomKeyboard(et_vin!!)
            }
            false
        }

        initCancelTipDialog()
        initPictureSelPop()
        initSharePop()
        initEmailPop()
        initTwoCodePop()
    }

    @SuppressLint("SetTextI18n")
    private fun initData() {
        obdstarKeyboard = ObdstarKeyboard(this, root_view)
        obdstarKeyboard.initKeys('V')
        gson = Gson()
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("yyyy-MM-dd HH-mm-sss", Locale.US)
        toDay = sdf.format(Date())
        factory = LayoutInflater.from(this)
        val data = FileUtils.readFromFile(filePath)
        diagHistoryBean = gson!!.fromJson(data, object : TypeToken<DiagHistoryBean?>() {}.type)

        diagHistoryBean.apply {
            storeName = storeName.ifBlank { mStoreInfo?.storeName ?: "" }
            storePhone = storePhone.ifBlank { mStoreInfo?.phoneNum ?: "" }
            storeEmail = storeEmail.ifBlank { mStoreInfo?.email ?: "" }
            storeAddress = storeAddress.ifBlank { mStoreInfo?.posAddress ?: "" }

            picPathList.clear()
            picPathList.addAll(diagHistoryBean.picPaths)
            originYear = yearName
            originRepairState = MaintainState
        }

        repairStatesMap = mapOf(
            0 to resources.getString(com.obdstar.common.ui.R.string.sh_diag_report_before_repair),
            1 to resources.getString(com.obdstar.common.ui.R.string.sh_diag_report_after_repair)
        )
        spinner_repair_state?.setDropList(
            repairStatesMap.values.toList(),
            originRepairState
        )


        pdfHeights.add(0)
        con_title!!.post {
            pdfHeight += con_title!!.height
            a4Height = (con_title!!.width * 1.41).toInt()
        }
        con_car_information!!.post { pdfHeight += con_car_information!!.height }
        tv_title!!.text = diagHistoryBean.title
        tv_title!!.text = diagHistoryBean.reportName
        tv_time!!.text = getString(R.string.sh_diag_diagnosis_time) + diagHistoryBean.createTime
        if (!Constants.isDP83Device && !Constants.isMD75CANDODevice && !Constants.isF8SMDevice) {
            tv_brand!!.text = mBrandName
        }
        tv_brand_name!!.text = MessageFormat.format(
            getString(R.string.sh_diag_brand) + "：{0}",
            if (isEditMode) "" else diagHistoryBean.brandName
        )
        updateBaseInfo()

        // 添加车主信息
        addOwnerInfo(ll_root)
        // 添加系統故障码数据
        val sysItems = diagHistoryBean.sysItems
        if (!sysItems.isNullOrEmpty()) {
            addSystemData(sysItems, ll_root)
        }
        // 添加数据流数据
        if (diagHistoryBean.DtcItems != null && !diagHistoryBean.DtcItems!!.isEmpty()) {
            addDataStreamData(diagHistoryBean.DtcItems!!, ll_root)
        }

        // 图片
        val picLayout = factory.inflate(R.layout.sh_diag_history_item_picture, null)
        val posView = picLayout.findViewById<View>(R.id.iv_pos_pop)
        rl_add_pic = picLayout.findViewById(R.id.rl_add_pic)
        rl_add_pic?.setOnClickListener {
            picturePop?.showAsDropDown(posView)
        }
        ll_root.addView(picLayout)
        calculatHeight(picLayout)

        adapter = DiagHistoryPhotoAdapter(picPathList)
        adapter?.setDeleteListener { pos ->
            updateEmptyPhotoVisibility()
            updateAddPhotoBtnVisibility()
        }
        val addPicLayout = factory.inflate(R.layout.sh_diag_history_item_add_picture, null)
        v_empty_pic = addPicLayout.findViewById(R.id.v_empty_pic)
        rv_add_photos = addPicLayout.findViewById(R.id.rv_add_photos)
        updatePicInfo()
        rv_add_photos?.layoutManager = GridLayoutManager(this, 2)
        rv_add_photos?.adapter = adapter

        ll_root.addView(addPicLayout)
        calculatHeight(addPicLayout)

        // 备注
        val inflate_detail_title2 = factory.inflate(R.layout.sh_diag_report_item_title, null)
        val tv_item_title2 =
            inflate_detail_title2.findViewById<AppCompatTextView>(R.id.tv_item_title)
        tv_item_title2.setText(R.string.sh_diag_note)
        ll_root.addView(inflate_detail_title2) // 添加故障码详情标题
        calculatHeight(inflate_detail_title2)

        ////添加备注
        val inflate_detail__descibe =
            factory.inflate(R.layout.sh_diag_history_item_repair_remark, ll_root, false)
        tv_repair_remark = inflate_detail__descibe.findViewById(R.id.tv_repair_remark)
        et_repair_remark = inflate_detail__descibe.findViewById(R.id.et_repair_remark)
        systemKeyboardETs.add(et_repair_remark)
        updateRepairRemark()
        ll_root.addView(inflate_detail__descibe)
        calculatHeight(inflate_detail__descibe)

        // 维修人员
        val inflate_maintenance_staff =
            factory.inflate(R.layout.sh_diag_history_item_maintenance_staff, ll_root, false)
        val key_maintenance_staff =
            inflate_maintenance_staff.findViewById<AppCompatTextView>(R.id.key_maintenance_staff)
        tv_maintenance_staff =
            inflate_maintenance_staff.findViewById(R.id.tv_maintenance_staff)
        et_maintenance_staff =
            inflate_maintenance_staff.findViewById(R.id.et_maintenance_staff)
        key_maintenance_staff.text =
            resources.getString(R.string.sh_diag_report_maintenance_staff) + "："
        updateMaintenanceStaff()
        ll_root.addView(inflate_maintenance_staff)
        calculatHeight(inflate_maintenance_staff)
        systemKeyboardETs.add(et_maintenance_staff)

        // 免责声明
        val inflate_disclaimer = factory.inflate(R.layout.sh_diag_report_item_title, null)
        val tv_item_disclaimer =
            inflate_disclaimer.findViewById<AppCompatTextView>(R.id.tv_item_title)
        tv_item_disclaimer.setText(R.string.sh_diag_disclaimer)
        ll_root.addView(inflate_disclaimer) // 添加故障码详情标题
        calculatHeight(inflate_disclaimer)
        // 免责声明内容
        val inflate_disclaimer__descibe =
            factory.inflate(R.layout.sh_diag_item_describe, ll_root, false)
        val tv_disclaimer_describe =
            inflate_disclaimer__descibe.findViewById<AppCompatTextView>(R.id.tv_describe)
        tv_disclaimer_describe.setText(R.string.sh_diag_report_fault_disclaimerinfo)
        ll_root.addView(inflate_disclaimer__descibe)
        calculatHeight(inflate_disclaimer__descibe)

        changeEditMode(false)
        systemKeyboardETs.forEach { et ->
            et?.setOnTouchListener { v, event ->
                if (v.id == R.id.et_repair_remark) {
                    val canScroll = v.canScrollVertically(1) || v.canScrollVertically(-1)
                    if (canScroll) {
                        v.parent.requestDisallowInterceptTouchEvent(true)
                        if (event.action == MotionEvent.ACTION_UP) {
                            v.parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                } else {
                    if (event.action == MotionEvent.ACTION_UP) {
                        mCurrentEdit = et
                        hideCustomKeyBoard()
                    }
                }
                false
            }
        }
    }

    /**
     * 更新json文件
     */
    private fun updateJsonFile() {
        val file = File(filePath)
        file.writeText(gson!!.toJson(diagHistoryBean))
    }

    private fun changeEditMode(isEditMode: Boolean) {
        this.isEditMode = isEditMode
        handleEditModeInfoUI()
        updateBaseInfo()
        updateOwnerInfo()
        updateRepairRemark()
        updateMaintenanceStaff()
        updatePicInfo()
    }

    private fun handleEditModeInfoUI() {
        if (isEditMode) {
            // btn_edit_cancel?.visibility = View.VISIBLE
            // btn_edit_save?.visibility = View.VISIBLE
            ll_edit_bottom?.visibility = View.VISIBLE
            ll_read_only_btn?.visibility = View.GONE
            spinner_repair_state?.visibility = View.VISIBLE
            tv_main_state?.visibility = View.GONE
            group_edit_mode_base_info?.visibility = View.VISIBLE
            group_edit_mode_owner_info?.visibility = View.VISIBLE
            tv_repair_remark?.visibility = View.GONE
            et_repair_remark?.visibility = View.VISIBLE
            tv_maintenance_staff?.visibility = View.GONE
            et_maintenance_staff?.visibility = View.VISIBLE
        } else {
            // btn_edit_cancel?.visibility = View.GONE
            // btn_edit_save?.visibility = View.GONE
            ll_edit_bottom?.visibility = View.GONE
            ll_read_only_btn?.visibility = View.VISIBLE
            spinner_repair_state?.visibility = View.GONE
            tv_main_state?.visibility = View.VISIBLE
            group_edit_mode_base_info?.visibility = View.GONE
            group_edit_mode_owner_info?.visibility = View.GONE
            tv_repair_remark?.visibility = View.VISIBLE
            et_repair_remark?.visibility = View.GONE
            tv_maintenance_staff?.visibility = View.VISIBLE
            et_maintenance_staff?.visibility = View.GONE
        }
        updateAddPhotoBtnVisibility()
    }

    private fun initPictureSelPop() {
        val view = LayoutInflater.from(this).inflate(R.layout.upload_pic_select, null)
        picturePop = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.findViewById<LinearLayout>(R.id.ll_camera).setOnClickListener {
            takePhoto()
        }
        view.findViewById<LinearLayout>(R.id.ll_album).setOnClickListener {
            choosePhoto()
        }
        picturePop?.isFocusable = true
        picturePop?.isOutsideTouchable = true
        picturePop?.setBackgroundDrawable(ColorDrawable())
        // picturePop?.showAsDropDown(v, v.getWidth() + 7, -(v.getHeight()))
    }

    private fun updateBaseInfo() {
        tv_main_state?.text = repairStatesMap[originRepairState] ?: ""
        et_model?.setText(diagHistoryBean.modelName)
        et_year?.setText(diagHistoryBean.yearName)
        et_mileage?.setText(diagHistoryBean.mileageVaule)
        et_hours?.setText(diagHistoryBean.HoursVal)
        et_vin?.setText(diagHistoryBean.vinName)
        et_store_name?.setText(diagHistoryBean.storeName)
        et_store_tel_num?.setText(diagHistoryBean.storePhone)
        et_store_email?.setText(diagHistoryBean.storeEmail)
        et_store_address?.setText(diagHistoryBean.storeAddress)

        tv_year!!.text = MessageFormat.format(
            getString(R.string.sh_diag_report_year) + "：{0}",
            if (isEditMode) "" else diagHistoryBean.yearName
        )
        tv_mileage?.text = String.format(
            Locale.ENGLISH,
            "%s : %s",
            getString(R.string.sh_diag_report_milage),
            if (isEditMode) "" else diagHistoryBean.mileageVaule
        )
        tvHours!!.text = MessageFormat.format(
            getString(R.string.sh_diag_report_hour) + "：{0}",
            if (isEditMode) "" else diagHistoryBean.HoursVal
        )
        tv_model!!.text = MessageFormat.format(
            getString(R.string.sh_diag_report_car_model) + "：{0}",
            if (isEditMode) "" else diagHistoryBean.modelName
        )
        tv_vin!!.text = MessageFormat.format(
            getString(R.string.sh_diag_report_vin) + "：{0}",
            if (isEditMode) "" else diagHistoryBean.vinName
        )
        tvStoreName!!.text = MessageFormat.format(
            getString(com.obdstar.common.ui.R.string.station_name) + "：{0}",
            if (isEditMode) "" else diagHistoryBean.storeName
        )
        tvStoreTel!!.text = MessageFormat.format(
            getString(com.obdstar.common.ui.R.string.phone) + "：{0}",
            if (isEditMode) "" else diagHistoryBean.storePhone
        )
        tvStoreEmail!!.text = MessageFormat.format(
            getString(R.string.diag_report_email) + "：{0}",
            if (isEditMode) "" else diagHistoryBean.storeEmail
        )
        tvStoreAddress!!.text = MessageFormat.format(
            getString(R.string.diag_report_station_address) + "：{0}",
            if (isEditMode) "" else diagHistoryBean.storeAddress
        )
    }

    /**
     * 车主信息
     */
    private fun addOwnerInfo(parent: ViewGroup) {
        val carOwnerTitleLayout = factory.inflate(R.layout.sh_diag_report_item_title, null)
        val tvOverview: AppCompatTextView = carOwnerTitleLayout.findViewById(R.id.tv_item_title)
        tvOverview.text = getString(R.string.diag_report_owner_info)
        parent.addView(carOwnerTitleLayout)
        calculatHeight(carOwnerTitleLayout)
        val carOwnerLayout = factory.inflate(R.layout.sh_diag_history_item_car_owner_info, null)
        tvOwner = carOwnerLayout.findViewById(R.id.tv_owner)
        tvPhone = carOwnerLayout.findViewById(R.id.tv_phone)
        tvPostalCode = carOwnerLayout.findViewById(R.id.tv_postal_code)
        tvVehiclePlate = carOwnerLayout.findViewById(R.id.tv_vehicle_plate)
        group_edit_mode_owner_info = carOwnerLayout.findViewById(R.id.group_edit_mode_owner_info)
        et_owner = carOwnerLayout.findViewById(R.id.et_owner)
        et_phone = carOwnerLayout.findViewById(R.id.et_phone)
        et_postal_code = carOwnerLayout.findViewById(R.id.et_postal_code)
        et_vehicle_plate = carOwnerLayout.findViewById(R.id.et_vehicle_plate)

        systemKeyboardETs.addAll(listOf(et_owner, et_phone, et_postal_code, et_vehicle_plate))

        updateOwnerInfo()
        parent.addView(carOwnerLayout)
        calculatHeight(carOwnerLayout)
    }

    private fun updateOwnerInfo() {
        et_owner?.setText(diagHistoryBean.ownerName)
        et_postal_code?.setText(diagHistoryBean.postalCode)
        et_phone?.setText(diagHistoryBean.phoneNumber)
        et_vehicle_plate?.setText(diagHistoryBean.vehiclePlate)

        // todo 从编辑模式保存后，需要把编辑框数据 存到DiagHistoryBean对象中
        tvOwner?.text = String.format(
            Locale.ENGLISH,
            "%s: %s",
            getString(R.string.diag_report_name),
            if (isEditMode) "" else diagHistoryBean.ownerName
        )
        tvPostalCode?.text = String.format(
            Locale.ENGLISH,
            "%s: %s",
            getString(R.string.diag_report_postal_code),
            if (isEditMode) "" else diagHistoryBean.postalCode
        )
        tvPhone?.text = String.format(
            Locale.ENGLISH,
            "%s: %s",
            getString(R.string.phone_num),
            if (isEditMode) "" else diagHistoryBean.phoneNumber
        )
        tvVehiclePlate?.text = String.format(
            Locale.ENGLISH,
            "%s: %s",
            getString(R.string.diag_report_vehicle_plate),
            if (isEditMode) "" else diagHistoryBean.vehiclePlate
        )
    }

    private fun updateRepairRemark() {
        et_repair_remark?.setText(diagHistoryBean.Remark)
        tv_repair_remark?.text = if (isEditMode) "" else diagHistoryBean.Remark
    }

    private fun updateMaintenanceStaff() {
        et_maintenance_staff?.setText(diagHistoryBean.MaintenanceStaff)
        tv_maintenance_staff?.text = if (isEditMode) "" else diagHistoryBean.MaintenanceStaff
    }

    private fun updateEmptyPhotoVisibility() {
        v_empty_pic?.visibility = if (picPathList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateAddPhotoBtnVisibility(): Boolean {
        if (picPathList.size < MAX_PHOTO && isEditMode) {
            rl_add_pic?.visibility = View.VISIBLE
            return true
        } else {
            rl_add_pic?.visibility = View.GONE
            return false
        }
    }

    private fun updatePicInfo() {
        adapter?.enableDelete(isEditMode)
        if (isEditMode) {
            picPathList.clear()
            picPathList.addAll(diagHistoryBean.picPaths)
            adapter?.setPhoto(picPathList)
        }
        updateEmptyPhotoVisibility()
    }


    /**
     * 系统故障码数据
     */
    private fun addSystemData(sysItems: List<DiagHistoryBean.SysItems>, parent: ViewGroup) {
        val systemMap = LinkedHashMap<String, MutableList<DiagHistoryBean.SysItems>>() // 所有的系统
        val systemFaultsMap =
            LinkedHashMap<String, MutableList<DiagHistoryBean.SysItems>>() // 有故障的系统
        var failuresNum = 0 // 故障总数
        for (item in sysItems) {
            if (!TextUtils.isEmpty(item.StateText) || item.state == 1) { // 有故障的系统
                failuresNum++
                if (!systemFaultsMap.containsKey(item.sysName)) {
                    systemFaultsMap[item.sysName] = ArrayList()
                }
                systemFaultsMap[item.sysName]!!.add(item)
            }
            if (!systemMap.containsKey(item.sysName)) {
                systemMap[item.sysName] = ArrayList()
            }
            systemMap[item.sysName]!!.add(item)
        }
        // 系统数
        val systemNum = systemMap.size
        // 有故障系统数
        val systemFailuresNum = systemFaultsMap.size

        // 适配2.0 Summarize字段为空则拼接文本 否则就使用Summarize字段
        val overviewStr = if (TextUtils.isEmpty(diagHistoryBean.Summarize)) MessageFormat.format(
            getString(
                R.string.sh_diag_report_statistics
            ), systemNum, systemFailuresNum, failuresNum
        ) else diagHistoryBean.Summarize

        // 概述
        val overviewLayout = factory.inflate(R.layout.sh_diag_report_item_title, null)
        val tvOverview = overviewLayout.findViewById<AppCompatTextView>(R.id.tv_item_title)
        tvOverview.setText(R.string.sh_diag_report_overview)
        parent.addView(overviewLayout)
        calculatHeight(overviewLayout)

        // 概述内容
        val overviewContentLayout = factory.inflate(R.layout.sh_diag_item_describe, null)
        val tv_describe = overviewContentLayout.findViewById<AppCompatTextView>(R.id.tv_describe)
        tv_describe.text = overviewStr
        parent.addView(overviewContentLayout)
        calculatHeight(overviewContentLayout)

        // 系统状态报告
        val inflate_detail_title = factory.inflate(R.layout.sh_diag_report_item_title, null)
        val tv_item_title = inflate_detail_title.findViewById<AppCompatTextView>(R.id.tv_item_title)
        tv_item_title.setText(R.string.sh_diag_report_system_status)
        parent.addView(inflate_detail_title)
        calculatHeight(inflate_detail_title)

        // 系统状态报告
        val inflateState_hint = factory.inflate(R.layout.sh_diag_item_state, null)
        val tv_name_hint = inflateState_hint.findViewById<TextView>(R.id.tv_name)
        val tv_number_hint = inflateState_hint.findViewById<TextView>(R.id.tv_number)
        val tv_state_hint = inflateState_hint.findViewById<TextView>(R.id.tv_state)
        tv_name_hint.setText(R.string.sh_diag_system)
        tv_number_hint.setText(R.string.sh_diag_report_number_fault_codes)
        tv_state_hint.setText(R.string.sh_diag_state)
        parent.addView(inflateState_hint)
        calculatHeight(inflateState_hint)
        var index = 0
        val entries: Set<Map.Entry<String, List<DiagHistoryBean.SysItems>>> = systemMap.entries
        for ((key, value) in entries) {
            var failures = 0
            for (items in value) {
                if (!TextUtils.isEmpty(items.StateText) || items.state == 1) {
                    failures++
                }
            }
            val inflateState = factory.inflate(R.layout.sh_diag_item_state, null)
            val tv_name = inflateState.findViewById<TextView>(R.id.tv_name)
            val tv_number = inflateState.findViewById<TextView>(R.id.tv_number)
            val tv_state = inflateState.findViewById<TextView>(R.id.tv_state)
            tv_name.text = key
            tv_number.text = MessageFormat.format("{0}", failures)
            tv_state.setText(if (failures == 0) R.string.normal else R.string.sh_diag_report_fault)
            parent.addView(inflateState)
            calculatHeight(inflateState)
        }

        // 添加故障码详情标题
        val dtc_detail_layout = factory.inflate(R.layout.sh_diag_report_item_title, null)
        val tvTitle = dtc_detail_layout.findViewById<AppCompatTextView>(R.id.tv_item_title)
        tvTitle.setText(R.string.sh_diag_report_fault_details)
        parent.addView(dtc_detail_layout)
        calculatHeight(dtc_detail_layout)
        val entries1: Set<Map.Entry<String, List<DiagHistoryBean.SysItems>>> =
            systemFaultsMap.entries
        for ((key, value) in entries1) {
            // 故障码详情小分类
            val inflate_detail_title2 =
                factory.inflate(R.layout.sh_diag_report_item_detail_title, null)
            val tv_item_title2 =
                inflate_detail_title2.findViewById<AppCompatTextView>(R.id.tv_item_title)
            tv_item_title2.text = MessageFormat.format("{0}.{1}", ++index, key)
            parent.addView(inflate_detail_title2) // 添加故障码详情标题
            calculatHeight(inflate_detail_title2)

            // 故障码详情小分类标题
            val inflate1 = factory.inflate(R.layout.sh_diag_item_details, null)
            val tv_code1 = inflate1.findViewById<TextView>(R.id.tv_code)
            val tv_describe1 = inflate1.findViewById<TextView>(R.id.tv_describe)
            val tv_state1 = inflate1.findViewById<TextView>(R.id.tv_state)
            tv_code1.setText(R.string.sh_diag_trouble_code)
            tv_describe1.setText(R.string.sh_diag_report_fault_describe)
            tv_state1.setText(R.string.sh_diag_state)
            parent.addView(inflate1)
            calculatHeight(inflate1)

            // 具体故障
            for (items in value) {
                val inflate_item_details = factory.inflate(R.layout.sh_diag_item_details, null)
                val tv_code_item_details = inflate_item_details.findViewById<TextView>(R.id.tv_code)
                val tv_describe_item_details =
                    inflate_item_details.findViewById<TextView>(R.id.tv_describe)
                val tv_state_item_details =
                    inflate_item_details.findViewById<TextView>(R.id.tv_state)
                tv_code_item_details.text = items.faultCode
                tv_describe_item_details.text = items.description
                if (TextUtils.isEmpty(items.StateText)) {
                    tv_state_item_details.text = ""
                } else {
                    tv_state_item_details.text = items.StateText
                }
                parent.addView(inflate_item_details)
                calculatHeight(inflate_item_details)
            }
        }
    }

    /**
     * 数据流数据
     */
    private fun addDataStreamData(dtcItems: List<DiagHistoryBean.DtcItem>, parent: ViewGroup?) {
        // 数据流详情
        val dsDetailTitleLayout = factory.inflate(R.layout.sh_diag_report_item_title, null)
        val tvTitle = dsDetailTitleLayout.findViewById<AppCompatTextView>(R.id.tv_item_title)
        tvTitle.setText(R.string.diag_report_ds_detail)
        parent!!.addView(dsDetailTitleLayout)
        calculatHeight(dsDetailTitleLayout)

        // 数据流详情标题
        val inflateState_hint = factory.inflate(R.layout.diag_report_ds_item, null)
        val tvName = inflateState_hint.findViewById<TextView>(R.id.tv_name)
        val tvValue = inflateState_hint.findViewById<TextView>(R.id.tv_value)
        val tvRange = inflateState_hint.findViewById<TextView>(R.id.tv_range)
        val tvUnit = inflateState_hint.findViewById<TextView>(R.id.tv_unit)
        tvName.typeface = Typeface.DEFAULT_BOLD
        tvValue.typeface = Typeface.DEFAULT_BOLD
        tvRange.typeface = Typeface.DEFAULT_BOLD
        tvUnit.typeface = Typeface.DEFAULT_BOLD
        tvName.setText(R.string.name)
        tvValue.setText(R.string.value)
        tvRange.setText(R.string.range)
        tvUnit.setText(R.string.unit)
        parent.addView(inflateState_hint)
        calculatHeight(inflateState_hint)

        // 数据流数据
        for (dtcItem in dtcItems) {
            val dsItemView = factory.inflate(R.layout.diag_report_ds_item, null)
            val tvName_ = dsItemView.findViewById<TextView>(R.id.tv_name)
            val tvValue_ = dsItemView.findViewById<TextView>(R.id.tv_value)
            val tvRange_ = dsItemView.findViewById<TextView>(R.id.tv_range)
            val tvUnit_ = dsItemView.findViewById<TextView>(R.id.tv_unit)
            val ivLight = dsItemView.findViewById<ImageView>(R.id.iv_light)
            tvName_.text = dtcItem.Name
            if (icons.containsKey(dtcItem.Val)) {
                tvValue_.visibility = View.GONE
                ivLight.visibility = View.VISIBLE
                ivLight.setImageDrawable(icons[dtcItem.Val])
            } else {
                tvValue_.text = dtcItem.Val
                tvValue_.visibility = View.VISIBLE
                ivLight.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(dtcItem.Min) && !TextUtils.isEmpty(dtcItem.Max)) {
                tvRange_.text = MessageFormat.format("[{0}...{1}]", dtcItem.Min, dtcItem.Max)
            }
            tvUnit_.text = dtcItem.Unit
            parent.addView(dsItemView)
            calculatHeight(dsItemView)
        }
    }

    private fun initCancelTipDialog() {
        if (mCancelTipPop == null) {
            val view =
                LayoutInflater.from(this).inflate(com.obdstar.common.ui.R.layout.dialog_card, null)
            mCancelTipPop = Dialog((this), com.obdstar.common.ui.R.style.BaseDialogTheme)
            mCancelTipPop?.setContentView(view)
            mCancelTipPop?.setCanceledOnTouchOutside(false)
            mCancelTipPop?.setOnDismissListener { setWindowAlpha(1f) }

            val tvTitle = view.findViewById<TextView>(R.id.tv_title)
            val tvSubtitle = view.findViewById<TextView>(R.id.tv_subtitle)
            val tvMessage = view.findViewById<TextView>(R.id.tv_message)
            tvTitle.text = resources.getString(com.obdstar.common.ui.R.string.tips)
            tvSubtitle.text = ""
            tvMessage.text = resources.getString(com.obdstar.common.ui.R.string.exit_not_save)
            view.findViewById<Button>(com.obdstar.common.ui.R.id.btn_negative).setOnClickListener {
                if (mCancelTipPop?.isShowing == true) {
                    mCancelTipPop?.dismiss()
                }
            }
            view.findViewById<Button>(com.obdstar.common.ui.R.id.btn_position).setOnClickListener {
                picPathList.clear()
                picPathList.addAll(diagHistoryBean.picPaths)
                adapter?.setPhoto(picPathList)
                mCancelTipPop?.dismiss()
                changeEditMode(false)
            }
            // tv_report3_vin = view.findViewById(R.id.tv_report3_vin)
            // ll_select_report = view.findViewById(R.id.ll_select_report)
            // tv_report3 = view.findViewById(R.id.tv_report3)
            // ll_new_report = view.findViewById(R.id.ll_new_report)
            // view.findViewById<View>(R.id.ll_select_report).setOnClickListener(this)
            // view.findViewById<View>(R.id.btn_cancel3).setOnClickListener(this)
            // view.findViewById<View>(R.id.btn_ok3).setOnClickListener(this)
            // ll_new_report.setOnClickListener(this)
            // ib_one_key_update_select = view.findViewById(R.id.ib_one_key_update_select)
        }
    }

    // 分享弹窗
    private fun initSharePop() {
        val view = LayoutInflater.from(this).inflate(R.layout.sh_diag_report_pop_share, null)
        mSharePop = PopupWindow(
            view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.findViewById<View>(R.id.btn_email).setOnClickListener { v: View? ->
            mSharePop!!.dismiss()
            if (mEmailPop?.isShowing == false) {
                setWindowAlpha(0.5f)
                mEmailPop?.show()
            }
        }
        view.findViewById<View>(R.id.btn_two_code).setOnClickListener { v: View? ->
            uploadPdfFile(File(mPdfPath), "", false)
            mSharePop!!.dismiss()
        }
        mSharePop!!.isOutsideTouchable = true
        mSharePop!!.setBackgroundDrawable(ColorDrawable())
    }

    // 邮箱弹窗
    private fun initEmailPop() {
        val view = LayoutInflater.from(this).inflate(R.layout.sh_diag_report_emali, null)
        mEmailPop = Dialog(this, com.obdstar.common.ui.R.style.BaseDialogTheme)
        mEmailPop?.setCancelable(false)
        mEmailPop?.setCanceledOnTouchOutside(false)
        mEmailPop?.setContentView(view)
        mEmailPop?.setOnDismissListener { setWindowAlpha(1f) }
        val et_email = view.findViewById<EditText>(R.id.et_email)
        view.findViewById<View>(R.id.btn_cancel).setOnClickListener { v: View? ->
            if (mEmailPop?.isShowing == true) {
                mEmailPop?.dismiss()
            }
        }
        view.findViewById<View>(R.id.btn_send).setOnClickListener { v: View? ->
            val email = et_email.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email) || !isEmail(email)) {
                ToastUtil.showToast(
                    mContext,
                    com.obdstar.common.ui.R.string.invalid_email,
                    Toast.LENGTH_SHORT
                )
                return@setOnClickListener
            }
            if (mEmailPop?.isShowing == true) {
                mEmailPop?.dismiss()
                uploadPdfFile(File(mPdfPath), et_email.text.toString().trim { it <= ' ' }, true)
            }
        }
    }

    // 二维码弹窗
    private fun initTwoCodePop() {
        val view = LayoutInflater.from(this).inflate(R.layout.sh_diag_report_two_code, null)
        mTwoCodePop = PopupWindow(
            view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mTwoCodePop!!.isOutsideTouchable = true
        mTwoCodePop!!.setBackgroundDrawable(ColorDrawable())
        mTwoCodePop!!.setOnDismissListener { setWindowAlpha(1f) }
        view.findViewById<View>(R.id.iv_close).setOnClickListener { v: View? ->
            if (mTwoCodePop != null && mTwoCodePop!!.isShowing) {
                mTwoCodePop!!.dismiss()
            }
        }
        val pb_loading = view.findViewById<ProgressBar>(R.id.pb_loading)
        pb_loading.visibility = View.GONE
        iv_qrcode = view.findViewById(R.id.iv_qrcode)
    }

    /**
     * 修改文件名
     * @param oldPath 原始文件路径
     * @param newYear 新的年款
     * @param newMaintainState 新的维修状态
     */
    private fun renameFile(oldPath: String, newYear: String? = null, newMaintainState: Int? = null) {
        val oldFile = File(oldPath)
        if (!oldFile.exists() || oldFile.parent == null) return

        val fileName = oldFile.name
        // 正则拆解：(模块名)年款_品牌_时间_故障码数量_维修状态.json
        // $1: (模块名)
        // $2: 年款
        // $3: _品牌_时间_故障码数量_
        // $4: 维修状态
        val regex = """^(\(.*\))(.*)(_.*_.*_.*_)(.*)(\.json)$""".toRegex()
        val matchResult = regex.find(fileName) ?: return

        Log.d(TAG, "renameFile: 重命名文件，新年款：$newYear, 新维修状态：$newMaintainState")

        val (module, year, middle, status, ext) = matchResult.destructured

        val finalYear = newYear ?: year
        val finalStatus = newMaintainState ?: status

        val newFileName = "$module$finalYear$middle$finalStatus$ext"

        if (newFileName == fileName) return

        val newFile = File(oldFile.parent, newFileName)
        try {
            if (newFile.exists() && newFile.absolutePath != oldFile.absolutePath) {
                if (!newFile.delete()) {
                    Log.w(TAG, "renameFile: 目标文件已存在且无法删除: $newFileName")
                    return
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.move(
                    oldFile.toPath(),
                    newFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            } else {
                @Suppress("DEPRECATION")
                if (!oldFile.renameTo(newFile)) {
                    oldFile.copyTo(newFile, overwrite = true)
                    if (!oldFile.delete()) {
                        Log.e(TAG, "renameFile: 复制后无法删除原文件: $oldPath")
                    }
                }
            }
            // 部分设备上 renameTo 返回 true 后旧路径仍可能残留，或存在双文件
            if (oldFile.exists() && newFile.exists() && oldFile.absolutePath != newFile.absolutePath) {
                if (!oldFile.delete()) {
                    Log.w(TAG, "renameFile: 仍无法删除原路径: $oldPath")
                }
            }
            Log.i(TAG, "重命名完成：$newFileName")
            if (oldPath == filePath && newFile.exists()) {
                filePath = newFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e(TAG, "重命名失败: $oldPath", e)
        }
    }

    private fun hideCustomKeyBoard() {
        if (::obdstarKeyboard.isInitialized && obdstarKeyboard.visibility == View.VISIBLE) {
            obdstarKeyboard.hideKeyboard()
        }
    }

    private fun hideSoftKeyboard() {
        val imm: InputMethodManager? = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        val root: View? = window.peekDecorView()
        if (null != root) {
            imm?.hideSoftInputFromWindow(mCurrentEdit?.windowToken, 0)
        }
    }

    private fun showCustomKeyboard(edit: EditText) {
        hideSoftKeyboard()
        obdstarKeyboard.editText = edit
        edit.isCursorVisible = true
        edit.showSoftInputOnFocus = false
        if (obdstarKeyboard.visibility != View.VISIBLE) {
            obdstarKeyboard.showKeyboard()
        }
    }

    private fun takePhoto() {
        val intent = Intent()
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE)
        val dirPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.getAbsoluteFile()
            .toString() + "/StoreInfoPhoto" + LanguageUtils.getLanguageAbbr(application.getLanguageType())
        val dir = File(dirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val format = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.ENGLISH)
        mPhotoFile = File(dirPath, "storeInfo" + format.format(Date()) + ".jpeg")
        photoPath = mPhotoFile.getPath()
        if (mPhotoFile.exists()) {
            try {
                mPhotoFile.delete()
                mPhotoFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        var uri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                uri = FileProvider.getUriForFile(
                    this,
                    application.getApplicationID() + ".fileprovider",
                    mPhotoFile
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            uri = Uri.fromFile(mPhotoFile)
        }
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, UPLOAD_PIC_CASE_CALL_CAMERA)
    }

    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, UPLOAD_PIC_CASE_CALL_ALBUM)
    }

    private fun addPhoto(path: String?) {
        if (picturePop?.isShowing == true) {
            picturePop?.dismiss()
        }
        if (path.isNullOrEmpty()) {
            return
        }
        if (updateAddPhotoBtnVisibility()) {
            adapter?.addPhoto(path)
            updateAddPhotoBtnVisibility()
        }
        updateEmptyPhotoVisibility()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPLOAD_PIC_CASE_CALL_ALBUM && resultCode == RESULT_OK && data != null) {// 调用相册
            photoPath = getRealPathFromUri(this, data.data)
            addPhoto(photoPath)
        } else if (requestCode == UPLOAD_PIC_CASE_CALL_CAMERA && resultCode == RESULT_OK) {// 拍照
            addPhoto(mPhotoFile.getPath())
        }
    }

    /**
     * 根据图片的Uri获取图片的绝对路径。@uri 图片的uri
     *
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    fun getRealPathFromUri(context: Context?, uri: Uri?): String? {
        if (context == null || uri == null) {
            return null
        }
        if ("file".equals(uri.getScheme(), ignoreCase = true)) {
            return getRealPathFromUri_Byfile(context, uri)
        } else if ("content".equals(uri.getScheme(), ignoreCase = true)) {
            return getRealPathFromUri_Api11To18(context, uri)
        }
        return getRealPathFromUri_AboveApi19(context, uri)
    }

    /**
     * //适配api11-api18,根据uri获取图片的绝对路径。
     * 针对图片URI格式为Uri:: content://media/external/images/media/1028
     */
    private fun getRealPathFromUri_Api11To18(context: Context?, uri: Uri?): String? {
        var filePath: String? = null
        val projection = arrayOf<String?>(MediaStore.Images.Media.DATA)

        val loader = CursorLoader(context, uri, projection, null, null, null)
        val cursor = loader.loadInBackground()

        if (cursor != null) {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(projection[0])
            if (idx >= 0) {
                filePath = cursor.getString(idx)
            }
            cursor.close()
        }
        return filePath
    }

    /**
     * 适配api19以上,根据uri获取图片的绝对路径
     */
    private fun getRealPathFromUri_AboveApi19(context: Context, uri: Uri?): String? {
        var filePath: String? = null
        var wholeID: String? = null

        wholeID = DocumentsContract.getDocumentId(uri)

        // 使用':'分割
        val id: String? =
            wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

        val projection = arrayOf<String?>(MediaStore.Images.Media.DATA)
        val selection = MediaStore.Images.Media._ID + "=?"
        val selectionArgs = arrayOf<String?>(id)

        val cursor: Cursor = context.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )!!
        val columnIndex = cursor.getColumnIndex(projection[0])

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex)
        }
        cursor.close()
        return filePath
    }

    private fun getRealPathFromUri_Byfile(context: Context?, uri: Uri): String {
        val uri2Str = uri.toString()
        val filePath = uri2Str.substring(uri2Str.indexOf(":") + 3)
        return filePath
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != mDisposable) {
            mDisposable!!.dispose()
        }
        if (null != mDisposable2) {
            mDisposable2!!.dispose()
        }
        System.gc()
        System.runFinalization()
        System.gc()
    }

    private fun setWindowAlpha(alpha: Float) {
        val lp = window.attributes
        lp.alpha = alpha
        window.attributes = lp
    }

    /**
     * 保存pdf到本地，截取scrollview的屏幕
     */
    private fun savePdf() {
        System.gc()
        System.runFinalization()
        System.gc()
        // 修改背景色
        for (i in 0 until scroll_view!!.childCount) scroll_view!!.getChildAt(i).setBackgroundColor(
            Color.WHITE
        )
        Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<String> ->
            val pngParentPath =
                (Constants.APP_ROOT + File.separator + "DATA" + File.separator + Constants.DIAG_HISTORY + File.separator + diagHistoryBean.vinName + File.separator + "pdf")
            var pngPath: String? = null
            val file = File(pngParentPath)
            if (!file.exists()) {
                file.mkdirs()
            }
            // 1、生成长图
            val w = scroll_view!!.width
            var h = 0
            var bitmap: Bitmap?
            // 获取scroiiview实际高度
            for (i in 0 until scroll_view!!.childCount) {
                h += scroll_view!!.getChildAt(i).height
            }
            Log.i("aaa", "开始生成长图")
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            scroll_view!!.draw(canvas)

            // 2、拆分长图
            val split = splitBitmap(bitmap)
            for (i in split.indices) {
                var out: FileOutputStream? = null
                try {
                    pngPath = pngParentPath + File.separator + toDay + "_" + i + ".png"
                    split[i].path = pngPath
                    out = FileOutputStream(pngPath)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                try {
                    if (null != out) {
                        split[i].bitmap!!.compress(Bitmap.CompressFormat.JPEG, 30, out)
                        out.flush()
                        out.close()
                        // 释放内存
                        if (split[i].bitmap != null && !split[i].bitmap!!.isRecycled) {
                            Log.i("aaa", "释放内存")
                            split[i].bitmap!!.recycle()
                            split[i].bitmap = null
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            // 释放内存
            if (bitmap != null && !bitmap.isRecycled) {
                Log.i("aaa", "释放内存2")
                bitmap.recycle()
                bitmap = null
            }
            // 3、生成pdf
            if (!TextUtils.isEmpty(pngPath)) {
                Log.i("aaa", "开始生成pdf")
                val pdf_save_address = pngParentPath + File.separator + toDay + ".pdf"
                var doc: Document? = Document(PageSize.A4, 0f, 0f, 0f, 0f)
                try {
                    PdfWriter.getInstance(doc, FileOutputStream(pdf_save_address)) // 获取PDF书写器
                    doc!!.open() // 打开文档
                    var img: Image // 图片对象
                    // 遍历
                    for (i in split.indices) {
                        // 获取图片
//                img = Image.getInstance(new URL(imgPaths[i]));
                        img = Image.getInstance(split[i].path)
                        img.scaleToFit(Rectangle(PageSize.A4)) // 使图片与A4纸张大小自适应
                        doc.add(img) // 添加到PDF文档
                        doc.newPage() // 下一页，每张图片一页
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    // 关闭文档
                    split.clear()
                    doc!!.close()
                    doc = null
                }
                emitter.onNext(pdf_save_address)
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable) {
                    mDisposable2 = d
                    mProgressBarDialog!!.show()
                }

                override fun onNext(aLong: String) {
                    mProgressBarDialog!!.dismiss()
                    ToastUtil.showToast(
                        mContext, R.string.sh_diag_report_pad_save_success, Toast.LENGTH_SHORT
                    )
                    mPdfPath = aLong
                    for (i in 0 until scroll_view!!.childCount) { // 修改背景色
                        scroll_view!!.getChildAt(i).setBackgroundColor(Color.parseColor("#d8dbde"))
                    }
                }

                override fun onError(e: Throwable) {}
                override fun onComplete() {}
            })
    }

    /**
     * 上传pdf文件
     *
     * @param file
     * @param email
     */
    private fun uploadPdfFile(file: File, email: String, isEmail: Boolean) {
        Observable.create<String?>(ObservableOnSubscribe<String?> { emitter: ObservableEmitter<String?> ->
            try {
//                val fileBody = RequestBody.create(parse.parse("application/pdf"), file)
                val fileBody = RequestBody.create(
                    MediaType.parse("application/pdf"), file
                )
                var md5 = ""
                try {
                    md5 = calculateBase64Md5(file.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val request: Request =
                    Request.Builder().url(BASE_URL + "/api/UploadDrp/" + file.name).addHeader(
                        "Drp-LangCode", LanguageUtils.getLanguageAbbr(
                            mDpApplication.languageType
                        )
                    ) // 语言
                        .addHeader("Content-MD5", md5)
                        .addHeader("Drp-ProSn", mDpApplication.sn) // sn
                        .addHeader("Drp-MailTo", email)
                        .addHeader("Drp-VinCode", diagHistoryBean.vinName) // vin码
                        .put(fileBody).build()
                Log.i("aaa", "email:$email")
                val call = okHttpClient.newCall(request)
                val response = call.execute()
                val string = response.body()!!.string()
                Log.i("aaa", "上传pdf 返回：$string")
                val responseCode = response.code()
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i("aaa", "上传pdf success!")
                    val uploadPdfBean = gson!!.fromJson(string, UploadPdfBean::class.java)
                    emitter.onNext(uploadPdfBean.outPutData!!.drpUrl!!)
                } else {
                    Log.e("aaa", "上传pdf fail!")
                    val uploadPdfBean = gson!!.fromJson(string, UploadPdfBean::class.java)
                    emitter.onError(Throwable(uploadPdfBean.errorMsg + ":" + uploadPdfBean.subCode))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(Throwable(mContext!!.getString(com.obdstar.common.ui.R.string.err_555)))
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable) {
                    mDisposable = d
                    mProgressBarDialog!!.show()
                }

                override fun onNext(aLong: String) {
                    mProgressBarDialog!!.dismiss()
                    mPdfLoadurl = aLong
                    if (isEmail) {
                        ToastUtil.showToast(
                            mContext, R.string.demo_immobiliserDemo_text_16, Toast.LENGTH_SHORT
                        )
                    } else {
                        val bitmap = QRCodeUtil.makeQRCode(mPdfLoadurl, 230)
                        iv_qrcode!!.setImageBitmap(bitmap)
                        if (!mTwoCodePop!!.isShowing) {
                            mTwoCodePop!!.animationStyle = R.style.animTranslate
                            setWindowAlpha(0.5f)
                            mTwoCodePop!!.showAtLocation(window.decorView, Gravity.CENTER, 0, 0)
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    mProgressBarDialog!!.dismiss()
                    ToastUtil.showToast(mContext, e.message, Toast.LENGTH_SHORT)
                }

                override fun onComplete() {}
            })
    }

    /**
     * 拆分长图
     *
     * @param bitmap 长图
     * @return 拆分的多图
     */
    private fun splitBitmap(bitmap: Bitmap?): MutableList<ImagePiece> {
        val xValue = 0
        val pieces: MutableList<ImagePiece> = ArrayList()
        try {
            val pieceWidth = bitmap!!.width
            val height = bitmap.height
            for (i in pdfHeights.indices) {
                val piece = ImagePiece()
                val yValue = pdfHeights[i]
                if (i == pdfHeights.size - 1) {
                    piece.bitmap =
                        Bitmap.createBitmap(bitmap, xValue, yValue, pieceWidth, height - yValue)
                } else {
                    piece.bitmap = Bitmap.createBitmap(
                        bitmap, xValue, yValue, pieceWidth, pdfHeights[i + 1] - pdfHeights[i]
                    )
                }
                pieces.add(piece)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pieces
    }

    // 计算添加布局的高度
    private fun calculatHeight(inflate: View) {
        inflate.post {
            if (pdfHeight + inflate.height > a4Height * pdfHeights.size) {
                val a = pdfHeight
                pdfHeights.add(a)
            }
            pdfHeight += inflate.height
        }
    }

    class ImagePiece {
        var path: String? = null
        var bitmap: Bitmap? = null
    }

    /**
     * calculate md5 for local file and base64 string back
     */
    @Throws(Exception::class)
    fun calculateBase64Md5(filePath: String?): String {
        return toBase64String(calculateMd5(filePath))
    }

    fun toBase64String(binaryData: ByteArray?): String {
        return String(Base64.encode(binaryData, Base64.DEFAULT)).trim { it <= ' ' }
    }

    /**
     * calculate md5 for local file
     */
    @Throws(Exception::class)
    fun calculateMd5(filePath: String?): ByteArray {
        var `is`: FileInputStream? = null
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(10 * 1024)
            `is` = FileInputStream(File(filePath))
            var len: Int
            while (`is`.read(buffer).also { len = it } != -1) {
                digest.update(buffer, 0, len)
            }
            digest.digest()
        } catch (e: Exception) {
            throw Exception("MD5 algorithm not found.")
        } finally {
            `is`?.close()
        }
    }

    fun isEmail(email: String?): Boolean {
        // Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,5}"); //简单匹配
        val m = pattern.matcher(email)
        return m.matches()
    }

    companion object {
        private var BASE_URL: String? = null
        val pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*")
        fun dp2px(dpValue: Float): Int {
            val scale = Resources.getSystem().displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        private var lastClickTime: Long = 0
        val isFastClick: Boolean
            get() {
                val now = SystemClock.uptimeMillis()
                val time = now - lastClickTime
                if (lastClickTime > 0L && time < 1000L) {
                    return true
                }
                lastClickTime = now
                return false
            }
    }
}