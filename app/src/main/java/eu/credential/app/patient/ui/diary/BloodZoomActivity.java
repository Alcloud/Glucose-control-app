package eu.credential.app.patient.ui.diary;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidplot.util.SeriesUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.administrator.credential_v020.R;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BloodZoomActivity extends AppCompatActivity {

    //Plot define
    private XYPlot plotZoom;
    private XYSeries series1;
    private XYSeries goal;
    private Spinner spinnerPeriod;
    private LineAndPointFormatter series1Format;
    private LineAndPointFormatter goalFormat;

    private enum SeriesPeriod {
        DAY,
        WEEK,
        MONTH
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_zoom);

        //UI initialisation
        initUI();
    }
    private void initUI() {

        //Toolbar settings
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_blood);
        toolbar.setTitle(R.string.diary_toolbar_blood);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        //Plot initialisation
        plotZoom = (XYPlot) findViewById(R.id.plotZoomBlood);
        spinnerPeriod = (Spinner) findViewById(R.id.spinner_period_blood);

        //Plot settings
        plotZoom.getLegend().setVisible(false);
        series1Format = new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels2);
        goalFormat = new LineAndPointFormatter(this, R.xml.goal_line);

        plotZoom.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.US);

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).longValue());
                return dateFormat.format(DiaryFragment.domainLabels().get(i), toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        ArrayAdapter<SeriesPeriod> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SeriesPeriod.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Spinner settings
        spinnerPeriod.setAdapter(adapter);
        spinnerPeriod.setSelection(SeriesPeriod.DAY.ordinal());
        spinnerPeriod.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                final SeriesPeriod selectedSize = (SeriesPeriod) arg0.getSelectedItem();
                ArrayList<Date> domainList = DiaryFragment.domainLabels();

                switch (selectedSize) {
                    case DAY:
                        ArrayList<Date> day = new ArrayList();

                        for (int i = 0; i < domainList.size(); i++) {
                            Date listed = domainList.get(i);
                            if (isToday(listed)==0) {
                                day.add(domainList.get(i));
                            }
                        }
                        howManyValueExist(day, domainList);
                        break;

                    case WEEK:
                        ArrayList<Date> week = new ArrayList();
                        for (int i = 0; i < domainList.size(); i++) {
                            Date listed = domainList.get(i);
                            if (isWeek(listed)) {
                                week.add(domainList.get(i));
                            }
                        }
                        howManyValueExist(week, domainList);
                        break;

                    case MONTH:
                        ArrayList<Date> month = new ArrayList();
                        for (int i = 0; i < domainList.size(); i++) {
                            Date listed = domainList.get(i);
                            if (isMonth(listed)) {
                                month.add(domainList.get(i));
                            }
                        }
                        howManyValueExist(month, domainList);
                        break;

                    default:
                        break;
                }
                updatePlot();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void howManyValueExist(ArrayList<Date> list, ArrayList<Date> domainList) {
        if (list.size()<1){
            plotZoom.setDomainBoundaries(domainList.size()-list.size(), BoundaryMode.FIXED, domainList.size(), BoundaryMode.AUTO);
            Toast.makeText(getApplicationContext(), R.string.toast_zoom,Toast.LENGTH_SHORT).show();
        }
        else if (list.size()==1 && domainList.size()>1){
            plotZoom.setDomainBoundaries(domainList.size()-list.size()-1, BoundaryMode.FIXED, domainList.size(), BoundaryMode.AUTO);
            plotZoom.setDomainStep(StepMode.INCREMENT_BY_VAL, list.size()+1);
        }
        else if (list.size()==1 && domainList.size()<=1){
            plotZoom.setRangeBoundaries(70, 200, BoundaryMode.FIXED);
            plotZoom.setRangeStep(StepMode.SUBDIVIDE, 5);
            plotZoom.setDomainBoundaries(0, 1, BoundaryMode.FIXED);
            plotZoom.setDomainStep(StepMode.SUBDIVIDE, 1);
            plotZoom.centerOnDomainOrigin(0, 1, BoundaryMode.FIXED);
            plotZoom.redraw();
        }
        else {
            plotZoom.setDomainBoundaries(domainList.size()-list.size(), BoundaryMode.FIXED, domainList.size(), BoundaryMode.AUTO);
            plotZoom.setDomainStep(StepMode.SUBDIVIDE, list.size());
        }
    }
    private int isToday(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Date today = Calendar.getInstance().getTime();
        String dateString = format.format(date);
        String todayString = format.format(today);

        return dateString.compareTo(todayString);
    }
    private boolean isWeek(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        LocalDate now = LocalDate.now();
        String dateString = format.format(date);
        DateTimeFormatter parser1 = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate then = LocalDate.parse(dateString,parser1);

        if (Days.daysBetween(then, now).getValue(0)<=7){
            return true;
        }
        else {
            return false;
        }
    }
    private boolean isMonth(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        LocalDate now = LocalDate.now();
        String dateString = format.format(date);
        DateTimeFormatter parser1 = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate then = LocalDate.parse(dateString,parser1);

        if (Days.daysBetween(then, now).getValue(0)<=31){
            return true;
        }
        else {
            return false;
        }
    }
    private void updatePlot() {

        // Remove all current series from each plot
        plotZoom.clear();

        // Setup our Series with the selected number of elements
        series1 = new SimpleXYSeries(DiaryFragment.series(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Weight");

        if (!DiaryFragment.series().isEmpty()){
            plotZoom.setRangeUpperBoundary(SeriesUtils.minMax(series1).
                    getMaxY().doubleValue() + 1, BoundaryMode.FIXED);
        }
        goal = new SimpleXYSeries(DiaryFragment.goalArray(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "goal");
        // add a new series' to the xyplot:
        plotZoom.addSeries(series1, series1Format);
        plotZoom.addSeries(goal, goalFormat);
        plotZoom.redraw();
    }

    public void onClickShare(View v) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "My blood";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}
