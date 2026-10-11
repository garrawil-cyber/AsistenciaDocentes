package pe.colegio.asistencia.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val ROL_KEY = stringPreferencesKey("rol")
        private val DOCENTE_ID_KEY = stringPreferencesKey("docente_id")
        private val NOMBRE_COMPLETO_KEY = stringPreferencesKey("nombre_completo")
    }

    suspend fun guardarSesion(
        token: String,
        username: String,
        rol: String,
        docenteId: Int?,
        nombreCompleto: String?
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USERNAME_KEY] = username
            prefs[ROL_KEY] = rol
            prefs[DOCENTE_ID_KEY] = docenteId?.toString() ?: ""
            prefs[NOMBRE_COMPLETO_KEY] = nombreCompleto ?: ""
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[TOKEN_KEY] }

    val usernameFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[USERNAME_KEY] }

    val rolFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[ROL_KEY] }

    val docenteIdFlow: Flow<Int?> = context.dataStore.data
        .map { prefs ->
            val str = prefs[DOCENTE_ID_KEY]
            str?.takeIf { it.isNotEmpty() }?.toIntOrNull()
        }

    val nombreCompletoFlow: Flow<String?> = context.dataStore.data
        .map { prefs ->
            prefs[NOMBRE_COMPLETO_KEY]?.takeIf { it.isNotEmpty() }
        }

    suspend fun limpiarSesion() {
        context.dataStore.edit { it.clear() }
    }
}