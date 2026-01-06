package com.example.chatapplication.popup

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.core.graphics.drawable.toDrawable
import com.example.chatapplication.data.ChatRoom
import com.example.chatapplication.databinding.FragmentDeleteChatPopupBinding
import com.example.chatapplication.repository.MessageRepository

class DeleteChatPopupFragment : DialogFragment() {

    private var _binding: FragmentDeleteChatPopupBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDeleteChatPopupBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val chatRoom = arguments?.getParcelable<ChatRoom>("chatRoom")
            ?: return

        binding.btnTrash.setOnClickListener {
            // Kör delete här
            MessageRepository().deleteChatRoom(chatRoom)
            dismiss()
        }

        binding.btnClosePopup.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(chatRoom: ChatRoom): DeleteChatPopupFragment {
            val fragment = DeleteChatPopupFragment()
            val args = Bundle()
            args.putParcelable("chatRoom", chatRoom)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}