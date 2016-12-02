package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by IvanLepi on 12/2/2016.
 */

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
    Context mContext = null;
    Cursor data;
    final private DecimalFormat dollarFormatWithPlus;
    final private DecimalFormat dollarFormat;
    final private DecimalFormat percentageFormat;


    public WidgetDataProvider(Context context, Intent intent){
        mContext = context;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        // Revert back to our process' identity so we can work with our
        // content provider
        final long identityToken = Binder.clearCallingIdentity();
        data = mContext.getContentResolver().query(Contract.Quote.uri,Contract.Quote.QUOTE_COLUMNS,null,null,null);
        Binder.restoreCallingIdentity(identityToken);

    }

    @Override
    public void onDestroy() {
        if (data != null){
            data.close();
            data = null;
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                data == null || !data.moveToPosition(position)) {
            return null;
            }
        RemoteViews mView = new RemoteViews(mContext.getPackageName(),
                R.layout.list_item_quote);

        mView.setTextViewText(R.id.symbol, data.getString(Contract.Quote.POSITION_SYMBOL));
        mView.setTextViewText(R.id.price,dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));

        float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        String percentage = percentageFormat.format(percentageChange / 100);

        mView.setTextViewText(R.id.change,percentage);

        if (rawAbsoluteChange > 0) {
            mView.setTextViewCompoundDrawables(R.id.change,
                    R.drawable.percent_change_pill_green,
                    R.drawable.percent_change_pill_green,
                    R.drawable.percent_change_pill_green,
                    R.drawable.percent_change_pill_green);
        } else {
            mView.setTextViewCompoundDrawables(R.id.change,
                    R.drawable.percent_change_pill_red,
                    R.drawable.percent_change_pill_red,
                    R.drawable.percent_change_pill_red,
                    R.drawable.percent_change_pill_red);
        }
        return mView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(),R.layout.list_item_quote);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
