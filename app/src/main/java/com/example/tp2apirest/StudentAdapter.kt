package com.example.tp2apirest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tp2apirest.databinding.ItemStudentBinding

class StudentAdapter(
    private var students: List<Student>,
    private val onEditClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(val binding: ItemStudentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.binding.apply {
            textViewInitial.text = student.nom.trim().take(1).uppercase()
            textViewName.text    = student.nom
            textViewEmail.text   = student.email
            textViewFiliere.text = student.filiere

            buttonEdit.setOnClickListener   { onEditClick(student) }
            buttonDelete.setOnClickListener { onDeleteClick(student) }
        }
    }

    override fun getItemCount(): Int = students.size

    fun updateData(newStudents: List<Student>) {
        this.students = newStudents
        notifyDataSetChanged()
    }
}