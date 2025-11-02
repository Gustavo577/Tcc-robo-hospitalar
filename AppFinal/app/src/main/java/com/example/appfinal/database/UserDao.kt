package com.example.appfinal.database

import androidx.room.Dao
import androidx.room.Delete // Certifique-se de que este import está correto
import androidx.room.Insert
import androidx.room.Query // Certifique-se de que este import está correto
import androidx.room.Update // Se você adicionou o @Update

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User)

    // Consulta para LOGIN:
    @Query("SELECT * FROM user_table WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    // Consulta para CADASTRO:
    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun findUserByEmail(email: String): User?

    // Consulta para LISTAGEM:
    @Query("SELECT * FROM user_table ORDER BY email ASC")
    suspend fun getAllUsers(): List<User>

    // ⭐ FUNÇÃO DE EXCLUSÃO
    @Delete
    suspend fun deleteUser(user: User) // O parâmetro 'user' deve ser do tipo User (a sua Entidade)
}