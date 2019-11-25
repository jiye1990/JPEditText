package com.example.jpedittext

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.jpedittext.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.jpEditText.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.jpEditText.hasFocus() && (s!!.length in 1..7)) {
                    binding.jpEditText.error = "8자 이상으로 부탁"
                    binding.jpEditText.setUnderlineColor(R.color.red500)
                    binding.jpEditText.hideBottomText = false
                } else {
                    binding.jpEditText.error = null
                    binding.jpEditText.setUnderlineColor(R.color.green500)
                    binding.jpEditText.hideBottomText = true
                }
                binding.jpEditText.invalidate()
            }

        })

        binding.jpEditText1.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.jpEditText1.hasFocus() && (s!!.length in 1..7)) {
                    binding.jpEditText1.error = "8자 이상으로 부탁"
                    binding.jpEditText1.setUnderlineColor(R.color.red500)
                    binding.jpEditText1.hideBottomText = false
                } else {
                    binding.jpEditText1.error = null
                    binding.jpEditText1.setUnderlineColor(R.color.green500)
                    binding.jpEditText1.hideBottomText = true
                }
                binding.jpEditText1.invalidate()
            }

        })

    }
}
