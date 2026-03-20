package com.obdstar.x300dp.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.obdstar.common.core.Constants;
import com.obdstar.common.core.config.db.App;
import com.obdstar.common.core.config.module.Cfg;
import com.obdstar.common.ui.view.ToastUtil;
import com.obdstar.module.account.router.LoginNavigationCallbackImpl;
import com.obdstar.x300dp.favorites.BatchFavoritesActivity;
import com.obdstar.x300dp.R;
import com.obdstar.x300dp.acitvity.DiagChoiceActivity;
import com.obdstar.x300dp.utils.RouteConstants;
import com.obdstar.x300dp.view.OcrChoiceDialog;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LONG
 * //诊断(防盗)页面右侧单页部分
 */
public class IconPagerFragment extends IconBaseFragment {

    private boolean first = true;
    //    private final String TAG = "IconPagerFragment";
    private boolean showAppTitle = false;
    private String sn = "";
    private String module = "";
    private String category = "";
    private List<App> data = new ArrayList<>();//数据源
    private final int number = Constants.is10InchesDevice ? 15 : (Constants.is5InchesDevice ? 9 : 12);

    List<TextView> tvFors = new ArrayList<>(number);//for字段
    List<ImageView> btnStarList = new ArrayList<>(number);//收藏(分类小图标)
    List<ImageView> btnVehicleList = new ArrayList<>(number);//椭圆图片
    List<TextView> tvVehicleList = new ArrayList<>(number);//汽车品牌名称集合
    List<ImageView> btnUpgradeList = new ArrayList<>(number);//升级按钮集合
    List<RelativeLayout> ibBackgroundList = new ArrayList<>(number);
    File iconDir = new File(Cfg.ICON_DIR + "ICON/");
    File logoIconDir = new File(Constants.APP_ROOT + "/.data/ICON/");//小图标路径

