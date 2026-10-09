package pe.colegio.asistencia.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para acceder al DataStore desde cualquier Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val ROL_KEY = stringPreferencesKey("rol")
        private val DOCENTE_ID_KEY = stringPreferencesKey("docente_id")
    }

    // Guardar sesión tras login exitoso
    suspend fun guardarSesion(
        token: String,
        username: String,
        rol: String,
        docenteId: Int?
    ) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USERNAME_KEY] = username
            prefs[ROL_KEY] = rol
            prefs[DOCENTE_ID_KEY] = docenteId?.toString() ?: ""
        }
    }

    // Leer el token (Flow para reactividad)
    val tokenFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[TOKEN_KEY] }

    // Leer el username
    val usernameFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[USERNAME_KEY] }

    // Leer el rol
    val rolFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[ROL_KEY] }

    // Leer el docenteId
    val docenteIdFlow: Flow<Int?> = context.dataStore.data
        .map { prefs ->
            val str = prefs[DOCENTE_ID_KEY]
            str?.takeIf { it.isNotEmpty() }?.toIntOrNull()
        }

    // Cerrar sesión
    suspend fun limpiarSesion() {
        context.dataStore.edit { it.clear() }
    }
}