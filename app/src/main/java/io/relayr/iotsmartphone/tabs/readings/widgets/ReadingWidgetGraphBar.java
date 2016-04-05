package io.relayr.iotsmartphone.tabs.readings.widgets;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.InjectView;
import io.relayr.iotsmartphone.R;
import io.relayr.java.model.action.Reading;
import io.relayr.java.model.models.schema.NumberSchema;
import io.relayr.java.model.models.schema.ValueSchema;

public class ReadingWidgetGraphBar extends ReadingWidget {

    @InjectView(R.id.history_chart) BarChart mChart;

    public ReadingWidgetGraphBar(Context context) {
        this(context, null);
    }

    public ReadingWidgetGraphBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReadingWidgetGraphBar(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean isBoolean = false;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        update();
    }

    @Override void update() {
        setGraphParameters();
    }

    @Override void refresh() {
        if (mChart != null) setData(mReadings);
    }

    @SuppressWarnings("unchecked")
    private void setGraphParameters() {
        if (mSchema.isIntegerSchema() || mSchema.isNumberSchema()) {
            final NumberSchema schema = mSchema.asNumber();
            initGraph(schema.getMin() != null ? schema.getMin().intValue() : 0, schema.getMax() != null ? schema.getMax().intValue() : 100);
            isBoolean = false;
        } else if (mSchema.isBooleanSchema()) {
            initGraph(0, 1);
            isBoolean = true;
        } else
            Crashlytics.log(Log.WARN, "RWGB", "Object not supported");
    }

    private void initGraph(int min, int max) {
        mChart.setDescription("");
        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setPinchZoom(false);

        mChart.getLegend().setEnabled(false);
        mChart.getAxisRight().setEnabled(true);

        initAxis(mChart.getAxisLeft(), min, max);
        initAxis(mChart.getAxisRight(), min, max);

        refresh();
    }

    private void initAxis(YAxis axis, int min, int max) {
        axis.setTextColor(ContextCompat.getColor(getContext(), R.color.secondary));
        axis.setAxisMaxValue(max);
        axis.setAxisMinValue(min);
        axis.setStartAtZero(min == 0);
    }

    @SuppressWarnings("unchecked")
    private void setData(List<Reading> points) {
        long mFirstPoint = System.currentTimeMillis() - DELAY_COMPLEX;
        long mDiff = DELAY_COMPLEX / mMaxPoints;

        List<BarEntry> yValues = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            final Reading reading = points.get(i);
            final int index = (int) ((reading.recorded - mFirstPoint) / mDiff);
            if (index < 0) continue;
            if (index >= mMaxPoints) break;

            if (isBoolean)
                yValues.add(new BarEntry(((Boolean) reading.value) ? 1 : 0, index));
            else
                yValues.add(new BarEntry(((Number) reading.value).intValue(), index));
        }

        BarDataSet barDataSet = new BarDataSet(yValues, mMeaning);
        barDataSet.setColor(ContextCompat.getColor(getContext(), R.color.graph_yellow));
        barDataSet.setBarSpacePercent(2f);

        BarData data = new BarData(axisX, barDataSet);
        data.setDrawValues(false);

        mChart.setData(data);
        mChart.invalidate();
    }
}
