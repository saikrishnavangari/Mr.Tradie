package au.com.appscore.mrtradie.Dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;

/**
 * Created by lijiazhou on 2/02/16.
 */
public class ReportRatingDialog extends MrTradieDialogBase {

    RelativeLayout reportRating;
    View.OnClickListener reportRatingListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View view = inflater.inflate(R.layout.report_rating_menu, container, false);
        reportRating = ControlPraser.PraserControl(view, R.id.action_report_rating);
        reportRating.setOnClickListener(reportRatingListener);
        return view;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window wdw = dialog.getWindow();
        WindowManager.LayoutParams wlp = wdw.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wdw.setAttributes(wlp);
        return dialog;
    }

    public void setReportRatingListener(View.OnClickListener listener)
    {
        reportRatingListener = listener;
    }

}
