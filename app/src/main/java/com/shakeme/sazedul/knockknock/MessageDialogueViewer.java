package com.shakeme.sazedul.knockknock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by Sazedul on 05-Dec-14.
 */
public class MessageDialogueViewer {
    /**
     * Function to display simple Alert Dialog
     * @param context - application context
     * @param title - alert dialog title
     * @param message - alert message
     * @param status - success/failure (used to set icon)
     *               - pass null if you don't want icon
     * */
    public void showAlertDialog(Context context, String title, String message, Boolean status, DialogInterface.OnClickListener listener) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        if(status != null)
            // Setting alert dialog icon
            alertDialog.setIcon((status) ? R.drawable.ic_dialog_info : R.drawable.ic_dialog_alert);

        alertDialog.setMessage(message);
        // Setting OK Button
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", listener);

        // Showing Alert Message
        alertDialog.show();
    }
}
