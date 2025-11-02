package com.example.appfinal.database
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    // A chave primária é necessária para identificar cada linha/usuário
    // autoGenerate = true permite que o Room crie IDs sequenciais automaticamente
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "email")
    val email: String,

    // É crucial salvar a senha, mas em uma aplicação real, você deve
    // salvar um HASH da senha, não o texto puro.
    @ColumnInfo(name = "password")
    val password: String
)