package com.example.appfinal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper // ⭐️ Importação correta
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView // ⭐️ Importação correta

// ⭐️ IMPORTS ESSENCIAIS DO ROOM/DATABASE: AJUSTE O PACOTE SE NECESSÁRIO
import com.example.appfinal.database.AppDatabase
import com.example.appfinal.database.User // Classe User
import com.example.appfinal.database.UserDao // Interface UserDao
import com.example.appfinal.databinding.ActivityUserListBinding // Classe Binding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserListActivity : AppCompatActivity() {

    // ⭐️ Variáveis Globais (Declaração correta das lateinit vars)
    private lateinit var binding: ActivityUserListBinding
    private lateinit var userDao: UserDao
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⭐️ Inicialização do Binding
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ⭐️ Inicialização do DAO
        userDao = AppDatabase.getDatabase(this).userDao()

        setupRecyclerView()
        loadUsers()
        setupSwipeToDelete()
    }

    private fun setupRecyclerView() {
        // Inicializa o Adapter com uma lista vazia, mas como MutableList
        userAdapter = UserAdapter(mutableListOf())

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

    private fun setupSwipeToDelete() {

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                // Acessa o adapter a partir da RecyclerView do binding
                val adapter = binding.recyclerViewUsers.adapter as UserAdapter

                val position = viewHolder.adapterPosition
                val userToDelete = adapter.getUserAt(position)

                // 1. Remove o item localmente (visual)
                adapter.removeAt(position)

                // 2. Remove o item do banco de dados (deve ser em background)
                deleteUserFromDatabase(userToDelete)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewUsers)
    }

    private fun deleteUserFromDatabase(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            userDao.deleteUser(user)

            withContext(Dispatchers.Main) {
                // ⭐️ Toast precisa do import do Android.widget.Toast
                Toast.makeText(this@UserListActivity, "Usuário '${user.email}' excluído.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}