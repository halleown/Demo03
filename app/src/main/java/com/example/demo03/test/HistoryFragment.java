package com.obdstar.x300dp.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.obdstar.common.core.Constants;
import com.obdstar.common.core.IObdstarApplication;
import com.obdstar.common.core.log.LogUtils;
import com.obdstar.common.core.utils.DoubleClickUtil;
import com.obdstar.common.core.utils.Utils;
import com.obdstar.common.ui.view.ObdstarKeyboard;
import com.obdstar.common.ui.view.ToastUtil;
import com.obdstar.module.data.manager.entity.DiagReportListBean;
import com.obdstar.module.data.manager.help.RecyclerViewDivider;
import com.obdstar.x300dp.R;
import com.obdstar.x300dp.acitvity.DiagChoiceActivity;
import com.obdstar.x300dp.adapters.DiagHistoryRvAdapter;
import com.obdstar.x300dp.adapters.DiagHistorySubAdapter;
import com.obdstar.x300dp.model.DiagHistoryBean;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * @author Luuuzi
 * 诊断菜单历史记录
 */
public class HistoryFragment extends IconBaseFragment implements View.OnClickListener {
    private static final String TAG = "HistoryFragment";
    private View rootView;
    private DiagChoiceActivity mActivity;
    private String sn;
    private IObdstarApplication mApplication;
    //统计数量
    Map<String, Integer> yearMonthCount = new HashMap<>();
    private final List<File> allFileList = new ArrayList<>();
    /**
     * 右侧 年月筛选 数据源
     */
    private final List<DiagHistoryBean> yearMonthDatas = new ArrayList<>();
    /**
     * 中间 故障码文件 数据源
     */
    private List<DiagReportListBean> subDatas = new ArrayList<>();
    private final List<DiagReportListBean> querySubDatas = new ArrayList<>();
    private final List<DiagHistoryBean> queryYearMonthDatas = new ArrayList<>();
    private DiagHistoryRvAdapter mAdapter;
    private DiagHistorySubAdapter mSubAdapter;
    private File baseDir;
    private boolean haveData = true;
    private int size;
    private RecyclerView mRvSub;
    private ImageView ivDelete, ivClear, ivCheckAll, ivEditData, ivEditBack;
    private EditText mSearchEdt;
    private ProgressBar progressBar;
    private LinearLayout ll, ll_empty;
    private Dialog mDeleteDialog;

    private final PublishSubject<String> mPublishSubject = PublishSubject.create();
    private final List<DiagReportListBean> selectedSubList = new ArrayList<>();
    private final List<File> deletedFileList = new ArrayList<>();
    private final List<DiagReportListBean> deletedSubList = new ArrayList<>();

    public ObdstarKeyboard obdstarKeyboard;
    /**
     * 选中的年月position
     */
    private int mLastSelectedP = 0;

    private static final String FILE_PTAH = Constants.APP_ROOT + "/DATA/" + Constants.DIAG_HISTORY;
    //    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);//todo  到月份
//    private SimpleDateFormat sdf;
    RecyclerView mRv;
    View content_bg;
    /**
     * 存放故障码数量 key：名称 value：数量
     */
    private Map<String, Integer> faultNumberMap = new HashMap<>();

    private boolean searchMode = false;

