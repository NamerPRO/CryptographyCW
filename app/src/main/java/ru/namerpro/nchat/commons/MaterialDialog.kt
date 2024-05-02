package ru.namerpro.nchat.commons

import android.content.Context
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.namerpro.nchat.R

fun showDialog(
    context: Context,
    title: String,
    message: String,
    action: () -> Unit = {}
) {
    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setNeutralButton(context.getString(R.string.dialog_cancel_button)) { _, _ -> }
        .setPositiveButton(context.getString(R.string.dialog_finish_button)) { _, _ ->
            action.invoke()
        }
    dialog.show()
}

fun showExitDialog(
    context: Context,
    title: String,
    message: String,
    action: () -> Unit = {}
) {
    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setNeutralButton(context.getString(R.string.dialog_cancel_button)) { _, _ -> }
        .setPositiveButton(context.getString(R.string.dialog_exit_button)) { _, _ ->
            action.invoke()
        }
    dialog.show()
}

fun showUnclosableDialog(
    context: Context,
    title: String,
    message: String
): AlertDialog {
    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
    return dialog.show()
}

fun showEditableDialog(
    context: Context,
    title: String,
    message: String,
    action: (String) -> Unit = {}
) {
    val input = EditText(context)
    input.inputType = InputType.TYPE_CLASS_TEXT

    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setMessage(message)
        .setView(input)
        .setCancelable(false)
        .setPositiveButton(context.getString(R.string.dialog_enter_input_button)) { _, _ ->
            action.invoke(input.text.toString())
        }
    dialog.show()
}