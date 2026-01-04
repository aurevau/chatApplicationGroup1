package com.example.chatapplication.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatapplication.data.Message
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


class MessageRepository {

    private val db = Firebase.firestore
    private val _message = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _message

    fun listenToChat(roomId: String){
        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener {snapshot, _ ->
                if (snapshot != null){
                    _message.value = snapshot.documents.mapNotNull {
                        it.toObject(Message::class.java)?.copy(id = it.id)
                    }
                }

            }
    }

    fun sendMessage(roomId: String, text: String){
        val user = Firebase.auth.currentUser ?: return
        val msg = Message(senderId = user.uid, text = text)
        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .add(msg)
    }

}