package com.example.appfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton // ⭐️ Novo Import para o botão
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appfinal.R
import com.example.appfinal.database.User

// ⭐️ 1. INTERFACE DE CALLBACK: Notifica a Activity sobre o clique
interface OnDeleteClickListener {
    fun onDeleteClick(user: User)
}


// ⭐️ 2. ADAPTER REVISADO PARA RECEBER O LISTENER
class UserAdapter(
    private val userList: MutableList<User>,
    private val deleteListener: OnDeleteClickListener // ⭐️ Novo Parâmetro para o listener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // 1. ViewHolder: Adicionar a referência ao botão
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete) // ⭐️ Referência ao Botão
    }

    // 2. Cria um novo item de view (infla o layout item_user.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }


    // 3. Conecta os dados (User) com as Views do ViewHolder e atribui o clique
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.tvUserId.text = "ID: ${currentUser.id}"
        holder.tvUserEmail.text = currentUser.email

        // ⭐️ ATRIBUIÇÃO DO CLIQUE: Notifica o listener com o objeto User correto
        holder.btnDelete.setOnClickListener {
            deleteListener.onDeleteClick(currentUser)
        }
    }

    // 4. Retorna o número total de itens na lista
    override fun getItemCount() = userList.size

    fun getUserAt(position: Int): User {
        return userList[position]
    }

    // O método removeAt se torna opcional no fluxo, mas é útil para remoção visual manual.
    fun removeAt(position: Int) {
        userList.removeAt(position)
        notifyItemRemoved(position)
    }

    // Função para atualizar a lista de dados
    fun updateData(newList: List<User>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }
}