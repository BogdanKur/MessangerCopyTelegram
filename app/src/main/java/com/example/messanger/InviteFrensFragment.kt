package com.example.messanger

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.messanger.databinding.FragmentInviteFrensBinding


class InviteFrensFragment : Fragment() {
    private var _binding: FragmentInviteFrensBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInviteFrensBinding.inflate(inflater, container, false)
        val view = binding.root
        val navController = findNavController()
        binding.toolbar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_ ->
            destination.label = "Пригласить друзей"
            binding.toolbar.title = destination.label
        }
        binding.btnInviteFrens.setOnClickListener {
            copyToClipboard("https://github.com/BogdanKur/MessangerCopyTelegram")
            openLink("https://github.com/BogdanKur/MessangerCopyTelegram")
        }
        return view
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clip = ClipData.newPlainText("copied_text", text)

        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), "Текст скопирован!", Toast.LENGTH_SHORT).show()
    }

}