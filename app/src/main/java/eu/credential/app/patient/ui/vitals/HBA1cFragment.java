package eu.credential.app.patient.ui.vitals;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.androidplot.util.SeriesUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.orchestration.http.PHRdata;

public class HBA1cFragment extends Fragment {
    //Boundary size
    private static final int DOMAIN_BOUNDARY_SIZE = 8;

    //Plot define
    private XYPlot plot;

    public static int getDomainBoundarySize() {
        return DOMAIN_BOUNDARY_SIZE;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_refresh).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_refresh:
                try {
                    refreshMeasurements();
                } catch (InterruptedException | ExecutionException | JSONException | ParseException e) {
                    e.printStackTrace();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            refreshMeasurements();
        } catch (InterruptedException | ParseException | JSONException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Load the preferences
        PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hba1c, container, false);

        //UI components initialisation
        initUI(v);

        // Load the preferences
        PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
        return v;
    }

    private void initUI(View v) {
        // initialize XYPlot reference:
        plot = v.findViewById(R.id.plot1);

        // show the zoom window
        plot.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), HBA1cZoom.class);
            startActivity(intent);
        });
    }

    public void refreshMeasurements() throws ParseException, InterruptedException, ExecutionException, JSONException {
        plot.clear();
        graph();
        plot.redraw();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    }

    private void setBoundaries(XYPlot plot, ArrayList<Date> list, Double value) {
        if (list.size() < 1) {
            plot.setDomainBoundaries(0, 2, BoundaryMode.FIXED);
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),
                    R.string.toast_zoom, Toast.LENGTH_LONG).show();
            plot.redraw();
        } else if (list.size() == 1) {
            plot.setRangeBoundaries(value - 5, value + 5, BoundaryMode.FIXED);
            plot.setRangeStep(StepMode.SUBDIVIDE, DOMAIN_BOUNDARY_SIZE);
            plot.setDomainBoundaries(0, 1, BoundaryMode.FIXED);
            plot.setDomainStep(StepMode.SUBDIVIDE, 1);
            plot.centerOnDomainOrigin(0, 1, BoundaryMode.FIXED);
            plot.redraw();
        } else if (list.size() < DOMAIN_BOUNDARY_SIZE) {
            plot.setDomainBoundaries(0, BoundaryMode.FIXED, list.size(), BoundaryMode.AUTO);
            //plot.setRangeStep(StepMode.INCREMENT_BY_VAL, 5);
            plot.setDomainStep(StepMode.SUBDIVIDE, list.size());
            plot.redraw();
        } else {
            plot.setDomainBoundaries(list.size() - DOMAIN_BOUNDARY_SIZE, BoundaryMode.FIXED, list.size(), BoundaryMode.AUTO);
            plot.setDomainStep(StepMode.SUBDIVIDE, DOMAIN_BOUNDARY_SIZE);
            plot.redraw();
        }
    }

    static class AddHBA1cMeasurment {
        // X/Y values
        private static ArrayList<Double> series = new ArrayList<>();
        private static ArrayList<Date> domainLabels = new ArrayList<>();

        static ArrayList<Double> getSeries() {
            return series;
        }

        static ArrayList<Date> getDomainLabels() {
            return domainLabels;
        }

        private static void addHBA1cMeasurmentValue(Context context) throws ExecutionException, InterruptedException,
                JSONException, ParseException {
            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
            JSONArray phrDocumentArray = new PHRdata("hba1c", "retrieveDocuments", null).execute().get();
            if (phrDocumentArray.getJSONObject(0).has("error")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(context));
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("Warning!");
                builder.setMessage(context.getString(R.string.server_not_respond));
                builder.setPositiveButton(context.getString(R.string.ok),
                        (arg0, arg1) -> {
                        });
                builder.show();
            } else {
                series.clear();
                domainLabels.clear();
                for (int i = 0; i < phrDocumentArray.length(); i++) {
                    series.add(Double.parseDouble(phrDocumentArray.getJSONObject(i)
                            .getJSONObject("properties").getJSONObject("concentration").getString("value")));
                    domainLabels.add(df.parse(phrDocumentArray.getJSONObject(i).getJSONObject("properties")
                            .getJSONObject("receiveTime").getString("value")));
                }
            }
        }
    }

    private void graph() throws ExecutionException, InterruptedException, JSONException, ParseException {
        plot.getLegend().setVisible(false);
        AddHBA1cMeasurment.addHBA1cMeasurmentValue(getContext());
        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(AddHBA1cMeasurment.getSeries(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Blood");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(this.getActivity(), R.xml.line_point_formatter_with_labels2);

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);

            @Override
            public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos) {
                int i = Math.round(((Number) obj).longValue());
                return dateFormat.format(AddHBA1cMeasurment.getDomainLabels().get(i), toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, @NonNull ParsePosition pos) {
                return null;
            }
        });

        if (SeriesUtils.minMax(series1).getMaxY() != null) {
            plot.setRangeUpperBoundary(SeriesUtils.minMax(series1).getMaxY().doubleValue() + 2, BoundaryMode.FIXED);
            setBoundaries(plot, AddHBA1cMeasurment.getDomainLabels(), AddHBA1cMeasurment.getSeries().get(0));
        } else {
            Toast.makeText(getContext(), R.string.toast_zoom, Toast.LENGTH_SHORT).show();
        }
    }
}
