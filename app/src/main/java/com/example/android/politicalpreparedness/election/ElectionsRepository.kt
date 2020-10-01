package com.example.android.politicalpreparedness.election

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.RepresentativeResponse
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class ElectionsRepository(private val database: ElectionDatabase) {

    val elections: LiveData<List<Election>> = database.electionDao.getElections()

    val voterInfo =  MutableLiveData<VoterInfoResponse>()
    val representatives =  MutableLiveData<RepresentativeResponse>()

    suspend fun refreshElections() {
        try {
            withContext(Dispatchers.IO) {
                val electionResponse = CivicsApi.retrofitService.getElectionsAsync().await()
                database.electionDao.insertElections(electionResponse.elections)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getElection(id: Int) = database.electionDao.getElection(id)

    suspend fun getVoterInfo(electionId: Int, address: String) {
        try {
            withContext(Dispatchers.IO) {
                val response = CivicsApi.retrofitService.getVoterInfoAsync(electionId, address).await()
                voterInfo.postValue(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertElection(election: Election) {
        Log.i("election", election.isSaved.toString())
        withContext(Dispatchers.IO) {
            database.electionDao.insertElection(election)
        }
    }

    val savedElections: LiveData<List<Election>> = database.electionDao.getSavedElections()
}