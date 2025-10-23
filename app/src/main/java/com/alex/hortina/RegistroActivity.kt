package com.alex.hortina

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alex.hortina.data.RetrofitClient
import com.alex.hortina.data.Usuario
import com.alex.hortina.data.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)

        btnRegistrar.setOnClickListener {
            val usuario = Usuario(
                nombre = etNombre.text.toString(),
                email = etEmail.text.toString(),
                contrasena = etContrasena.text.toString()
            )

            // Llamada a Retrofit
            RetrofitClient.instance.registrarUsuario(usuario)
                .enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            val res = response.body()
                            Toast.makeText(this@RegistroActivity, res?.message, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@RegistroActivity, "Error en la petición", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@RegistroActivity, t.message, Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}
