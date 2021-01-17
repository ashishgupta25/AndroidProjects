package com.example.deepakk.ola;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Utils {

    Context context;

    public Utils(Context context) {
        this.context = context;
    }

    public int dpToPx(int dp) {

        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));

        /*
        Incorrect result:
        float density = context.getResources().getDisplayMetrics().densityDpi/ DisplayMetrics.DENSITY_DEFAULT;
        int pixel = (int)(dp*density);
        return pixel;*/

    }
}
