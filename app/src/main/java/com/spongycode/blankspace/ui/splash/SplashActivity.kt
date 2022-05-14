package com.spongycode.blankspace.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.spongycode.blankspace.R
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.ui.auth.AuthActivity
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.util.userdata


class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        this.supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        firestore = FirebaseFirestore.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            val uid = auth.currentUser?.uid.toString()
            userdata.afterLoginUserData = UserModel()
            firestore.collection("users")
                .whereEqualTo("userId", uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (data in task.result!!) {
                            userdata.afterLoginUserData.imageUrl =
                                data.toObject(UserModel::class.java).imageUrl
                            userdata.afterLoginUserData.email =
                                data.toObject(UserModel::class.java).email
                            userdata.afterLoginUserData.status =
                                data.toObject(UserModel::class.java).status
                            userdata.afterLoginUserData.userId =
                                data.toObject(UserModel::class.java).userId
                            userdata.afterLoginUserData.username =
                                data.toObject(UserModel::class.java).username
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
                        }
                    }
                }
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
}

