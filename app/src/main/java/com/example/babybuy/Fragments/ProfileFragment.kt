package com.example.babybuy.Fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.babybuy.LoginActivity
import com.example.babybuy.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Find the logout button from your layout
        val logoutButton = view.findViewById<Button>(R.id.buttonLogout)

        auth = FirebaseAuth.getInstance()

        logoutButton.setOnClickListener {
            // Display a confirmation dialog when the logout button is clicked
            showLogoutConfirmationDialog()
        }

        return view
    }

    // Define the function to show the logout confirmation dialog
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout Confirmation")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Handle logout here (clear user session, navigate to login screen)
                handleLogout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    // Define the function to handle the logout process
    private fun handleLogout() {
        // Implement your logout logic here:
        // 1. Clear user session (if needed)
        auth.signOut();
        // 2. Navigate to the login screen
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Close the current activity (optional)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
