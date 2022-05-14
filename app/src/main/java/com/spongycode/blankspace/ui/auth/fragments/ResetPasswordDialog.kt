package com.spongycode.blankspace.ui.auth.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.spongycode.blankspace.R
import com.spongycode.blankspace.ui.auth.AuthActivity
import com.spongycode.blankspace.util.Helper


class ResetPasswordDialog : DialogFragment() {

    private val firebaseAuth = AuthActivity().firebaseAuth


    companion object {
        fun newInstance(): ResetPasswordDialog {
            return ResetPasswordDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_reset_password, container, false)
    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        Helper.buttonEffect(view.findViewById<Button>(R.id.reset_password_btn), "#C665F37D")
        view.findViewById<MaterialButton>(R.id.reset_password_btn).setOnClickListener {
            val resetEmail = requireView().findViewById<TextInputLayout>(R.id.emailTextField_reset).editText?.text.toString()
            if(resetEmail.isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(resetEmail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            dismiss()
                            Snackbar.make(
                                requireActivity().findViewById(android.R.id.content),
                                getString(R.string.check_your_email),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        } else {
                            // failed!
                        }
                    }
            }
        }
        view.findViewById<MaterialButton>(R.id.reset_password_btn_close).setOnClickListener{
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


}