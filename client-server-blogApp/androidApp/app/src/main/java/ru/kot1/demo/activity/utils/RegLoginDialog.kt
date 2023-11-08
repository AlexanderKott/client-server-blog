package ru.kot1.demo.activity.utils

import android.app.Activity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.kot1.demo.R
import ru.kot1.demo.activity.AppActivity
import ru.kot1.demo.repository.AppNetState


enum class Dialog {
    LOGIN, REGISTER, REGISTER_AVATAR
}



fun Activity.showLoginAuthDialog(
    dialogType: Dialog,
    actionCallback: (login: String, password: String, name: String) -> Unit) {

    val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.ThemeOverlayAlertDialogX)
    val dialogView = layoutInflater.inflate(R.layout.login_auth, null)

    dialogBuilder.setCancelable(false)
    dialogBuilder.setView(dialogView)
        .setPositiveButton(getString(R.string.ok)) { a, b ->
            val login = dialogView.findViewById<EditText>(R.id.login)
            val password = dialogView.findViewById<EditText>(R.id.password)
            val name = dialogView.findViewById<EditText>(R.id.name)

            actionCallback(login.text.toString(), password.text.toString(), name.text.toString())
        }
        .setNegativeButton(getString(R.string.cancel)) { a, b ->

        }

    when (dialogType) {
        Dialog.LOGIN -> {
            dialogBuilder.setTitle(R.string.sign_in)
        }

        Dialog.REGISTER -> {
            dialogBuilder.setTitle(R.string.sign_up)

        }

        Dialog.REGISTER_AVATAR -> {
            dialogBuilder.setTitle(R.string.sign_up)
        }
    }

    val alertDialog = dialogBuilder.create()

    when (dialogType) {
        Dialog.REGISTER -> {
            showRegFields(dialogView, alertDialog, this)
        }

        Dialog.REGISTER_AVATAR -> {
            showRegFields(dialogView, alertDialog, this)
        }
    }

    alertDialog.show()

    when (dialogType) {
        Dialog.REGISTER, Dialog.REGISTER_AVATAR -> {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                false
        }
    }


}

private fun showRegFields(
    dialogView: View,
    alertDialog: AlertDialog,
    activity: Activity
) {

    val password = dialogView.findViewById<EditText>(R.id.password)

    val passwordL = dialogView.findViewById<TextView>(R.id.passwordL2)
    val password2 = dialogView.findViewById<EditText>(R.id.password2)
    val nameL = dialogView.findViewById<TextView>(R.id.nameL)
    val name = dialogView.findViewById<EditText>(R.id.name)
    val setAva = dialogView.findViewById<MaterialButton>(R.id.setAva)

    passwordL.visibility = View.VISIBLE
    password2.visibility = View.VISIBLE
    name.visibility = View.VISIBLE
    nameL.visibility = View.VISIBLE
    setAva.visibility = View.VISIBLE

    setAva.setOnClickListener {
        ImagePicker.with(activity)
            .crop()
            .compress(1024)
            .galleryOnly()
            .galleryMimeTypes(
                arrayOf(
                    "image/png",
                    "image/jpeg",
                )
            )
            .start(AppActivity.photoRequestCode)
    }

    password.addTextChangedListener {
        checkField(activity,password, password2, alertDialog)
    }

    password2.addTextChangedListener {
        checkField(activity,password2, password, alertDialog)
    }
}

private fun checkField(
    activity: Activity,
    password: EditText,
    password2: EditText,
    alertDialog: AlertDialog
) {
    if (password.text.toString() == password2.text.toString()) {
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
            true
        password.error = null
        password2.error = null

    } else {
        password.error = activity.getString(R.string.Passwords_do_not_match)
        password2.error = null
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
            false
    }
}

fun Activity.showAuthResultDialog(message: AppNetState) {
    runOnUiThread {
        val dialog = MaterialAlertDialogBuilder(this, R.style.ThemeOverlayAlertDialogX)
            .setTitle(R.string.enter_dialog)
            .setPositiveButton(android.R.string.ok) { dialog, which -> }
            .setIcon(android.R.drawable.ic_dialog_alert)
        when (message) {
            AppNetState.NO_INTERNET -> dialog.setMessage(R.string.error_dialog_auth1)
            AppNetState.NO_SERVER_CONNECTION -> dialog.setMessage(R.string.error_dialog_auth2)
            AppNetState.CONNECTION_ESTABLISHED -> dialog.setMessage(R.string.error_dialog_auth3)
            AppNetState.THIS_USER_NOT_REGISTERED -> dialog.setMessage(R.string.error_dialog_auth4)
            AppNetState.INCORRECT_PASSWORD -> dialog.setMessage(R.string.error_dialog_auth5)
            AppNetState.SERVER_ERROR_500 -> dialog.setMessage(R.string.error_dialog_auth6)
        }
        dialog.show()
    }
}