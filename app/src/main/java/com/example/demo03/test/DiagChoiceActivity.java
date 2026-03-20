package com.obdstar.x300dp.acitvity;

import static com.obdstar.module.diag.activity.DiagActivity.ACTION_VOLTAGE_CHANGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.obdstar.common.core.Constants;
import com.obdstar.common.core.IObdstarApplication;
import com.obdstar.common.core.bean.KeyValueItem;
import com.obdstar.common.core.config.Category;
import com.obdstar.common.core.config.Configs;
import com.obdstar.common.core.config.Module;
import com.obdstar.common.core.config.db.App;
import com.obdstar.common.core.log.LogUtils;
import com.obdstar.common.core.utils.AppComparator;
import com.obdstar.common.core.utils.DrawableUtils;
import com.obdstar.common.core.utils.LanguageUtils;
import com.obdstar.common.core.utils.Utils;
import com.obdstar.common.ui.view.ObdstarKeyboard;
import com.obdstar.common.ui.view.PgbDlg;
import com.obdstar.common.ui.view.UIndicator;
import com.obdstar.common.ui.view.dialog.SweetAlertDialog;
import com.obdstar.common.utils.BinaryUtil;
import com.obdstar.module.account.center.SizeUtils;
import com.obdstar.module.account.router.LoginNavigationCallbackImpl;
import com.obdstar.module.diag.table.util.DiagUpgradeUtils;
import com.obdstar.module.diag.utils.DiagFileUtils;
import com.obdstar.module.diag.v3.ai.AiQAActivity;
import com.obdstar.x300dp.MainFuncActivity;
import com.obdstar.x300dp.R;
import com.obdstar.x300dp.adapters.CategoryNameAdapter;
import com.obdstar.x300dp.adapters.IconPagerAdapter;
import com.obdstar.x300dp.app.DpApplication;
import com.obdstar.x300dp.fragment.HistoryFragment;
import com.obdstar.x300dp.fragment.IconBaseFragment;
import com.obdstar.x300dp.fragment.IconPagerFragment;
import com.obdstar.x300dp.jni.Core;
import com.obdstar.x300dp.jni.vciupgrade.VciUpgradeManager;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * @author LONG
 */
@Route(path = "/obdstar/diag")
public class DiagChoiceActivity extends RxFragmentActivity implements DiagUpgradeUtils.UpdateListener, Core.VciUpgradeCallback {

    public static final String ALL_FLAG = "ALL";
    public static final String FAVORITES_FLAG = "FAVORITES";
    public static final String HISTORY_FLAG = "HISTORY";

    public static final String VCI_FILE_NAME = "vci.bin";
    public static final String RFID_FILE_NAME = "rfid.bin";
    public static final int REQUEST_CODE = 5;//收藏返回
    public static final int ITEM_TYPE_ADD = -100;
    public static final int ITEM_TYPE_DELETE = -101;
    public static final int ITEM_TYPE_AUTO = -102;      //自动检测
    public static final int ITEM_TYPE_INPUT = -103;     //手动输入
    public static final int ITEM_TYPE_SCAN = -104;       //扫车牌/VIN码

    File iconDir = new File(Constants.APP_ROOT + "/.data/ICON/");//小图标路径
    //存储车标小分类
    public static Map<String, String> logoIcon = new HashMap<>();


    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    private DpApplication mDpApplication;
    private int upgradeCount = 0;
    public TextView mTitle1 = null;
    private SweetAlertDialog dialog = null;
    private AsyncTask<File, Integer, String> upgradeTask;
    ArrayList<File> upgradeFiles = new ArrayList<>();
    public String sn = "";

    File vciFile;
    File mcuFile;
    File rfidFile;

    public Module currModule = null;
    public String currModuleName = "";
    public static String currModuleCode = "";

    public int currentMenuIndex = 0;//记录点击的下标
    public List<KeyValueItem> categoryList = new ArrayList<>();//分类导航栏数据源

    public ConcurrentMap<String, List<App>> categoryMap = new ConcurrentHashMap<>(15);
    public Map<String, Integer> areaMap = new HashMap<>(10);
    public ObdstarKeyboard obdstarKeyboard;

    public EditText etSearch;
    public ImageView ivSearchClear;
    //    private Handler handler = new DiagChoiceHandler();
    private ViewPager mViewPage = null;//右侧viewpager显示
    //    private AreaNameAdapter areaNameAdapter = null;
    private CategoryNameAdapter categoryNameAdapter = null;
    private ViewGroup leftLayout, rightLayout, displayLayout;
    private RadioGroup areaRadioGroup;
    public PgbDlg mProgressBarDialog = null;

    LinearLayout mllDisplay = null;
    ListView lvCategoryList;//左侧导航2栏

    private ViewGroup mEmpty;
    private UIndicator indicator;

    private final PublishSubject<String> mPublishSubject = PublishSubject.create();
    private Observer<List<App>> mObserver;

    List<IconBaseFragment> fragmentList = new ArrayList<>();//右侧单页Fragment
    public String currCategoryCode = "";
    public String currAreaCode = "";
    private IconPagerAdapter pageAdapter; //右侧侧滑adapter
    int currentPageIndex = 0;

    Configs configs = null;

