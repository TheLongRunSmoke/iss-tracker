package ru.tlrs.iss.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import ru.tlrs.iss.R;

public class DialogHelper{

    public static AlertDialog createOKDialog(Context context, int messageResId, DialogInterface.OnClickListener onClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(messageResId);
        builder.setNeutralButton(R.string.neutral_label, onClickListener);
        return builder.create();
    }
}
