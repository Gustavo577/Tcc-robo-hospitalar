package com.example.appfinal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

// ⭐️ IMPORTS ESSENCIAIS DO ROOM/DATABASE:
import com.example.appfinal.database.AppDatabase
import com.example.appfinal.database.User
import com.example.appfinal.database.UserDao
import com.example.appfinal.databinding.ActivityUserListBinding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ⭐️ 1. IMPLEMENTA A INTERFACE OnDeleteClickListener
class UserListActivity : AppCompatActivity(), OnDeleteClickListener {

    private lateinit var binding: ActivityUserListBinding
    private lateinit var userDao: UserDao
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = AppDatabase.getDatabase(this).userDao()

        setupRecyclerView()
        loadUsers()
        // ⭐️ REMOVIDO: Não precisamos mais de setupSwipeToDelete()
    }

    private fun setupRecyclerView() {
        // ⭐️ 2. PASSA 'this' (a Activity) como o Listener de Exclusão
        userAdapter = UserAdapter(mutableListOf(), this)

        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(this@UserListActivity)
            adapter = userAdapter
        }
    }

    private fun loadUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            val users = userDao.getAllUsers()

            withContext(Dispatchers.Main) {
                if (users.isNotEmpty()) {
                    userAdapter.updateData(users)
                }
            }
        }
    }

    // ⭐️ 3. IMPLEMENTAÇÃO DO MÉTODO DA INTERFACE (Chamado pelo Adapter no clique do botão)
    override fun onDeleteClick(user: User) {
        // Inicia o processo de exclusão no banco de dados
        deleteUserFromDatabase(user)
    }

    // ⭐️ 4. LÓGICA DE EXCLUSÃO (Agora chamada via callback do botão)
    private fun deleteUserFromDatabase(user: User) {
        CoroutineScope(Dispatchers.IO).launch {

            // Exclui do Room
            userDao.deleteUser(user)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@UserListActivity, "Usuário '${user.email}' excluído.", Toast.LENGTH_SHORT).show()

                // Recarrega os dados para atualizar a RecyclerView visualmente
                loadUsers()
            }
        }
    }
}