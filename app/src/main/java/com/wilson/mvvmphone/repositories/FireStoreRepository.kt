package com.wilson.mvvmphone.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.wilson.mvvmphone.models.Cabpools
import com.wilson.mvvmphone.models.ShopItems
import com.wilson.mvvmphone.models.State
import com.wilson.mvvmphone.models.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class FireStoreRepository @Inject constructor(val firestore: FirebaseFirestore) {


    private val usersCollection = firestore.collection("Users")
    private val cabpoolsCollection = firestore.collection("Cabpools")
    private val itemsCollection = firestore.collection("StoreItems").document("Items")
    private val imageStorage = Firebase.storage.reference

    private var cabsList = ArrayList<Cabpools>()
    private var itemsList = ArrayList<ShopItems>()

    fun checkUserExists(idcard:String) = flow<State<Users>> {
        emit(State.loading())
        val snapshot = usersCollection.document(idcard).get().await()
        if(snapshot.exists()){
            val testuser = snapshot.toObject(Users::class.java)
            if(testuser!=null) emit(State.success(testuser))
            else emit(State.failed("TestUser Exists but is null wow"))
        }
        else emit(State.failed("Null DATA???"))

        // Emit success state with data
       // emit(State.success(posts))
    }.catch {
        // If exception is thrown, emit failed state along with message.
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun addNewUser(newUser:Users) = flow<State<Void>> {

        // Emit loading state
        emit(State.loading())
        Log.d("WOWTag",usersCollection.document(newUser.id.toString()).path)
        val postRef = usersCollection.document(newUser.id.toString()).set(newUser).await()
        // Emit success state with post reference
        emit(State.success(postRef))

    }.catch {
        // If exception is thrown, emit failed state along with message.
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun addCabpool_toCloud(cabpool: Cabpools) = flow<State<Void>>{
        emit(State.loading())
        val postRef = cabpoolsCollection
            .document(cabpool.date.toString().substring(3,6)+" "+cabpool.date.toString().substring(7,11))
            .collection(cabpool.date.toString().substring(0,2))
            .document(cabpool.startUserId.toString()).set(cabpool).await()
        emit(State.success(postRef))
    }.catch {
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun addCabpool_toUser(cabpool: Cabpools) = flow<State<Void>>{
        emit(State.loading())
        val postRef = usersCollection
            .document(cabpool.startUserId.toString())
            .collection("Cabpools")
            .document(cabpool.startUserId.toString()).set(cabpool).await()
        emit(State.success(postRef))
    }.catch {
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun deleteCabpool_toUser(idcard:String) = flow<State<String>>{
        emit(State.loading())

        val snapshot = usersCollection.document(idcard).collection("Cabpools").document(idcard).get().await()
        var date = ""
        if(snapshot.exists()){
            date = snapshot.toObject(Cabpools::class.java)?.date.toString()
            snapshot.reference.delete().await()
//            val postRef = usersCollection
//                .document(idcard)
//                .collection("Cabpools")
//                .document(idcard).delete().await()
        }
        emit(State.success(date))
    }.catch {
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun deleteCabpool_toCloud(date:String,idcard:String) = flow<State<String>>{
        emit(State.loading())

        val snapshot = cabpoolsCollection.document(date.substring(3,6)+" "+date.substring(7,11))
        .collection(date.substring(0,2))
        .document(idcard).get().await()

        var deleted = ""
        if(snapshot.exists()){
            deleted = "Deleted"
            snapshot.reference.delete().await()
        }

        emit(State.success(deleted))
    }.catch {
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun fetchCabpoolList(date : String) = flow<State<List<Cabpools>>>{
        emit(State.loading())

        Log.d("wow data loading : ", " date = $date : '"+date.substring(3,6)+"' and '"+date.substring(7,11)+"' and '"+date.substring(0,2)+"'")
        cabpoolsCollection.document(date.substring(3,6)+" "+date.substring(7,11))
        val snapshot = cabpoolsCollection.document(date.substring(3,6)+" "+date.substring(7,11))
            .collection(date.substring(0,2)).get().await()
        if(!snapshot.isEmpty){
            cabsList.clear()
            for (docs in snapshot){ cabsList.add(docs.toObject(Cabpools::class.java))
                Log.d("wow data : ",docs.toObject(Cabpools::class.java).toString())}
            emit(State.success(cabsList))

            Log.d("wow data success : ","")
        }
        else {
            Log.d("wow data empty: ","")
            emit(State.Failed("empty"))
        }


    }.catch {
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    fun uploadImagetoCloud(imageFile: ByteArray,category:String) = flow<State<String>>{
        emit(State.loading())
        val imagename = UUID.randomUUID().toString()
        val filepath = imageStorage.child("ShopItems/$category/$imagename")
        filepath.putBytes(imageFile).await()
        val imageuri = filepath.downloadUrl.await()
        emit(State.success(imageuri.toString()))

    }.catch {
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun sendShopItem(shopItems: ShopItems)= flow<State<Int>>{
        emit(State.loading())

        itemsCollection
            .collection(shopItems.category.toString())
            .add(shopItems).await()
        emit(State.success(1))
        usersCollection
            .document(shopItems.sellerid.toString())
            .collection("ShopItems")
            .add(shopItems).await()
        emit(State.success(2))
    }.catch {
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun fetchItemsList(category : String) = flow<State<List<ShopItems>>>{
        emit(State.loading())

        val snapshot = itemsCollection.collection(category).get().await()
        if(!snapshot.isEmpty){
            itemsList.clear()
            for (docs in snapshot){
                itemsList.add(docs.toObject(ShopItems::class.java))
                Log.d("wow data : ",docs.toObject(ShopItems::class.java).toString())
            }
            emit(State.success(itemsList))
            Log.d("wow data items : ","")
        }
        else {
            Log.d("wow data items empty: ","")
            emit(State.Failed("empty"))
        }


    }.catch {
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)
}
