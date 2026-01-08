package com.example.chatapplication.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatapplication.data.ChatRoom
import com.example.chatapplication.data.Message
import com.example.chatapplication.data.User
import com.example.chatapplication.util.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.UUID

class MessageRepository {

    private val db = Firebase.firestore
    private val _message = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _message

    private val _recentChats = MutableLiveData<List<ChatRoom>>()
    val recentChats: LiveData<List<ChatRoom>> get() = _recentChats

    fun listenToChat(roomId: String) {
        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _message.value = snapshot.documents.mapNotNull {
                        it.toObject(Message::class.java)?.copy(id = it.id)
                    }
                }

            }
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
                    onSuccess(uri.toString()) //This is the URL we send as a message
                }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun sendTextMessage(roomId: String, text: String, otherUserId: String? = null) {
        ensureChatRoomExists(roomId, otherUserId)
        val user = Firebase.auth.currentUser ?: return

        val msg = Message(
            senderId = user.uid,
            roomId = roomId,
            text = text ?: "",
            timestamp = System.currentTimeMillis()
        )

        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .add(msg).addOnSuccessListener {
                updateChatRoomLastMessage(roomId, text)
            }
    }



    fun sendImageMessage(roomId: String, imageUrl: String, text: String?, otherUserId: String? = null) {
        ensureChatRoomExists(roomId, otherUserId)
        val user = Firebase.auth.currentUser ?: return

        val msg = Message(
            senderId = user.uid,
            roomId = roomId,
            text = text ?: "",
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )

        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .add(msg).addOnSuccessListener {
                updateChatRoomLastImage(roomId, imageUrl)
            }
    }

    fun allChatRoomCollectionReference(): CollectionReference =
        FirebaseFirestore.getInstance().collection("chatRooms")


    fun createGroupChat(
        roomId: String,
        userIds: List<String>,
        groupName: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val chatRoomData = mapOf(
            "roomId" to roomId,
            "groupName" to groupName,
            "members" to userIds,
            "createdAt" to System.currentTimeMillis(),
            "lastMessage" to "",
            "lastImageMessage" to "",
            "lastMessageTimestamp" to System.currentTimeMillis(),
            "lastMessageSenderId" to "",
            "isGroup" to true
        )

        val ref = db.collection("chatRooms").document(roomId)

        ref.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    ref.set(chatRoomData)
                }
                onSuccess(roomId)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }


    private fun ensureChatRoomExists(roomId: String, otherUserId: String? = null) {
        db.collection("chatRooms").document(roomId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    val currentUserId = Firebase.auth.currentUser?.uid ?: return@addOnSuccessListener
                    val members = if (otherUserId != null) {
                        listOf(currentUserId, otherUserId)
                    } else {
                        listOf(currentUserId)
                    }

                    db.collection("chatRooms").document(roomId).set(mapOf(
                        "members" to members,
                        "createdAt" to System.currentTimeMillis(),
                        "lastMessage" to "",
                        "lastImageMessage" to "",
                        "lastMessageTimestamp" to System.currentTimeMillis(),
                        "isGroup" to false
                    ))
                }
            }
    }

    fun deleteChatRoom(chatRoom: ChatRoom) {
        chatRoom.roomId?.let {
            db.collection("chatRooms")
                .document(it)
        }
            ?.delete()
            ?.addOnSuccessListener {
                Log.d("DELETE_CHAT", "Chat room $chatRoom.id deleted successfully")
            }
            ?.addOnFailureListener { e ->
                Log.e("DELETE_CHAT", "Error deleting chat room", e)
            }
    }


    //fetch Firebase and return a list of chat rooms
    fun getRecentChats() {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        db.collection("chatRooms")
            .whereArrayContains("members", currentUserId)
            .orderBy("lastMessageTimestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) {
                    _recentChats.value = emptyList()
                    return@addSnapshotListener
                }

                val chatList = mutableListOf<ChatRoom>()
                var processedCount = 0
                val totalDocs = snapshot.documents.size

                if (totalDocs == 0) {
                    _recentChats.value = emptyList()
                    return@addSnapshotListener
                }

                snapshot.documents.forEach { doc ->
                    val members = doc.get("members") as? List<*>
                    val otherUserId = members?.firstOrNull { it != currentUserId } as? String

                    val isGroup = doc.getBoolean("isGroup") == true
                    if(isGroup) {
                        chatList.add(
                                ChatRoom(
                                    roomId = doc.id,
                                    userName = doc.getString("groupName") ?: "Grupp",
                                    lastMessage = doc.getString("lastMessage") ?: "",
                                    lastImageMessage = doc.getString("lastImageMessage") ?: "",
                                    timestamp = DateUtils.formatTimestamp(
                                        doc.getLong("lastMessageTimestamp") ?: 0
                                    ),
                                    isGroup = true
                                )
                        )
                        processedCount++

                    } else {
                        val members = doc.get("members") as? List<*>
                    val otherUserId =
                        members?.firstOrNull { it != currentUserId } as? String

                    if (otherUserId == null) {
                        processedCount++
                        return@forEach
                    }

                    db.collection("users").document(otherUserId).get()
                        .addOnSuccessListener { userDoc ->
                            chatList.add(
                                ChatRoom(
                                    roomId = doc.id,
                                    userName = userDoc.getString("fullName") ?: "Unknown User",
                                    chatRoomImageUrl = userDoc.getString("profileImageUrl") ?: "",
                                    lastMessage = doc.getString("lastMessage"),
                                    lastImageMessage = doc.getString("lastImageMessage"),
                                    timestamp = DateUtils.formatTimestamp(
                                        doc.getLong("lastMessageTimestamp") ?: 0
                                    )
                                )
                            )
                            processedCount++
                            if (processedCount == totalDocs) {
                                _recentChats.value = chatList
                            }
                        }
                    return@forEach
                }

                if (processedCount == totalDocs) {
                    _recentChats.value = chatList
                }
            }
    }
}


    private fun updateChatRoomLastMessage(roomId: String, message: String) {
        db.collection("chatRooms").document(roomId).update(
            mapOf(
                "lastMessage" to message,
                "lastImageMessage" to null,
                "lastMessageTimestamp" to System.currentTimeMillis()
            )
        )
    }

    private fun updateChatRoomLastImage(roomId: String, imageUrl: String) {
        db.collection("chatRooms").document(roomId).update(
            mapOf(
                "lastMessage" to null,
                "lastImageMessage" to imageUrl,
                "lastMessageTimestamp" to System.currentTimeMillis()
            )
        )
    }


    fun deleteMessage(messageId: String, roomId: String, senderId: String) {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        // Only allow sender to delete
        if (senderId != currentUserId) return

        val messagesRef = db.collection("chatRooms")
            .document(roomId)
            .collection("messages")

        // Delete the message
        messagesRef.document(messageId)
            .delete()
            .addOnSuccessListener {
                // Get latest message sent before delete
                messagesRef
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.isEmpty) {
                            //  Ingen kvar → nollställ lastMessage och lastImageMessage
                            db.collection("chatRooms").document(roomId).update(
                                mapOf(
                                    "lastMessage" to null,
                                    "lastImageMessage" to null,
                                    "lastMessageTimestamp" to System.currentTimeMillis()
                                )
                            )
                        } else {

                            val last = snapshot.documents[0].toObject(Message::class.java)
                            val lastTimestamp: Long = last?.timestamp ?: System.currentTimeMillis()

                            //  Uppdate chatroom with the latest message so the text changes from picture
                            db.collection("chatRooms").document(roomId).update(
                                mapOf(
                                    "lastMessage" to last?.text,
                                    "lastImageMessage" to last?.imageUrl,
                                    "lastMessageTimestamp" to lastTimestamp
                                )
                            )
                        }
                    }
            }
    }


    private fun checkAndDeleteEmptyChat(roomId: String) {
        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    // No messages left - delete the chat room
                    db.collection("chatRooms")
                        .document(roomId)
                        .delete()
                }
            }
    }



}
