package com.example.tp2apirest

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tp2apirest.databinding.ActivityMainBinding
import com.example.tp2apirest.databinding.DialogStudentBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: StudentAdapter
    private var studentsList = ArrayList<Student>()

    private val filieresArray = arrayOf(
        "Développement Digital",
        "Infrastructure Digitale",
        "Génie Logiciel",
        "Cybersécurité",
        "Intelligence Artificielle"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupRecyclerView()
        refreshList()

        // Clic sur le FAB → ouvrir formulaire en mode AJOUT
        binding.fabAdd.setOnClickListener {
            openStudentForm(null)
        }

        // Filtre dynamique en temps réel
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim()
                if (keyword.isNotEmpty()) {
                    performSearch(keyword)
                } else {
                    refreshList()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(
            studentsList,
            onEditClick   = { student -> openStudentForm(student) },
            onDeleteClick = { student -> confirmDelete(student) }
        )
        binding.recyclerViewStudents.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewStudents.adapter = adapter
    }

    private fun refreshList() {
        studentsList.clear()
        studentsList.addAll(dbHelper.getAllStudents())
        adapter.updateData(studentsList)
    }

    private fun performSearch(query: String) {
        adapter.updateData(dbHelper.searchStudents(query))
    }

    // ── Formulaire double usage : AJOUT (student=null) / ÉDITION ─────────────
    private fun openStudentForm(student: Student?) {
        val dialogBinding = DialogStudentBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this).setView(dialogBinding.root)

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            filieresArray
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerFiliere.adapter = spinnerAdapter

        val isEdit = student != null
        if (isEdit && student != null) {
            dialogBinding.textViewDialogTitle.text = "Modifier la fiche étudiant"
            dialogBinding.editTextName.setText(student.nom)
            dialogBinding.editTextEmail.setText(student.email)
            val index = filieresArray.indexOf(student.filiere)
            if (index >= 0) dialogBinding.spinnerFiliere.setSelection(index)
        }

        builder.setPositiveButton("Enregistrer") { dialog, _ ->
            val nom     = dialogBinding.editTextName.text.toString().trim()
            val email   = dialogBinding.editTextEmail.text.toString().trim()
            val filiere = dialogBinding.spinnerFiliere.selectedItem.toString()

            if (nom.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Erreur : tous les champs sont requis !", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (isEdit && student != null) {
                val result = dbHelper.updateStudent(Student(student.id, nom, email, filiere))
                if (result > 0) {
                    Toast.makeText(this, "Modifications appliquées.", Toast.LENGTH_SHORT).show()
                    refreshList()
                }
            } else {
                val resultId = dbHelper.insertStudent(nom, email, filiere)
                if (resultId > 0) {
                    Toast.makeText(this, "Étudiant enregistré dans la base !", Toast.LENGTH_SHORT).show()
                    refreshList()
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Fermer") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    // ── Confirmation avant suppression ───────────────────────────────────────
    private fun confirmDelete(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Suppression définitive")
            .setMessage("Voulez-vous retirer ${student.nom} de l'application ? Cette action est irréversible.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Supprimer") { dialog, _ ->
                val success = dbHelper.deleteStudent(student.id)
                if (success > 0) {
                    Toast.makeText(this, "Étudiant supprimé.", Toast.LENGTH_SHORT).show()
                    refreshList()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler") { dialog, _ -> dialog.dismiss() }
            .create().show()
    }
}