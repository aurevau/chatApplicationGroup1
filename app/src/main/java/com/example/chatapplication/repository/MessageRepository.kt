package com.example.chatapplication.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatapplication.data.Message
import com.example.chatapplication.data.User
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


    //To get the target user's details
    fun getUserDetailsById(userId: String, callback: (User?) -> Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    callback(user?.copy(id = document.id))
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }


    //To make it support group chats, We can send an array of Ids to the function
    fun getMessagesList(myUserId:String, targetUserId:String, callback: (List<Message>) -> Unit){
        //sort ids to the array
        val sortedIds = listOf(myUserId, targetUserId).sorted()
        //make a unique string from the sorted array for roomId
        val roomId = "${sortedIds[0]}_${sortedIds[1]}"

        //Query to FireBase to get massages related to roomId
        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { snapshot ->
                val messages = snapshot.documents.mapNotNull {
                    it.toObject(Message::class.java)?.copy(id = it.id)
                }
                callback(messages)
            }
            .addOnFailureListener { exception ->
                callback(emptyList())
            }
    }

}

















