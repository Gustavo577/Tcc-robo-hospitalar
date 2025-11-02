package com.example.appfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appfinal.R // Importa a classe R (Recursos)
import com.example.appfinal.database.User // Importa a Entidade User

class UserAdapter(
    private val userList: MutableList<User> // Usar MutableList para facilitar a remoção local
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // 1. ViewHolder: Responsável por manter as referências das views de um item
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        // A senha não será exibida na lista, mas está acessível se necessário
    }

    // 2. Cria um novo item de view (infla o layout item_user.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }


    // 3. Conecta os dados (User) com as Views do ViewHolder
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.tvUserId.text = "ID: ${currentUser.id}"
        holder.tvUserEmail.text = currentUser.email
    }

    // 4. Retorna o número total de itens na lista
    override fun getItemCount() = userList.size

    fun getUserAt(position: Int): User {
        return userList[position]
    }

    fun removeAt(position: Int) {
        userList.removeAt(position)
        notifyItemRemoved(position)
    }

    // Função para atualizar a lista de dados (Agora recebe MutableList)
    fun updateData(newList: List<User>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }
}