    public static HistoryFragment newInstance(EditText mSearchEdt, ImageView clear) {
        HistoryFragment historyFragment = new HistoryFragment();
        historyFragment.mSearchEdt = mSearchEdt;
        historyFragment.ivClear = clear;
        return historyFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (obdstarKeyboard != null && obdstarKeyboard.getVisibility() == View.VISIBLE) {
                    obdstarKeyboard.hideKeyboard();
                    return;
                }

                if (mSubAdapter != null && mSubAdapter.isDeleteMode()) {
                    handleBack();
                } else {
                    this.setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = getView();
        if (rootView != null) {
            return rootView;
        }
        mActivity = (DiagChoiceActivity) getActivity();
        rootView = LayoutInflater.from(mActivity).inflate(R.layout.fragment_history, null);
        initView();
        setListener();
        initData();
        return rootView;
    }

    private void initView() {
        mApplication = (IObdstarApplication) mActivity.getApplication();
        sn = mApplication.getSN();
        progressBar = rootView.findViewById(R.id.pb);

        ivDelete = rootView.findViewById(R.id.iv_delete_data);
        ivCheckAll = rootView.findViewById(R.id.iv_check_all);
        ivEditData = rootView.findViewById(R.id.iv_edit_data);
        ivEditBack = rootView.findViewById(R.id.iv_edit_back);
        ivEditData.setVisibility(View.VISIBLE);

        mRv = rootView.findViewById(R.id.lv_filter_date);
        mRvSub = rootView.findViewById(R.id.lv_sub_history);
        ll = rootView.findViewById(R.id.ll_content_container);
        ll_empty = rootView.findViewById(R.id.ll_empty);
        content_bg = rootView.findViewById(R.id.content_bg);
        ConstraintLayout parent = rootView.findViewById(R.id.con_parent);
        obdstarKeyboard = new ObdstarKeyboard(mActivity, parent);
        obdstarKeyboard.setEditText(mSearchEdt);
        obdstarKeyboard.initKeys('A');

        mAdapter = new DiagHistoryRvAdapter(mActivity);
        mSubAdapter = new DiagHistorySubAdapter(mActivity);
        mRv.setLayoutManager(new LinearLayoutManager(mActivity));

        mRvSub.setLayoutManager(new LinearLayoutManager(mActivity));
        // item分隔线
        mRvSub.addItemDecoration(new RecyclerViewDivider(mActivity));
        mRv.setAdapter(mAdapter);
        mAdapter.setData(yearMonthDatas);
        mSubAdapter.setData(subDatas);
        mRvSub.setAdapter(mSubAdapter);

        ivDelete.setOnClickListener(this);
        ivCheckAll.setOnClickListener(this);
        ivEditData.setOnClickListener(this);
        ivEditBack.setOnClickListener(this);

        mSearchEdt.setCursorVisible(false);
        mSearchEdt.setText("");
        ll_empty.setVisibility(View.GONE);
        setSearchTouchListener();
    }

    /**
     *
     * @param yearMonthDatas 当前年月数据源
     * @param currentPos 当前点击的年月pos
     */
    private void nothing(List<DiagHistoryBean> yearMonthDatas, int currentPos) {
        // 取消之前的选中年月
        for (DiagHistoryBean bean : yearMonthDatas) {
            bean.setSelected(false);
        }

        if (currentPos <= yearMonthDatas.size() - 1) {
            // 选中当前的年月
            yearMonthDatas.get(currentPos).setSelected(true);
        }
        mAdapter.notifyDataSetChanged();

        mLastSelectedP = currentPos;
        if (obdstarKeyboard != null && obdstarKeyboard.getVisibility() == View.VISIBLE) {
            obdstarKeyboard.hideKeyboard();
        }
        // initQuerySubDatas(yearMonthDatas, currentPos, subDatas);
        List<DiagReportListBean> subDatas = yearMonthDatas.get(currentPos).getSubData();
        //排序
        Collections.sort(subDatas);
        querySubDatas.clear();
        querySubDatas.addAll(subDatas);
        this.subDatas.clear();
        this.subDatas.addAll(subDatas);


        showSubEmpty(false);
        resetCheckStatusUI(subDatas);
    }

    /**
     * 初始化第position个年月数据
     * @param position
     */
    private void initQuerySubDatas(List<DiagHistoryBean> source, int position) {
        querySubDatas.clear();
        if (!source.isEmpty()) {
            querySubDatas.addAll(source.get(position).getSubData());
        }
    }

    private List<DiagReportListBean> getSubDatas() {
        if (searchMode) {
            return querySubDatas;
        } else {
            return subDatas;
        }
    }

    private List<DiagHistoryBean> getYearMonthDatas() {
        if (searchMode) {
            return queryYearMonthDatas;
        } else {
            return yearMonthDatas;
        }
    }

    @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
    private void setListener() {
        mAdapter.setOnItemClickListener((view, position) -> {
            nothing(getYearMonthDatas(), position);
        });
        mSubAdapter.setOnItemClickListener((view, position) -> {
            DiagReportListBean sub = getSubDatas().get(position);
            if (mSubAdapter.isDeleteMode()) {
                boolean isCheck = sub.isCheck();
                if (isCheck) {
                    sub.setCheck(false);
                    selectedSubList.remove(sub);
                } else {
                    sub.setCheck(true);
                    selectedSubList.add(sub);
                }
                mSubAdapter.notifyDataSetChanged();
                updateEditBtn();
            }
        });

        mSubAdapter.setEndOnClick((view, position) -> {//进入
            if (Utils.isFastClick()) {
                return;
            }
            try {
                DiagReportListBean sub = getSubDatas().get(position);
                Bundle bundle = new Bundle();
                bundle.putString("path", sub.getFile().getAbsolutePath());
                LogUtils.i("aaa", "path:" + sub.getFile().getAbsolutePath());

                bundle.putString("mBrandName", sub.bran);
                ARouter.getInstance().build("/diagCore/history").with(bundle).navigation(mActivity);
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        });

        mSubAdapter.setItemLongClickListener((view, position) -> {
            if (DoubleClickUtil.getInstance().enableClick()) {
                DiagReportListBean sub = getSubDatas().get(position);
                if (mSubAdapter.isDeleteMode()) { //编辑模式下
                    boolean isCheck = sub.isCheck();
                    if (isCheck) {
                        sub.setCheck(false);
                        selectedSubList.remove(sub);
                    } else {
                        sub.setCheck(true);
                        selectedSubList.add(sub);
                    }
                    mSubAdapter.notifyDataSetChanged();
                    updateEditBtn();
                } else {
                    //长按删除当前项
                    mDeleteDialog = new Dialog(mActivity, com.obdstar.common.ui.R.style.BaseDialogTheme);
                    LayoutInflater factory = LayoutInflater.from(mActivity);
                    View view1 = factory.inflate(com.obdstar.common.ui.R.layout.diag_delete_file, null);
                    TextView tvContent = view1.findViewById(com.obdstar.common.ui.R.id.tv_context);
                    tvContent.setText(getResources().getText(com.obdstar.common.ui.R.string.delete_file_tip));
                    mDeleteDialog.setContentView(view1);

                    Button btnYes = view1.findViewById(R.id.ll_ok);
                    Button btnNo = view1.findViewById(R.id.ll_esc);
                    btnYes.setText(com.obdstar.common.ui.R.string.delete);
                    btnNo.setOnClickListener(v1 -> {
                        mDeleteDialog.dismiss();
                    });
                    btnYes.setOnClickListener(v12 -> doDelete(sub));
                    mDeleteDialog.setCanceledOnTouchOutside(true);
                    mDeleteDialog.show();
                }

            }

        });

        setSearchTouchListener();
        mSearchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mPublishSubject != null && mSearchEdt.isCursorVisible()) {
                    Log.d("HistoryFragment", "xialj___onTextChanged: 搜索文本：" + s.toString().trim());
                    mPublishSubject.onNext(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mSearchEdt.getText().length() != 0) {
                    ivClear.setImageResource(com.obdstar.common.ui.R.drawable.ic_clear2);
                } else {
                    ivClear.setImageResource(com.obdstar.common.ui.R.drawable.ic_search2);
                }
            }
        });
        ivClear.setOnClickListener(v -> {
            if (mSearchEdt.getText().length() != 0) {
                resetSearch();
            }
        });

