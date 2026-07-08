package edu.cit.erag.souvenirpos

import android.app.Application
import edu.cit.erag.souvenirpos.core.di.ServiceLocator

class SouvenirPosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
