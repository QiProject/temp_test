package com.example.bdfuv.manager

import com.example.bdfuv.model.AreaModel
import com.example.bdfuv.model.HistoryDeviceModel
import com.example.bdfuv.model.QuestionModel
import io.realm.Realm
import io.realm.kotlin.where

class RealmManager() {
    //TODO where to .close() ?
    private var realm: Realm = Realm.getDefaultInstance()

    companion object {
        val instance = RealmManager()
    }

    fun saveHistoryDevice(historyDeviceModel: HistoryDeviceModel) {
        realm.executeTransaction {
            realm.copyToRealmOrUpdate(historyDeviceModel)
        }
    }

    fun deleteAllHistoryDevice() {
        realm.executeTransaction {
            realm.where<HistoryDeviceModel>().findAll().deleteAllFromRealm()
        }
    }


    fun retrieveFirstHistoryDevice(): HistoryDeviceModel? {
        val result = realm.where<HistoryDeviceModel>().findFirst() ?: return null
        return realm.copyFromRealm(result)
    }


    fun saveQuestion(questionModel: QuestionModel) {
        realm.executeTransaction {
            realm.copyToRealmOrUpdate(questionModel)
        }
    }

    fun saveQuestions(questions: List<QuestionModel>) {
        realm.executeTransaction { realm -> realm.copyToRealmOrUpdate(questions) }
    }

    fun retrieveQuestions(): MutableList<QuestionModel>? {
        val result = realm.where<QuestionModel>().sort("index").findAll()

        return realm.copyFromRealm(result)
    }

    fun retrieveCheckedQuestions(): MutableList<QuestionModel>? {
        val result = realm.where<QuestionModel>().equalTo("isChecked", true).sort("index").findAll()

        return realm.copyFromRealm(result)
    }


    fun questionExist(): Boolean {
        return realm.where<QuestionModel>().findFirst() != null
    }

    fun saveArea(areaModel: AreaModel) {
        realm.executeTransaction {
            realm.copyToRealmOrUpdate(areaModel)
        }
    }

    fun saveAreas(areas: List<AreaModel>) {
        realm.executeTransaction { realm -> realm.copyToRealmOrUpdate(areas) }
    }

    fun retrieveAreas(): MutableList<AreaModel>? {
        val result = realm.where<AreaModel>().sort("index").findAll()
        return realm.copyFromRealm(result)
    }

    fun areaExist(): Boolean {
        return realm.where<AreaModel>().findFirst() != null
    }

    fun retrieveCheckedAreas(): MutableList<AreaModel>? {
        val result = realm.where<AreaModel>().equalTo("isChecked", true) .sort("index").findAll()
        return realm.copyFromRealm(result)
    }
}