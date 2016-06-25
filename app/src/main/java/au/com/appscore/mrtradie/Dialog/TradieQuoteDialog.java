package au.com.appscore.mrtradie.Dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import au.com.appscore.mrtradie.R;
import au.com.appscore.mrtradie.utils.ControlPraser;

/**
 * Created by lijiazhou on 3/02/16.
 */
public class TradieQuoteDialog extends MrTradieDialogBase {

    RelativeLayout accept;
    RelativeLayout decline;
    RelativeLayout delete;
    View.OnClickListener acceptListener;
    View.OnClickListener declineListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View view = inflater.inflate(R.layout.tradie_quote_reply_dialog, container, false);
        accept = ControlPraser.PraserControl(view, R.id.tradie_quote_accept);
        decline = ControlPraser.PraserControl(view, R.id.tradie_quote_decline);
        delete = ControlPraser.PraserControl(view, R.id.tradie_quote_delete);
        accept.setOnClickListener(acceptListener);
        decline.setOnClickListener(declineListener);
        delete.setOnClickListener(declineListener);
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

    public void setListener(View.OnClickListener acceptListener, View.OnClickListener declineListener)
    {
        this.acceptListener = acceptListener;
        this.declineListener = declineListener;
    }

    public void setDisable(View.OnClickListener declineListener)
    {
        ImageView acc = ControlPraser.PraserControl(this.getView(), R.id.tradie_quote_accept_image);
        acc.setImageResource(R.drawable.icon_accept_disabled);
        ImageView del = ControlPraser.PraserControl(this.getView(), R.id.tradie_quote_decline_image);
        del.setImageResource(R.drawable.icon_decline_disabled);
        delete.setOnClickListener(declineListener);
    }
}
