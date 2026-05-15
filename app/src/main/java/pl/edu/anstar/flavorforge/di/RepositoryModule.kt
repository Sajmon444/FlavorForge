package pl.edu.anstar.flavorforge.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.edu.anstar.flavorforge.data.repository.AuthRepositoryImpl
import pl.edu.anstar.flavorforge.data.repository.RecipeRepositoryImpl
import pl.edu.anstar.flavorforge.domain.repository.AuthRepository
import pl.edu.anstar.flavorforge.domain.repository.RecipeRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        recipeRepositoryImpl: RecipeRepositoryImpl
    ): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
