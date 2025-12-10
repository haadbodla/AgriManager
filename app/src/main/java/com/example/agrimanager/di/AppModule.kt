package com.example.agrimanager.di

import android.content.Context
import androidx.room.Room
import com.example.agrimanager.data.local.FarmDao
import com.example.agrimanager.data.local.FarmDatabase
import com.example.agrimanager.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FarmDatabase {
        return Room.databaseBuilder(
            context,
            FarmDatabase::class.java,
            "agri_manager_db"
        ).fallbackToDestructiveMigration() // Wipes data if you change columns (good for dev)
            .build()
    }

    @Provides
    @Singleton
    fun provideFarmDao(database: FarmDatabase): FarmDao {
        return database.farmDao()
    }
    
    @Provides
    @Singleton
    fun provideUserDao(database: FarmDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
