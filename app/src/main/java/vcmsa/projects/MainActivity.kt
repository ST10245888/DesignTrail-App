package vcmsa.projects.fkj_consultants

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.fkj_consultants.activities.LoginActivity
import vcmsa.projects.fkj_consultants.activities.RegisterActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        btnLogin.setOnClickListener {
            // Navigate to Login Activity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnRegister.setOnClickListener {
            // Navigate to Register Activity
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}