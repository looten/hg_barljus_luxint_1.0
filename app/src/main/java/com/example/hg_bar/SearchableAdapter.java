package com.example.hg_bar;

/**
 * Created by Looten on 2015-08-23.
 */

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

// The standard text view adapter only seems to search from the beginning of whole words
// so we've had to write this whole class to make it possible to search
// for parts of the arbitrary string we want
public class SearchableAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = "SearchableAdapter";
    private List<String> originalData = null;
    private List<String> filteredData = null;
    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();
    private boolean rowNr;
    private Context context;
    DBService dbService;
    AssetManager assetManager;
    //ViewGroup.LayoutParams lp;
    FontCache font;

    public class BaseActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                    Log.e("Error"+Thread.currentThread().getStackTrace()[2],paramThrowable.getLocalizedMessage());
                }
            });
        }
    }

    /*
        private static String prev_separator = "";
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
    */
    public SearchableAdapter(Context context, AssetManager assetManager, List<String> data, boolean rowNr) {
        this.filteredData = data;
        this.originalData = data;
        this.assetManager = assetManager;
        this.rowNr = rowNr;
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return filteredData.size();
    }

    public Object getItem(int position) {
        return filteredData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        ViewHolder holder;
        dbService = new DBService();
        font = new FontCache();

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            Typeface typeFace = font.get("gnuolanerg.ttf", context);

            convertView = mInflater.inflate(R.layout.rowlayout, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();

            holder.text = (TextView) convertView.findViewById(R.id.label);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.text.setTypeface(typeFace);
            ((TextView) convertView.findViewById(R.id.nr)).setTypeface(typeFace);

            // Bind the data efficiently with the holder.

            convertView.setTag(holder);


        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        if (rowNr) {
            (convertView.findViewById(R.id.nr)).setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.nr)).setText(position + 1 + ".");
        } else {
            (convertView.findViewById(R.id.nr)).setVisibility(View.GONE);
        }

        String type = dbService.getType(filteredData.get(position), context);

        //ikoner

        switch (type) {
            case "S":
                holder.icon.setImageResource(R.drawable.soft_icon);
                break;
            case "M":
                holder.icon.setImageResource(R.drawable.milk_icon);
                break;
            case "J":
                holder.icon.setImageResource(R.drawable.fruit_icon);
                break;
            default:
                holder.icon.setImageResource(R.drawable.random_icon);
                break;
        }
        // If weren't re-ordering this you could rely on what you set last time

        holder.text.setText(filteredData.get(position));

        return convertView;
    }

    static class ViewHolder {
        TextView text;
        ImageView icon;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<String> list = originalData;

            int count = list.size();
            final ArrayList<String> nlist = new ArrayList<String>(count);

            String filterableString;
            String prev_sep = "";
            for (int i = 0; i < count; i++) {

                filterableString = list.get(i);
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }

    }

    public ArrayList<String> getList() {
        return new ArrayList<>(originalData);
    }

}

