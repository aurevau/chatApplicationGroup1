package com.example.chatapplication.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatapplication.data.ChatRoom
import com.example.chatapplication.data.Message
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.example.chatapplication.R
import com.google.firebase.firestore.Query

class MessageRepository {

    private val db = Firebase.firestore
    private val _message = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _message

    private val _recentChats = MutableLiveData<List<ChatRoom>>()
    val recentChats: LiveData<List<ChatRoom>> get() = _recentChats

    private val _chatRoomDetails = MutableLiveData<ChatRoom>()
    val chatRoomDetails: LiveData<ChatRoom> get() = _chatRoomDetails

    fun listenToChat(roomId: String) {
        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val messages = mutableListOf<Message>()
                var processedCount = 0
                val totalMessages = snapshot.documents.size

                if (totalMessages == 0) {
                    _message.value = emptyList()
                    return@addSnapshotListener
                }

                snapshot.documents.forEach { doc ->
                    val message = doc.toObject(Message::class.java)?.copy(id = doc.id)
                    if (message == null) {
                        processedCount++
                        if (processedCount == totalMessages) {
                            _message.value = messages.sortedBy { it.timestamp }
                        }
                        return@forEach
                    }

                    db.collection("users").document(message.senderId).get()
                        .addOnSuccessListener { userDoc ->
                            message.senderName = userDoc.getString("fullName")
                            messages.add(message)
                            processedCount++
                            if (processedCount == totalMessages) {
                                _message.value = messages.sortedBy { it.timestamp }
                            }
                        }
                        .addOnFailureListener {
                            messages.add(message)
                            processedCount++
                            if (processedCount == totalMessages) {
                                _message.value = messages.sortedBy { it.timestamp }
                            }
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

        val user = Firebase.auth.currentUser ?: return

        val msg = Message(
            senderId = user.uid,
            roomId = roomId,
            text = text ?: "",
            timestamp = System.currentTimeMillis()
        )
        ensureChatRoomExists(roomId, otherUserId) {
            db.collection("chatRooms")
                .document(roomId)
                .collection("messages")
                .add(msg).addOnSuccessListener {
                    updateChatRoomLastMessage(roomId, text)
                }
        }
    }


    fun sendImageMessage(
        roomId: String,
        imageUrl: String,
        text: String?,
        otherUserId: String? = null
    ) {

        val user = Firebase.auth.currentUser ?: return

        val cleanText = text?.trim() ?: ""

        val msg = Message(
            senderId = user.uid,
            roomId = roomId,
            text = cleanText,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis()
        )
        ensureChatRoomExists(roomId, otherUserId) {
            db.collection("chatRooms")
                .document(roomId)
                .collection("messages")
                .add(msg).addOnSuccessListener {
                    updateChatRoomLastImage(roomId, imageUrl, cleanText)
                }
        }
    }

    fun createGroupChat(
        roomId: String,
        userIds: List<String>,
        groupName: String,
        memberNames: List<String>,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val chatRoomData = mapOf(
            "roomId" to roomId,
            "groupName" to groupName,
            "members" to userIds,
            "memberNames" to memberNames,
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


    private fun ensureChatRoomExists(
        roomId: String,
        otherUserId: String? = null,
        onReady: () -> Unit = {}
    ) {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return


        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { currentUserDoc ->
                val currentUserName = currentUserDoc.getString("fullName") ?: "Me"

        db.collection("chatRooms").document(roomId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    val members = if (otherUserId != null) listOf(currentUserId, otherUserId)
                    else listOf(currentUserId)

                    if (otherUserId != null) {
                        db.collection("users").document(otherUserId).get()
                            .addOnSuccessListener { otherUserDoc ->
                                val otherUserName =
                                    otherUserDoc.getString("fullName") ?: "Unknown User"
                                db.collection("chatRooms").document(roomId).set(
                                    mapOf(
                                        "members" to members,
                                        "userName" to otherUserName,
                                        "groupName" to null,
                                        "memberNames" to listOf(otherUserName,currentUserName),
                                        "createdAt" to System.currentTimeMillis(),
                                        "lastMessage" to "",          // Important: empty String
                                        "lastImageMessage" to "",
                                        "lastMessageTimestamp" to System.currentTimeMillis(),
                                        "isGroup" to false
                                    )
                                ).addOnSuccessListener { onReady() }
                            }
                    } else {

                        db.collection("chatRooms").document(roomId).set(
                            mapOf(
                                "members" to members,
                                "memberNames" to listOf(currentUserId),
                                "createdAt" to System.currentTimeMillis(),
                                "lastMessage" to "",
                                "lastImageMessage" to "",
                                "lastMessageTimestamp" to System.currentTimeMillis(),
                                "isGroup" to false
                            )
                        ).addOnSuccessListener { onReady() }
                    }
                } else {
                    onReady()
                }
            }
    }}


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
    fun getRecentChats(context: Context) {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        db.collection("chatRooms")
            .whereArrayContains("members", currentUserId)
            .orderBy(
                "lastMessageTimestamp",
                Query.Direction.DESCENDING
            )
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null || snapshot.isEmpty) {
                    _recentChats.value = emptyList()
                    return@addSnapshotListener
                }

                val totalDocs = snapshot.documents.size
                val chatList = MutableList<ChatRoom?>(totalDocs) { null }
                var processedCount = 0

                snapshot.documents.forEachIndexed { index, doc ->
                    val members = doc.get("members") as? List<*>
                    val isGroup = doc.getBoolean("isGroup") == true
                    val lastMessageTimestamp = doc.getLong("lastMessageTimestamp") ?: 0L
                    val groupNameFromDoc = doc.getString("groupName")?.takeIf { it.isNotBlank() }


                    if (isGroup) {
                        chatList[index] =
                            ChatRoom(
                                roomId = doc.id,
                                userName = null,
                                groupName = groupNameFromDoc,
                                memberNames = doc.get("memberNames") as? List<String>
                                    ?: emptyList(),
                                lastMessage = doc.getString("lastMessage") ?: "",
                                lastImageMessage = doc.getString("lastImageMessage") ?: "",
                                timestamp = lastMessageTimestamp,
                                isGroup = doc.getBoolean("isGroup") == true
                            )

                        processedCount++
                        if (processedCount == totalDocs) _recentChats.value = chatList.filterNotNull()
                        return@forEachIndexed
                    }


                    val otherUserId = members?.firstOrNull { it != currentUserId } as? String
                    val targetUserId = otherUserId ?: currentUserId

                    db.collection("users").document(targetUserId).get()
                        .addOnSuccessListener { userDoc ->
                            val userName = if (otherUserId == null) {
                                userDoc.getString("fullName")?.let {
                                    context.getString(R.string.me_following_text, it)
                                } ?: "Me"
                            } else {
                                userDoc.getString("fullName") ?: "Unknown User"
                            }
                            chatList[index] =
                                ChatRoom(
                                    roomId = doc.id,
                                    userName = userName,
                                    groupName = groupNameFromDoc,
                                    chatRoomImageUrl = userDoc.getString("profileImageUrl") ?: "",
                                    lastMessage = doc.getString("lastMessage") ?: "",
                                    lastImageMessage = doc.getString("lastImageMessage") ?: "",
                                    timestamp = lastMessageTimestamp,
                                    isGroup = false
                                )
                            processedCount++
                            if (processedCount == totalDocs) _recentChats.value =
                                chatList.filterNotNull()
                        }.addOnFailureListener {
                            processedCount++
                            if (processedCount == totalDocs) _recentChats.value =
                                chatList.filterNotNull()
                        }


                }
            }
    }


    private fun updateChatRoomLastMessage(roomId: String, message: String) {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        db.collection("chatRooms").document(roomId).update(
            mapOf(
                "lastMessage" to message,
                "lastImageMessage" to null,
                "lastMessageTimestamp" to System.currentTimeMillis(),
                "lastMessageSenderId" to currentUserId
            )
        )
    }

    private fun updateChatRoomLastImage(roomId: String, imageUrl: String, text: String?) {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        db.collection("chatRooms").document(roomId).update(
            mapOf(
                "lastMessage" to text,
                "lastImageMessage" to imageUrl,
                "lastMessageTimestamp" to System.currentTimeMillis(),
                "lastMessageSenderId" to currentUserId
            )
        )
    }

    fun getChatRoomDetailsById(roomId: String) {
        db.collection("chatRooms").document(roomId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val room = snapshot.toObject(ChatRoom::class.java)?.copy(
                        isGroup = snapshot.getBoolean("isGroup") == true
                    )
                    _chatRoomDetails.value = room!!
                }
            }
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

                            //  Update chatroom with the latest message so the text changes from picture
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


    fun updateChatName(roomId: String, newName: String) {
        val chatRef = db.collection("chatRooms").document(roomId)
        chatRef.update("groupName", newName)
            .addOnSuccessListener {
                Log.d("Chat", "Chat name updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Chat", "Failed to update chat name", e)
            }
    }


}
