package ru.tlrs.iss.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import ru.tlrs.iss.R;

public class DialogHelper {

    public static AlertDialog createOKDialog(Context context, int messageResId, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(messageResId);
        builder.setNeutralButton(R.string.neutral_label, onClickListener);
        return builder.create();
    }

    public static AlertDialog createTwoButtonDialog(Context context, int messageResId, int positiveButtonResId, int negativeButtonResId,
                                                    DialogInterface.OnClickListener positiveClickListener, DialogInterface.OnClickListener negativeClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(messageResId);
        builder.setPositiveButton(positiveButtonResId, positiveClickListener);
        builder.setNegativeButton(negativeButtonResId, negativeClickListener);
        builder.setCancelable(false);
        return builder.create();
    }
}