        mPublishSubject.debounce(400, TimeUnit.MILLISECONDS).filter(s -> {
            if (s.length() > 0 && progressBar != null) {
                mActivity.runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
            } else if (TextUtils.isEmpty(s)) {
                return true;
            }
            return s.length() > 0;
        }).switchMap(new Function<String, ObservableSource<List<DiagHistoryBean>>>() {
            @Override
            public ObservableSource<List<DiagHistoryBean>> apply(String query) throws Exception {
                return getSearchObservable(query);
            }
        }).observeOn(AndroidSchedulers.mainThread()).compose(mActivity.bindToLifecycle()).subscribe(new Observer<List<DiagHistoryBean>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<DiagHistoryBean> data) {
                Log.d("HistoryFragment", "xialj___onNext: 搜索到结果：" + data.size());
                searchMode = true;
                resetCheckStatusUI(null);
                if (!data.isEmpty()) {
                    showSubEmpty(false);
                    showQueryData(data);
                } else {
                    querySubDatas.clear();
                    queryYearMonthDatas.clear();
                    showSubEmpty(true);
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onComplete() {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 取消选中所有复选框
     */
    private void resetCheckStatusUI(List<DiagReportListBean> subDates) {
        selectedSubList.clear();
        subDates = subDates == null ? getSubDatas() : subDates;
        for (DiagReportListBean sub : subDates) {
            sub.setCheck(false);
            sub.setSortType(0);
        }
        mSubAdapter.setData(subDates);
        if (mSubAdapter.isDeleteMode()) {
            updateEditBtn();
        }
    }

    private void resetSearch() {
        obdstarKeyboard.hideKeyboard();
        mSearchEdt.setText("");
        searchMode = false;
        queryYearMonthDatas.clear();
        querySubDatas.clear();
        mPublishSubject.onNext("");
    }

    private void setSearchTouchListener() {
        mSearchEdt.setOnTouchListener((v, event) -> {
            if (!isVisible()) {
                return false;
            }
            Log.d(TAG, "xialj___HistoryFragment 处理触摸: haveData=" + haveData);
            if (haveData) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mSearchEdt.setCursorVisible(true);
                    mSearchEdt.setShowSoftInputOnFocus(false);
                    if (obdstarKeyboard != null) {
                        obdstarKeyboard.showKeyboard();
                    }
                }
                return true;
            }
            return false;
        });
    }


    private void initData() {
        File cacheDir = new File(FILE_PTAH);
        baseDir = new File(cacheDir.getAbsolutePath());
        if (!baseDir.exists() || baseDir.isFile()) {
            noData();
            return;
        }
        Observable.create((ObservableOnSubscribe<File>) emitter -> {

            scanFile(baseDir);
            if (mFiles.isEmpty()) {
                emitter.onComplete();
                return;
            }
            for (File file : mFiles) {
                emitter.onNext(file);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).compose(mActivity.bindToLifecycle()).subscribe(new Observer<File>() {
            @Override
            public void onSubscribe(Disposable d) {
                allFileList.clear();
            }

            @Override
            public void onNext(File file) {
                allFileList.add(file);
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onError(Throwable e) {
                ToastUtil.showToast(mActivity, e.getMessage(), Toast.LENGTH_SHORT);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onComplete() {
                if (allFileList.isEmpty()) {
                    noData();
                    return;
                }
                Collections.sort(allFileList, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return Long.compare(f2.lastModified(), f1.lastModified());
                    }
                });
                disposeData(yearMonthDatas, subDatas);
            }
        });
    }

    private void handleBack() {
        ivEditBack.setVisibility(View.GONE);
        ivEditData.setVisibility(View.VISIBLE);
        ivDelete.setVisibility(View.GONE);
        ivCheckAll.setVisibility(View.GONE);
        enterDeleteMode(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_edit_data) { //编辑按钮
            if (!haveData) return;
            ivEditBack.setVisibility(View.VISIBLE);
            ivEditData.setVisibility(View.GONE);
            ivDelete.setVisibility(View.GONE);
            ivCheckAll.setVisibility(View.VISIBLE);
            enterDeleteMode(true);
            updateEditBtn();
        } else if (i == R.id.iv_delete_data) { //删除按钮
            if (!haveData) return;
            if (!mSubAdapter.isDeleteMode()) {
                enterDeleteMode(true);
            } else {
                if (selectedSubList.size() <= 0) {
                    ToastUtil.showToast(mActivity, getResources().getString(com.obdstar.common.ui.R.string.please_select), Toast.LENGTH_SHORT);
                    return;
                }
                mDeleteDialog = new Dialog(mActivity, com.obdstar.common.ui.R.style.BaseDialogTheme);
                LayoutInflater factory = LayoutInflater.from(mActivity);
                View view = factory.inflate(com.obdstar.common.ui.R.layout.diag_delete_file, null);
                TextView tvContent = view.findViewById(com.obdstar.common.ui.R.id.tv_context);
                tvContent.setText(getResources().getText(com.obdstar.common.ui.R.string.delete_file_tip));
                mDeleteDialog.setContentView(view);

                Button btnYes = view.findViewById(R.id.ll_ok);
                Button btnNo = view.findViewById(R.id.ll_esc);
                btnYes.setText(com.obdstar.common.ui.R.string.delete);
                btnNo.setOnClickListener(v1 -> {
                    mDeleteDialog.dismiss();
                });
                btnYes.setOnClickListener(v12 -> doDelete());
                mDeleteDialog.setCanceledOnTouchOutside(true);
                mDeleteDialog.show();
            }
        } else if (i == R.id.iv_edit_back) { //返回按钮
            handleBack();
        } else if (i == R.id.iv_check_all) { //全选
            if (selectedSubList.size() == subDatas.size()) {
                for (DiagReportListBean sub : subDatas) {
                    sub.setCheck(false);
                }
                selectedSubList.clear();
            } else {
                selectedSubList.clear();
                for (DiagReportListBean sub : subDatas) {
                    sub.setCheck(true);
                    selectedSubList.add(sub);
                }
            }
            mSubAdapter.notifyDataSetChanged();
            updateEditBtn();
        }
    }

    // @Override
    // public void onResume() {
    //     super.onResume();
    //     setSearchTouchListener();
    // }

    // @Override
    // public void onPause() {
    //     super.onPause();
    //     if (mActivity != null) {
    //         mActivity.initSearchListener();
    //     }
    // }

    // @Override
    // public void onDestroyView() {
    //     if (mActivity != null) {
    //         mActivity.initSearchListener();
    //     }
    //     super.onDestroyView();
    // }

    @Override
    public void clear() {
    }

    /**
     * 显示查询后数据
     */
    private void showQueryData(List<DiagHistoryBean> data) {
        mLastSelectedP = 0;
        initQuerySubDatas(data, 0);

        mSubAdapter.setData(querySubDatas);

        queryYearMonthDatas.clear();
        queryYearMonthDatas.addAll(data);
        mAdapter.setData(queryYearMonthDatas);
    }

    /**
     * 查询数据
     * 可以同时搜索名称和VIN码，不区分大小写以及多余的空格
     */
    private ObservableSource<List<DiagHistoryBean>> getSearchObservable(String query) {
        Log.d("HistoryFragment", "getSearchObservable: 开始搜索数据: " + query);
        List<DiagHistoryBean> allHistoryData = new ArrayList<>(yearMonthDatas);

        return Observable.create((ObservableOnSubscribe<List<DiagHistoryBean>>) emitter -> {
            List<DiagHistoryBean> searchResult = new ArrayList<>();

            if (TextUtils.isEmpty(query)) {
                searchResult.addAll(allHistoryData);
            } else {
                String upQuery = query.toUpperCase().trim();

                for (DiagHistoryBean originalBean : allHistoryData) {
                    List<DiagReportListBean> originalSubs = originalBean.getSubData();
                    if (originalSubs == null || originalSubs.isEmpty()) continue;

                    List<DiagReportListBean> matchedSubs = new ArrayList<>();

                    for (DiagReportListBean sub : originalSubs) {
                        boolean isMatch = false;

                        if (!TextUtils.isEmpty(sub.name)) {
                            String upName = sub.name.replace(" ", "").toUpperCase();
                            if (upName.contains(upQuery)) {
                                isMatch = true;
                            }
                        }

                        if (!isMatch && !TextUtils.isEmpty(sub.vin)) {
                            String upVin = sub.vin.replace(" ", "").toUpperCase();
                            if (upVin.contains(upQuery)) {
                                isMatch = true;
                            }
                        }

                        if (isMatch) {
                            matchedSubs.add(sub);
                        }
                    }

                    if (!matchedSubs.isEmpty()) {
                        DiagHistoryBean filteredBean = new DiagHistoryBean();
                        filteredBean.setDate(originalBean.getDate());
                        filteredBean.setSubData(matchedSubs);
                        searchResult.add(filteredBean);
                    }
                }
            }
            searchResult.get(0).setSelected(true);
            emitter.onNext(searchResult);
            emitter.onComplete();
        }).subscribeOn(Schedulers.computation());
    }

    private final List<File> mFiles = new ArrayList<>();

    private void scanFile(File parentFile) {
        File[] files = parentFile.listFiles();

        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanFile(file);
            } else {
                if (file.getName().toLowerCase(Locale.ENGLISH).endsWith(".json")) {
                    mFiles.add(file);
                } else {
                    continue;
                }
            }
        }
    }

    private boolean AsyncDelete() {
        for (DiagReportListBean sub : selectedSubList) {
            File file = sub.getFile();
            if (file.exists()) {
                if (!file.delete()) {
                    return false;
                }
                Log.d(TAG, "AsyncDelete_all: 被删除的文件vin：" + sub.vin);
                deletedFileList.add(sub.getFile());
                deletedSubList.add(sub);
            }
        }
        return true;
    }

    private boolean AsyncDelete(DiagReportListBean sub) {
        File file = sub.getFile();
        if (file.exists()) {
            if (!file.delete()) {
                return false;
            }
            Log.d(TAG, "AsyncDelete_single: 被删除的文件vin：" + sub.vin);
            deletedFileList.add(sub.getFile());
            deletedSubList.add(sub);
        }
        return true;
    }

    /**
     * 删除勾选数据
     */
    private void doDelete() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                emitter.onNext(AsyncDelete());
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(Boolean result) {
                if (result) {
                    onComplete();
                } else {
                    onError(new Throwable(getResources().getString(com.obdstar.common.ui.R.string.failed_to_delete_files)));
                }
            }

            @Override
            public void onError(Throwable e) {
                refresh();
                ToastUtil.showToast(mActivity, "error:" + e.getMessage(), Toast.LENGTH_SHORT);
            }

            @Override
            public void onComplete() {
                refresh();
                ToastUtil.showToast(mActivity, getString(com.obdstar.common.ui.R.string.successfully_to_delete_files), Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * 删除数据后刷新界面
     */
    private void refresh() {
        try {
            if (mDeleteDialog != null) {
                mDeleteDialog.dismiss();
            }
            resetSearch();
            progressBar.setVisibility(View.GONE);
            enterDeleteMode(false);
            allFileList.removeAll(deletedFileList);
            // initData();
            disposeData(getYearMonthDatas(), getSubDatas());
            deletedFileList.clear();
            deletedSubList.clear();
            ivEditBack.setVisibility(View.GONE);
            ivEditData.setVisibility(View.VISIBLE);
            ivDelete.setVisibility(View.GONE);
            ivCheckAll.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理填充数据
     */
    private void disposeData(List<DiagHistoryBean> data, List<DiagReportListBean> subDate) {
        data.clear();
        subDate.clear();
        //统计数量
        yearMonthCount.clear();

        if (mLastSelectedP >= data.size()) {
            mLastSelectedP = 0;
        }
        data.addAll(getDataByFile());

        // 右侧年月筛选 倒叙排序
        Collections.sort(data, new Comparator<DiagHistoryBean>() {
            @Override
            public int compare(DiagHistoryBean o1, DiagHistoryBean o2) {
                return o2.getDate().compareTo(o1.getDate());
            }
        });
        mSubAdapter.setFaultData(faultNumberMap);

        //排序
//        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(yearMonthCount.entrySet());
//        Collections.sort(sortedList, (e1, e2) -> {
//            return e2.getKey().compareTo(e1.getKey()); // 倒序
//        });
        if (!data.isEmpty()) {
            subDate.addAll(data.get(mLastSelectedP).getSubData());
        }

        //默认时间排序
//        Collections.sort(subDate);

        if (!subDate.isEmpty() && !querySubDatas.isEmpty()) {
            querySubDatas.removeAll(deletedSubList);
            // todo xialj 如果是搜索模式，那么queryData里的数据也需要被删除
            //当删除完一个menu下所有数据后
            if (size != data.size()) {
                mSearchEdt.setText("");
                mSearchEdt.setCursorVisible(false);
                querySubDatas.clear();
                subDate.clear();
                if (!data.isEmpty()) {
                    size = data.size();
                    data.get(0).setSelected(true);
                    mSubAdapter.setData(subDate);
                    mAdapter.setData(data);
                } else {
                    noData();
                }
                return;
            }
            if (querySubDatas.isEmpty()) {
                showSubEmpty(true);
            } else {
                mSubAdapter.setData(querySubDatas);
                showSubEmpty(false);
            }
            data.get(mLastSelectedP).setSelected(true);
            mAdapter.notifyDataSetChanged();
        } else {
            if (!subDate.isEmpty()) {
                size = data.size();
                data.get(mLastSelectedP).setSelected(true);
                mSubAdapter.setData(subDate);
                mAdapter.setData(data);
            } else {
                noData();
            }
        }
    }

    private void showSubEmpty(boolean b) {
        ll_empty.setVisibility(b ? View.VISIBLE : View.GONE);
        mRvSub.setVisibility(b ? View.INVISIBLE : View.VISIBLE);
    }

    private void noData() {
        haveData = false;
        mRv.setVisibility(View.INVISIBLE);
        content_bg.setVisibility(View.INVISIBLE);
        mRvSub.setVisibility(View.INVISIBLE);
        ll_empty.setVisibility(View.INVISIBLE);
        ll.setVisibility(View.VISIBLE);
        ll.removeAllViews();
        View view = LayoutInflater.from(mActivity).inflate(com.obdstar.module.data.manager.R.layout.ui_message_box, ll);
        TextView tv = (TextView) view.findViewById(com.obdstar.module.data.manager.R.id.tv_infor);
        tv.setText(getResources().getString(com.obdstar.common.ui.R.string.no_data_currently));
        ivDelete.setEnabled(false);
        mSearchEdt.setEnabled(false);
    }

    private List<DiagHistoryBean> getDataByFile() {
        // 定义 Map，key 为 "yyyy/MM"，value 为该月的 bean 列表
        Map<String, List<DiagReportListBean>> map = new HashMap<>();
        String regex = "^\\((.*?)\\)((\\d{4})_(.*?)_(\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}))_(\\d+)_(\\d+)\\.json$";
        Pattern pattern = Pattern.compile(regex);

        List<DiagHistoryBean> data = new ArrayList<>();

        for (int i = 0; i < allFileList.size(); i++) {
            File file1 = allFileList.get(i);
            String file1Name = file1.getName();
            Matcher matcher = pattern.matcher(file1Name);
            if (!matcher.find()) {
                Log.e(TAG, "handleFileName: 文件名：" + file1Name + "格式不符合规范");
                continue;
            }
            DiagReportListBean bean = new DiagReportListBean();
            bean.bran = matcher.group(1);
            bean.name = matcher.group(2);
            bean.vin = file1.getParentFile().getName();
            bean.time = matcher.group(5);
            if (bean.time != null) {
                bean.time = bean.time.replace("-", ":").replaceFirst(":", "-").replaceFirst(":", "-");
            }
            bean.state = "0".equals(matcher.group(7)) ? getResources().getString(com.obdstar.common.ui.R.string.sh_diag_report_before_repair)
                    : getResources().getString(com.obdstar.common.ui.R.string.sh_diag_report_after_repair);
            bean.setFile(file1);

            faultNumberMap.put(bean.name, getFaultCodeCount(file1Name));
            bean.setSortType(mLastSelectedP);

            /** 统计月份数据 */
            String rawTime = bean.time;
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM", Locale.getDefault());

            try {
                Date date = inputFormat.parse(rawTime);
                String yearMonth = outputFormat.format(date);

                // 将bean放入对应的年月列表
                if (!map.containsKey(yearMonth)) {
                    map.put(yearMonth, new ArrayList<>());
                    DiagHistoryBean diagHistoryBean = new DiagHistoryBean();
                    diagHistoryBean.setDate(yearMonth);
                    diagHistoryBean.setSubData(map.get(yearMonth));
                    diagHistoryBean.setSelected(false);
                    data.add(diagHistoryBean);
                }
                map.get(yearMonth).add(bean);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    /**
     * 从文件名称中截取 故障码数量
     * @param fileName (模块名)年款_品牌_yyyy-MM-dd HH-mm-ss_故障码数量_维修状态.json
     * @return
     */
    private Integer getFaultCodeCount(String fileName) {
        if (fileName == null || !fileName.contains("_")) {
            return 0;
        }
        String nameWithoutExt = fileName.replace(".json", "");
        String[] parts = nameWithoutExt.split("_");

        String faultCodeCount = parts[parts.length - 2];
        if (faultCodeCount.matches("^[1-9]\\d*$")) {
            return Integer.parseInt(faultCodeCount);
        }
        return 0;
    }

    /**
     * 长按删除
     */
    private void doDelete(DiagReportListBean sub) {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                emitter.onNext(AsyncDelete(sub));
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(Boolean result) {
                if (result) {
                    onComplete();
                } else {
                    onError(new Throwable(getResources().getString(com.obdstar.common.ui.R.string.failed_to_delete_files)));
                }
            }

            @Override
            public void onError(Throwable e) {
                refresh();
                ToastUtil.showToast(mActivity, "error:" + e.getMessage(), Toast.LENGTH_SHORT);
            }

            @Override
            public void onComplete() {
                refresh();
                ToastUtil.showToast(mActivity, getResources().getString(com.obdstar.common.ui.R.string.successfully_to_delete_files), Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * 是否进入删除模式
     */
    private void enterDeleteMode(boolean b) {
        mSubAdapter.setDeleteMode(b);
        selectedSubList.clear();
        for (DiagReportListBean sub : subDatas) {
            sub.setCheck(false);
        }
        mSubAdapter.notifyDataSetChanged();
    }

    /**
     * 刷新右上角编辑按钮状态
     */
    private void updateEditBtn() {
        if (mSubAdapter.isDeleteMode()) {
            if (!selectedSubList.isEmpty()) {
                ivEditBack.setVisibility(View.GONE);
                ivEditData.setVisibility(View.GONE);
                ivDelete.setVisibility(View.VISIBLE);
                ivCheckAll.setVisibility(View.VISIBLE);
                if (selectedSubList.size() == subDatas.size()) {
                    ivCheckAll.setImageResource(com.obdstar.common.ui.R.drawable.checkbox_sel_svg);
                } else {
                    ivCheckAll.setImageResource(com.obdstar.common.ui.R.drawable.checkbox_sel_some_svg);
                }
            } else {
                ivEditBack.setVisibility(View.VISIBLE);
                ivEditData.setVisibility(View.GONE);
                ivDelete.setVisibility(View.GONE);
                ivCheckAll.setVisibility(View.VISIBLE);
                ivCheckAll.setImageResource(com.obdstar.common.ui.R.drawable.checkbox_bg_svg);
            }
        } else {
            ivEditBack.setVisibility(View.GONE);
            ivEditData.setVisibility(View.VISIBLE);
            ivDelete.setVisibility(View.GONE);
            ivCheckAll.setVisibility(View.GONE);
        }

    }
}