    private static final int REQUEST_CODE_VINCODE = 1;
    private OcrChoiceDialog mChoiceDialog;
    //    private OcrResultDialog mResultDialog;
//    private ObdstarKeyboard mObdstarKeyboard;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
//        Log.e(TAG, "1 onCreate()" + this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        Log.e(TAG, "2 onCreateView()" + this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            module = bundle.getString("module", "");
            category = bundle.getString("category", "");
            sn = bundle.getString("sn", "");
            data = (List<App>) bundle.get("data");
            showAppTitle = bundle.getBoolean("showTitle", false);
        }
        View view = getView();
        if (view != null) {
            return view;
        }
        if (showAppTitle) {
            if (Constants.is5InchesDevice) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.vehicle_pager_view_special_9, null);
            } else {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.vehicle_pager_view_special_12, null);
            }
        } else {
            if (Constants.is5InchesDevice) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.vehicle_pager_view_9, null);
            } else {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.vehicle_pager_view_12, null);
            }

        }
        this.clear();
        for (int i = 0; i < number; i++) {
            int forId = 0;
            int starId = 0;
            int ibtnID = 0;
            int tvID = 0;
            int btnUpdataID = 0;
            int ibBackgroundID = 0;
            switch (i) {
                case 0:
                    forId = R.id.tvFor1;
                    starId = R.id.iv_star_01;
                    ibtnID = R.id.ibtn_01;
                    tvID = R.id.tv_01;
                    btnUpdataID = R.id.ibtn_updata_01;
                    ibBackgroundID = R.id.ib_background_01;
                    break;
                case 1:
                    forId = R.id.tvFor2;
                    starId = R.id.iv_star_02;
                    ibtnID = R.id.ibtn_02;
                    tvID = R.id.tv_02;
                    btnUpdataID = R.id.ibtn_updata_02;
                    ibBackgroundID = R.id.ib_background_02;
                    break;
                case 2:
                    forId = R.id.tvFor3;
                    starId = R.id.iv_star_03;
                    ibtnID = R.id.ibtn_03;
                    tvID = R.id.tv_03;
                    btnUpdataID = R.id.ibtn_updata_03;
                    ibBackgroundID = R.id.ib_background_03;
                    break;
                case 3:
                    forId = R.id.tvFor4;
                    starId = R.id.iv_star_04;
                    btnUpdataID = R.id.ibtn_updata_04;
                    ibBackgroundID = R.id.ib_background_04;
                    ibtnID = R.id.ibtn_04;
                    tvID = R.id.tv_04;
                    break;
                case 4:
                    forId = R.id.tvFor5;
                    starId = R.id.iv_star_05;
                    ibtnID = R.id.ibtn_05;
                    tvID = R.id.tv_05;
                    btnUpdataID = R.id.ibtn_updata_05;
                    ibBackgroundID = R.id.ib_background_05;
                    break;
                case 5:
                    forId = R.id.tvFor6;
                    starId = R.id.iv_star_06;
                    ibtnID = R.id.ibtn_06;
                    tvID = R.id.tv_06;
                    btnUpdataID = R.id.ibtn_updata_06;
                    ibBackgroundID = R.id.ib_background_06;
                    break;
                case 6:
                    forId = R.id.tvFor7;
                    starId = R.id.iv_star_07;
                    ibtnID = R.id.ibtn_07;
                    tvID = R.id.tv_07;
                    btnUpdataID = R.id.ibtn_updata_07;
                    ibBackgroundID = R.id.ib_background_07;
                    break;
                case 7:
                    forId = R.id.tvFor8;
                    starId = R.id.iv_star_08;
                    ibtnID = R.id.ibtn_08;
                    tvID = R.id.tv_08;
                    btnUpdataID = R.id.ibtn_updata_08;
                    ibBackgroundID = R.id.ib_background_08;
                    break;
                case 8:
                    forId = R.id.tvFor9;
                    starId = R.id.iv_star_09;
                    ibtnID = R.id.ibtn_09;
                    tvID = R.id.tv_09;
                    btnUpdataID = R.id.ibtn_updata_09;
                    ibBackgroundID = R.id.ib_background_09;
                    break;
                case 9:
                    forId = R.id.tvFor10;
                    starId = R.id.iv_star_10;
                    ibtnID = R.id.ibtn_10;
                    tvID = R.id.tv_10;
                    btnUpdataID = R.id.ibtn_updata_10;
                    ibBackgroundID = R.id.ib_background_10;
                    break;
                case 10:
                    forId = R.id.tvFor11;
                    starId = R.id.iv_star_11;
                    ibtnID = R.id.ibtn_11;
                    tvID = R.id.tv_11;
                    btnUpdataID = R.id.ibtn_updata_11;
                    ibBackgroundID = R.id.ib_background_11;
                    break;
                case 11:
                    forId = R.id.tvFor12;
                    starId = R.id.iv_star_12;
                    ibtnID = R.id.ibtn_12;
                    tvID = R.id.tv_12;
                    btnUpdataID = R.id.ibtn_updata_12;
                    ibBackgroundID = R.id.ib_background_12;
                    break;
                /*case 12:
                    forId = R.id.tvFor13;
                    starId = R.id.iv_star_13;
                    ibtnID = R.id.ibtn_13;
                    tvID = R.id.tv_13;
                    btnUpdataID = R.id.ibtn_updata_13;
                    ibBackgroundID = R.id.ib_background_13;
                    break;
                case 13:
                    forId = R.id.tvFor14;
                    starId = R.id.iv_star_14;
                    ibtnID = R.id.ibtn_14;
                    tvID = R.id.tv_14;
                    btnUpdataID = R.id.ibtn_updata_14;
                    ibBackgroundID = R.id.ib_background_14;
                    break;
                case 14:
                    forId = R.id.tvFor15;
                    starId = R.id.iv_star_15;
                    ibtnID = R.id.ibtn_15;
                    tvID = R.id.tv_15;
                    btnUpdataID = R.id.ibtn_updata_15;
                    ibBackgroundID = R.id.ib_background_15;
                    break;*/
                default:
                    break;
            }
            tvFors.add(view.findViewById(forId));
            ImageView starImg = view.findViewById(starId);
            btnStarList.add(starImg);
            btnVehicleList.add(view.findViewById(ibtnID));
            tvVehicleList.add(view.findViewById(tvID));
            btnUpgradeList.add(view.findViewById(btnUpdataID));
            ibBackgroundList.add(view.findViewById(ibBackgroundID));
        }
        mChoiceDialog = new OcrChoiceDialog(getContext());
        mChoiceDialog.setOnChoiceListener(new OcrChoiceDialog.OnChoiceListener() {
            @Override
            public void onBarCode() {
                ARouter.getInstance().build(RouteConstants.ROUTE_OCR_BAR_CODE)
                        .navigation(getContext());
                mChoiceDialog.dismiss();
            }

            @Override
            public void onText() {
                ARouter.getInstance().build(RouteConstants.ROUTE_OCR_TEXT)
                        .navigation(getContext());
                mChoiceDialog.dismiss();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        Log.e(TAG, "3 onActivityCreated()" + this);
        Bundle bundle = savedInstanceState;
        if (first && bundle != null && !TextUtils.isEmpty(bundle.getString("module", ""))) {
            sn = bundle.getString("sn", "");
            module = bundle.getString("module", "");
            category = bundle.getString("category", "");
            data = (List<App>) bundle.get("data");
            showAppTitle = bundle.getBoolean("showTitle", false);
            refreshVehicleIcon();
            return;
        }
        bundle = getArguments();
        if (bundle != null) {
            sn = bundle.getString("sn", "");
            module = bundle.getString("module", "");
            category = bundle.getString("category", "");
            data = (List<App>) bundle.get("data");
            showAppTitle = bundle.getBoolean("showTitle", false);
        }
        refreshVehicleIcon();
    }

    public void refreshVehicleIcon() {
        if (data == null || data.size() < 1) {
            return;
        }
        int size = data.size();
        for (int i = 0; i < size && i < number; i++) {
            showIcon(i, data.get(i));
        }
        if (size < number) {//添加空项
            for (int i = number - 1; i >= size; i--) {
                ibBackgroundList.get(i).setVisibility(View.INVISIBLE);
            }
        }
    }

    private long mStartTime;

    @SuppressLint("ResourceAsColor")
    public void showIcon(final int index, final App app) {

        if ((DiagChoiceActivity.ALL_FLAG.equals(category) || DiagChoiceActivity.FAVORITES_FLAG.equals(category))) {
            ImageView imageView = btnStarList.get(index);
            if (app.category.contains(",")) {
                imageView.setImageResource(R.drawable.comp_icon);
            } else {
                String imgName = DiagChoiceActivity.logoIcon.get(app.category);
                if (logoIconDir.exists() && !TextUtils.isEmpty(imgName)) {
                    String replace = imgName.replace(".", "_icon.");
                    File icon1 = new File(logoIconDir, replace);
                    if (icon1.exists() && !TextUtils.isEmpty(app.category)) {
                        Drawable drawable = Drawable.createFromPath(icon1.getAbsolutePath());
                        imageView.setImageDrawable(drawable);
                        imageView.setVisibility(View.VISIBLE);
                    } else {
                        btnStarList.get(index).setVisibility(View.GONE);//收藏星星全部隐藏
//                    if (tvFors.get(index) != null) {
//                        tvFors.get(index).setVisibility(View.GONE);
//                    }
                    }
                } else {
                    btnStarList.get(index).setVisibility(View.GONE);//收藏星星全部隐藏
//                if (tvFors.get(index) != null) {
//                    tvFors.get(index).setVisibility(View.GONE);
//                }
                }
            }

        } else {
//            btnStarList.get(index).setVisibility(View.GONE);//收藏星星全部隐藏
//            if (tvFors.get(index) != null) {
//                tvFors.get(index).setVisibility(View.GONE);
//            }
            if (btnStarList.get(index) != null) {
                btnStarList.get(index).setVisibility(View.GONE);
            }
        }

        if (app.flag == DiagChoiceActivity.ITEM_TYPE_ADD) {//收藏添加
            ImageView imageView = btnVehicleList.get(index);
            imageView.setImageResource(R.drawable.add_favorites);
            imageView.setBackgroundColor(android.R.color.transparent);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageView.setLayoutParams(layoutParams);

            if (tvFors.get(index) != null) {
                tvFors.get(index).setVisibility(View.GONE);
            }
            tvVehicleList.get(index).setVisibility(View.GONE);
            btnUpgradeList.get(index).setVisibility(View.GONE);
            ibBackgroundList.get(index).setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), BatchFavoritesActivity.class);
                Bundle bundle = new Bundle();//创建一个句柄
                DiagChoiceActivity activity = (DiagChoiceActivity) getActivity();
                bundle.putString("module", activity.currModuleCode);
                bundle.putString("name", activity.currModuleName);
                bundle.putInt("type", BatchFavoritesActivity.TYPE_ADD);
//                    bundle.putSerializable("aa", (Serializable) selectVehicleFragment.AreaMap);
                intent.putExtras(bundle);
                activity.startActivityForResult(intent, DiagChoiceActivity.REQUEST_CODE);
            });
            return;
        }
        if (app.flag == DiagChoiceActivity.ITEM_TYPE_DELETE) {//收藏删除
            ImageView imageView = btnVehicleList.get(index);
            imageView.setImageResource(R.drawable.cancel_favorites);
            imageView.setBackgroundColor(android.R.color.transparent);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            imageView.setLayoutParams(layoutParams);

            if (tvFors.get(index) != null) {
                tvFors.get(index).setVisibility(View.GONE);
            }
            tvVehicleList.get(index).setVisibility(View.GONE);
            btnUpgradeList.get(index).setVisibility(View.GONE);
            ibBackgroundList.get(index).setOnClickListener(v -> {
                /**
                 * todo 跳转收藏
                 */
                Intent intent = new Intent(getContext(), BatchFavoritesActivity.class);
                Bundle bundle = new Bundle();//创建一个句柄
                DiagChoiceActivity activity = (DiagChoiceActivity) getActivity();
                bundle.putString("module", activity.currModuleCode);
                bundle.putString("name", activity.currModuleName);
                bundle.putInt("type", BatchFavoritesActivity.TYPE_DELETE);
                intent.putExtras(bundle);
                activity.startActivityForResult(intent, DiagChoiceActivity.REQUEST_CODE);
            });
            return;
        }

        if (app.flag == DiagChoiceActivity.ITEM_TYPE_AUTO) {//自动检测
            ImageView imageView = btnVehicleList.get(index);
            createPressedSelector(imageView, ContextCompat.getDrawable(getContext(), R.drawable.quick_open_auto_bg_nor),
                    ContextCompat.getDrawable(getContext(), R.drawable.quick_open_auto_bg_sel));

            tvVehicleList.get(index).setVisibility(View.VISIBLE);
            tvVehicleList.get(index).setText(app.name);
            btnUpgradeList.get(index).setVisibility(View.GONE);
            ibBackgroundList.get(index).setOnClickListener(v -> {
                ToastUtil.showToast(getContext(), "自动检测", 2);
            });
            return;
        }

        if (app.flag == DiagChoiceActivity.ITEM_TYPE_INPUT) {//手动输入
            ImageView imageView = btnVehicleList.get(index);
            createPressedSelector(imageView, ContextCompat.getDrawable(getContext(), R.drawable.quick_open_input_bg_nor),
                    ContextCompat.getDrawable(getContext(), R.drawable.quick_open_input_bg_sel));

            tvVehicleList.get(index).setVisibility(View.VISIBLE);
            tvVehicleList.get(index).setText(app.name);
            btnUpgradeList.get(index).setVisibility(View.GONE);
            ibBackgroundList.get(index).setOnClickListener(v -> {
                ToastUtil.showToast(getContext(), "手动输入", 2);
            });
            return;
        }

        if (app.flag == DiagChoiceActivity.ITEM_TYPE_SCAN) {//扫描
            ImageView imageView = btnVehicleList.get(index);
            createPressedSelector(imageView, ContextCompat.getDrawable(getContext(), R.drawable.quick_open_scan_bg_nor),
                    ContextCompat.getDrawable(getContext(), R.drawable.quick_open_scan_bg_sel));

            tvVehicleList.get(index).setVisibility(View.VISIBLE);
            tvVehicleList.get(index).setText(app.name);
            btnUpgradeList.get(index).setVisibility(View.GONE);
            ibBackgroundList.get(index).setOnClickListener(v -> {
                XXPermissions.with(this)
                        // 申请单个权限
                        .permission(Permission.CAMERA)
                        .permission(Permission.READ_EXTERNAL_STORAGE)
                        .permission(Permission.WRITE_EXTERNAL_STORAGE)
                        .request(new OnPermissionCallback() {

                            @Override
                            public void onGranted(List<String> permissions, boolean all) {
                                if (all) {
                                    goOcrScanActivity();
                                } else {
                                    ToastUtil.showToast(getContext(), "获取部分权限成功，但部分权限未正常授予", 2);
                                }
                            }

                            @Override
                            public void onDenied(List<String> permissions, boolean never) {
                                if (never) {
                                    ToastUtil.showToast(getContext(), "被永久拒绝授权，请手动授予相机、读取写入内存卡权限", 2);
                                    // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                    XXPermissions.startPermissionActivity(getContext(), permissions);
                                } else {
                                    ToastUtil.showToast(getContext(), "获取权限失败", 2);
                                }
                            }
                        });
            });
            return;
        }

        if (TextUtils.isEmpty(app.icon)) {
            //加载 name
            tvVehicleList.get(index).setVisibility(View.VISIBLE);
            tvVehicleList.get(index).setText(app.name);
        } else {
            Drawable drawable = null;
            Drawable drawableSelected = null;
            if (iconDir.exists()) {
                File icon1 = new File(iconDir, app.icon + ".png");
                File icon2 = new File(iconDir, app.iconSel + ".png");
                if (icon1.exists() && icon2.exists()) {
                    //加载图片
                    drawable = Drawable.createFromPath(icon1.getAbsolutePath());
                    drawableSelected = Drawable.createFromPath(icon2.getAbsolutePath());
                }
            }
            tvVehicleList.get(index).setText(app.name);
            if (drawable != null && drawableSelected != null) {
                createPressedSelector(btnVehicleList.get(index), drawable, drawableSelected);
                tvVehicleList.get(index).setVisibility(View.GONE);
            } else {
                tvVehicleList.get(index).setVisibility(View.VISIBLE);
            }
            if (showAppTitle) {
                tvVehicleList.get(index).setVisibility(View.VISIBLE);
            }
        }
        if (app.upgradable == 1) {
            btnUpgradeList.get(index).setVisibility(View.VISIBLE);
        } else {
            btnUpgradeList.get(index).setVisibility(View.INVISIBLE);
        }
        btnUpgradeList.get(index).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ARouter.getInstance().build("/upgrade/main")
                            .withInt("version", 1)
                            .withString("selectCodes", app.code)
                            .navigation(getActivity(), new LoginNavigationCallbackImpl());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ibBackgroundList.get(index).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currTime = System.currentTimeMillis();
                if ((currTime - mStartTime) / 1000 < 1) {
                    return;
                }
                try {
                    ARouter.getInstance().build("/obdstar/diag/version")
                            .withString("sn", sn)
                            .withString("name", app.name)
                            .withString("code", app.code)
                            .withString("path", app.path)
                            .withString("currModuleName", ((DiagChoiceActivity) getActivity()).currModuleName)
                            .navigation(getActivity(), 8, new LoginNavigationCallbackImpl());
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                vehicleFragment.gotoDiag(functionNode);
                mStartTime = System.currentTimeMillis();
            }
        });
    }

    public void createPressedSelector(ImageView imageView, Drawable drawable, Drawable
            drawableSelected) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, drawableSelected);//  状态  , 设置按下的图片
        stateListDrawable.addState(new int[]{}, drawable);//默认状态,默认状态下的图片
        imageView.setImageDrawable(stateListDrawable);
        imageView.setAdjustViewBounds(true);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
//        Log.e(TAG, "4 onSaveInstanceState()" + this);
        super.onSaveInstanceState(bundle);
        // Save State Here
        bundle.putString("sn", sn);
        bundle.putString("module", module);
        bundle.putSerializable("data", (Serializable) data);
        bundle.putBoolean("showTitle", showAppTitle);
    }

    @Override
    public void onDestroyView() {
//        Log.e(TAG, "5 onDestroyView()" + this);
        super.onDestroyView();
    }
    @Override
    public void clear() {
        btnStarList.clear();
        btnVehicleList.clear();
        tvVehicleList.clear();
        btnUpgradeList.clear();
        ibBackgroundList.clear();
        first = false;
    }

//    public boolean isSpecial(String module) {
//        return Module.OR.name().equalsIgnoreCase(module)
//                || Module.OTHER.name().equalsIgnoreCase(module)
//                || Module.PROG.name().equalsIgnoreCase(module)
//                || Module.RFID.name().equalsIgnoreCase(module);
//    }


    private void goOcrScanActivity() {
        mChoiceDialog.showDialog();
    }
}