    /**
     * 每页大小
     */
    public static final int PAGE_SIZE = Constants.isDP8000Device ? 15 : (Constants.is5InchesDevice ? 9 : 12);
    AtomicInteger ai = new AtomicInteger(0);
    private String strCode = "";
    private Context mContext;
    private int vciUpgradeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDpApplication = (DpApplication) getApplication();
        if (mDpApplication != null) {
            LanguageUtils.changeLanguage(this, mDpApplication.getLanguageType());
            mDpApplication.putActivity(this.getClass().getName(), this);
        } else {
            finish();
        }
        setContentView(R.layout.activity_diag_choice);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            currModuleCode = bundle.getString("module");
            currModuleName = bundle.getString("name");
            AiQAActivity.Companion.setCurrModuleName(currModuleName);//设置ai功能模块名称
        }

        sn = mDpApplication.getSN();
        if (TextUtils.isEmpty(sn) || sn.length() != 12) {
            try {
                try {
                    ARouter.getInstance().build("/user/login").with(bundle).withString("to", "/obdstar/diag").navigation();
                } catch (Exception var5) {
                    var5.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 获取根布局视图
        final View rootView = findViewById(android.R.id.content);
        mContext = this;
        strCode = getString(R.string.code);
        initUpgradeListener();
        // 添加布局完成监听器
        ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 这里是布局完成后的回调
                LogUtils.i("aaa", "布局初始化完成");
                initUpgrade();
                initView();
                initData(true, 2);
                // 为了避免重复触发，可以在回调中移除监听器
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        mObserver = new Observer<List<App>>() {
            @Override
            public void onSubscribe(Disposable d) {
//                Log.e("Search", "onSubscribe()");
            }

            @Override
            public void onNext(List<App> data) {
//                Log.e("Search", "onNext()");
                Log.d("DiagChoiceActivity", "xialj___onNext: 获取到搜索结果：数量：" + data.size());
                if (!data.isEmpty()) {
//                    show(data, mSelectRadionId == -1 ? true : false);
                    // TODO: xialj 2026-03-13 搜索完成后，到这里
                    show("", data, false);
                } else {
                    mViewPage.setVisibility(View.INVISIBLE);
                    mEmpty.setVisibility(View.VISIBLE);
                }
                if (mProgressBarDialog != null && !isDestroyed()) {
                    mProgressBarDialog.dismiss();
                }
            }

            @Override
            public void onError(Throwable throwable) {
//                Log.e("Search", "onError()");
                if (mProgressBarDialog != null && !isDestroyed()) {
                    mProgressBarDialog.dismiss();
                }
            }

            @Override
            public void onComplete() {
//                Log.e("Search", "onComplete()");
                if (mProgressBarDialog != null && !isDestroyed()) {
                    mProgressBarDialog.dismiss();
                }
            }
        };

        mPublishSubject.debounce(400, TimeUnit.MILLISECONDS).filter(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                if (s.length() > 0 && mProgressBarDialog != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBarDialog.show();
                        }
                    });
                } else if (TextUtils.isEmpty(s)) {
                    return true;
                }
                return s.length() > 0;
            }
        }).switchMap(new Function<String, ObservableSource<List<App>>>() {
            @Override
            public ObservableSource<List<App>> apply(String query) {
                return getSearchObservable(query);
            }
        }).observeOn(AndroidSchedulers.mainThread()).compose(this.bindToLifecycle()).subscribe(mObserver);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        LogUtils.i("aaa", "onNewIntent");
        if (bundle != null) {
            String module = bundle.getString("module", "");
            if (!TextUtils.isEmpty(module)) {
                if ("OR".equals(currModuleCode)) {
                    if (!currModuleCode.equals(module)) {
                        currModuleCode = module;
                        currModuleName = bundle.getString("name");
                        AiQAActivity.Companion.setCurrModuleName(currModuleName);//设置ai功能模块名称
                        if (mTitle1 != null) {
                            mTitle1.setText(currModuleName);
                        }
                        currAreaCode = "";
                        initData(true, 2);
                    }
                } else {
                    currModuleCode = module;
                    currModuleName = bundle.getString("name");
                    AiQAActivity.Companion.setCurrModuleName(currModuleName);//设置ai功能模块名称
                    if (mTitle1 != null) {
                        mTitle1.setText(currModuleName);
                    }
                    currAreaCode = "";
                    initData(true, 2);
                }

            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(App app) {
        LogUtils.i("aaa", "app更新：" + app.name + ":" + app.upgradable);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        LogUtils.i("aaa", "size:" + categoryMap.size());
        List<App> apps = categoryMap.get(app.category);
        if (apps != null && !apps.isEmpty()) {
            for (App app1 : apps) {
                if (app1.code.equals(app.code)) {
                    app1.upgradable = app.upgradable;
                    return;
                }
            }
        } else {
            List<App> appsAll = categoryMap.get(ALL_FLAG);
            for (App app1 : appsAll) {
                if (app1.code.equals(app.code)) {
                    app1.upgradable = app.upgradable;
                    return;
                }
            }
        }
    }

    private void initUpgradeListener() {
        VciUpgradeManager.getInstance().setVciMax(mDpApplication.get("vciMax", -1));
        VciUpgradeManager.getInstance().setMcuMax(mDpApplication.get("mcuMax", -1));
        VciUpgradeManager.getInstance().setRfidMax(mDpApplication.get("rfidMax", -1));
        // 1. 设置监听器
        // Kotlin 的 Interface 在 Java 里就是普通的 Interface
        VciUpgradeManager.getInstance().setListener(new VciUpgradeManager.UpgradeListener() {
            @Override
            public void onUpgradeStart() {
                // UI 操作
                LogUtils.i("aaa", "开始升级...");
                dialog.setTitleText(getString(com.obdstar.common.ui.R.string.please_wait));
                dialog.setContentText("");
                dialog.showCancelButton(false);
                dialog.show();
                dialog.setmProgressVisibility(View.VISIBLE);
                dialog.setMpbVisibility(View.GONE);
            }

            @Override
            public void onProgress(int index, int progress, String message) {
                // 已经在主线程了，直接更新 UI
                if (progress == -1) {
                    if ("".equals(message)) {
                        dialog.dismiss();
                    } else {
                        if (message.startsWith("-1:")) {
                            dialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                            dialog.setTitleText(getString(com.obdstar.module.diag.R.string.need_upgrade));
                        } else if (message.startsWith("0:")) {
                            dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            dialog.setTitleText(getString(com.obdstar.module.diag.R.string.vci_upgrade_failed));
                        } else if (message.startsWith("1:")) {
                            dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            dialog.setTitleText(getString(com.obdstar.module.diag.R.string.muc_upgrade_failed));
                        } else if (message.startsWith("2:")) {
                            dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            dialog.setTitleText(getString(R.string.rfid_upgrade_failed));
                        }
                        dialog.setContentText(strCode + message);
                        dialog.setMpbVisibility(View.GONE);
                        dialog.setCancelable(true);
                        dialog.showCancelButton(true);
                    }
                }
                if (progress == -2) {//升级中
                    if (index == 0) {
                        dialog.setTitleText(message);
                        dialog.setmProgressVisibility(View.GONE);
                        dialog.setMpbVisibility(View.VISIBLE);
                    } else {
                        dialog.setTitleText(message);
                        dialog.setmProgressVisibility(View.GONE);
                        dialog.setMpbVisibility(View.VISIBLE);
                        dialog.setProgress(0);
                        dialog.setContentText("");
                    }
                } else if (progress == -3) {//已经是最新版本不需要升级
                    LogUtils.i("aaa", "已经是最新版本：" + index);
                    if (index == 0) {
                        dialog.setPbMax(vciMax);
                        dialog.setProgress(vciMax);
                    } else if (index == 1) {
                        dialog.setPbMax(mcuMax);
                        dialog.setProgress(mcuMax);
                    } else if (index == 2) {
                        dialog.setPbMax(rfidMax);
                        dialog.setProgress(rfidMax);
                    }
                    dialog.setContentText(message);
                } else if (progress == -4) {//设置最大进度
                    if (index == 0) {
                        LogUtils.i("aaa", "设置最大进度，vcimax:" + vciMax);
                        dialog.setPbMax(vciMax);
                    } else if (index == 1) {
                        LogUtils.i("aaa", "设置最大进度，mcuMax:" + mcuMax);
                        dialog.setPbMax(mcuMax);
                    }
                    dialog.setProgress(0);
                } else if (progress == -5) {//rfid初始化
                    dialog.setTitleText(getResources().getString(R.string.rfid_upgrading));
                    dialog.setmProgressVisibility(View.GONE);
                    dialog.setMpbVisibility(View.VISIBLE);
                } else if (progress == -6) {//rfid文件不存在
                    dialog.setTitleText(getString(com.obdstar.common.ui.R.string.please_wait));
                    dialog.setmProgressVisibility(View.VISIBLE);
                    dialog.setMpbVisibility(View.GONE);
                    dialog.setContentText(message);
                    dialog.setCancelable(true);
                    dialog.showCancelButton(true);
                } else if (progress == -7) {
                    dialog.setTitleText(message);
                    dialog.setProgress(0);
                    dialog.setContentText("");
                } else if (progress == -8) {//rfid不需要升级
                    dialog.setProgress(rfidMax);
                    dialog.setContentText(message);
                } else if (progress == -9) {//设置rfid进度最大值
                    rfidMax = mDpApplication.get("rfidMax", 0);
                    dialog.setPbMax(rfidMax);
                } else if (progress == -10) {//rfid 更新进度
                    try {
                        int parseInt = Integer.parseInt(message);
                        dialog.setProgress(parseInt);
                        if (parseInt == rfidMax) {
                            dialog.setContentText(getString(R.string.upgrade_done));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (progress == -11) {//升级成功
                    LogUtils.i("aaa", "升级成功!" + index);
                    if (index == 0) {
                        dialog.setProgress(vciMax);
                    } else if (index == 1) {
                        dialog.setProgress(mcuMax);
                    }
                    dialog.setContentText(message);
                }
            }

            @Override
            public void onUpgradeSuccess() {
                LogUtils.i("aaa", "成功！");
            }

            @Override
            public void onUpgradeError(int index, int errorCode, String errorMessage) {

                LogUtils.i("aaa", "失败: " + errorMessage);
            }
        });
    }

    public void initUpgrade() {
        vciMax = mDpApplication.get("vciMax", -1);
        mcuMax = mDpApplication.get("mcuMax", -1);
        rfidMax = mDpApplication.get("rfidMax", -1);
        dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.setCancelable(false);
        dialog.setTitleText(getString(com.obdstar.common.ui.R.string.please_wait));
        dialog.setConfirmText(getString(R.string.retry));
        dialog.setCancelClickListener(sDialog -> sDialog.cancel());

        initVciFiles();

        dialog.setConfirmClickListener(sweetAlertDialog -> {
            dialog.setTitleText(getString(com.obdstar.common.ui.R.string.upgrade));
            dialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
//            upgradeTask.cancel(true);
            upgradeTask = null;
            startUpgradeVci();
        });
        //设置vci升级回调
        mDpApplication.core.setVciUpgradeCallback(this);
        startUpgradeVci();
    }

    private void initVciFiles() {
        File core03File = new File(Constants.APP_ROOT + "/" + sn + "/" + Constants.STR_RESOURCE + "/CORE03/");
        vciFile = new File(Constants.APP_ROOT + "/" + sn + "/" + Constants.STR_RESOURCE + "/VCI/" + VCI_FILE_NAME);
        if (!vciFile.exists()) {
            vciFile = new File(Constants.APP_ROOT + "/" + sn + "/" + Constants.STR_RESOURCE + "/" + VCI_FILE_NAME);
        }
        rfidFile = new File(Constants.APP_ROOT + "/" + sn + "/" + Constants.STR_RESOURCE + "/RFID/" + RFID_FILE_NAME);


        if ('4' == sn.charAt(3)) {
            if (core03File.exists()) {
                mcuFile = new File(core03File, "MCU4.BIN");
                if (!mcuFile.exists()) {
                    mcuFile = new File(core03File.getParent(), "MCU4.BIN");
                }
            } else {
                mcuFile = new File(core03File.getParent(), "MCU4.BIN");
            }
        } else {
            if (core03File.exists()) {
                mcuFile = new File(core03File, "MCU3.BIN");
                if (!mcuFile.exists()) {
                    mcuFile = new File(core03File.getParent(), "MCU3.BIN");
                }
            } else {
                mcuFile = new File(core03File.getParent(), "MCU3.BIN");
            }
        }

        if (Constants.isMD76Device) {
            if (core03File.exists()) {
                mcuFile = new File(core03File, "MCU6.BIN");
                if (!mcuFile.exists()) {
                    mcuFile = new File(core03File.getParent(), "MCU6.BIN");
                }
            } else {
                mcuFile = new File(core03File.getParent(), "MCU6.BIN");
            }
        }
        addUpgradeFiles();
    }

    private void addUpgradeFiles() {
        upgradeFiles.clear();
        if (mDpApplication.deviceVersion == 3 || Constants.is64Bit()) {
//            upgradeTask = new UpgradeV3Task();
//            upgradeTask.execute(vciFile, mcuFile, rfidFile);
            // 直接调用，没有任何语法障碍
            upgradeFiles.add(vciFile);
            upgradeFiles.add(mcuFile);
            upgradeFiles.add(rfidFile);
            vciUpgradeType = 3;
        } else {
//                upgradeTask = new UpgradeV2Task();
//                upgradeTask.execute(vciFile, mcuFile);
            upgradeFiles.add(vciFile);
            upgradeFiles.add(mcuFile);

            if (Constants.isMD76Device) {
                vciUpgradeType = 4;
            } else {
                vciUpgradeType = 1;
            }
        }
    }

    private void startUpgradeVci() {
        System.gc();
        System.runFinalization();
        System.gc();
        VciUpgradeManager.getInstance().startUpgrade(upgradeFiles, mDpApplication, mContext, vciUpgradeType);
    }

    public void initView() {
        //UI
        categoryNameAdapter = new CategoryNameAdapter(this, categoryList);
        mTitle1 = findViewById(com.obdstar.module.diag.R.id.tv_title1);
        mTitle1.setText(currModuleName);
        displayLayout = findViewById(R.id.ll_vehicle_select_display);
        leftLayout = findViewById(R.id.left_layout);
        rightLayout = findViewById(R.id.right_layout);
        areaRadioGroup = findViewById(com.obdstar.module.diag.R.id.rg_area_list);

        areaRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

            }
        });

        obdstarKeyboard = new ObdstarKeyboard(this, rightLayout);
        obdstarKeyboard.setXmlLayoutResId(com.obdstar.common.ui.R.xml.keyboard_letter);

        etSearch = findViewById(com.obdstar.module.diag.R.id.edit_search);
        ivSearchClear = findViewById(com.obdstar.module.diag.R.id.iv_delete);
        obdstarKeyboard.initKeys('Z');
        obdstarKeyboard.setEditText(etSearch);

        mViewPage = findViewById(R.id.vehicle_viewpager);
        lvCategoryList = findViewById(R.id.lv_vehicles_select_list);
        mllDisplay = findViewById(R.id.ll_vehicle_select_display);
        mEmpty = findViewById(R.id.fl_empty);
        indicator = findViewById(R.id.vehicle_indicator);
        mProgressBarDialog = new PgbDlg(this, com.obdstar.common.ui.R.string.please_wait);

        pageAdapter = new IconPagerAdapter(getSupportFragmentManager(), fragmentList);
        mViewPage.setAdapter(pageAdapter);
//        mViewPage.setOffscreenPageLimit(20);
        mViewPage.setSaveFromParentEnabled(false);
//        mTitle3.setVisibility(View.VISIBLE);
        lvCategoryList.setAdapter(categoryNameAdapter);
        //左侧导航栏点击事件
        lvCategoryList.setOnItemClickListener((adapterView, view1, position, id) -> {
            if (ai.get() != 0) {
                return;
            }
            if (currentMenuIndex >= categoryList.size()) {
                return;
            }
            if (currentMenuIndex == position) {
                if (!TextUtils.isEmpty(currAreaCode)) {
                    mSelectRadionId = -1;
                    currentPageIndex = 0;
                    currAreaCode = "";
                } else {
                    return;
                }
            }

            currentPageIndex = 0;
            KeyValueItem valueItem = categoryList.get(position);
            if (TextUtils.isEmpty(valueItem.getCode())) {
                return;
            }
            for (KeyValueItem item : categoryList) {
                item.setSelected(false);
            }
            currentMenuIndex = position;
            valueItem.setSelected(true);
            currCategoryCode = valueItem.getCode();
            mSelectRadionId = -1;
            currAreaCode = "";
            categoryNameAdapter.notifyDataSetChanged();
            obdstarKeyboard.hideKeyboard();
            etSearch.setCursorVisible(false);
            etSearch.setText("");
            show(currCategoryCode, categoryMap.get(currCategoryCode), true);
        });
        initSearchListener();
        etSearch.setCursorVisible(false);
        etSearch.setText("");
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int pos, int i1, int i2) {
                if (etSearch.isCursorVisible()) {
                    if (getCurrentFragment() instanceof HistoryFragment) {
                        return;
                    }
                    Log.d("DiagChoiceActivity", "xialj___onTextChanged: 搜索文本：" + charSequence.toString().trim());
                    mPublishSubject.onNext(charSequence.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (etSearch.getText().length() != 0) {
                    ivSearchClear.setImageResource(com.obdstar.common.ui.R.drawable.ic_clear2);
                } else {
                    ivSearchClear.setImageResource(com.obdstar.common.ui.R.drawable.ic_search2);
                }
            }
        });

        //搜索&清除图标
        ivSearchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSearch();
            }
        });
    }

    public void initSearchListener() {
        Log.d("DiagChoiceActivity", "xialj___initSearchListener: " );
        etSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("DiagChoiceActivity", "xialj___onTouch: " );
                etSearch.setCursorVisible(true);
                etSearch.setShowSoftInputOnFocus(false);
                obdstarKeyboard.showKeyboard();
                return false;
            }
        });
    }

    /**
     * 获取 ViewPager 中当前显示的 Fragment
     */
    private Fragment getCurrentFragment() {
        if (fragmentList != null && mViewPage != null) {
            int currentItem = mViewPage.getCurrentItem();
            if (currentItem < fragmentList.size()) {
                return fragmentList.get(currentItem);
            }
        }
        return null;
    }

    /*清除搜索结果，显示全部车系*/
    private void clearSearch() {
        if (etSearch != null && etSearch.getText().length() != 0) {
            obdstarKeyboard.hideKeyboard();
            etSearch.setText("");
            currAreaCode = "";
            show(currCategoryCode, categoryMap.get(currCategoryCode), true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (IObdstarApplication.UPGRADED_COUNT.get() > 0) {
            if (IObdstarApplication.UPGRADED_COUNT.get() != upgradeCount) {
                initData(true, 2);
            }
        }
        IntentFilter filter = new IntentFilter();
        // 设置优先级
        filter.setPriority(999);
        filter.addAction(ACTION_VOLTAGE_CHANGE);
//        registerReceiver(receiver, filter);
//        if (mDpApplication.core != null) {
//            mDpApplication.core.loadCoreLibrary(false);
//        }

        clearSearch();
        super.onResume();
    }

//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String voltage = intent.getStringExtra("voltage");
//            if (voltage != null) {
//                if (mTitle3 != null) {
//                    mDpApplication.setDC(String.format(Locale.ENGLISH, "%sV", voltage));
//                    voltageCalibrator(voltage, mTitle3);
//                }
//            }
//        }
//    };

    private void voltageCalibrator(String voltage, TextView mtvTitle3) {
        final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        float DC_Calibrate = settings.getFloat("DC_Calibrate", 1);
        float calibrated = Float.parseFloat(voltage) * DC_Calibrate;
        if (calibrated == 0) {
            mtvTitle3.setText(String.format(Locale.ENGLISH, "%sV", voltage));
        } else {
            String calibratedd = "" + calibrated;
            int dot = calibratedd.indexOf(".");
            int length = calibratedd.length();
            String calibrated_dc_Integer = calibratedd.substring(0, dot);
            String calibrated_dc_Decimal = calibratedd.substring(dot + 1, length);
            String calibrated_dc_Decimal_2 = "";
            int len = calibrated_dc_Decimal.length();
            if (len <= 2) {
                calibrated_dc_Decimal_2 = calibrated_dc_Decimal.substring(0, len);
            } else {
                calibrated_dc_Decimal_2 = calibrated_dc_Decimal.substring(0, 2);
            }
            String calibrated_dc = calibrated_dc_Integer + "." + calibrated_dc_Decimal_2;
            mtvTitle3.setText(String.format(Locale.ENGLISH, "%sV", calibrated_dc));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Utils.isFastClick()) {
            return false;
        }

        if (getCurrentFragment() instanceof HistoryFragment) {
            return super.onKeyDown(keyCode, event);
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            if (obdstarKeyboard != null && obdstarKeyboard.getVisibility() == View.VISIBLE) {
                obdstarKeyboard.hideKeyboard();
                return true;
            }
            startActivity(new Intent(DiagChoiceActivity.this, MainFuncActivity.class));
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (upgradeTask != null) {
            upgradeTask.cancel(true);
        }
        if (mProgressBarDialog != null && !isDestroyed()) {
            mProgressBarDialog.dismiss();
        }
//        mDpApplication.set(Config.WORKING_FLAG, false);
        singleThreadExecutor.shutdown();
        DiagUpgradeUtils.Companion.instance().clear();
        if (Constants.is64Bit()) {//关闭诊断任务栈
            ActivityManager systemService = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> appTasks = systemService.getAppTasks();
            for (ActivityManager.AppTask appTask : appTasks) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (appTask.getTaskInfo().taskId == getTaskId()) {
                        appTask.finishAndRemoveTask();
                        break;
                    }
                }
            }
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        mDpApplication.finishActivity(this.getClass().getName());
    }

    /**
     * 判断outState不等以null就弹出所有Fragment全部重新加载
     */
//    @SuppressLint("MissingSuperCall")
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        if (outState != null) {
//            FragmentManager manager = getSupportFragmentManager();
//            manager.popBackStackImmediate(null, 1);
//        }
//    }
    @SuppressLint("CheckResult")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_CODE: {//收藏返回
                initData(true, currentMenuIndex);
                break;
            }
            case 8: {
                if (resultCode == Activity.RESULT_OK) {//删除返回
                    if (etSearch != null) {
                        etSearch.setText("");
                    }
                    obdstarKeyboard.hideKeyboard();
                    initData(true, currentMenuIndex);
                }
                break;
            }
            default: {
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void updateData() {
        initData(true, 2);
    }

    public static synchronized boolean readFileToBuff(final File file, ByteBuffer buffer) {
        if (!file.exists()) {
            return false;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                buffer.put(b, 0, n);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /*@Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = res.getConfiguration();
        config.densityDpi = 0;
        config.fontScale = 1;
//        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }*/
    // 字体大小不跟随系统
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(getConfigurationContext(newBase));
    }

    private Context getConfigurationContext(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.fontScale = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(LanguageUtils.getLocale(context));
        }
        return context.createConfigurationContext(configuration);
    }

    //是否正在初始化,避免重复调用
    private boolean isIniting = false;
    int classFlyIndex;

    public synchronized void initData(boolean flush, int selectIndex) {
        if (isIniting) {
            return;
        }
        classFlyIndex = selectIndex;

        isIniting = true;
        configs = DpApplication.configs;
        if (configs != null) {
            if (configs.getModules() != null && !configs.getModules().isEmpty()) {
                for (Module item : configs.getModules()) {
                    if (item.getCode().equals(currModuleCode)) {
                        currModule = item;
                    }
                }
            }
            if (currModule == null) {
                isIniting = false;
                this.finish();
            }
        } else {
            isIniting = false;
            this.finish();
        }
        categoryList.clear();
        categoryMap.clear();
        areaMap.clear();
        if (currModule != null && currModule.getShowCategory()) {
            if (leftLayout != null) {
                leftLayout.setVisibility(View.VISIBLE);
            }
            if (currModule.getShowFavorites()) {
                KeyValueItem keyValueItem = new KeyValueItem(getString(com.obdstar.common.ui.R.string.area_name_favorites), FAVORITES_FLAG);
                //设置小图标
                if (iconDir.exists()) {
                    File icon1 = new File(iconDir, "icon_favorites.png");
                    File iconSel = new File(iconDir, "icon_favorites_t.png");
                    if (icon1.exists() && iconSel.exists()) {
                        //加载图片
                        Drawable drawable = Drawable.createFromPath(icon1.getAbsolutePath());
                        Drawable drawableSelected = Drawable.createFromPath(iconSel.getAbsolutePath());
                        keyValueItem.setIcon(drawable);
                        keyValueItem.setIconPressed(drawableSelected);
                        if (Constants.isDP8000Device) {
                            keyValueItem.setIcon(DrawableUtils.zoomDrawable(drawable, 100, 100));
                            keyValueItem.setIconPressed(DrawableUtils.zoomDrawable(drawableSelected, 100, 100));
                        }
                    } else {
                        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_favorites, null);
                        Drawable drawableSelected = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_favorites_t, null);
                        keyValueItem.setIcon(drawable);
                        keyValueItem.setIconPressed(drawableSelected);
                    }
                } else {
                    Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_favorites, null);
                    Drawable drawableSelected = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_favorites_t, null);
                    keyValueItem.setIcon(drawable);
                    keyValueItem.setIconPressed(drawableSelected);
                }

                categoryList.add(keyValueItem);
                categoryMap.put(FAVORITES_FLAG, new ArrayList<>());
            }
            //添加历史
            if (currModule.getShowHistory()) {
                addHistoryClass();
            } else {
                if (flush) {
                    classFlyIndex = 1;
                }
            }


            if (currModule.getShowAll()) {
                KeyValueItem keyValueItem = new KeyValueItem(getString(com.obdstar.common.ui.R.string.area_name_all), ALL_FLAG);
                //设置小图标
                if (iconDir.exists()) {
                    File icon1 = new File(iconDir, "icon_all.png");
                    File iconSel = new File(iconDir, "icon_all_t.png");
                    if (icon1.exists() && iconSel.exists()) {
                        //加载图片
                        Drawable drawable = Drawable.createFromPath(icon1.getAbsolutePath());
                        Drawable drawableSelected = Drawable.createFromPath(iconSel.getAbsolutePath());
                        keyValueItem.setIcon(drawable);
                        keyValueItem.setIconPressed(drawableSelected);
                        if (Constants.isDP8000Device) {
                            keyValueItem.setIcon(DrawableUtils.zoomDrawable(drawable, 100, 100));
                            keyValueItem.setIconPressed(DrawableUtils.zoomDrawable(drawableSelected, 100, 100));
                        }
                    } else {
                        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_all, null);
                        Drawable drawableSelected = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_all_t, null);
                        keyValueItem.setIcon(drawable);
                        keyValueItem.setIconPressed(drawableSelected);
                    }
                } else {
                    Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_all, null);
                    Drawable drawableSelected = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_all_t, null);
                    keyValueItem.setIcon(drawable);
                    keyValueItem.setIconPressed(drawableSelected);
                }
                categoryList.add(keyValueItem);
                categoryMap.put(ALL_FLAG, new ArrayList<>());
            }
