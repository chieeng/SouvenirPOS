package edu.cit.erag.souvenirpos

import android.app.Application
import edu.cit.erag.souvenirpos.di.ServiceLocator

class SouvenirPosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
