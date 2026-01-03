package com.example.chatapplication.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatapplication.data.Message
import com.example.chatapplication.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.UUID

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

    fun sendMessage(targetUserId: String, text: String){
        val user = Firebase.auth.currentUser ?: return

        val sortedIds = listOf(user.uid, targetUserId).sorted()
        val roomId = "${sortedIds[0]}_${sortedIds[1]}"

        val msg = Message(
            senderId = user.uid,
            receiverId = targetUserId,
            roomId = roomId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .add(msg)
    }

    fun sendImageMessage(targetUserId: String, imageUrl: String) {
        val user = Firebase.auth.currentUser ?: return

        val sortedIds = listOf(user.uid, targetUserId).sorted()
        val roomId = "${sortedIds[0]}_${sortedIds[1]}"

        val msg = Message(
            senderId = user.uid,
            receiverId = targetUserId,
            roomId = roomId,
            timestamp = System.currentTimeMillis(),
            imageUrl = imageUrl,
        )

        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .add(msg)

    }

    fun uploadChatImage(
        imageUri: Uri,
        roomId: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val storageRef = Firebase.storage.reference
            .child("chatRooms/$roomId/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString()) // Detta är URL som vi skickar som message
                }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }


}