//            if (currModule.getShowHistory()) {
//                categoryList.add(new KeyValueItem(getString(R.string.area_name_all), HISTORY_FLAG));
//                categoryMap.put(HISTORY_FLAG, new ArrayList<>());
//            }
            if (configs.getCategories() != null && configs.getCategories().size() > 0) {
                for (Category category : configs.getCategories()) {
                    categoryMap.put(category.getCode(), new ArrayList<>());
                }
            }
        } else {
            if (leftLayout != null) {
                leftLayout.setVisibility(View.GONE);
            }
            categoryMap.put(ALL_FLAG, new ArrayList<>());
        }
        if (currModule != null && currModule.getShowArea()) {
            if (configs.getAreas() != null && configs.getAreas().size() > 0) {
                for (Category area : configs.getAreas()) {
                    areaMap.put(area.getCode(), 0);
                }
            }
        }
        List<String> favoritesCodes = mDpApplication.getAppDatabase().favoritesDao().findAllCodes();
        Observable.create((ObservableOnSubscribe<List<App>>) emitter -> {
            //更新依赖包
            mDpApplication.getDependenciesList();
            List<App> flowable = mDpApplication.getAppDatabase().appDao().findByModule(currModuleCode);
            emitter.onNext(flowable);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<App>>() {
            @Override
            public void onSubscribe(@NonNull Disposable subscription) {
                if (mProgressBarDialog != null && !isDestroyed()) {
                    mProgressBarDialog.show();
                }
            }

            @Override
            public void onNext(List<App> apps) {
                final List<App> flagAllAppList = new ArrayList<>();
                if (apps != null && !apps.isEmpty()) {
                    for (final App app : apps) {
                        if (app.packType != 0) {
                            continue;
                        }
                        if (Constants.isDP85Device && app.packType == 1) {//过滤主包
                            continue;
                        }
                        if (mDpApplication.getDependenciesPack().contains(app.code)) {//过滤依赖包
                            continue;
                        }
//                                if (app.available == 0) {
//                                    continue;
//                                }
                        if (ALL_FLAG.equalsIgnoreCase(app.category)) {
                            flagAllAppList.add(app);
                        } else {
                            if (app.category.contains(",")) {//多类型车系
                                String[] split = app.category.split(",");
                                for (String s : split) {
                                    if (categoryMap.containsKey(s)) {//单类型车系
                                        categoryMap.get(s).add(app);
                                    }
                                }
                            } else {
                                if (categoryMap.containsKey(app.category)) {//单类型车系
                                    categoryMap.get(app.category).add(app);
                                }
                            }
                        }
                        if (categoryMap.containsKey(ALL_FLAG)) {
                            categoryMap.get(ALL_FLAG).add(app);
                        }
                        if (favoritesCodes.contains(app.code) && categoryMap.containsKey(FAVORITES_FLAG)) {
                            categoryMap.get(FAVORITES_FLAG).add(app);
                        }
                    }
                    if (categoryMap.containsKey(FAVORITES_FLAG)) {
                        //收藏 添加和删除符号
                        App favoritesAdd = new App();
                        favoritesAdd.flag = ITEM_TYPE_ADD;
                        favoritesAdd.sort = 9998;
                        favoritesAdd.category = FAVORITES_FLAG;
                        favoritesAdd.area = ALL_FLAG;
                        favoritesAdd.stared = 1;
                        App favoritesDelete = new App();
                        favoritesDelete.flag = ITEM_TYPE_DELETE;
                        favoritesDelete.sort = 9999;
                        favoritesDelete.category = FAVORITES_FLAG;
                        favoritesDelete.area = ALL_FLAG;
                        favoritesDelete.stared = 1;
                        categoryMap.get(FAVORITES_FLAG).add(favoritesAdd);
                        categoryMap.get(FAVORITES_FLAG).add(favoritesDelete);
                    }
                    for (Category category : configs.getCategories()) {
                        if (categoryMap.containsKey(category.getCode()) && categoryMap.get(category.getCode()).size() > 0) {
                            String name = category.getName(mDpApplication.getLanguageType());

                            KeyValueItem keyValueItem = new KeyValueItem(name, category.getCode());
                            //85设置小图标
                            if (!TextUtils.isEmpty(category.getIcon()) && !TextUtils.isEmpty(category.getIconPressed())) {
                                if (iconDir.exists()) {
                                    File icon1 = new File(iconDir, category.getIcon());
                                    File iconSel = new File(iconDir, category.getIconPressed());
                                    if (icon1.exists() && iconSel.exists()) {
                                        //存储车标小分类
                                        logoIcon.put(category.getCode(), category.getIcon());

                                        //加载图片
                                        Drawable drawable = Drawable.createFromPath(icon1.getAbsolutePath());
                                        Drawable drawableSelected = Drawable.createFromPath(iconSel.getAbsolutePath());
                                        keyValueItem.setIcon(drawable);
                                        keyValueItem.setIconPressed(drawableSelected);
                                    }
                                }
                            }
                            categoryList.add(keyValueItem);
                        }
                    }
                } else {
                    isIniting = false;
                    showEmptyView();
                }
                if (!categoryList.isEmpty()) {
                    currentMenuIndex = classFlyIndex;
                    KeyValueItem valueItem;
                    if (currentMenuIndex < categoryList.size()) {
                        currCategoryCode = categoryList.get(currentMenuIndex).getCode();
                        valueItem = categoryList.get(currentMenuIndex);
                    } else {
                        currCategoryCode = categoryList.get(0).getCode();
                        valueItem = categoryList.get(0);
                    }

                    valueItem.setSelected(true);
                    if (!flagAllAppList.isEmpty()) {
                        for (KeyValueItem item : categoryList) {
                            final String code = item.getCode();
                            if (ALL_FLAG.equalsIgnoreCase(code) || FAVORITES_FLAG.equalsIgnoreCase(code) || HISTORY_FLAG.equalsIgnoreCase(code)) {
                                continue;
                            }
                            for (App app : flagAllAppList) {
                                if (categoryMap.containsKey(code)) {
                                    categoryMap.get(code).add(app);
                                }
                            }
                        }
                    }
                }
                if (flush) {
                    if (!isDestroyed()) {
                        show(currCategoryCode, categoryMap.get(currCategoryCode), true);
                        categoryNameAdapter.notifyDataSetChanged();
                    }
                    isIniting = false;
                } else {
                    if (mProgressBarDialog != null && !isDestroyed()) {
                        mProgressBarDialog.dismiss();
                    }
                    isIniting = false;
                }
            }

            @Override
            public void onError(Throwable throwable) {
                LogUtils.e("initData", "onError()");
                if (mProgressBarDialog != null && !isDestroyed()) {
                    mProgressBarDialog.dismiss();
                }
                isIniting = false;
            }

            @Override
            public void onComplete() {
                LogUtils.e("initData", "onComplete()");
                if (mProgressBarDialog != null && !isDestroyed()) {
                    mProgressBarDialog.dismiss();
                }
                isIniting = false;
            }
        });
        upgradeCount = IObdstarApplication.UPGRADED_COUNT.get();
    }

    /**
     * 添加历史图标
     */
    private void addHistoryClass() {
        KeyValueItem keyValueItem = new KeyValueItem(getString(com.obdstar.common.ui.R.string.area_name_history), HISTORY_FLAG);
        //设置小图标
        if (iconDir.exists()) {
            File icon1 = new File(iconDir, "icon_history.png");
            File iconSel = new File(iconDir, "icon_history_t.png");
            if (icon1.exists() && iconSel.exists()) {
                //加载图片
                Drawable drawable = Drawable.createFromPath(icon1.getAbsolutePath());
                Drawable drawableSelected = Drawable.createFromPath(iconSel.getAbsolutePath());
                keyValueItem.setIcon(drawable);
                keyValueItem.setIconPressed(drawableSelected);
                if (Constants.isDP8000Device) {
                    keyValueItem.setIcon(DrawableUtils.zoomDrawable(drawable, 100, 100));
                    keyValueItem.setIconPressed(DrawableUtils.zoomDrawable(drawableSelected, 100, 100));
                }
            } else {
                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_all, null);
                Drawable drawableSelected = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_all_t, null);
                keyValueItem.setIcon(drawable);
                keyValueItem.setIconPressed(drawableSelected);
            }
        } else {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_all, null);
            Drawable drawableSelected = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_all_t, null);
            keyValueItem.setIcon(drawable);
            keyValueItem.setIconPressed(drawableSelected);
        }
        categoryList.add(keyValueItem);
        categoryMap.put(ALL_FLAG, new ArrayList<>());
    }

    //右侧汽车品牌列表显示
    private int mSelectRadionId = -1;

    /**
     * @param categoryCode 模块code
     * @param data         数据
     * @param flushArea    是否刷新
     */
    private synchronized void show(String categoryCode, final List<App> data, boolean flushArea) {
        if (HISTORY_FLAG.equals(categoryCode)) {//历史
            showHistory();
            return;
        }
        indicator.setVisibility(View.VISIBLE);
        if (mViewPage.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mViewPage.getLayoutParams();
            layoutParams.bottomMargin = SizeUtils.dp2px(getResources().getDimension(com.obdstar.common.ui.R.dimen._60dp));
            mViewPage.setLayoutParams(layoutParams);
        }

        int mPageCount;
        if (data == null || data.isEmpty()) {
            mViewPage.setVisibility(View.INVISIBLE);
            mEmpty.setVisibility(View.VISIBLE);
            if (mProgressBarDialog != null && !isDestroyed()) {
                mProgressBarDialog.dismiss();
            }
            return;
        } else {
            mEmpty.setVisibility(View.GONE);
            mViewPage.setVisibility(View.VISIBLE);
            if (currModule.getShowArea() && flushArea) {
                for (Map.Entry<String, Integer> listEntry : areaMap.entrySet()) {
                    listEntry.setValue(0);
                }
                for (App app : data) {
                    if (areaMap.containsKey(app.area)) {
                        areaMap.put(app.area, areaMap.get(app.area) + 1);
                    }
                }
                areaRadioGroup.removeAllViews();
                areaRadioGroup.clearCheck();
                if (configs.getAreas() != null && configs.getAreas().size() > 0) {
                    RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.setMargins(0, 0, 0, 0);

                    //计算存在的车标分区数
                    List<Category> categoryList = new ArrayList<>();
                    for (int i = 0; i < configs.getAreas().size(); i++) {
                        Category area = configs.getAreas().get(i);
                        if (areaMap.containsKey(area.getCode()) && areaMap.get(area.getCode()) > 0) {
                            categoryList.add(area);
                        }
                    }
                    int areasSize = categoryList.size();
                    for (int i = 0; i < areasSize; i++) {
                        Category area = categoryList.get(i);
//                        if (areaMap.containsKey(area.getCode()) && areaMap.get(area.getCode()) > 0) {
                        final RadioButton radioButton = (RadioButton) LayoutInflater.from(this).inflate(com.obdstar.module.diag.R.layout.btn_radio_layout, null);
                        int widthInPx = (int) getResources().getDimension(com.obdstar.common.ui.R.dimen._170dp);
                        radioButton.setLayoutParams(new ViewGroup.LayoutParams(widthInPx, ViewGroup.LayoutParams.WRAP_CONTENT));
                        radioButton.setId(area.getCode().hashCode());
                        radioButton.setTag(area.getCode());
                        String name = area.getName(mDpApplication.getLanguageType());
                        radioButton.setText(name);
//                        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,
//                                Constants.is7InchesDevice ? 12 : getResources().getDimension(R.dimen.favorite_title3_size));
                        radioButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                areaRadioGroup.clearCheck();
                                if (etSearch.getText().length() != 0) {
                                    etSearch.setText("");
                                }
                                if (mSelectRadionId == radioButton.getId()) {//选中变未选中
                                    mSelectRadionId = -1;
                                    currentPageIndex = 0;
                                    currAreaCode = "";
                                    show(currCategoryCode, categoryMap.get(currCategoryCode), true);
                                } else {//未选中变选中
                                    mSelectRadionId = radioButton.getId();
                                    radioButton.setChecked(true);
                                    currAreaCode = (String) radioButton.getTag();
                                    currentPageIndex = 0;
                                    show(currCategoryCode, categoryMap.get(currCategoryCode), false);
                                }
                            }
                        });
                        if (areasSize == 1) {
                            radioButton.setBackgroundResource(R.drawable.top_bar_area_rb_r_selector);
                            areaRadioGroup.addView(radioButton);
                        } else {
                            if (i == 0) {
                                radioButton.setBackgroundResource(R.drawable.top_bar_area_rb_l_selector);
                                areaRadioGroup.addView(radioButton);
                            } else if (i == areasSize - 1) {
                                radioButton.setBackgroundResource(R.drawable.top_bar_area_rb_r_selector);
                                areaRadioGroup.addView(radioButton);
                            } else {
                                radioButton.setBackgroundResource(R.drawable.top_bar_area_rb_m_selector);
                                areaRadioGroup.addView(radioButton);
                            }
                        }
//                        }
                    }

                    if (areaRadioGroup.getChildCount() == 1) {
//                        areaRadioGroup.check(areaRadioGroup.getChildAt(0).getId());
                    }
                }
            }
        }
        List<App> filter;
        if (TextUtils.isEmpty(currAreaCode)) {
            filter = data;
        } else {
//            areaRadioGroup.check(currAreaCode.hashCode());
            filter = new ArrayList<>();
            for (App app : data) {
                if (currAreaCode.equalsIgnoreCase(app.area) || ALL_FLAG.equalsIgnoreCase(app.area)) {
                    filter.add(app);
                }
            }
        }
        if (filter.isEmpty()) {
            mViewPage.setVisibility(View.INVISIBLE);
            mEmpty.setVisibility(View.VISIBLE);
            if (mProgressBarDialog != null && !isDestroyed()) {
                mProgressBarDialog.dismiss();
            }
            return;
        }
        Collections.sort(filter, new AppComparator(mDpApplication.getLanguageType()));
        if (filter.size() % PAGE_SIZE == 0) {
            mPageCount = filter.size() / PAGE_SIZE;
        } else {
            mPageCount = filter.size() / PAGE_SIZE + 1;
        }
        clearFragments();
        for (int page = 0; page < mPageCount; page++) {
            List<App> pageData = new ArrayList<>(PAGE_SIZE);//右侧汽车品牌列表数据源
            int index = page * PAGE_SIZE;
            for (int i = 0; i < PAGE_SIZE; i++) {
                if (index + i >= filter.size()) {
                    break;
                }
                pageData.add(filter.get(index + i));
            }
            IconPagerFragment iconPagerFragment = new IconPagerFragment();
            Bundle bundle = new Bundle();
            bundle.putString("sn", sn);
            bundle.putString("module", currModuleName);
            bundle.putString("category", currCategoryCode);
            bundle.putBoolean("showTitle", currModule.getShowAppTitle());
            bundle.putSerializable("data", (Serializable) pageData);
            iconPagerFragment.setArguments(bundle);
            fragmentList.add(iconPagerFragment);
        }
        pageAdapter.notifyDataSetChanged();
        mViewPage.setCurrentItem(currentPageIndex, false);
        indicator.attachToViewPager(mViewPage);

        mViewPage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPageIndex = position;
                ai.set(0);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                ai.set(state);
            }
        });
        indicator.requestLayout();
        if (mProgressBarDialog != null && !isDestroyed()) {
            mProgressBarDialog.dismiss();
        }
    }

    /**
     * 显示历史页面
     */
    private void showHistory() {
        if (mProgressBarDialog != null && !isDestroyed()) {
            mProgressBarDialog.dismiss();
        }
        mEmpty.setVisibility(View.GONE);
        mViewPage.setVisibility(View.VISIBLE);
        indicator.setVisibility(View.GONE);
        if (mViewPage.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mViewPage.getLayoutParams();
            layoutParams.bottomMargin = 0;
            mViewPage.setLayoutParams(layoutParams);
        }

        areaRadioGroup.removeAllViews();
        areaRadioGroup.clearCheck();
        clearFragments();
        HistoryFragment iconPagerFragment = HistoryFragment.newInstance(etSearch,ivSearchClear);
        Bundle bundle = new Bundle();
        bundle.putString("sn", sn);
        iconPagerFragment.setArguments(bundle);
        fragmentList.add(iconPagerFragment);

        pageAdapter.notifyDataSetChanged();
        mViewPage.setCurrentItem(0, false);
        indicator.attachToViewPager(mViewPage);
    }

    /**
     * 清除缓存 fragment
     */
    public void clearFragments() {
        if (fragmentList.size() == 0) {
            return;
        }
        mViewPage.removeAllViewsInLayout();
        for (IconBaseFragment fragment : fragmentList) {
            fragment.clear();
        }
        fragmentList.clear();
        pageAdapter.notifyDataSetChanged();
        mViewPage.setAdapter(pageAdapter);
    }

    public void gotoUpgrade() {
        try {
            ARouter.getInstance().build("/upgrade/main").withInt("version", 1).navigation(this, new LoginNavigationCallbackImpl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 没有数据显示
    private void showEmptyView() {
        displayLayout.removeAllViews();
        View view = LayoutInflater.from(this).inflate(com.obdstar.module.diag.R.layout.ui_message_box, displayLayout);
        TextView tv = (TextView) view.findViewById(com.obdstar.module.diag.R.id.tv_infor);
        tv.setText(getResources().getString(com.obdstar.module.diag.R.string.no_diagnostic_data));
        Button btnOK = (Button) view.findViewById(R.id.btn_01);
        btnOK.setVisibility(View.VISIBLE);
        btnOK.setBackgroundResource(com.obdstar.common.ui.R.drawable.button2);
        btnOK.setTypeface(Typeface.DEFAULT_BOLD);
        btnOK.setText(getResources().getString(com.obdstar.common.ui.R.string.Ok));

        areaRadioGroup.removeAllViews();
        areaRadioGroup.clearCheck();
        currentPageIndex = 0;

        btnOK.setOnClickListener(v -> {
            gotoUpgrade();
            finish();
        });
    }

    // 搜索逻辑处理
    private Observable<List<App>> getSearchObservable(final String query) {
        Log.d("DiagChoiceActivity", "xialj___getSearchObservable: 搜索逻辑处理" );
        return Observable.create((ObservableOnSubscribe<List<App>>) observableEmitter -> {
            try {
                List<App> filter = new ArrayList<>();
                List<App> all = categoryMap.get(currCategoryCode);
                int size = query.length();
                int languageType = mDpApplication.getLanguageType();
                if (all != null && all.size() > 0) {
                    if (languageType == 0 || languageType == 2) {
                        for (App item : all) {
                            if (size >= 6) {
                                if (item.pinyin.contains(query)) {
                                    filter.add(item);
                                }
                            } else {
                                if (item.initial.contains(query)) {
                                    filter.add(item);
                                }
                            }
                        }
                    } else {
                        for (App item : all) {
                            if (item.name.toUpperCase(Locale.ENGLISH).contains(query)) {
                                filter.add(item);
                            }
                        }
                    }
                }
                observableEmitter.onNext(filter);
            } catch (Exception e) {
                if (!observableEmitter.isDisposed()) {
                    observableEmitter.onError(e);
                }
            }
            observableEmitter.onComplete();
        }).subscribeOn(Schedulers.computation());
    }

    //设置进度最大值
    @Override
    public void getMax(int vciMax, int mcuMax) {
        if (vciMax != 0) {
            this.vciMax = vciMax;
            mDpApplication.set("vciMax", vciMax);
            VciUpgradeManager.getInstance().setVciMax(vciMax);
        }
        if (mcuMax != 0) {
            this.mcuMax = mcuMax;
            mDpApplication.set("mcuMax", mcuMax);
            VciUpgradeManager.getInstance().setMcuMax(mcuMax);
        }
    }

    /**
     * 更新升级进度
     *
     * @param progress
     * @param vciIsLatest
     * @param mcuIsLatest
     */
    @Override
    public void getCurrentProgress(int progress, boolean vciIsLatest, boolean mcuIsLatest) {
//        if (upgradeTask instanceof UpgradeV3Task) {
//            UpgradeV3Task task = (UpgradeV3Task) upgradeTask;
//            if (vciIsLatest || !task.vciUpgraded) {
//                currentPg = progress;
//            } else if (mcuIsLatest || !task.mcuUpgraded) {
////                currentPg = vciMax + progress;
//                currentPg = progress;
//            }
//            task.updateProgress(currentPg);
//        }
        LogUtils.i("aaa", "currentProgress:" + progress);
        runOnUiThread(() -> dialog.setProgress(progress));
//        if (upgradeTask instanceof UpgradeV2Task) {
//            UpgradeV2Task task = (UpgradeV2Task) upgradeTask;
//            if (vciIsLatest || !task.vciUpgraded) {
//                currentPg = progress;
//            } else if (mcuIsLatest || !task.mcuUpgraded) {
////                currentPg = vciMax + progress;
//                currentPg = progress;
//            }
//            task.updateProgress(currentPg);
//        }
    }

    /**
     * 2.0 使用
     */
    class UpgradeV2Task extends AsyncTask<File, Integer, String> {
        File md5SaveFile = null;
        String newMd5 = "";
        boolean upgrade;//是否需要升级

        boolean vciUpgraded = false;
        boolean mcuUpgraded = false;

        @Override
        protected void onPreExecute() {
            LogUtils.i("UpgradeTask2", "onPreExecute：开始升级vci");
            mDpApplication.isUpgradeVci = true;
            super.onPreExecute();
            dialog.setTitleText(getString(com.obdstar.common.ui.R.string.please_wait));
            dialog.setContentText("");
            dialog.showCancelButton(false);
            dialog.show();
            dialog.setmProgressVisibility(View.VISIBLE);
            dialog.setMpbVisibility(View.GONE);
        }

        public void updateProgress(int progress) {
            publishProgress(progress);
        }

        @Override
        protected String doInBackground(File... arg) {
            LogUtils.i("UpgradeTask2", "拷贝core3");
            if (mDpApplication.core != null) {
                mDpApplication.core.loadCoreLibrary(true);
            }
            LogUtils.i("UpgradeTask2", "UpgradeV2Task.doInBackground");
            for (int i = 0; i < arg.length; i++) {
                if (isCancelled()) {
                    break;
                }
                File file = arg[i];
                if (file.exists()) {
                    if (vciMax == -1 || mcuMax == -1) {
                        String vciSn = mDpApplication.getVciSN();
                        if (TextUtils.isEmpty(vciSn)) {
                            return "-1:" + getString(com.obdstar.common.ui.R.string.message_read_sn_fail);
                        }
                        int size = Long.valueOf(file.length()).intValue();
                        ByteBuffer buffer = ByteBuffer.allocateDirect(size + 4);
                        if (readFileToBuff(file, buffer)) {
                            int mode = i + 1;
                            int type = i == 0 ? 12 : 21;
                            Future<Integer> future = singleThreadExecutor.submit(new Callable<Integer>() {
                                @Override
                                public Integer call() throws Exception {
                                    for (int j = 0; j < 3; j++) {
                                        int m = mDpApplication.core.SetVciMode(mode);
                                        if (m == mode) {
                                            break;
                                        }
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    return mDpApplication.core.GetVciUpgradeable(buffer, size, type, true);
                                }
                            });
                            try {
                                //0，已经是最新，1，需要升级
                                int res = future.get(25, TimeUnit.SECONDS);
                                if (res != 1 && res != 0) {
                                    return i + ":GET UPGRADEABLE [" + res + "]";
                                }
                                if (i == 1) {
//                                    dialog.setPbMax(vciMax + mcuMax);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                return i + ":GET UPGRADEABLE TIMEOUT";
                            }
                        }
                    }
                } else {
                    return i + "file [" + file.getName() + "] " + getResources().getString(R.string.is_not_exist);
                }
//                dialog.setPbMax(vciMax + mcuMax);
            }
            //vciFile和mcuFile升级: i=0为vicFile,i=1为mcuFile
            for (int i = 0; i < arg.length; i++) {
                if (isCancelled()) {
                    break;
                }
                upgrade = false;
                File file = arg[i];
                if (file.exists()) {
                    try {
                        newMd5 = BinaryUtil.calculateBase64Md5(file.getAbsolutePath());
                        if (i == 0) {
                            md5SaveFile = new File(vciFile.getParent(), "vci.md5");
                        } else {
                            md5SaveFile = new File(mcuFile.getParent(), "mcu.md5");
                        }
                        if (md5SaveFile.exists()) {
                            String line = DiagFileUtils.readLineFromFile(md5SaveFile);
                            if (!newMd5.equals(line)) {
                                upgrade = true;
                            }
                        } else {
                            upgrade = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (i == 1) {//不需要升级
                        mDpApplication.isUpgradeVci = false;
                    }
                    continue;
                }
                if (upgrade) {
                    if (i == 0) {
                        runOnUiThread(() -> {
                            dialog.setTitleText(getResources().getString(com.obdstar.module.diag.R.string.vci_upgrading));
//                            dialog.setPbMax(vciMax + mcuMax);
                            dialog.setPbMax(vciMax);
                            dialog.setProgress(0);
                            dialog.setmProgressVisibility(View.GONE);
                            dialog.setMpbVisibility(View.VISIBLE);
                        });
                    } else {
                        runOnUiThread(() -> {
                            dialog.setTitleText(getResources().getString(com.obdstar.module.diag.R.string.mu_upgrading));
//                            dialog.setPbMax(vciMax + mcuMax);
                            dialog.setPbMax(mcuMax);
                            dialog.setProgress(0);
                            dialog.setmProgressVisibility(View.GONE);
                            dialog.setMpbVisibility(View.VISIBLE);
                            dialog.setContentText("");
                        });
                    }
                    LogUtils.i("UpgradeTask2", "upgrade:" + file.getAbsolutePath());
                    if (vciUpgraded) {
                        for (int j = 0; j < 5; j++) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String vciSn = mDpApplication.getVciSN();
                            if (!TextUtils.isEmpty(vciSn) && vciSn.length() == 12) {
                                break;
                            }
                        }
                    }
                    String vciSn = mDpApplication.getVciSN();
                    if (TextUtils.isEmpty(vciSn)) {
                        return "-1:" + getString(com.obdstar.common.ui.R.string.message_read_sn_fail);
                    }
                    int size = Long.valueOf(file.length()).intValue();
                    ByteBuffer buffer = ByteBuffer.allocateDirect(size + 4);
                    if (readFileToBuff(file, buffer)) {
                        int finalI = i;
                        Future<Integer> future = singleThreadExecutor.submit(new Callable<Integer>() {
                            @Override
                            public Integer call() {
                                int f;
                                if (finalI == 0) {//vic升级
                                    LogUtils.i("UpgradeTask2", "vic开始升级");
                                    mDpApplication.isUpgradeVci = true;
                                    for (int i = 0; i < 3; i++) {
                                        int m = mDpApplication.core.SetVciMode(1);
                                        if (m == 1) {
                                            break;
                                        }
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    int upgradeable = mDpApplication.core.GetVciUpgradeable(buffer, size, 12, false);
                                    if (upgradeable != 1) {
                                        return upgradeable;
                                    }
                                    f = mDpApplication.core.UpgradeVci(buffer, size, 2, false); //DP50/DP82/DP85/
                                    LogUtils.i("UpgradeTask2", "vci升级结果,f：" + f);
                                } else {//mcu升级
                                    LogUtils.i("UpgradeTask2", "mcu开始升级");
                                    mDpApplication.isUpgradeVci = true;
                                    for (int i = 0; i < 3; i++) {
                                        int m = mDpApplication.core.SetVciMode(2);
                                        if (m == 2) {
                                            break;
                                        }
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    int upgradeable = mDpApplication.core.GetVciUpgradeable(buffer, size, 21, false);
                                    if (upgradeable != 1) {
                                        return upgradeable;
                                    }
                                    f = mDpApplication.core.UpgradeMcu(buffer, size, 1, false); //MCU BOOT
                                }
                                return f;
                            }
                        });
                        try {
                            int res = future.get(30, TimeUnit.SECONDS);
                            if (res == 0) {//升级成功
                                if (i == 0) {
                                    //需求：2372 下位机升级 提示优化
                                    runOnUiThread(() -> onProgressUpdate(vciMax));
                                    Thread.sleep(1000);
                                    vciUpgraded = true;
                                } else if (i == 1) {
                                    //需求：2372 下位机升级 提示优化
                                    runOnUiThread(() -> onProgressUpdate(mcuMax));
                                    Thread.sleep(1000);
                                    mcuUpgraded = true;
                                }
                                DiagFileUtils.saveToFile(md5SaveFile.getAbsolutePath(), newMd5, true);
                            } else {//升级失败
                                return i + ":" + res;
                            }
                        } catch (TimeoutException | InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            return i + ":upgrade timeout";
                        }
                    }
                } else {
                    //不需要升级
                    if (i == 1) {
                        mDpApplication.isUpgradeVci = false;
                    }
                }
            }
//            publishProgress(vciMax + mcuMax);
            return "";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (isCancelled()) {
                return;
            }
            dialog.setProgress(progress[0]);
            if (!vciUpgraded) {
                if (progress[0] == vciMax) {
                    dialog.setContentText(getString(R.string.upgrade_done));
                }
            } else if (!mcuUpgraded) {
                if (progress[0] == mcuMax) {
                    dialog.setContentText(getString(R.string.upgrade_done));
                }
            }

            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            mDpApplication.isUpgradeVci = false;
            LogUtils.i("UpgradeTask2", "result:" + result);
            if ("".equals(result)) {
                dialog.dismiss();
            } else {
                if (result.startsWith("-1:")) {
                    dialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                    dialog.setTitleText(getString(com.obdstar.module.diag.R.string.need_upgrade));
                } else if (result.startsWith("0:")) {
                    dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText(getString(com.obdstar.module.diag.R.string.vci_upgrade_failed));
                } else if (result.startsWith("1:")) {
                    dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText(getString(com.obdstar.module.diag.R.string.muc_upgrade_failed));
                }
                dialog.setContentText(strCode + result);
                dialog.setCancelable(true);
                dialog.showCancelButton(true);
            }
            super.onPostExecute(result);
        }
    }

    int vciMax, mcuMax, currentPg, rfidMax;

    /**
     * 3.0 使用
     */
    class UpgradeV3Task extends AsyncTask<File, Integer, String> {
        File md5SaveFile = null;
        String newMd5 = "";
        boolean upgrade;
        boolean vciUpgraded = false;
        boolean mcuUpgraded = false;
        boolean rfidUpgraded = false;
        //是否正在升级
        boolean isVciUpgradeding, isMcuUpgradeding, isRfidUpgradeding;

        @Override
        protected void onPreExecute() {
            singleThreadExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    boolean b = mDpApplication.core.loadCoreLibrary(true);
                    LogUtils.i("UpgradeV3Task", "loadCoreLibrary:" + b);
                }
            });
            LogUtils.i("UpgradeV3Task", "onPreExecute");
            super.onPreExecute();
            dialog.setTitleText(getString(com.obdstar.common.ui.R.string.please_wait));
            dialog.setContentText("");
            dialog.showCancelButton(false);
            dialog.show();
            dialog.setmProgressVisibility(View.VISIBLE);
            dialog.setMpbVisibility(View.GONE);
        }

        public void updateProgress(int progress) {
            if (dialog.getPbVisibility() == View.GONE) {
                dialog.setMpbVisibility(View.VISIBLE);
            }
            publishProgress(progress);
        }

        @Override
        protected String doInBackground(File... arg) {
            LogUtils.i("UpgradeV3Task", "doInBackground");

            for (int i = 0; i < arg.length - 1; i++) {
                if (isCancelled()) {
                    break;
                }
                upgrade = false;
                File file = arg[i];
                if (file.exists()) {
                    try {
                        newMd5 = BinaryUtil.calculateBase64Md5(file.getAbsolutePath());
                        if (i == 0) {
                            md5SaveFile = new File(vciFile.getParent(), "vci.md5");
                        } else {
                            md5SaveFile = new File(mcuFile.getParent(), "mcu.md5");
                        }
                        if (md5SaveFile.exists()) {
                            String line = DiagFileUtils.readLineFromFile(md5SaveFile);
                            if (!newMd5.equals(line)) {
                                upgrade = true;
                            }
                        } else {
                            upgrade = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
//                    continue;
                    return i + "file [" + file.getName() + "] " + getResources().getString(R.string.is_not_exist);
                }
                if (upgrade) {
                    if (i == 0) {
                        runOnUiThread(() -> {
                            dialog.setTitleText(getResources().getString(com.obdstar.module.diag.R.string.vci_upgrading));
                            dialog.setmProgressVisibility(View.GONE);
                            dialog.setMpbVisibility(View.VISIBLE);
                        });
                    } else {
                        runOnUiThread(() -> {
                            dialog.setTitleText(getResources().getString(com.obdstar.module.diag.R.string.mu_upgrading));
                            dialog.setmProgressVisibility(View.GONE);
                            dialog.setMpbVisibility(View.VISIBLE);
                            dialog.setProgress(0);
                            dialog.setContentText("");
                        });
                    }
                    LogUtils.i("UpgradeV3Task", "upgrade:" + file.getAbsolutePath());
                    /**
                     * todo
                     * 1、调用接口判断是否需要升级
                     * 2、获取当前VCI模式
                     * 3、跳转到相应模式
                     * 4、等待模式切换成功（循环获取当前VCI模式）
                     * 5、进行升级
                     */
                    final int tagMode = i + 1;
                    final int fileSize = Long.valueOf(file.length()).intValue();
                    final ByteBuffer buffer = ByteBuffer.allocateDirect(fileSize + 4);
                    final int uType = i == 0 ? 13 : 21;
                    try {
                        byte[] data = FileUtils.readFileToByteArray(file);
                        buffer.put(data);
                    } catch (Exception e) {
                        return i + ":READ FILE FAIL-" + file.getName();
                    }
                    Future<Integer> future = singleThreadExecutor.submit(new Callable<Integer>() {
                        @Override
                        public Integer call() {
                            //1、设置为bootmode，只有vci升级才需要设置
                            if (tagMode == 1) {
                                for (int i = 0; i < 3; i++) {
                                    int setModeRes = mDpApplication.setVciMode(tagMode);
                                    LogUtils.i("UpgradeV3Task", String.format(Locale.ENGLISH, "setVciMode results:%d", setModeRes));
                                    if (setModeRes == 1) {
                                        break;
                                    }
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                //1、设置为appmode，只有mcu升级才需要设置
                                for (int i = 0; i < 3; i++) {
                                    int setModeRes = mDpApplication.setVciMode(tagMode);
                                    LogUtils.i("UpgradeV3Task", String.format(Locale.ENGLISH, "setVciMode results:%d", setModeRes));
                                    if (setModeRes == 1) {
                                        break;
                                    }
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            //2、获取升级状态：StartUpdate
                            LogUtils.i("UpgradeV3Task", "uType:" + uType);
                            return mDpApplication.core.GetVciUpgradeable(buffer, fileSize, uType, false);
                        }
                    });
                    try {
                        int res = future.get(25, TimeUnit.SECONDS);
                        LogUtils.i("UpgradeV3Task", "GetVciUpgradeable:" + res);
                        if (res == 0) {//已经是最新版本，不需要升级
                            DiagFileUtils.saveToFile(md5SaveFile.getAbsolutePath(), newMd5, true);
                            if (i == 0) {
                                //需求：2372 下位机升级 提示优化
                                runOnUiThread(() -> {
                                    isVciUpgradeding = true;
                                    dialog.setPbMax(vciMax);
                                    onProgressUpdate(vciMax);
                                });
                                Thread.sleep(1000);
                                isVciUpgradeding = false;
                                vciUpgraded = true;
                            } else {
                                //需求：2372 下位机升级 提示优化
                                runOnUiThread(() -> {
                                    isMcuUpgradeding = true;
                                    dialog.setPbMax(mcuMax);
                                    onProgressUpdate(mcuMax);
                                });
                                Thread.sleep(1000);
                                isMcuUpgradeding = false;

                                mcuUpgraded = true;
                            }
                            continue;
                        } else if (res != 1) {
                            return i + ":GET UPGRADEABLE [" + res + "]";
                        }
                    } catch (TimeoutException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        return i + ":GET UPGRADEABLE TIMEOUT";
                    }
                    int finalI = i;
                    //设置最大进度
                    if (i == 0) {
                        if (!vciUpgraded) {
                            runOnUiThread(() -> {
                                dialog.setContentText("");
                                dialog.setPbMax(vciMax);
                            });
                        }
                    } else {
                        if (!mcuUpgraded) {
                            runOnUiThread(() -> {
                                dialog.setContentText("");
                                dialog.setPbMax(mcuMax);
                                onProgressUpdate(0);
                            });
                        }
                    }
                    Future<Integer> future2 = singleThreadExecutor.submit(new Callable<Integer>() {
                        @Override
                        public Integer call() {
                            int f;
                            if (finalI == 0) {
                                if (!vciUpgraded) {
                                    //3.升级vci
                                    isVciUpgradeding = true;
                                    f = mDpApplication.core.UpgradeVci(buffer, fileSize, 3, true); //3.0 VCI
                                } else {
                                    return 0;
                                }
                            } else {
                                if (!mcuUpgraded) {
                                    //3.升级mcu
                                    isMcuUpgradeding = true;
                                    f = mDpApplication.core.UpgradeMcu(buffer, fileSize, 1, true); //MCU BOOT
                                } else {
                                    return 0;
                                }
                            }
                            return f;
                        }
                    });
                    try {
                        int res = future2.get(30, TimeUnit.SECONDS);
                        if (res == 0) {//升级成功
                            if (i == 0) {//vci
                                vciUpgraded = true;
                                isVciUpgradeding = false;
                            } else if (i == 1) {//mcu
                                mcuUpgraded = true;
                                isMcuUpgradeding = false;
                            }
                            DiagFileUtils.saveToFile(md5SaveFile.getAbsolutePath(), newMd5, true);
                        } else {//升级失败
                            isVciUpgradeding = false;
                            isMcuUpgradeding = false;
                            return i + ":" + res;
                        }
                    } catch (TimeoutException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        isVciUpgradeding = false;
                        isMcuUpgradeding = false;
                        return i + ":UPGRADE TIMEOUT";
                    }
                }
            }
            upgrade = false;
            if (!Constants.isDP801Device) {
                if (rfidFile.exists()) {
                    runOnUiThread(() -> {
                        dialog.setmProgressVisibility(View.GONE);
                        dialog.setMpbVisibility(View.VISIBLE);
                    });
                    final int fileSize = Long.valueOf(rfidFile.length()).intValue();
                    final ByteBuffer buffer = ByteBuffer.allocateDirect(fileSize + 4);
                    try {
                        byte[] data = FileUtils.readFileToByteArray(rfidFile);
//                    LogUtils.i("UpgradeV3Task", "file data GetRFIDUpgradeable:" + PrintUtils.printBytes(data));
                        buffer.put(data);
                    } catch (Exception e) {
                        return 2 + ":READ FILE FAIL-" + rfidFile.getName();
                    }
                    try {
                        newMd5 = BinaryUtil.calculateBase64Md5(rfidFile.getAbsolutePath());
                        md5SaveFile = new File(rfidFile.getParent(), "rfid.md5");
                        if (md5SaveFile.exists()) {
                            String line = DiagFileUtils.readLineFromFile(md5SaveFile);
                            if (!newMd5.equals(line)) {
                                upgrade = true;
                            }
                        } else {
                            upgrade = true;
                        }
                        if (!Constants.is64Bit()) {//DP800 或 DP8000 时才检查升级
                            upgrade = false;
                        }
                        if (upgrade) {
                            runOnUiThread(() -> {
                                dialog.setTitleText(getResources().getString(R.string.rfid_upgrading));
                                publishProgress(0);
                                dialog.setContentText("");
                            });
                            Future<Integer> future = singleThreadExecutor.submit(new Callable<Integer>() {
                                @Override
                                public Integer call() {
                                    LogUtils.i("UpgradeV3Task", "调用：GetRFIDUpgradeable");
                                    //1、获取rfid升级状态
                                    return mDpApplication.core.getRFIDUpgradeable(buffer, fileSize);
                                }
                            });
                            try {
                                int res = future.get(25, TimeUnit.SECONDS);
                                LogUtils.i("UpgradeV3Task", "GetRFIDUpgradeable:" + res);
                                if (res == 0) {
                                    DiagFileUtils.saveToFile(md5SaveFile.getAbsolutePath(), newMd5, true);
                                    rfidUpgraded = true;
                                } else if (res != 1) {
                                    return 2 + ":GET RFID UPGRADEABLE FAILED [" + res + "]";
                                }
                            } catch (TimeoutException | InterruptedException |
                                     ExecutionException e) {
                                e.printStackTrace();
                                return 2 + ":GET RFID UPGRADEABLE TIMEOUT";
                            }
                            //设置rfid升级进度最大值
                            if (rfidMax == -1) {
                                try {
                                    byte[] bytes = FileUtils.readFileToByteArray(rfidFile);
                                    rfidMax = (bytes.length - 4) / 128;
                                    if ((bytes.length - 1) % 128 != 0) {
                                        rfidMax += 1;
                                    }
                                    mDpApplication.set("rfidMax", rfidMax);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            LogUtils.i("UpgradeV3Task", "rfidMax:" + rfidMax);
                            dialog.setPbMax(rfidMax);
                            Future<Integer> future1 = singleThreadExecutor.submit(new Callable<Integer>() {
                                @Override
                                public Integer call() throws Exception {
                                    if (rfidUpgraded) {
                                        //rfid无需更新 直接返回
                                        return 1;
                                    }
                                    byte[] buf = new byte[132];
                                    int ret;
                                    byte[] bytes = FileUtils.readFileToByteArray(rfidFile);

                                    int count = (bytes.length - 4) / 128;
                                    ByteBuffer buffer = ByteBuffer.allocateDirect(132);
                                    isRfidUpgradeding = true;
                                    for (int i = 0; i < count; i++) {
                                        if (i == 0) {
                                            System.arraycopy(bytes, 0, buf, 0, buf.length);
                                        } else {
                                            System.arraycopy(bytes, 128 * i + 4, buf, 0, buf.length);
                                        }
                                        System.out.println(Arrays.toString(buf));
                                        buffer.clear();
                                        buffer.put(buf);
//                                    LogUtils.i("UpgradeV3Task", "read buf:" + PrintUtils.toHexString(buffer, 0, buf.length, true));
                                        //2、开始升级rfid
                                        ret = mDpApplication.core.UpgradeRFID(buffer, i, 128, i == 0);
                                        if (i == 0) {
                                            buf = new byte[128];
                                            buffer = ByteBuffer.allocateDirect(128);
                                        }
                                        if (ret != 0) {
                                            return 33;
                                        }
                                        publishProgress(i + 1);
                                    }
                                    //没有整除最后一次发送
                                    if ((bytes.length - 4) % buf.length != 0) {
                                        final ByteBuffer bf = ByteBuffer.allocateDirect((bytes.length - 4) % buf.length);
                                        buf = new byte[(bytes.length - 4) % buf.length];
                                        System.arraycopy(bytes, 128 * count + 4, buf, 0, buf.length);
                                        bf.put(buf);
                                        //2、开始升级rfid
                                        ret = mDpApplication.core.UpgradeRFID(bf, count, buf.length, false);
//                                    LogUtils.i("UpgradeV3Task", "last line read buf:" + PrintUtils.toHexString(bf, 0, bytes.length % buf.length, true));
//                                        logger.info("升级最后的数据：" + PrintUtils.toHexString(bf, 0, bytes.length % buf.length, true));
                                        if (ret != 0) {
                                            return 33;
                                        }
                                        publishProgress(rfidMax);
                                    }
                                    byte[] bs = new byte[]{(byte) 128, 33, (byte) 241, 1, 32, (byte) 179};//80 21 F1 01 20 B3
                                    final ByteBuffer bfData = ByteBuffer.allocateDirect(bs.length);
                                    bfData.put(bs);
//                                LogUtils.i("UpgradeV3Task", "固定数据:::" + Arrays.toString(bs));
                                    //3、发送数据
                                    ret = mDpApplication.core.SendRFIDData(bfData, bs.length);
                                    return ret;
                                }
                            });
                            int ret = 0;
                            try {
                                ret = future1.get(35, TimeUnit.SECONDS);
                                if (ret == 1) {
                                    rfidUpgraded = true;
                                    isRfidUpgradeding = false;
                                    DiagFileUtils.saveToFile(md5SaveFile.getAbsolutePath(), newMd5, true);
                                } else {
                                    isRfidUpgradeding = false;
                                    return 2 + ":" + ret;
                                }
                            } catch (ExecutionException | InterruptedException |
                                     TimeoutException e) {
                                isRfidUpgradeding = false;
                                e.printStackTrace();
                            }
                            LogUtils.i("UpgradeV3Task", "update rfid:" + ret);
                        } else {
                            publishProgress(rfidMax);
                        }
                    } catch (Exception e) {
                        isRfidUpgradeding = false;
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> {
                        dialog.setTitleText(getString(com.obdstar.common.ui.R.string.please_wait));
                        dialog.setmProgressVisibility(View.VISIBLE);
                        dialog.setMpbVisibility(View.GONE);

                    });
                    isRfidUpgradeding = false;
                    return "2 file[" + rfidFile.getName() + "] " + getResources().getString(R.string.is_not_exist);
                }
            }
//            publishProgress(rfidMax);
            return "";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (isCancelled()) {
                return;
            }
            dialog.setProgress(progress[0]);
            if (isVciUpgradeding) {
                if (progress[0] == vciMax) {
//                    LogUtils.i("UpgradeV3Task", "vciprogress:" + progress[0]);
                    dialog.setContentText(getString(R.string.upgrade_done));
                }
            } else if (isMcuUpgradeding) {
                if (progress[0] == mcuMax) {
//                    LogUtils.i("UpgradeV3Task", "mcuprogress:" + progress[0]);
                    dialog.setContentText(getString(R.string.upgrade_done));
                }
            } else if (isRfidUpgradeding) {
//                LogUtils.i("UpgradeV3Task", "rfidprogress:" + progress[0]);
                if (progress[0] == rfidMax) {
                    dialog.setContentText(getString(R.string.upgrade_done));
                }
            }
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            if ("".equals(result)) {
                dialog.dismiss();
            } else {
                if (result.startsWith("-1:")) {
                    dialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                    dialog.setTitleText(getString(com.obdstar.module.diag.R.string.need_upgrade));
                } else if (result.startsWith("0:")) {
                    dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText(getString(com.obdstar.module.diag.R.string.vci_upgrade_failed));
                } else if (result.startsWith("1:")) {
                    dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText(getString(com.obdstar.module.diag.R.string.muc_upgrade_failed));
                } else if (result.startsWith("2:")) {
                    dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText(getString(R.string.rfid_upgrade_failed));
                }
                dialog.setContentText(strCode + result);
                dialog.setMpbVisibility(View.GONE);
                dialog.setCancelable(true);
                dialog.showCancelButton(true);
            }
            LogUtils.i("UpgradeV3Task", "onPostExecute");
            super.onPostExecute(result);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
