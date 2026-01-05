package com.example.chatapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.chatapplication.databinding.FragmentFriendBinding


class FriendFragment : DialogFragment() {

    private lateinit var binding: FragmentFriendBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        binding = FragmentFriendBinding.inflate(inflater, container, false)

        val btnClose = binding.btnClose
        btnClose.setOnClickListener {
            dismiss()
        }
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

}