package com.wsl.datepickerlib;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.wsl.datepickerlib.Utils.DateUtil;
import com.wsl.datepickerlib.Utils.ScreenUtil;
import com.wsl.datepickerlib.Utils.TextUtil;
import com.wsl.datepickerlib.view.PickerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CustomDate {
    private int scrollUnits;
    private CustomDate.ResultHandler handler;
    private Context mContext;
    private final String FORMAT_STR = "yyyy-MM-dd HH:mm";
    private String dateRegex = "yyyy-MM-dd HH:mm";
    private Dialog seletorDialog;
    private PickerView year_pv, month_pv, day_pv, hour_pv, minute_pv;
    private int MAXHOUR = 23;
    private int MINHOUR = 0;
    private final int MAXMINUTE = 59;
    private final int MINMINUTE = 0;
    private final int MAXMONTH = 12;
    private ArrayList<String> mYearList;
    private ArrayList<String> mMonthList;
    private ArrayList<String> mDayList;
    private ArrayList<String> mHourList;
    private ArrayList<String> mMinuteList;
    private int mStartYear, mStartMonth, mStartDay, mStartHour, mStartMininute;
    private int mEndYear, mEndMonth, mEndDay, mEndHour, mEndMininute;
    private int mCurrYear, mCurrMonth, mCurrDay, mCurrHour, mCurrMiniute;
    private int mMinute_workStart, mMinute_workEnd, mHour_workStart, mHour_workEnd;
    private boolean spanYear, spanMon, spanDay, spanHour, spanMin;
    private Calendar selectedCalender;
    private final long ANIMATORDELAY, CHANGEDELAY;
    private String workStart_str, workEnd_str;
    private String mStartDate, mEndDate, mCurrentDate;
    private Calendar startCalendar, endCalendar, mCurrCalendar;
    private TextView tv_cancle, tv_select, tv_title;
    private TextView year_tv, month_tv, day_tv, hour_text, minute_text;

    public CustomDate(Context context, CustomDate.ResultHandler resultHandler, String startDate, String endDate, MODE mode) {
        scrollUnits = CustomDate.SCROLLTYPE.HOUR.value + CustomDate.SCROLLTYPE.MINUTE.value;
        selectedCalender = Calendar.getInstance();
        ANIMATORDELAY = 200L;
        CHANGEDELAY = 90L;
        mContext = context;
        handler = resultHandler;
        mStartDate = startDate;
        mEndDate = endDate;
        mCurrentDate = endDate;
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        mCurrCalendar = Calendar.getInstance();
//        dateRegex = TextUtil.isEmpty(format_date) ? dateRegex : format_date;
        initDialog();
        initView();
        setMode(mode);
    }

    public CustomDate(Context context, CustomDate.ResultHandler resultHandler, String startDate, String endDate, String workStartTime, String workEndTime) {
        this(context, resultHandler, startDate, endDate, MODE.YMDHM);
        workStart_str = workStartTime;
        workEnd_str = workEndTime;
    }

    public void show() {
        long currTime = timeParse(mCurrentDate, dateRegex);
        long startTime = timeParse(mStartDate, dateRegex);
        long endTime = timeParse(mEndDate, dateRegex);
        if (currTime >= startTime && currTime <= endTime) {
            startCalendar.setTime(DateUtil.parse(mStartDate, dateRegex));
            endCalendar.setTime(DateUtil.parse(mEndDate, dateRegex));
            mCurrCalendar.setTime(DateUtil.parse(mCurrentDate, dateRegex));
            if (startCalendar.getTime().getTime() >= endCalendar.getTime().getTime()) {
                Toast.makeText(mContext, "start>end", Toast.LENGTH_SHORT).show();
            } else if (excuteWorkTime()) {
                initParameter();
                initTimer();
                addListener();
                initCurrenTime();
                seletorDialog.show();
            }
        } else {
            throw new IllegalArgumentException("当前日期不在起始日期和结束日期之内");
        }
    }

    private void initCurrenTime() {
        if (!TextUtil.isEmpty(mCurrentDate)) {
            Calendar calendar = Calendar.getInstance();
            Date date = timeToDate(mCurrentDate, dateRegex);
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int index = mYearList.indexOf(String.valueOf(year));
            if (index > -1) {
                year_pv.setSelected(index);
            }

            index = mMonthList.indexOf(getString(String.valueOf(month)));
            if (index > -1) {
                month_pv.setSelected(index);
            }

            index = mDayList.indexOf(getString(String.valueOf(day)));
            if (index > -1) {
                day_pv.setSelected(index);
            }

            index = mHourList.indexOf(String.valueOf(hour));
            if (index > -1) {
                hour_pv.setSelected(index);
            }

            index = mMinuteList.indexOf(String.valueOf(minute));
            if (index > -1) {
                minute_pv.setSelected(index);
            }

        }
    }

    private String getString(String monthStr) {
        monthStr = monthStr.length() > 1 ? monthStr : String.format("%d%s", new Object[]{Integer.valueOf(0), monthStr});
        return monthStr;
    }

    private void initDialog() {
        if (seletorDialog == null) {
            seletorDialog = new Dialog(mContext, R.style.time_dialog);
            seletorDialog.setCancelable(false);
            seletorDialog.requestWindowFeature(1);
            seletorDialog.setContentView(R.layout.dialog_selector);
            Window window = seletorDialog.getWindow();
            window.setGravity(80);
            WindowManager.LayoutParams lp = window.getAttributes();
            int width = ScreenUtil.getInstance(mContext).getScreenWidth();
            lp.width = width;
            window.setAttributes(lp);
        }

    }

    private void initView() {
        year_pv = (PickerView) seletorDialog.findViewById(R.id.year_pv);
        month_pv = (PickerView) seletorDialog.findViewById(R.id.month_pv);
        day_pv = (PickerView) seletorDialog.findViewById(R.id.day_pv);
        hour_pv = (PickerView) seletorDialog.findViewById(R.id.hour_pv);
        minute_pv = (PickerView) seletorDialog.findViewById(R.id.minute_pv);
        tv_cancle = (TextView) seletorDialog.findViewById(R.id.tv_cancle);
        tv_select = (TextView) seletorDialog.findViewById(R.id.tv_select);
        tv_title = (TextView) seletorDialog.findViewById(R.id.tv_title);
        year_tv = (TextView) seletorDialog.findViewById(R.id.year_tv);
        month_tv = (TextView) seletorDialog.findViewById(R.id.month_tv);
        day_tv = (TextView) seletorDialog.findViewById(R.id.day_tv);
        hour_text = (TextView) seletorDialog.findViewById(R.id.hour_text);
        minute_text = (TextView) seletorDialog.findViewById(R.id.minute_text);
        tv_cancle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                seletorDialog.dismiss();
            }
        });
        tv_select.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                handler.handle(DateUtil.format(selectedCalender.getTime(), dateRegex));
                seletorDialog.dismiss();
            }
        });
    }

    private void initParameter() {
        mStartYear = startCalendar.get(Calendar.YEAR);
        mStartMonth = startCalendar.get(Calendar.MONTH) + 1;
        mStartDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        mStartHour = startCalendar.get(Calendar.HOUR_OF_DAY);
        mStartMininute = startCalendar.get(Calendar.MINUTE);
        mEndYear = endCalendar.get(Calendar.YEAR);
        mEndMonth = endCalendar.get(Calendar.MONTH) + 1;
        mEndDay = endCalendar.get(Calendar.DAY_OF_MONTH);
        mEndHour = endCalendar.get(Calendar.HOUR_OF_DAY);
        mEndMininute = endCalendar.get(Calendar.MINUTE);
        mCurrYear = mCurrCalendar.get(Calendar.YEAR);
        mCurrMonth = mCurrCalendar.get(Calendar.MONTH) + 1;
        mCurrDay = mCurrCalendar.get(Calendar.DAY_OF_MONTH);
        mCurrHour = mCurrCalendar.get(Calendar.HOUR_OF_DAY);
        mCurrMiniute = mCurrCalendar.get(Calendar.MINUTE);
        spanYear = mStartYear != mEndYear;
        spanMon = !spanYear && mStartMonth != mEndMonth;
        spanDay = !spanMon && mStartDay != mEndDay;
        spanHour = !spanDay && mStartHour != mEndHour;
        spanMin = !spanHour && mStartMininute != mEndMininute;
        Date date = timeToDate(mCurrentDate, dateRegex);
        selectedCalender.setTime(date);
    }

    private void initTimer() {
        initArrayList();
        if (spanYear) {
            addYear();
            addMonth();
            addDay();
            addHours();
            addMinute();
        } else {
            int i;
            if (spanMon) {
                mYearList.add(String.valueOf(mStartYear));
                i = 1;

                int j;
                for (j = mStartMonth; j <= mEndMonth; ++j) {
                    mMonthList.add(fomatTimeUnit(j));
                }

                if (mStartMonth == mEndMonth) {
                    i = mStartDay;
                }

                for (j = i; j <= mCurrCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); ++j) {
                    mDayList.add(fomatTimeUnit(j));
                }

                if ((scrollUnits & CustomDate.SCROLLTYPE.HOUR.value) != CustomDate.SCROLLTYPE.HOUR.value) {
                    mHourList.add(fomatTimeUnit(mStartHour));
                } else {
                    i = 1;
                    if (mStartMonth == mEndMonth && mStartDay == mEndDay) {
                        i = mStartHour;
                    }

                    for (j = i; j <= MAXHOUR; ++j) {
                        mHourList.add(fomatTimeUnit(j));
                    }
                }

                if ((scrollUnits & CustomDate.SCROLLTYPE.MINUTE.value) != CustomDate.SCROLLTYPE.MINUTE.value) {
                    mMinuteList.add(fomatTimeUnit(mStartMininute));
                } else {
                    for (j = mStartMininute; j <= 59; ++j) {
                        mMinuteList.add(fomatTimeUnit(j));
                    }
                }
            } else if (spanDay) {
                mYearList.add(String.valueOf(mStartYear));
                mMonthList.add(fomatTimeUnit(mStartMonth));

                for (i = mStartDay; i <= mEndDay; ++i) {
                    mDayList.add(fomatTimeUnit(i));
                }

                if ((scrollUnits & CustomDate.SCROLLTYPE.HOUR.value) != CustomDate.SCROLLTYPE.HOUR.value) {
                    mHourList.add(fomatTimeUnit(mStartHour));
                } else {
                    for (i = mStartHour; i <= MAXHOUR; ++i) {
                        mHourList.add(fomatTimeUnit(i));
                    }
                }

                if ((scrollUnits & CustomDate.SCROLLTYPE.MINUTE.value) != CustomDate.SCROLLTYPE.MINUTE.value) {
                    mMinuteList.add(fomatTimeUnit(mStartMininute));
                } else {
                    for (i = mStartMininute; i <= 59; ++i) {
                        mMinuteList.add(fomatTimeUnit(i));
                    }
                }
            } else if (spanHour) {
                mYearList.add(String.valueOf(mStartYear));
                mMonthList.add(fomatTimeUnit(mStartMonth));
                mDayList.add(fomatTimeUnit(mStartDay));
                if ((scrollUnits & CustomDate.SCROLLTYPE.HOUR.value) != CustomDate.SCROLLTYPE.HOUR.value) {
                    mHourList.add(fomatTimeUnit(mStartHour));
                } else {
                    for (i = mStartHour; i <= mEndHour; ++i) {
                        mHourList.add(fomatTimeUnit(i));
                    }
                }

                if ((scrollUnits & CustomDate.SCROLLTYPE.MINUTE.value) != CustomDate.SCROLLTYPE.MINUTE.value) {
                    mMinuteList.add(fomatTimeUnit(mStartMininute));
                } else {
                    for (i = mStartMininute; i <= 59; ++i) {
                        mMinuteList.add(fomatTimeUnit(i));
                    }
                }
            } else if (spanMin) {
                mYearList.add(String.valueOf(mStartYear));
                mMonthList.add(fomatTimeUnit(mStartMonth));
                mDayList.add(fomatTimeUnit(mStartDay));
                mHourList.add(fomatTimeUnit(mStartHour));
                if ((scrollUnits & CustomDate.SCROLLTYPE.MINUTE.value) != CustomDate.SCROLLTYPE.MINUTE.value) {
                    mMinuteList.add(fomatTimeUnit(mStartMininute));
                } else {
                    for (i = mStartMininute; i <= mEndMininute; ++i) {
                        mMinuteList.add(fomatTimeUnit(i));
                    }
                }
            }
        }

        loadComponent();
    }

    private void addYear() {
        for (int i = mStartYear; i <= mEndYear; ++i) {
            mYearList.add(String.valueOf(i));
        }

    }

    private void addMonth() {
        int startIndex = mStartMonth;
        int endIndex = mEndMonth;
        if (mStartYear != mEndYear) {
            startIndex = 1;
        }

        if (mCurrYear != mEndYear) {
            endIndex = 12;
        }

        for (int i = startIndex; i <= endIndex; ++i) {
            mMonthList.add(fomatTimeUnit(i));
        }

    }

    private void addDay() {
        int endIndex = getCurrentMonthDays(mCurrYear, mCurrMonth);
        int startIndex = 1;
        Log.e("debug_CustomDate", "410行...addDay:  = " + endIndex);
        if (mStartYear == mEndYear && mStartMonth == mEndMonth) {
            startIndex = mStartDay;
        }

        if (mCurrYear == mEndYear && mCurrMonth == mEndMonth) {
            endIndex = mEndDay;
        }

        for (int i = startIndex; i <= endIndex; ++i) {
            mDayList.add(fomatTimeUnit(i));
        }

    }

    private void addHours() {
        if ((scrollUnits & CustomDate.SCROLLTYPE.HOUR.value) != CustomDate.SCROLLTYPE.HOUR.value) {
            mHourList.add(fomatTimeUnit(mStartHour));
        } else {
            int startIndex = 0, endHour = 23;
            boolean isYmd = mStartYear == mEndYear && mStartMonth == mEndMonth && mStartDay == mEndDay;//年月日是否相等
            if (isYmd) {
                startIndex = mStartHour;
            }

            if (isYmd && mStartHour != mEndHour) {
                endHour = endCalendar.get(Calendar.HOUR_OF_DAY);
            }
//            boolean isStart = mCurrYear >= mStartYear && mCurrMonth >= mStartMonth && mCurrDay > mStartDay;
//            boolean isEnd = mCurrYear <= mEndYear && mCurrMonth <= mEndMonth && mCurrDay < mEndDay ;
//            if () {
//
//            }

            for (int i = startIndex; i <= endHour; ++i) {
                mHourList.add(fomatTimeUnit(i));
            }
        }

    }

    private void addMinute() {
        if ((scrollUnits & CustomDate.SCROLLTYPE.MINUTE.value) != CustomDate.SCROLLTYPE.MINUTE.value) {
            mMinuteList.add(fomatTimeUnit(mStartMininute));
        } else {
            int startIndex = 0;
            if (mStartYear == mEndYear && mStartMonth == mEndMonth && mStartDay == mEndDay && mStartYear == mEndHour) {
                startIndex = mStartMininute;
            }

            for (int i = startIndex; i <= 59; ++i) {
                mMinuteList.add(fomatTimeUnit(i));
            }
        }

    }

    private boolean excuteWorkTime() {
        boolean res = true;
        if (!TextUtil.isEmpty(workStart_str) && !TextUtil.isEmpty(workEnd_str)) {
            String[] start = workStart_str.split(":");
            String[] end = workEnd_str.split(":");
            mHour_workStart = Integer.parseInt(start[0]);
            mMinute_workStart = Integer.parseInt(start[1]);
            mHour_workEnd = Integer.parseInt(end[0]);
            mMinute_workEnd = Integer.parseInt(end[1]);
            Calendar workStartCalendar = Calendar.getInstance();
            Calendar workEndCalendar = Calendar.getInstance();
            workStartCalendar.setTime(startCalendar.getTime());
            workEndCalendar.setTime(endCalendar.getTime());
            workStartCalendar.set(Calendar.HOUR_OF_DAY, mHour_workStart);
            workStartCalendar.set(Calendar.MINUTE, mMinute_workStart);
            workEndCalendar.set(Calendar.HOUR_OF_DAY, mHour_workEnd);
            workEndCalendar.set(Calendar.MINUTE, mMinute_workEnd);
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            Calendar startWorkTime = Calendar.getInstance();
            Calendar endWorkTime = Calendar.getInstance();
            startTime.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY));
            startTime.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE));
            endTime.set(Calendar.HOUR_OF_DAY, endCalendar.get(Calendar.HOUR_OF_DAY));
            endTime.set(Calendar.MINUTE, endCalendar.get(Calendar.MINUTE));
            startWorkTime.set(Calendar.HOUR_OF_DAY, workStartCalendar.get(Calendar.HOUR_OF_DAY));
            startWorkTime.set(Calendar.MINUTE, workStartCalendar.get(Calendar.MINUTE));
            endWorkTime.set(Calendar.HOUR_OF_DAY, workEndCalendar.get(Calendar.HOUR_OF_DAY));
            endWorkTime.set(Calendar.MINUTE, workEndCalendar.get(Calendar.MINUTE));
            if (startTime.getTime().getTime() == endTime.getTime().getTime() || startWorkTime.getTime().getTime() < startTime.getTime().getTime() && endWorkTime.getTime().getTime() < startTime.getTime().getTime()) {
                Toast.makeText(mContext, "Wrong parames!", Toast.LENGTH_SHORT).show();
                return false;
            }

            startCalendar.setTime(startCalendar.getTime().getTime() < workStartCalendar.getTime().getTime() ? workStartCalendar.getTime() : startCalendar.getTime());
            endCalendar.setTime(endCalendar.getTime().getTime() > workEndCalendar.getTime().getTime() ? workEndCalendar.getTime() : endCalendar.getTime());
            MINHOUR = workStartCalendar.get(Calendar.HOUR_OF_DAY);
            MAXHOUR = workEndCalendar.get(Calendar.HOUR_OF_DAY);
        }

        return res;
    }

    private String fomatTimeUnit(int unit) {
        return unit < 10 ? "0" + String.valueOf(unit) : String.valueOf(unit);
    }

    private void initArrayList() {
        if (mYearList == null) {
            mYearList = new ArrayList();
        }

        if (mMonthList == null) {
            mMonthList = new ArrayList();
        }

        if (mDayList == null) {
            mDayList = new ArrayList();
        }

        if (mHourList == null) {
            mHourList = new ArrayList();
        }

        if (mMinuteList == null) {
            mMinuteList = new ArrayList();
        }

        mYearList.clear();
        mMonthList.clear();
        mDayList.clear();
        mHourList.clear();
        mMinuteList.clear();
    }

    private void addListener() {
        year_pv.setOnSelectListener(new PickerView.onSelectListener() {
            public void onSelect(String text) {
                selectedCalender.set(Calendar.YEAR, Integer.parseInt(text));
                monthChange();
            }
        });
        month_pv.setOnSelectListener(new PickerView.onSelectListener() {
            public void onSelect(String text) {
                selectedCalender.set(Calendar.DAY_OF_MONTH, 1);
                selectedCalender.set(Calendar.MONTH, Integer.parseInt(text) - 1);
                dayChange();
            }
        });
        day_pv.setOnSelectListener(new PickerView.onSelectListener() {
            public void onSelect(String text) {
                selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text));
                hourChange();
            }
        });
        hour_pv.setOnSelectListener(new PickerView.onSelectListener() {
            public void onSelect(String text) {
                selectedCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(text));
                minuteChange();
            }
        });
        minute_pv.setOnSelectListener(new PickerView.onSelectListener() {
            public void onSelect(String text) {
                selectedCalender.set(Calendar.MINUTE, Integer.parseInt(text));
            }
        });
    }

    private void loadComponent() {
        year_pv.setData(mYearList);
        month_pv.setData(mMonthList);
        day_pv.setData(mDayList);
        hour_pv.setData(mHourList);
        minute_pv.setData(mMinuteList);
        year_pv.setSelected(0);
        month_pv.setSelected(0);
        day_pv.setSelected(0);
        hour_pv.setSelected(0);
        minute_pv.setSelected(0);
        excuteScroll();
    }

    public void setCurrentDate(String currentDate) {
        mCurrentDate = currentDate;
    }

    private void excuteScroll() {
        year_pv.setCanScroll(mYearList.size() > 1);
        month_pv.setCanScroll(mMonthList.size() > 1);
        day_pv.setCanScroll(mDayList.size() > 1);
        hour_pv.setCanScroll(mHourList.size() > 1 && (scrollUnits & CustomDate.SCROLLTYPE.HOUR.value) == CustomDate.SCROLLTYPE.HOUR.value);
        minute_pv.setCanScroll(mMinuteList.size() > 1 && (scrollUnits & CustomDate.SCROLLTYPE.MINUTE.value) == CustomDate.SCROLLTYPE.MINUTE.value);
    }

    private void monthChange() {
        mMonthList.clear();
        int selectedYear = selectedCalender.get(Calendar.YEAR);
        int i;
        if (selectedYear == mStartYear) {
            for (i = mStartMonth; i <= 12; ++i) {
                mMonthList.add(fomatTimeUnit(i));
            }
        } else if (selectedYear == mEndYear) {
            for (i = 1; i <= mEndMonth; ++i) {
                mMonthList.add(fomatTimeUnit(i));
            }
        } else {
            for (i = 1; i <= 12; ++i) {
                mMonthList.add(fomatTimeUnit(i));
            }
        }

        selectedCalender.set(Calendar.MONTH, Integer.parseInt((String) mMonthList.get(0)) - 1);
        month_pv.setData(mMonthList);
        month_pv.setSelected(0);
        excuteAnimator(200L, month_pv);
        month_pv.postDelayed(new Runnable() {
            public void run() {
                dayChange();
            }
        }, 90L);
    }

    private void dayChange() {
        mDayList.clear();
        int selectedYear = selectedCalender.get(Calendar.YEAR);
        int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
        int i;
        if (selectedYear == mStartYear && selectedMonth == mStartMonth) {
            for (i = mStartDay; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); ++i) {
                mDayList.add(fomatTimeUnit(i));
            }
        } else if (selectedYear == mEndYear && selectedMonth == mEndMonth) {
            for (i = 1; i <= mEndDay; ++i) {
                mDayList.add(fomatTimeUnit(i));
            }
        } else {
            for (i = 1; i <= selectedCalender.getActualMaximum(Calendar.DAY_OF_MONTH); ++i) {
                mDayList.add(fomatTimeUnit(i));
            }
        }

        selectedCalender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mDayList.get(0)));
        day_pv.setData(mDayList);
        day_pv.setSelected(0);
        excuteAnimator(200L, day_pv);
        day_pv.postDelayed(new Runnable() {
            public void run() {
                hourChange();
            }
        }, 90L);
    }

    private void hourChange() {
        if ((scrollUnits & CustomDate.SCROLLTYPE.HOUR.value) == CustomDate.SCROLLTYPE.HOUR.value) {
            mHourList.clear();
            int selectedYear = selectedCalender.get(Calendar.YEAR);
            int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
            int i;
            if (selectedYear == mStartYear && selectedMonth == mStartMonth && selectedDay == mStartDay) {
                for (i = mStartHour; i <= MAXHOUR; ++i) {
                    mHourList.add(fomatTimeUnit(i));
                }
            } else if (selectedYear == mEndYear && selectedMonth == mEndMonth && selectedDay == mEndDay) {
                for (i = MINHOUR; i <= mEndHour; ++i) {
                    mHourList.add(fomatTimeUnit(i));
                }
            } else {
                for (i = MINHOUR; i <= MAXHOUR; ++i) {
                    mHourList.add(fomatTimeUnit(i));
                }
            }

            selectedCalender.set(Calendar.HOUR_OF_DAY, Integer.parseInt(mHourList.get(0)));
            hour_pv.setData(mHourList);
            hour_pv.setSelected(0);
            excuteAnimator(200L, hour_pv);
        }

        hour_pv.postDelayed(new Runnable() {
            public void run() {
                minuteChange();
            }
        }, 90L);
    }

    private void minuteChange() {
        if ((scrollUnits & CustomDate.SCROLLTYPE.MINUTE.value) == CustomDate.SCROLLTYPE.MINUTE.value) {
            mMinuteList.clear();
            int selectedYear = selectedCalender.get(Calendar.YEAR);
            int selectedMonth = selectedCalender.get(Calendar.MONTH) + 1;
            int selectedDay = selectedCalender.get(Calendar.DAY_OF_MONTH);
            int selectedHour = selectedCalender.get(Calendar.HOUR_OF_DAY);
            int i;
            if (selectedYear == mStartYear && selectedMonth == mStartMonth && selectedDay == mStartDay && selectedHour == mStartHour) {
                for (i = mStartMininute; i <= 59; ++i) {
                    mMinuteList.add(fomatTimeUnit(i));
                }
            } else if (selectedYear == mEndYear && selectedMonth == mEndMonth && selectedDay == mEndDay && selectedHour == mEndHour) {
                for (i = 0; i <= mEndMininute; ++i) {
                    mMinuteList.add(fomatTimeUnit(i));
                }
            } else if (selectedHour == mHour_workStart) {
                for (i = mMinute_workStart; i <= 59; ++i) {
                    mMinuteList.add(fomatTimeUnit(i));
                }
            } else if (selectedHour == mHour_workEnd) {
                for (i = 0; i <= mMinute_workEnd; ++i) {
                    mMinuteList.add(fomatTimeUnit(i));
                }
            } else {
                for (i = 0; i <= 59; ++i) {
                    mMinuteList.add(fomatTimeUnit(i));
                }
            }

            selectedCalender.set(Calendar.MINUTE, Integer.parseInt((String) mMinuteList.get(0)));
            minute_pv.setData(mMinuteList);
            minute_pv.setSelected(0);
            excuteAnimator(200L, minute_pv);
        }

        excuteScroll();
    }

    private void excuteAnimator(long ANIMATORDELAY, View view) {
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", new float[]{1.0F, 0.0F, 1.0F});
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", new float[]{1.0F, 1.3F, 1.0F});
        PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", new float[]{1.0F, 1.3F, 1.0F});
        ObjectAnimator.ofPropertyValuesHolder(view, new PropertyValuesHolder[]{pvhX, pvhY, pvhZ}).setDuration(ANIMATORDELAY).start();
    }

    public void setNextBtTip(String str) {
        tv_select.setText(str);
    }

    public void setTitle(String str) {
        tv_title.setText(str);
    }

    public int disScrollUnit(CustomDate.SCROLLTYPE... scrolltypes) {
        if (scrolltypes == null || scrolltypes.length == 0) {
            scrollUnits = CustomDate.SCROLLTYPE.HOUR.value + CustomDate.SCROLLTYPE.MINUTE.value;
        }

        CustomDate.SCROLLTYPE[] var2 = scrolltypes;
        int var3 = scrolltypes.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            CustomDate.SCROLLTYPE scrolltype = var2[var4];
            scrollUnits ^= scrolltype.value;
        }

        return scrollUnits;
    }

    public void setMode(CustomDate.MODE mode) {
        if (mode.value == MODE.YMD.value) {
            dateRegex = "yyyy-MM-dd";
            disScrollUnit(new CustomDate.SCROLLTYPE[]{CustomDate.SCROLLTYPE.HOUR, CustomDate.SCROLLTYPE.MINUTE});

            year_pv.setVisibility(View.VISIBLE);
            month_pv.setVisibility(View.VISIBLE);
            day_pv.setVisibility(View.VISIBLE);

            year_tv.setVisibility(View.VISIBLE);
            month_tv.setVisibility(View.VISIBLE);
            day_tv.setVisibility(View.VISIBLE);

            hour_pv.setVisibility(View.GONE);
            minute_pv.setVisibility(View.GONE);
            hour_text.setVisibility(View.GONE);
            minute_text.setVisibility(View.GONE);
        } else if (mode.value == MODE.YMDHM.value) {
            disScrollUnit(new CustomDate.SCROLLTYPE[0]);
            dateRegex = "yyyy-MM-dd HH:mm";
            year_pv.setVisibility(View.VISIBLE);
            month_pv.setVisibility(View.VISIBLE);
            day_pv.setVisibility(View.VISIBLE);
            hour_pv.setVisibility(View.VISIBLE);
            minute_pv.setVisibility(View.VISIBLE);

            year_tv.setVisibility(View.VISIBLE);
            month_tv.setVisibility(View.VISIBLE);
            day_tv.setVisibility(View.VISIBLE);
            hour_text.setVisibility(View.VISIBLE);
            minute_text.setVisibility(View.VISIBLE);
        } else if (mode.value == MODE.HM.value) {
            disScrollUnit(new CustomDate.SCROLLTYPE[]{CustomDate.SCROLLTYPE.HOUR, CustomDate.SCROLLTYPE.MINUTE});
            dateRegex = "HH:mm";
            year_pv.setVisibility(View.GONE);
            month_pv.setVisibility(View.GONE);
            day_pv.setVisibility(View.GONE);
            year_tv.setVisibility(View.GONE);
            month_tv.setVisibility(View.GONE);
            day_tv.setVisibility(View.GONE);

            hour_pv.setVisibility(View.VISIBLE);
            hour_text.setVisibility(View.VISIBLE);
            minute_pv.setVisibility(View.VISIBLE);
            minute_text.setVisibility(View.VISIBLE);

        }


    }

    public void setIsLoop(boolean isLoop) {
        year_pv.setIsLoop(isLoop);
        month_pv.setIsLoop(isLoop);
        day_pv.setIsLoop(isLoop);
        hour_pv.setIsLoop(isLoop);
        minute_pv.setIsLoop(isLoop);
    }

    private Date timeToDate(String date, String regex) {
        Date date1 = null;
        SimpleDateFormat format = new SimpleDateFormat(regex);

        try {
            date1 = format.parse(date);
            return date1;
        } catch (ParseException var6) {
            throw new IllegalArgumentException("时间格式不正确");
        }
    }

    private long timeParse(String date, String regex) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(regex);

        try {
            return dateFormat.parse(date).getTime();
        } catch (ParseException var5) {
            throw new RuntimeException("日期格式不正确");
        }
    }

    public int getCurrentMonthDays(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.roll(Calendar.DAY_OF_MONTH, -1);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static enum MODE {
        YMD(1),
        YMDHM(2),
        HM(3);

        public int value;

        private MODE(int value) {
            this.value = value;
        }
    }

    public static enum SCROLLTYPE {
        HOUR(1),
        MINUTE(2);

        public int value;

        private SCROLLTYPE(int value) {
            this.value = value;
        }
    }

    public interface ResultHandler {
        void handle(String time);
    }
}
