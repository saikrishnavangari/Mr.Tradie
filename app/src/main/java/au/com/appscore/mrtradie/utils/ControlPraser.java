package au.com.appscore.mrtradie.utils;


import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Created by lijiazhou on 31/01/2016.
 */
public class ControlPraser {

    public static <T> T PraserControl(View context, int id)
    {
        return (T)(context.findViewById(id));
    }

    public static <T> T PraserControl(Activity context, int id)
    {
        return (T)(context.findViewById(id));
    }

    public static AttributeSet GetAttr(Context context, int id)
    {
        return Xml.asAttributeSet(context.getResources().getLayout(id));
    }

    public static <T> T GetAttributeByName(Object parent, String name) throws NoSuchFieldException, IllegalAccessException
    {
        Field field = parent.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T)(field.get(parent));
    }
}
